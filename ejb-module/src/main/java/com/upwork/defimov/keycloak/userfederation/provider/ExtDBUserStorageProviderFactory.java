package com.upwork.defimov.keycloak.userfederation.provider;

import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class ExtDBUserStorageProviderFactory implements UserStorageProviderFactory<ExtDBUserStorageProvider> {
	private static final Logger logger = Logger.getLogger(ExtDBUserStorageProviderFactory.class);
	private static final String USER_FEDERATION_ID = "PocExternalPGFederation";

	@Override
	public ExtDBUserStorageProvider create(KeycloakSession session, ComponentModel model) {
		logger.infov("create database provider for session {0} and model {1}", session, model.getName());

		try {
			InitialContext ctx = new InitialContext();

			ExtDBUserStorageProvider provider = (ExtDBUserStorageProvider) ctx
					.lookup("java:global/extdb-storage-provider/extdb-storage-provider-module/" + ExtDBUserStorageProvider.class.getSimpleName());

			provider.setModel(model);
			provider.setSession(session);
			return provider;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getId() {
		return USER_FEDERATION_ID;
	}
}
