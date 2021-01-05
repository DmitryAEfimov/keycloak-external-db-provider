package com.upwork.defimov.keycloak.userfederation.provider.password;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.Radix64Encoder;

public class BCryptPasswordHashValidator implements PasswordValidator {
	private static final Pattern BCRYPT_HASH_PATTERN = Pattern
			.compile("^\\$(2[abxy])\\$(0[4-9]|[1-2]\\d|3[0-1])\\$(.{22})(.{31})$");

	@Override
	public boolean validate(String hashedPassword, String rawPassword) {
		boolean result = false;
		Matcher matcher = BCRYPT_HASH_PATTERN.matcher(hashedPassword);

		if (matcher.matches()) {
			BCrypt.Version version = BCrypt.Version.SUPPORTED_VERSIONS.stream()
					.filter(supportedVersion -> Arrays.equals(supportedVersion.versionIdentifier,
							matcher.group(1).getBytes(StandardCharsets.UTF_8)))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Unknown BCrypt version: " + matcher.group(1)));
			int rounds = Integer.parseInt(matcher.group(2));
			byte[] salt = new Radix64Encoder.Default().decode(matcher.group(3).getBytes(StandardCharsets.UTF_8));
			byte[] hashRaw = new Radix64Encoder.Default().decode(matcher.group(4).getBytes(StandardCharsets.UTF_8));

			result = BCrypt.verifyer(version).verify(rawPassword.getBytes(StandardCharsets.UTF_8), rounds, salt,
					hashRaw).verified;
		}

		return result;
	}
}
