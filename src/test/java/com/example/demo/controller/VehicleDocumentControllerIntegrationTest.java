package com.example.demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("Vehicle Document API Integration Tests")
class VehicleDocumentControllerIntegrationTest {

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern REF_PATTERN = Pattern.compile("\"storedFileRef\"\\s*:\\s*\"([^\"]+)\"");

    @TempDir
    static Path tempUploadDir;

    @DynamicPropertySource
    static void registerStorageProperties(DynamicPropertyRegistry registry) {
        registry.add("app.storage.upload-dir", () -> tempUploadDir.toAbsolutePath().toString());
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Document endpoints reject unauthenticated requests with 401 Unauthorized")
    void documentEndpoints_RequireAuthentication() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "reg.pdf", "application/pdf", "pdf".getBytes());

        mockMvc.perform(get("/api/v1/vehicles/VMS-01/documents"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(multipart("/api/v1/vehicles/VMS-01/documents")
                        .file(file)
                        .param("documentType", "REGISTRATION_CERTIFICATE"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("Full document lifecycle: upload -> list -> download -> delete")
    void fullDocumentLifecycle() throws Exception {
        byte[] pdfBytes = "%PDF-1.4 sample registration certificate".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "registration-cert.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfBytes
        );

        // 1. Upload
        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/vehicles/VMS-01/documents")
                        .file(file)
                        .param("documentType", "REGISTRATION_CERTIFICATE")
                        .param("expiryDate", "2027-06-30"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.vehicleId", is("VMS-01")))
                .andExpect(jsonPath("$.data.documentType", is("REGISTRATION_CERTIFICATE")))
                .andExpect(jsonPath("$.data.documentTypeLabel", is("Registration Certificate")))
                .andExpect(jsonPath("$.data.originalFilename", is("registration-cert.pdf")))
                .andExpect(jsonPath("$.data.expiryDate", is("2027-06-30")))
                .andReturn();

        String body = uploadResult.getResponse().getContentAsString();
        String documentId = extractGroup(ID_PATTERN, body);
        String storedFileRef = extractGroup(REF_PATTERN, body);

        Path storedFile = tempUploadDir.resolve(storedFileRef);
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readAllBytes(storedFile)).isEqualTo(pdfBytes);

        // 2. List documents for vehicle
        mockMvc.perform(get("/api/v1/vehicles/VMS-01/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(documentId)));

        // 3. Download document
        mockMvc.perform(get("/api/v1/vehicles/VMS-01/documents/" + documentId + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=\"registration-cert.pdf\"")))
                .andExpect(content().bytes(pdfBytes));

        // 4. Delete document
        mockMvc.perform(delete("/api/v1/vehicles/VMS-01/documents/" + documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        assertThat(Files.exists(storedFile)).isFalse();

        // 5. Download after delete returns 404
        mockMvc.perform(get("/api/v1/vehicles/VMS-01/documents/" + documentId + "/download"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("Upload rejects unsupported file type with 400 Bad Request")
    void upload_UnsupportedFileType_Returns400() throws Exception {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file",
                "malware.exe",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "MZ".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/vehicles/VMS-01/documents")
                        .file(exeFile)
                        .param("documentType", "OTHER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("Upload for non-existent vehicle returns 404 Not Found")
    void upload_UnknownVehicle_Returns404() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "tax.png",
                MediaType.IMAGE_PNG_VALUE,
                "png".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/vehicles/UNKNOWN-999/documents")
                        .file(file)
                        .param("documentType", "TAX_TOKEN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    private String extractGroup(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            throw new AssertionError("Pattern " + pattern + " not found in: " + text);
        }
        return matcher.group(1);
    }
}
