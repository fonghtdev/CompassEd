package com.compassed.compassed_api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SecurityHardeningTests {

    private static final String BASE = "com.compassed.compassed_api.";

    @Test
    void devAdminEndpointsAreNotShipped() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName(BASE + "api.controller.DevController"));
    }

    @Test
    void unusedUserIdAsTokenInterceptorIsGone() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName(BASE + "config.RoleInterceptor"));
    }

    @Test
    void jwtSecretHasNoCommittedDefault() throws Exception {
        String yml = Files.readString(Path.of("src/main/resources/application.yml"));
        assertFalse(yml.contains("JWT_SECRET:"), "JWT_SECRET must come from the environment only");
    }

    @Test
    void vnpayHashSecretHasNoPublicDefault() throws Exception {
        String src = Files.readString(Path.of("src/main/java/com/compassed/compassed_api/service/PaymentService.java"));
        assertFalse(src.contains("DEMO_SECRET_KEY"), "anyone could forge VNPay callbacks with a known secret");
    }
}
