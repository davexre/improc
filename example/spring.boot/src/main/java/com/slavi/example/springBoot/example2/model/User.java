package com.slavi.example.springBoot.example2.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
public class User {
	@Id
	String username;

	String name;

	Role role;

	Integer someInt;

	Boolean enabled;

	Date created;

	public User(String username) {
		this.username = this.name = username;
		role = Role.USER;
		enabled = true;
		someInt = 12;
		created = new Date();
	}
}
