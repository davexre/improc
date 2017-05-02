package com.slavi.example.springBoot.example2.model;

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
	
	public Department(String name) {
		this.name = name;
		this.description = "Description for " + name;
	}
}
