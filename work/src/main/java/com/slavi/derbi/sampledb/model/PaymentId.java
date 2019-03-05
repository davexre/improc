package com.slavi.derbi.sampledb.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.google.common.base.Objects;

@Embeddable
public class PaymentId implements Serializable {
	@JoinColumn(name = "customerNumber", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	Customer customer;

	@Column(length = 50, nullable = false)
	String checkNumber;

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getCheckNumber() {
		return checkNumber;
	}

	public void setCheckNumber(String checkNumber) {
		this.checkNumber = checkNumber;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PaymentId))
			return false;
		PaymentId that = (PaymentId) o;
		return
				getCustomer().getCustomerNumber() == that.getCustomer().getCustomerNumber() &&
				getCheckNumber().equals(that.getCheckNumber());
	}

	public int hashCode() {
		return Objects.hashCode(getCustomer().getCustomerNumber(), getCheckNumber());
	}
}
