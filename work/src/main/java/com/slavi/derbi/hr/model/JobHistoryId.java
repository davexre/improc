package com.slavi.derbi.hr.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.google.common.base.Objects;

@Embeddable
public class JobHistoryId implements Serializable {
	@JoinColumn(name = "employee_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	Employee employee;

	@Column(name = "start_date", nullable = false)
	Date startDate;

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof JobHistoryId))
			return false;
		JobHistoryId that = (JobHistoryId) o;
		return
				getEmployee().getId() == that.getEmployee().getId() &&
				getStartDate() == that.getStartDate();
	}

	public int hashCode() {
		return Objects.hashCode(getEmployee().getId(), getStartDate());
	}
}
