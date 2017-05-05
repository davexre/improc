package com.slavi.example.springBoot.example2.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name="users")
@Access(AccessType.FIELD)
@XmlAccessorType(XmlAccessType.FIELD)
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
	@ManyToOne(fetch=FetchType.EAGER)
	User manager;

	@XmlTransient
	@OneToMany(mappedBy="manager", fetch=FetchType.LAZY)
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

	@XmlTransient
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
