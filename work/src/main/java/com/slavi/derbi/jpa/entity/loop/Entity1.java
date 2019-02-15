package com.slavi.derbi.jpa.entity.loop;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@DiscriminatorColumn(name = "SYS_TYPE")
@DiscriminatorValue("E1")
public class Entity1 {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "SYS_OID")
	int id;

	String data;
}
