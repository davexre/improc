package com.slavi.derbi.jpa.entity;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Access(AccessType.FIELD)
public class Ref<T> implements Serializable {
	@Column(name = "obj")
	public T o;

	public Ref() {}

	public Ref(T o) {
		this.o = o;
	}
}
