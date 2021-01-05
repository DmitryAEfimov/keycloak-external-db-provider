package com.upwork.defimov.keycloak.userfederation.provider.password;

public interface PasswordValidator {
	boolean validate(String hashedPassword, String rawPassword);
}
