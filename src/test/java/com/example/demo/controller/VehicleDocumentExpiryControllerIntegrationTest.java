package com.example.demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("Vehicle Document Expiry API Integration Tests")
class VehicleDocumentExpiryControllerIntegrationTest {

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
    @DisplayName("Expiry endpoints reject unauthenticated requests with 401 Unauthorized")
    void expiryEndpoints_RequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/documents/expired"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/documents/expiring-soon"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/documents/expiry-alerts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/documents/vehicles/VMS-01/expiry"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("Expiry endpoints classify EXPIRED, EXPIRING_SOON, VALID and exclude null expiry dates")
    void expiryEndpoints_ClassifyAndFilterCorrectly() throws Exception {
        LocalDate today = LocalDate.now();

        uploadDocument("expired.pdf", "INSURANCE", today.minusDays(10).toString());
        uploadDocument("soon.pdf", "TAX_TOKEN", today.plusDays(15).toString());
        uploadDocument("valid.pdf", "FITNESS_CERTIFICATE", today.plusDays(90).toString());
        uploadDocument("no-expiry.pdf", "OTHER", null);

        // 1. Expired
        mockMvc.perform(get("/api/v1/documents/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[*].originalFilename", hasItem("expired.pdf")))
                .andExpect(jsonPath("$.data[*].originalFilename", not(hasItem("soon.pdf"))))
                .andExpect(jsonPath("$.data[*].originalFilename", not(hasItem("valid.pdf"))))
                .andExpect(jsonPath("$.data[*].originalFilename", not(hasItem("no-expiry.pdf"))));

        // 2. Expiring soon (30 days)
        mockMvc.perform(get("/api/v1/documents/expiring-soon").param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[*].originalFilename", hasItem("soon.pdf")))
                .andExpect(jsonPath("$.data[*].originalFilename", not(hasItem("expired.pdf"))))
                .andExpect(jsonPath("$.data[*].originalFilename", not(hasItem("valid.pdf"))))
                .andExpect(jsonPath("$.data[*].originalFilename", not(hasItem("no-expiry.pdf"))));

        // 3. Expiry alerts (expired + expiring soon)
        mockMvc.perform(get("/api/v1/documents/expiry-alerts").param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[*].originalFilename", hasItem("expired.pdf")))
                .andExpect(jsonPath("$.data[*].originalFilename", hasItem("soon.pdf")))
                .andExpect(jsonPath("$.data[*].originalFilename", not(hasItem("valid.pdf"))))
                .andExpect(jsonPath("$.data[*].originalFilename", not(hasItem("no-expiry.pdf"))));

        // 4. Vehicle-specific expiry status (includes EXPIRED, EXPIRING_SOON, VALID; excludes null)
        mockMvc.perform(get("/api/v1/documents/vehicles/VMS-01/expiry").param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].vehicleId", is("VMS-01")))
                .andExpect(jsonPath("$.data[*].expiryStatus", hasItem("EXPIRED")))
                .andExpect(jsonPath("$.data[*].expiryStatus", hasItem("EXPIRING_SOON")))
                .andExpect(jsonPath("$.data[*].expiryStatus", hasItem("VALID")));
    }

    @Test
    @WithMockUser(username = "admin@starkgroup.com", roles = "ADMIN")
    @DisplayName("Vehicle expiry endpoint returns 404 for unknown vehicle")
    void getVehicleExpiryStatus_UnknownVehicle_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/documents/vehicles/UNKNOWN-999/expiry"))
                .andExpect(status().isNotFound());
    }

    private void uploadDocument(String filename, String documentType, String expiryDate) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4 test content".getBytes()
        );

        var request = multipart("/api/v1/vehicles/VMS-01/documents")
                .file(file)
                .param("documentType", documentType);

        if (expiryDate != null) {
            request.param("expiryDate", expiryDate);
        }

        mockMvc.perform(request).andExpect(status().isCreated());
    }
}
