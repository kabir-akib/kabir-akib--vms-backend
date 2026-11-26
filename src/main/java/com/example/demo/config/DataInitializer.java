package com.example.demo.config;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedInitialAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                if (userRepository.count() == 0) {
                    User admin = new User(
                            "admin@starkgroup.com",
                            passwordEncoder.encode("stark2026"),
                            "Stark Fleet Admin",
                            "ADMIN"
                    );
                    userRepository.save(admin);
                    log.info("Seeded default fleet admin user: admin@starkgroup.com");
                }
            } catch (Exception e) {
                log.warn("User initialization skipped or encountered error: {}", e.getMessage());
            }
        };
    }

    @Bean
    public CommandLineRunner seedInitialVehicles(VehicleRepository repository) {
        return args -> {
            try {
                if (repository.count() == 0) {
                    log.info("No vehicles found in database. Seeding initial Stark Group fleet vehicles...");
                    List<Vehicle> initialFleet = List.of(
                            new Vehicle("VMS-01", "STK-1042", "Stark Logistics", VehicleType.TRUCK, "Scania", "R500 V8 Highline", VehicleStatus.ACTIVE),
                            new Vehicle("VMS-02", "STK-2019", "Stark Distribution", VehicleType.VAN, "Mercedes-Benz", "Sprinter 319 CDI", VehicleStatus.ACTIVE),
                            new Vehicle("VMS-03", "STK-3088", "Stark Nordic Transport", VehicleType.HEAVY_HAULER, "Volvo", "FH16 750 Globetrotter", VehicleStatus.MAINTENANCE),
                            new Vehicle("VMS-04", "STK-4105", "Stark Building Supplies", VehicleType.TRUCK, "MAN", "TGX 26.510", VehicleStatus.ACTIVE),
                            new Vehicle("VMS-05", "STK-5520", "Stark Fleet Services", VehicleType.ELECTRIC, "Volvo", "FE Electric Box", VehicleStatus.ACTIVE),
                            new Vehicle("VMS-06", "STK-6114", "Stark Logistics", VehicleType.VAN, "Ford", "Transit Custom 320", VehicleStatus.INACTIVE),
                            new Vehicle("VMS-07", "STK-7203", "Stark Materials", VehicleType.SUV, "Toyota", "Land Cruiser 300", VehicleStatus.ACTIVE),
                            new Vehicle("VMS-08", "STK-8491", "Stark Nordic Transport", VehicleType.SEDAN, "Volkswagen", "Passat Estate Business", VehicleStatus.ACTIVE),
                            new Vehicle("VMS-09", "STK-9032", "Stark Distribution", VehicleType.TRUCK, "DAF", "XG+ 530 Super Space", VehicleStatus.MAINTENANCE)
                    );
                    repository.saveAll(initialFleet);
                    log.info("Successfully seeded {} Stark Group fleet vehicles.", initialFleet.size());
                } else {
                    log.info("Database already contains {} vehicles. Skipping seed.", repository.count());
                }
            } catch (Exception e) {
                log.warn("Database initialization skipped or encountered error: {}", e.getMessage());
            }
        };
    }
}

