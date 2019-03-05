package com.slavi.derbi.hr.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "job_history")
@Access(AccessType.FIELD)
public class JobHistory implements Serializable {
	@Id
	JobHistoryId id;

	@Column(name = "end_date", nullable = false)
	Date endDate;

	@JoinColumn(name = "job_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	Job job;

	@JoinColumn(name = "department_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Department department;

	public JobHistoryId getId() {
		return id;
	}

	public void setId(JobHistoryId id) {
		this.id = id;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}
}
