package com.slavi.example.springBoot.example2.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="users")
@Access(AccessType.FIELD)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class User {
	@Id
	String username;

	String displayName;

	@Column(length=20)
	@Convert(converter = RoleConverter.class)
	Role role;

	@XmlTransient
	String password;

	Integer someInt;

	Boolean enabled;

	Date created;

	@XmlTransient
	@ManyToOne
	Department department;

	@XmlTransient
	@JoinColumn(name="manager")
	@ManyToOne(fetch=FetchType.EAGER)
	User manager;

	@XmlTransient
	@JsonIgnore
	@OneToMany(mappedBy="manager", fetch=FetchType.LAZY)
	Set<User> subordinate;

	public User() {}

	public User(String username, Department department) {
		this.username = this.displayName = username;
		role = Role.ROLE_USER;
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
