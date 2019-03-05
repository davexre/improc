package com.slavi.derbi.jpa.entity;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Cacheable(false)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
//@SecondaryTable(name="DateStyle", pkJoinColumns=@PrimaryKeyJoinColumn(name="dateId"))
public class EntityWithDate implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int entityWithDateId;

	@Column
	String data;

	//@Column //(name="dateStyleRef")
	@JoinColumn(name="kuku")
	@ManyToOne
	DateStyle ds;

//	@XmlTransient
//	@JsonIgnore
	int dateIdRef;

	/*
	@OneToOne(targetEntity=DateStyle.class)
	@JoinColumn(name="dateIdRef", updatable=false, insertable=false)
	@AttributeOverride(name="dateFormat", column=@Column(name="format"))
	//@AssociationOverride(name="format", joinColumns=@JoinColumn(name="dateIdRef"))
	//@Column(table="DateStyle", name="format", updatable=false)
	String dateFormat;

	public String getDateFormat() {
		return dateFormat;
	}

	public int getDateIdRef() {
		return dateIdRef;
	}
*/
	public void setDateIdRef(int dateIdRef) {
		this.dateIdRef = dateIdRef;
	}

	public int getEntityWithDateId() {
		return entityWithDateId;
	}

	public void setEntityWithDateId(int entityWithDateId) {
		this.entityWithDateId = entityWithDateId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
