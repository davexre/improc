package com.slavi.derbi.jpa.entity;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

@Entity
@Table(name="USERS")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "UTYPE")
public class User {
	@Id
	@Column(name = "un")
	String username;

	String name;

	Role role;

	Integer someInt;

	Boolean enabled;

	Date created;

	@ManyToOne
	Department department;

	@JoinColumn(name="manager")
	@ManyToOne
	User manager;

	@ManyToMany
	@JoinTable(
		name = "UserEntities",
		joinColumns = {
			@JoinColumn(name = "un_fk")
		},
		inverseJoinColumns = {
			@JoinColumn(name = "ent_fk")
		}
	)
	@OrderColumn(name = "sys_key")
	List<MyEntity> entities;

	@OneToMany(mappedBy="manager")
	Set<User> subordinate;

	public User() {}

	public User(String username, Department department) {
		this.username = this.name = username;
		role = Role.USER;
		enabled = true;
		someInt = 12;
		created = new Date();
		this.department = department;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Integer getSomeInt() {
		return someInt;
	}

	public void setSomeInt(Integer someInt) {
		this.someInt = someInt;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public User getManager() {
		return manager;
	}

	public void setManager(User manager) {
		this.manager = manager;
	}

	public Set<User> getSubordinate() {
		return subordinate;
	}

	public void setSubordinate(Set<User> subordinate) {
		this.subordinate = subordinate;
	}
}
