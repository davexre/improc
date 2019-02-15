package com.slavi.derbi.jpa.entity.loop;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Entity2 {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	int id;

	@javax.persistence.JoinColumn
	@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = CascadeType.ALL)
	Entity1 parent;

	String data;
}
