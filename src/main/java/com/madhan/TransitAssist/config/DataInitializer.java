package com.madhan.TransitAssist.config;

import com.madhan.TransitAssist.model.Helper;
import com.madhan.TransitAssist.model.HelperAvailability;
import com.madhan.TransitAssist.model.Role;
import com.madhan.TransitAssist.model.User;
import com.madhan.TransitAssist.repository.HelperRepository;
import com.madhan.TransitAssist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final HelperRepository helperRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, 
                           HelperRepository helperRepository, 
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.helperRepository = helperRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initializeAdmin();
        initializeStaff();
        initializeTestUser();
        initializeHelpers();
    }

    private void initializeAdmin() {
        String adminEmail = "admin@transitassist.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setFullName("TransitAssist Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setPhone("9876543299");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            logger.info("Default ADMIN account initialized: {}", adminEmail);
        } else {
            logger.info("Admin account already exists. Skipping initialization.");
        }
    }

    private void initializeStaff() {
        String staffEmail = "staff@transitassist.com";
        if (userRepository.findByEmail(staffEmail).isEmpty()) {
            User staff = new User();
            staff.setFullName("Transit Coordinator");
            staff.setEmail(staffEmail);
            staff.setPassword(passwordEncoder.encode("Staff@123"));
            staff.setPhone("9876543200");
            staff.setRole(Role.STAFF);
            staff.setEnabled(true);
            userRepository.save(staff);
            logger.info("Default STAFF account initialized: {}", staffEmail);
        }
    }

    private void initializeTestUser() {
        String userEmail = "madhan@example.com";
        if (userRepository.findByEmail(userEmail).isEmpty()) {
            User user = new User();
            user.setFullName("Madhan");
            user.setEmail(userEmail);
            user.setPassword(passwordEncoder.encode("Password@123"));
            user.setPhone("9876543210");
            user.setRole(Role.USER);
            user.setEnabled(true);
            userRepository.save(user);
            logger.info("Default USER account initialized: {}", userEmail);
        }
    }

    private void initializeHelpers() {
        if (helperRepository.count() == 0) {
            Helper h1 = new Helper("John", "9876543211", "Wheelchair Assistance", HelperAvailability.AVAILABLE);
            Helper h2 = new Helper("Priya", "9876543212", "Escort Assistance", HelperAvailability.AVAILABLE);
            Helper h3 = new Helper("Arun", "9876543213", "Shuttle Transit Assistance", HelperAvailability.AVAILABLE);

            helperRepository.save(h1);
            helperRepository.save(h2);
            helperRepository.save(h3);
            logger.info("Default helpers initialized: John, Priya, Arun");
        }
    }
}
