package com.astraval.coreflow.modules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/hash/{password}")
    public String hashPassword(@PathVariable String password) {
        try {
            log.info("INFO: Hash request received for password: {}", password);
            log.debug("DEBUG: Debugging information for password: {}", password);
            log.warn("WARN: Password hashing is being used for testing only");

            // Actual hashing
            String hashed = passwordEncoder.encode(password);
            log.info("INFO: Hashed password generated successfully");

            return hashed;
        } catch (Exception e) {
            log.error("ERROR: Password hashing failed", e);
            throw new RuntimeException("Password hashing failed");
        }
    }

    @GetMapping("")
    public String hello() {
        log.info("INFO: Hello endpoint accessed");
        return "Hello, World!";
    }
}
