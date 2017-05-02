package com.slavi.derbi.jpa.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="users")
@Data
@ToString
@NoArgsConstructor
public class User {
	@Id
	String username;

	String name;

	Role role;

	Integer someInt;

	Boolean enabled;

	Date created;
	
	@ManyToOne
	Department department;

	public User(String username, Department department) {
		this.username = this.name = username;
		role = Role.USER;
		enabled = true;
		someInt = 12;
		created = new Date();
		this.department = department;
	}
}
