package com.slavi.derbi.jpa.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ObjectWithId {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
}
