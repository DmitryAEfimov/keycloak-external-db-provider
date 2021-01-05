package com.upwork.defimov.keycloak.userfederation.passprovider.passprovider;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.upwork.defimov.keycloak.userfederation.provider.password.BCryptPasswordHashValidator;

public class BCryptEncodeTest {
	@Test
	public void shouldEncrypt() {
		String rawPassword = "secret";
		String expected = "$2y$10$YPfU5w2xgCq4CvHSEv0yie.GFyNq5NmOv6EC7L1UUfUKWstpKhRrm";

		BCryptPasswordHashValidator validator = new BCryptPasswordHashValidator();
		boolean result = validator.validate(expected, rawPassword);

		assertTrue(result, "encode password to correct value");
	}
}
