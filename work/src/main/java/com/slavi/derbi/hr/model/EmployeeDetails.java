package com.slavi.derbi.hr.model;

import java.io.Serializable;

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
@Table(name = "emp_details_view")
@Access(AccessType.FIELD)
public class EmployeeDetails implements Serializable {
	@Id
	@JoinColumn(name = "employee_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Employee employee;

	@JoinColumn(name = "job_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Job job;

	@JoinColumn(name = "manager_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Employee manager;

	@JoinColumn(name = "department_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Department department;

	@JoinColumn(name = "location_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Location location;

	@JoinColumn(name = "country_id")
	@ManyToOne(fetch = FetchType.LAZY)
	Country country;

	@Column(name = "first_name")
	String firstName;

	@Column(name = "last_name")
	String lastName;

	Float salary;

	@Column(name = "commission_pct")
	Float commission;

	@Column(name = "department_name")
	String departmentName;

	@Column(name = "job_title")
	String jobTitle;

	String city;

	@Column(name = "state_province")
	String stateProvance;

	@Column(name = "country_name")
	String countryName;

	@Column(name = "region_name")
	String regionName;

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
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

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
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

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStateProvance() {
		return stateProvance;
	}

	public void setStateProvance(String stateProvance) {
		this.stateProvance = stateProvance;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
}
