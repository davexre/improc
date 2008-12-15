package com.slavi.improc.pano;

public class GammaCorrection {

	static final int channelSize = 256;

	static final int channelStretch = 16;

	static final int gammaBufferSize = channelSize * channelStretch;
	
	double gamma;
	
	int G[] = new int[gammaBufferSize];
	
	double deG[] = new double[channelSize];
	
	public int applyGamma(double pix) {
		int p = (int) (pix * channelStretch);
		return G[p < 0 ? 0 : (p >= gammaBufferSize ? (gammaBufferSize - 1) : p)];
	}

	public double deGamma(int color) {
		return deG[color < 0 ? 0 : (color >= channelSize ? (channelSize - 1) : color)];
	}
	
	public double getGamma() {
		return gamma;
	}
	
	public void setGamma(double gamma) {
		if (gamma <= 0.0)
			gamma = 1.0;
		this.gamma = gamma;
		
		double rgamma = 1.0 / gamma;
		double gnorm = (channelSize - 1) / Math.pow((channelSize - 1), gamma);
		deG[0] = 0.0;
		for (int i = deG.length - 1; i > 0; i--)
			deG[i] = Math.pow(i, rgamma) * gnorm;
		
		gnorm = (channelSize - 1) / Math.pow((channelSize - 1), rgamma);
		G[0] = 0;
		for (int i = gammaBufferSize - 1; i > 0; i--) {
			int x = (int) (Math.pow((double) i / (double) channelStretch, rgamma) * gnorm + 0.5);
			G[0] = x < 0 ? 0 : (x > 255 ? 255 : x);
		}
	}
}
