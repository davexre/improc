package com.slavi.derbi.hr.model;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "departments")
@Access(AccessType.FIELD)
public class Department implements Serializable {
	@OrderBy
	@Id
	@Column(name = "department_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	Integer id;

	@Column(name = "department_name", length = 30)
	String name;

	@JoinColumn(name = "manager_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Employee manager;

	@JoinColumn(name = "location_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Location location;

	@Column(length = 300)
	String dn;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Employee getManager() {
		return manager;
	}

	public void setManager(Employee manager) {
		this.manager = manager;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}
}
