package com.slavi.derbi.jpa.entity;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@DiscriminatorValue("Adr")
public abstract class Address extends ObjectWithId {
	public String address;
}
