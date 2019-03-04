package com.slavi.derbi.hr.model;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "regions")
@Access(AccessType.FIELD)
public class Region implements Serializable {
	@OrderBy
	@Id
	@Column(name = "region_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	Integer id;

	@Column(name = "region_name", length = 25)
	String name;

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
}
