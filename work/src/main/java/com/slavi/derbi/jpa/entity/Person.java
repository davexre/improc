package com.slavi.derbi.jpa.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;

@javax.persistence.Entity
@javax.persistence.Inheritance(strategy = javax.persistence.InheritanceType.JOINED)
public abstract class Person implements Serializable {
	@Id
	@Column(name = "un", length=123)
//	@GeneratedValue(strategy=GenerationType.AUTO)
	String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
