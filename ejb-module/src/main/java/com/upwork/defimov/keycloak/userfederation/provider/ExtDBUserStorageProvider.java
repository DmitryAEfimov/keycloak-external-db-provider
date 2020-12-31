package com.upwork.defimov.keycloak.userfederation.provider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Local;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import com.upwork.defimov.keycloak.userfederation.model.User;
import com.upwork.defimov.keycloak.userfederation.model.UserRepository;
import com.upwork.defimov.keycloak.userfederation.provider.modeladapter.UserAdapter;

@Stateful
@Local(ExtDBUserStorageProvider.class)
public class ExtDBUserStorageProvider implements UserStorageProvider, UserLookupProvider, UserQueryProvider.Streams,
		CredentialInputUpdater, CredentialInputValidator {
	private static final Logger logger = Logger.getLogger(ExtDBUserStorageProvider.class);
	private static final Set<String> SUPPORTED_PARAMETERS = Set.of("first", "last", "email", "username", "enabled");

	@Inject
	private UserRepository users;

	private KeycloakSession session;
	private ComponentModel model;

	public void setSession(KeycloakSession session) {
		this.session = session;
	}

	public void setModel(ComponentModel model) {
		this.model = model;
	}

	// ================== CredentialInputValidator begin ============
	@Override
	public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
		return supportsCredentialType(credentialType);
	}

	@Override
	public boolean supportsCredentialType(String credentialType) {
		return PasswordCredentialModel.TYPE.equals(credentialType);
	}

	@Override
	public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
		logger.debugv("isValid user credential: userId={0}", user.getId());

		boolean valid = false;
		if (supportsCredentialType(input.getType()) && input instanceof UserCredentialModel) {
			User entity = users.findByUsername(user.getUsername());
			valid = entity != null && entity.getPassword().equals(input.getChallengeResponse());
		}

		return valid;
	}
	// ================== CredentialInputValidator end ==============

	// ================== CredentialInputUpdater begin =============
	@Override
	public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
		logger.debugv("updating credential: realm={0} user={1}", realm.getId(), user.getUsername());

		if (input.getType().equals(PasswordCredentialModel.TYPE)) {
			throw new ReadOnlyException("update user password in external DB");
		}

		return false;
	}

	@Override
	public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
		// nothing to do
	}

	@Override
	public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
		return Collections.emptySet();
	}
	// ================== CredentialInputUpdater end ===============

	// ================== UserStorageProvider begin =============
	@Remove
	@Override
	public void close() {
		logger.debug("closing");
	}
	// ================== UserStorageProvider end ===============

	// ================== UserLookupProvider begin ==================
	@Override
	public UserModel getUserById(String id, RealmModel realm) {
		logger.debugv("lookup user by id: realm={0} userId={1}", realm.getId(), id);
		String persistenceId = StorageId.externalId(id);
		User user = users.findById(persistenceId);
		if (user == null) {
			logger.info("could not find user by id: " + id);
			return null;
		}
		return new UserAdapter(session, realm, model, user);
	}

	@Override
	public UserModel getUserByUsername(String username, RealmModel realm) {
		logger.debugv("lookup user by username: realm={0} username={1}", realm.getId(), username);
		User user = users.findByUsername(username);

		if (user == null) {
			logger.info("could not find username: " + username);
			return null;
		}

		return new UserAdapter(session, realm, model, user);
	}

	@Override
	public UserModel getUserByEmail(String email, RealmModel realm) {
		logger.debugv("lookup user by username: realm={0} email={1}", realm.getId(), email);
		User user = users.findByEmail(email);

		if (user == null)
			return null;
		return new UserAdapter(session, realm, model, user);
	}
	// ================== UserLookupProvider end ====================

	// ================== UserQueryProvider.Streams begin ===========
	@Override
	public Stream<UserModel> getUsersStream(RealmModel realm) {
		logger.debugv("stream users: realm={0}", realm.getId());

		return users.getAllUsers().map(user -> new UserAdapter(session, realm, model, user));
	}

	@Override
	public Stream<UserModel> getUsersStream(RealmModel realm, int firstResult, int maxResults) {
		logger.debugv("list users: realm={0} firstResult={1} maxResults={2}", realm.getId(), firstResult, maxResults);

		return getUsersStream(realm).skip(firstResult).limit(maxResults);
	}

	@Override
	public Stream<UserModel> searchForUserStream(String search, RealmModel realm) {
		logger.debugv("search for users: realm={0} search={1}", realm.getId(), search);

		Map<String, String> searchParameters = SUPPORTED_PARAMETERS.stream().filter(param -> !"enabled".equals(param))
				.collect(Collectors.toMap(Function.identity(), (val) -> search));
		return searchForUserStream(searchParameters, realm);
	}

	@Override
	public Stream<UserModel> searchForUserStream(String search, RealmModel realm, Integer firstResult,
			Integer maxResults) {
		logger.debugv("search for users: realm={0} search={1} firstResult={2} maxResults={3}", realm.getId(), search,
				firstResult, maxResults);

		return searchForUserStream(search, realm).skip(firstResult).limit(maxResults);
	}

	@Override
	public Stream<UserModel> searchForUserStream(Map<String, String> params, RealmModel realm) {
		logger.debugv("search for users with params: realm={0} params={1}", realm.getId(), params);
		if (params.isEmpty()) {
			return getUsersStream(realm);
		}

		Stream<User> result = Stream.empty();
		for (Map.Entry<String, String> paramEntry : params.entrySet()) {
			if (SUPPORTED_PARAMETERS.contains(paramEntry.getKey().toLowerCase())) {
				logger.debugv("current param {0} with value {1}", paramEntry.getKey(), paramEntry.getValue());

				switch (paramEntry.getKey().toLowerCase()) {
				case "username":
				case "email":
					result = Stream.concat(result, users.findByUsernameOrEmail(paramEntry.getValue()));
					break;
				case "first":
				case "last":
					String firstName = params.get("first");
					String lastName = params.get("last");
					result = Stream.concat(result, users.findByFirstOrLastNames(firstName, lastName));
					break;
				case "enabled":
					return getUsersStream(realm);
				}
			}
		}

		return result.distinct().map(user -> new UserAdapter(session, realm, model, user));
	}

	@Override
	public Stream<UserModel> searchForUserStream(Map<String, String> params, RealmModel realm, Integer firstResult,
			Integer maxResults) {
		logger.debugv("search for users with params: realm={0} params={1} firstResult={2} maxResults={3}",
				realm.getId(), params, firstResult, maxResults);

		return searchForUserStream(params, realm).skip(firstResult).limit(maxResults);
	}

	@Override
	public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group) {
		logger.debugv("search for group members: realm={0} groupId={1} firstResult={2} maxResults={3}", realm.getId(),
				group.getId());

		return Stream.empty();
	}

	@Override
	public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult,
			Integer maxResults) {
		logger.debugv("search for group members with params: realm={0} groupId={1} firstResult={2} maxResults={3}",
				realm.getId(), group.getId(), firstResult, maxResults);

		return getGroupMembersStream(realm, group).skip(firstResult).limit(maxResults);
	}

	@Override
	public Stream<UserModel> searchForUserByUserAttributeStream(String attrName, String attrValue, RealmModel realm) {
		logger.debugv("search for group members: realm={0} attrName={1} attrValue={2}", realm.getId(), attrName,
				attrValue);

		return Stream.empty();
	}

	@Override
	public int getUsersCount(RealmModel realm) {
		logger.debugv("search for users count: realm={0}", realm.getId());
		return users.getUsersCount();
	}
	// ================== UserQueryProvider.Streams end =============
}
