package com.upwork.defimov.keycloak.userfederation.model.jsonbmapping;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserSettings implements Serializable {
	private String settings;

	public String getSettings() {
		return settings;
	}

	public void setSettings(String settings) {
		this.settings = settings;
	}
}
