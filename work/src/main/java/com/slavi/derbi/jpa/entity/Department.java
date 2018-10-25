package com.slavi.derbi.jpa.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Department {

	@Id @GeneratedValue
	Integer id;

	String name;

	String description;

	DepartmentType type;

	public Department() {}

	public Department(String name, DepartmentType type) {
		this.name = name;
		this.type = type;
		this.description = "Description for " + name + "(" + type + ")";
	}

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DepartmentType getType() {
		return type;
	}

	public void setType(DepartmentType type) {
		this.type = type;
	}
}
