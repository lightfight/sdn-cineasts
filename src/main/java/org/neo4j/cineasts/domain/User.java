/*
 * Copyright [2011-2016] "Neo Technology"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.neo4j.cineasts.domain;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.neo4j.cineasts.converter.UserRolesConverter;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.springframework.security.core.GrantedAuthority;

@NodeEntity
public class User {
	public static final String FRIEND = "FRIEND";
	public static final String RATED = "RATED";
	private static final String SALT = "cewuiqwzie";

	@Id @GeneratedValue Long nodeId;
	String login;
	String name;
	String password;
	String info;

	@Relationship(type = FRIEND, direction = Relationship.UNDIRECTED) Set<User> friends = new HashSet<>();

	@Convert(UserRolesConverter.class) private SecurityRole[] roles;

	@Relationship(type = "RATED") private Set<Rating> ratings = new HashSet<>();

	public User() {}

	public User(String login, String name, String password) {
		this.login = login;
		this.name = name;
		this.password = password;
	}

	public User(String login, String name, String password, SecurityRole... roles) {
		this.login = login;
		this.name = name;
		this.password = encode(password);
		this.roles = roles;
	}

	private String encode(String password) {
		return DigestUtils.md5Hex(password);
	}

	public void addFriend(User friend) {
		this.friends.add(friend);
	}

	public Rating rate(Movie movie, int stars, String comment) {
		if (ratings == null) {
			ratings = new HashSet<>();
		}

		Rating rating = new Rating(this, movie, stars, comment);
		ratings.add(rating);
		movie.addRating(rating);
		return rating;
	}

	public Set<Rating> getRatings() {
		return ratings;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, login);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<User> getFriends() {
		return friends;
	}

	public SecurityRole[] getRole() {
		return roles;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public void updatePassword(String old, String newPass1, String newPass2) {
		if (!password.equals(encode(old))) {
			throw new IllegalArgumentException("Existing Password invalid");
		}
		if (!newPass1.equals(newPass2)) {
			throw new IllegalArgumentException("New Passwords don't match");
		}
		this.password = encode(newPass1);
	}

	public boolean isFriend(User other) {
		return other != null && getFriends().contains(other);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof User)) {
			return false;
		}

		User user = (User) o;

		if (login != null ? !login.equals(user.login) : user.login != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return login != null ? login.hashCode() : 0;
	}

	public enum SecurityRole implements GrantedAuthority {
		ROLE_USER, ROLE_ADMIN;

		@Override
		public String getAuthority() {
			return name();
		}
	}
}
