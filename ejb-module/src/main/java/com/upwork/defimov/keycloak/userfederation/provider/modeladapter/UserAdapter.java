package com.upwork.defimov.keycloak.userfederation.provider.modeladapter;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import com.upwork.defimov.keycloak.userfederation.model.User;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {
	private static final Logger logger = Logger.getLogger(UserAdapter.class);

	private final User user;
	private final String keycloakId;

	public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, User user) {
		super(session, realm, model);
		this.user = user;
		this.keycloakId = StorageId.keycloakId(model, user.getId().toString());
		grantRoles();
		initAttributes();
	}

	@Override
	public String getId() {
		return keycloakId;
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public void setUsername(String username) {
		// can't change username
	}

	@Override
	public String getEmail() {
		return user.getEmail();
	}

	@Override
	public void setEmail(String email) {
		user.setEmail(email);
	}

	@Override
	public String getFirstName() {
		return user.getFirstName();
	}

	@Override
	public void setFirstName(String firstName) {
		user.setFirstName(firstName);
	}

	@Override
	public String getLastName() {
		return user.getLastName();
	}

	@Override
	public void setLastName(String lastName) {
		user.setLastName(lastName);
	}

	@Override
	public String getFirstAttribute(String name) {
		return super.getFirstAttribute(name);
	}

	private void initAttributes() {
		logger.debugv("Init additional attributes for userId={0}", user.getId());
		setSingleAttribute("externalId", user.getId().toString());
		setSingleAttribute("displayName", user.getDisplayName());
		setSingleAttribute("gender", user.getGender().name());
		setSingleAttribute("avatar", user.getAvatar());
		setSingleAttribute("phone", user.getPhone());
		setSingleAttribute("settings", user.getSettings().toString());

		setCreatedTimestamp(user.getCreatedAt().getTime());
	}

	@Override
	public void setSingleAttribute(String name, String value) {
		logger.debugv("setting attribute {0} to value {1}", name, value);

		if (StringUtils.isEmpty(value)) {
			removeAttribute(name);
		} else {
			super.setSingleAttribute(name, value);
		}
	}

	private void grantRoles() {
		for (String roleName : user.getRoles()) {
			RoleModel roleModel = session.roleStorageManager().getRealmRole(realm, roleName);

			if (roleModel == null) {
				roleModel = session.roleStorageManager().addRealmRole(realm, roleName);
			}

			if (!hasRole(roleModel)) {
				grantRole(roleModel);
			}
		}
	}
}
