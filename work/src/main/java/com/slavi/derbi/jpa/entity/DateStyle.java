package com.slavi.derbi.jpa.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class DateStyle implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	int dateId;

	String format;

	public int getDateId() {
		return dateId;
	}

	public void setDateId(int dateId) {
		this.dateId = dateId;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
