package com.slavi.derbi.jpa.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("sa")
public class SimpleAddress extends Address {
	public String address;
}
