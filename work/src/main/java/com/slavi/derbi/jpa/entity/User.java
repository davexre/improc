package com.slavi.derbi.jpa.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="USERS")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "UTYPE")
public class User implements Serializable {
	@Id
	@Column(name = "un")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	String username;
/*
	@ElementCollection
	@MapKeyColumn(name="sys_key")
	@Column(name="sys_val")
	@CollectionTable(name="UserParams", joinColumns=@JoinColumn(name="sys_oid"))
	Map<String, String> params = new HashMap();
*/
	@JoinColumn(name = "ent_ref")
//	@ManyToOne(fetch = FetchType.LAZY)
	MyEntity ent;

	String name;

	@Enumerated(EnumType.STRING)
	Role role;
/*
	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "sys_val")
	@OrderColumn(name = "sys_key")
	@CollectionTable(name = "UserRoles_t", joinColumns=@JoinColumn(name = "sys_oid"))
	List<Role> roles;
*/
	Integer someInt;

	Boolean enabled;

	@Convert(converter = DateConverter.class)
	@Column(name = "created_long")
	Date created2;

	@Temporal(TemporalType.DATE)
	Date created;

	@ManyToOne
	Department department;

	@JoinColumn(name="manager")
	@ManyToOne
	User manager;

	@ManyToMany(fetch = FetchType.LAZY)
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
