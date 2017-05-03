package com.slavi.derbi.jpa.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@ToString
public class Department {

	@Id @GeneratedValue
	Integer id;

	String name;

	String description;

	DepartmentType type;

	public Department(String name, DepartmentType type) {
		this.name = name;
		this.type = type;
		this.description = "Description for " + name + "(" + type + ")";
	}
}
