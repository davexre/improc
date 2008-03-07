package com.slavi.parallel;

import com.slavi.img.DGaussianFilter;

public class BlurStripeTask {

	DGaussianFilter filter;
	
	Stripe data[];
	
	public BlurStripeTask(DGaussianFilter filter) {
		this.filter = filter;
		data = new Stripe[filter.mask.length];
	}
	
	public void executeTask() {
		//dest.resize(src.sizeX, src.sizeY);
		int sizeX = data[0].sizeX;
		int sizeY = filter.mask.length;
		Stripe dest = null;
		Stripe tmp = null;
		dest.resize(sizeX);

		for (int i = 0; i < sizeX; i++) {
			double sum = 0;
			for (int k = 0; k < sizeY; k++) 
				sum += data[k].getPixel(i) * filter.mask[k];
			tmp.setPixel(i, sum / sizeY);
		}
		
		for (int i = 0; i < sizeX; i++) {
			double sum = 0;
			double msum = 0;
			for (int k = 0; k < sizeY; k++) {
				int indx = i + k - (sizeY >> 1);
				if ((indx >= 0) && (indx < sizeX))
					sum += tmp.getPixel(indx) * filter.mask[k];
				else
					msum += filter.mask[k];
			}
			dest.setPixel(i, sum / (1 - msum));
		}
	}
}
