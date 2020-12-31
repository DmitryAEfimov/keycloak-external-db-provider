package com.upwork.defimov.keycloak.userfederation.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.Where;

import com.upwork.defimov.keycloak.userfederation.model.jsonbmapping.UserSettings;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLCITextType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

@Entity
@Table(name = "users")
@Access(AccessType.FIELD)
@TypeDefs({ @TypeDef(name = "pgsql_citext", typeClass = PostgreSQLCITextType.class),
		@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
@NamedQueries({
		@NamedQuery(name = User.GET_USER_BY_USERNAME, query = "select u from User u where u.username = :username"),
		@NamedQuery(name = User.GET_USER_BY_EMAIL, query = "select u from User u where u.email = :email"),
		@NamedQuery(name = User.GET_USER_COUNT, query = "select count(u) from User u"),
		@NamedQuery(name = User.GET_ALL_USERS, query = "select u from User u"),
		@NamedQuery(
				name = User.GET_USERS_MATCH_FULL_NAME,
				query = "select u from User u where ( upper(u.firstName) like :firsName and upper(u.lastName) like :lastName ) order by u.username"),
		@NamedQuery(
				name = User.GET_USERS_MATCH_USERNAME_OR_EMAIL,
				query = "select u from User u where ( upper(u.username) like :search or upper(u.email) like :search ) order by u.username") })
public class User implements Serializable {
	public static final String GET_USER_BY_USERNAME = "getUserByUsername";
	public static final String GET_USER_BY_EMAIL = "getUserByEmail";
	public static final String GET_USERS_MATCH_USERNAME_OR_EMAIL = "getUsersMatchUsernameOrEmail";
	public static final String GET_USERS_MATCH_FULL_NAME = "getUsersMatchFullName";
	public static final String GET_USER_COUNT = "getUserCount";
	public static final String GET_ALL_USERS = "getAllUsers";

	public static final String DEFAULT_ROLE = "regular";

	private UUID id;

	protected User() {
		// JPA only
	}

	public User(@NotNull String username, @NotNull String password,
			@Pattern(regexp = "^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$") String email, String firstName,
			String lastName, Gender gender, String avatar, String phone, UserSettings settings,
			Set<String> accountTypes) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender != null ? gender : Gender.decline;
		this.avatar = avatar;
		this.phone = phone;
		this.settings = ObjectUtils.defaultIfNull(settings, new UserSettings());
		this.roles = !CollectionUtils.isEmpty(roles) ? accountTypes : Set.of(DEFAULT_ROLE);
	}

	@NotNull
	@Column(name = "username", nullable = false, unique = true)
	private String username;

	@Column(name = "email", unique = true)
	@Type(type = "pgsql_citext")
	@Pattern(regexp = "^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$")
	private String email;

	@NotNull
	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "sex", nullable = false)
	private Gender gender;

	@Column(name = "avatar")
	private String avatar;

	@Column(name = "phone")
	private String phone;

	@NotNull
	@Column(name = "settings", nullable = false)
	@Type(type = "jsonb")
	private UserSettings settings;

	@Column(name = "created_at", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@NotNull
	@ElementCollection(targetClass = String.class)
	@CollectionTable(name = "accounts", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "account_type", nullable = false)
	@Where(clause = "active = true")
	private Set<String> roles;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@Access(AccessType.PROPERTY)
	public UUID getId() {
		return id;
	}

	// JPA Only
	protected void setId(UUID id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public Gender getGender() {
		return gender;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public UserSettings getSettings() {
		return settings;
	}

	public void setSettings(UserSettings settings) {
		this.settings = settings;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public String getDisplayName() {
		return !StringUtils.isEmpty(firstName) && !StringUtils.isEmpty(lastName) ? firstName + " " + lastName.charAt(0)
				: null;
	}

	public static class Builder {
		private String username;
		private String password;
		private String email;
		private String firstName;
		private String lastName;
		private Gender gender;
		private String avatar;
		private String phone;
		private UserSettings settings;
		private Set<String> accountTypes;

		public Builder(@NotNull String username, @NotNull String password) {
			checkArgument(!StringUtils.isEmpty(username));
			checkArgument(!StringUtils.isEmpty(password));

			this.username = username;
			this.password = password;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public Builder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public Builder gender(Gender gender) {
			this.gender = gender;
			return this;
		}

		public Builder avatar(String avatar) {
			this.avatar = avatar;
			return this;
		}

		public Builder phone(String phone) {
			this.phone = phone;
			return this;
		}

		public Builder settings(UserSettings settings) {
			this.settings = settings;
			return this;
		}

		public Builder accounts(Set<String> accountTypes) {
			this.accountTypes = accountTypes;
			return this;
		}

		public User build() {
			return new User(username, password, email, firstName, lastName, gender, avatar, phone, settings,
					accountTypes);
		}
	}
}
