package com.slavi.derbi.jpa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.Noncacheable;

//@Cacheable(value = false)
@Entity
@Table(name="USERS")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "UTYPE")
@NamedEntityGraph(name = "manager.subordinate", attributeNodes = @NamedAttributeNode("subordinate"))
public class User implements Serializable {
	@Id
	@Column(name = "un", length=123)
//	@GeneratedValue(strategy=GenerationType.AUTO)
	String username;
/*
	@MapKeyColumn(name="map_sys_key")
	@JoinTable(name = "UserParams", joinColumns=@JoinColumn(name = "map_sys_oid"), inverseJoinColumns = @JoinColumn(name = "map_sys_val"))
	@ManyToMany(fetch = FetchType.LAZY)
	Map<String, MyEntity> params = new HashMap();
	*/
/*
	@ElementCollection(fetch = FetchType.LAZY)
	@MapKeyJoinColumn(name="map_sys_key")
	@Column(name="sys_val")
	@CollectionTable(name="UserParams", joinColumns=@javax.persistence.JoinColumn(name="sys_oid"))*/

	@ManyToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "UserParams", joinColumns = @JoinColumn(name = "map_sys_oid"), inverseJoinColumns = @JoinColumn(name = "map_sys_val"))
	@MapKeyJoinColumn(name = "map_sys_key")
	Set<User> params = new HashSet<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@OrderColumn(name="sys_key")
	@MapKeyColumn(name="map_sys_key")
	@Column(name="sys_val")
	@CollectionTable(name="UserParams1", joinColumns=@javax.persistence.JoinColumn(name="sys_oid"))
	Set<String> paramsSet = new HashSet<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@OrderColumn(name="sys_key")
	@Column(name="sys_val")
	@CollectionTable(name="UserParamsStr", joinColumns=@javax.persistence.JoinColumn(name="sys_oid"))
	List<String> paramsStr = new ArrayList<>();

	@ElementCollection(fetch = javax.persistence.FetchType.LAZY)
	@MapKeyJoinColumn(name="sys_key")
	@Column(name="sys_val")
	@CollectionTable(name="UserParams4", joinColumns=@javax.persistence.JoinColumn(name="sys_oid"))
	private Map<Department, String> params4 = new HashMap();

	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(name = "UserParams3", joinColumns = @JoinColumn(name = "map_sys_oid"), inverseJoinColumns = @JoinColumn(name = "map_sys_val"))
	@MapKeyJoinColumn(name = "map_sys_key")
	public Map<MyEntity, Department> params3 = new HashMap();

//	@MapKeyJoinColumn(name="map_sys_key")
/*	@JoinColumn(name="map_sys_val")
	@JoinTable(name = "UserParams2", joinColumns=@JoinColumn(name = "map_sys_oid"))
 */
	@Noncacheable
	@Convert(converter = NullStringConverter.class, attributeName="key")
	@Convert(converter = NullStringConverter.class, attributeName="value")
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name="UserParams2", joinColumns=@javax.persistence.JoinColumn(name="sys_oid"))
	@MapKeyColumn(name="sys_key")
	@Column(name="sys_val")
	public Map<String, String> params2 = new HashMap();

	@JoinColumn(name = "ent_ref")
	@ManyToOne(fetch = FetchType.LAZY)
	MyEntity ent;

	@OrderColumn(name = "sys_key")
	@JoinTable(name = "MyEntities_t", joinColumns=@JoinColumn(name = "sys_oid"), inverseJoinColumns = @JoinColumn(name = "sys_val"))
	@ManyToMany(fetch = FetchType.LAZY)
	public List<MyEntity> myEntities = new ArrayList<>();

/*
	// This is not working well in Eclipselink - fails to make a join to this field.
	@OrderColumn(name = "sys_key")
	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "sys_val")
	@CollectionTable(name = "subordinates_t", joinColumns=@JoinColumn(name = "sys_oid"))
*/
	@OrderColumn(name = "sys_key")
	@JoinTable(name = "subordinates_t", joinColumns = @JoinColumn(name = "sys_oid"), inverseJoinColumns = @JoinColumn(name = "sys_val"))
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//	@OneToMany(mappedBy="manager")
	private List<User> subordinate = new ArrayList<>();

	String name;

	// TODO: I want this to be just Address - test inheritance
    @javax.persistence.JoinColumn(name = "addr_c")
    @javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = javax.persistence.CascadeType.ALL)
	ComplexAddress address;

	@Enumerated(EnumType.STRING)
	Role role;

	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "sys_val")
	@OrderColumn(name = "sys_key")
	@CollectionTable(name = "UserRoles_t", joinColumns=@JoinColumn(name = "sys_oid"))
	public List<Role> roles = new ArrayList<>();

	Integer someInt;

	Boolean enabled;

	@Convert(converter = DateConverter.class)
	@Column(name = "created_long")
	Date created2;

	@Temporal(TemporalType.DATE)
	Date created;

	@ManyToOne(fetch = FetchType.LAZY)
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

	public List<User> getSubordinate() {
		return subordinate;
	}

	public void setSubordinate(List<User> subordinate) {
		this.subordinate = subordinate;
	}
}
