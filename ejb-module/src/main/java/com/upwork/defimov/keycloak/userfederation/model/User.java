package com.upwork.defimov.keycloak.userfederation.model;

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
				name = User.GET_USERS_MATCH_NAMES,
				query = "select u from User u where ( upper(u.firstName ) like '%' || :firstName || '%' or upper(u.lastName) like '%' || :lastName || '%') order by u.username"),
		@NamedQuery(
				name = User.GET_USERS_MATCH_USERNAME_OR_EMAIL,
				query = "select u from User u where ( upper(u.username) like '%' || :search || '%' or upper(u.email) like '%' || :search || '%' ) order by u.username") })
public class User implements Serializable {
	public static final String GET_USER_BY_USERNAME = "getUserByUsername";
	public static final String GET_USER_BY_EMAIL = "getUserByEmail";
	public static final String GET_USERS_MATCH_USERNAME_OR_EMAIL = "getUsersMatchUsernameOrEmail";
	public static final String GET_USERS_MATCH_NAMES = "getUsersMatchNames";
	public static final String GET_USER_COUNT = "getUserCount";
	public static final String GET_ALL_USERS = "getAllUsers";

	public static final String DEFAULT_ROLE = "regular";

	private UUID id;

	protected User() {
		// JPA only
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
}
