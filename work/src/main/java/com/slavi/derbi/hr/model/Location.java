package com.slavi.derbi.hr.model;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "locations")
@Access(AccessType.FIELD)
public class Location implements Serializable {
	@OrderBy
	@Id
	@Column(name = "location_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	Integer id;

	@Column(name = "street_address", length = 40)
	String streetAddress;

	@Column(name = "postal_code", length = 12)
	String postalCode;

	@Column(name = "city", length = 30, nullable = false)
	String city;

	@Column(name = "state_province", length = 25)
	String stateProvince;

	@JoinColumn(name = "country_id")
	@ManyToOne
	Country country;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStateProvince() {
		return stateProvince;
	}

	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
}
