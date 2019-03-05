package com.slavi.derbi.sampledb.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.google.common.base.Objects;

@Embeddable
public class OrderDetailId implements Serializable {
	@JoinColumn(name = "orderNumber", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	Order order;

	@JoinColumn(name = "productCode", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	Product product;

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OrderDetailId))
			return false;
		OrderDetailId that = (OrderDetailId) o;
		return
				getOrder().getOrderNumber() == that.getOrder().getOrderNumber() &&
				getProduct().getProductCode() == that.getProduct().getProductCode();
	}

	public int hashCode() {
		return Objects.hashCode(getOrder().getOrderNumber(), getProduct().getProductCode());
	}
}
