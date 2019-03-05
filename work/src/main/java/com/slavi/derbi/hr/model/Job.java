package com.slavi.derbi.hr.model;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jobs")
@Access(AccessType.FIELD)
public class Job implements Serializable {
	@Id
	@Column(name = "job_id", length = 10)
	String id;

	@Column(name = "job_title", length = 35)
	String title;

	@Column(name = "min_salary")
	Integer minSalary;

	@Column(name = "max_salary")
	Integer maxSalary;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getMinSalary() {
		return minSalary;
	}

	public void setMinSalary(Integer minSalary) {
		this.minSalary = minSalary;
	}

	public Integer getMaxSalary() {
		return maxSalary;
	}

	public void setMaxSalary(Integer maxSalary) {
		this.maxSalary = maxSalary;
	}
}
