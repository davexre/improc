package com.slavi.derbi.jpa.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Complex")
public class ComplexAddress extends Address {
	public String country;
	public String city;
	public String street;
}
