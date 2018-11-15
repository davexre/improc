package com.slavi.derbi.jpa.entity;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;

@Entity
@DiscriminatorColumn(name = "dtype")
public abstract class Address extends ObjectWithId {
	public String address;
}
