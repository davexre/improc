package com.slavi.example.springBoot.example3.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="users")
@Data
@ToString(exclude="subordinate")
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

	@JoinColumn(name="manager")
	@ManyToOne(fetch=FetchType.LAZY)
	User manager;

	@XmlTransient
	@OneToMany(mappedBy="manager", fetch=FetchType.LAZY)
	Set<User> subordinate;

	public User(String username, Department department) {
		this.username = this.name = username;
		role = Role.USER;
		enabled = true;
		someInt = 12;
		created = new Date();
		this.department = department;
	}
}
