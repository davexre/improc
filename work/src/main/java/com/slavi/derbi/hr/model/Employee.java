package com.slavi.derbi.hr.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "employees")
@Access(AccessType.FIELD)
public class Employee implements Serializable {
	@OrderBy
	@Id
	@Column(name = "employee_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	Integer id;

	@Column(name = "first_name", length = 20)
	String firstName;

	@Column(name = "last_name", length = 25, nullable = false)
	String lastName;

	@Column(length = 25, nullable = false)
	String email;

	@Column(name = "phone_number", length = 20)
	String phoneNumber;

	@Column(name = "hire_date", nullable = false)
	Date hireDate;

	@JoinColumn(name = "job_id", nullable = false)
	@ManyToOne
	Job job;

	Float salary;

	@Column(name = "commission_pct")
	Float commission;

	@JoinColumn(name = "manager_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Employee manager;

	@JoinColumn(name = "department_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Department department;

	@Column(length = 300)
	String dn;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Date getHireDate() {
		return hireDate;
	}

	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Float getSalary() {
		return salary;
	}

	public void setSalary(Float salary) {
		this.salary = salary;
	}

	public Float getCommission() {
		return commission;
	}

	public void setCommission(Float commission) {
		this.commission = commission;
	}

	public Employee getManager() {
		return manager;
	}

	public void setManager(Employee manager) {
		this.manager = manager;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}
}
