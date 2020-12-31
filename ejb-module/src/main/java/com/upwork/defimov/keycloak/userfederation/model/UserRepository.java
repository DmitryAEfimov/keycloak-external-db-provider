package com.upwork.defimov.keycloak.userfederation.model;

import java.util.UUID;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.ObjectUtils;

@Stateless
public class UserRepository {
	@PersistenceContext
	private EntityManager em;

	public User findById(String persistenceId) {
		UUID externalId = UUID.fromString(persistenceId);
		return em.find(User.class, externalId);
	}

	public User findByUsername(String username) {
		User user;
		try {
			user = em.createNamedQuery(User.GET_USER_BY_USERNAME, User.class).setParameter("username", username)
					.getSingleResult();
		} catch (NoResultException ex) {
			user = null;
		}
		return user;
	}

	public User findByEmail(String email) {
		User user;
		try {
			user = em.createNamedQuery(User.GET_USER_BY_EMAIL, User.class).setParameter("email", email)
					.getSingleResult();
		} catch (NoResultException ex) {
			user = null;
		}

		return user;
	}

	public int getUsersCount() {
		return em.createNamedQuery(User.GET_USER_COUNT, Integer.class).getSingleResult();
	}

	public Stream<User> getAllUsers() {
		return em.createNamedQuery(User.GET_ALL_USERS, User.class).getResultStream();
	}

	public Stream<User> findByUsernameOrEmail(String search) {
		return em.createNamedQuery(User.GET_USERS_MATCH_USERNAME_OR_EMAIL, User.class)
				.setParameter("search", search.toUpperCase()).getResultStream();
	}

	public Stream<User> findByFullName(String firstName, String lastName) {
		String nullSafeFirstName = ObjectUtils.defaultIfNull(firstName, "");
		String nullSafeLastName = ObjectUtils.defaultIfNull(lastName, "");
		return em.createNamedQuery(User.GET_USERS_MATCH_FULL_NAME, User.class)
				.setParameter("firstName", nullSafeFirstName.toUpperCase())
				.setParameter("lastName", nullSafeLastName.toUpperCase()).getResultStream();
	}
}
