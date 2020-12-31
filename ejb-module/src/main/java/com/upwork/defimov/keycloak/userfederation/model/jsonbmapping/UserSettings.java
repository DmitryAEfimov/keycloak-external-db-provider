package com.upwork.defimov.keycloak.userfederation.model.jsonbmapping;

import java.io.Serializable;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserSettings implements Serializable {
	private static final Logger logger = Logger.getLogger(UserSettings.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private final Object settings;

	public UserSettings() {
		this.settings = "{}";
	}

	public UserSettings(Object settings) {
		this.settings = settings;
	}

	public Object getSettings() {
		return settings;
	}

	@Override
	public String toString() {
		try {
			return objectMapper.writeValueAsString(settings);
		} catch (JsonProcessingException ex) {
			logger.error("can't parse user settins", ex);
		}

		return null;
	}
}
