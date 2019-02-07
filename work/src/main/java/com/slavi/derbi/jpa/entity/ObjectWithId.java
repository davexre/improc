package com.slavi.derbi.jpa.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ObjectWithId {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="sys_id")
	public int id;
}
