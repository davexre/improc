package com.slavi.derbi.hr.model;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "countries")
@Access(AccessType.FIELD)
public class Country implements Serializable {
	@Id
	@Column(name = "country_id", length = 2)
	String id;

	@Column(name = "country_name", length = 40)
	String name;

	@JoinColumn(name = "region_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Region region;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}
}
