package com.slavi.parallel;

public class StripeHandler {

	Stripe rows[];
	
	int sizeY;
	
	public StripeHandler(int sizeY) {
		sizeY = 0;
		
	}
	
	public void resize(int sizeY) {
		if (this.sizeY == sizeY)
			return;
		if (sizeY <= 0)
			throw new Error("Invalid size parameter");
		this.sizeY = sizeY;
		this.rows = new Stripe[sizeY];
	}
	
	public Stripe getStripe(int atY) {
		if (atY < 0)
			atY = 0;
		if (atY > sizeY)
			atY = sizeY;
		return rows[atY];
	}
	
	
}
