package com.slavi.improc.pano;


public class InterpolatorFunctions {

	
	public static double sinc(double x) {
		if (x == 0.0)
			return 1.0;
		x *= Math.PI;
		return Math.sin(x) / x;
	}

	public static abstract class Interpolator {
		public double support = 0.0;
		public abstract double filter(double x);
		
		public Interpolator(double support) {
			this.support = support;
		}
	}

	public static enum InterpolatorEnum {
		poly3,				// Third order polynomial fitting 16 nearest pixels
		spline16,			// Cubic Spline fitting 16 nearest pixels
		spline36,			// Cubic Spline fitting 36 nearest pixels
		sinc256,			// Sinc windowed to 8 pixels
		spline64,			// Cubic Spline fitting 64 nearest pixels
		bilinear,			// Bilinear interpolation
		nn,					// Nearest neighbor
		sinc1024,
		// Thomas Rauscher: New antialiasing filter. 
		// Plots of the functions are available at http://www.pano2qtvr.com/dll_patch/
		box,				// Antialiasing: Box
		triangle,			// Antialiasing: Bartlett/Triangle Filter
		hermite,			// Antialiasing: Hermite Filter
		hanning,			// Antialiasing: Hanning Filter
		hamming,			// Antialiasing: Hamming Filter
		blackman,			// Antialiasing: Blackmann Filter
		gaussian,			// Antialiasing: Gaussian 1/sqrt(2) Filter (blury)
		gaussian2,			// Antialiasing: Gaussian 1/2 Filter (sharper)
		quadratic,			// Antialiasing: Quadardic Filter
		cubic,				// Antialiasing: Cubic Filter
		catrom,				// Antialiasing: Catmull-Rom Filter
		mitchell,			// Antialiasing: Mitchell Filter
		lanczos2,			// Antialiasing: Lanczos2 Filter
		lanczos3,			// Antialiasing: Lanczos3 Filter
		blackmanbessel,		// Antialiasing: Blackman/Bessel Filter
		blackmansinc		// Antialiasing: Blackman/sinc Filter
	}

	public static Interpolator getInterpolator(InterpolatorEnum i) {
		switch (i) {
		case poly3:
		case spline16:
		case spline36:
		case sinc256:
		case spline64:
		case bilinear:
		case nn:	
		case sinc1024:
			return null;
		case box:				return new Box();
		case triangle:			return new Triangle();
		case hermite:			return new Hermite();
		case hanning:			return new Hanning();
		case hamming:			return new Hamming();
		case blackman:			return new Blackman();
		case gaussian:			return new Gaussian();
		case gaussian2:			return new Gaussian_2();
		case quadratic:			return new Quadratic();
		case cubic:				return new Cubic();
		case catrom:			return new Catrom();
		case mitchell:			return new Mitchell();
		case lanczos2:			return new Lanczos2();
		case lanczos3:			return new Lanczos3();
		case blackmanbessel:	return new BlackmanBessel();
		case blackmansinc:		return new BlackmanSinc();
		}
		return null;
	}

	// Cubic polynomial with parameter A
	// A = -1: sharpen; A = - 0.5 homogeneous
	// make sure x >= 0
	// 0 <= x < 1
	static double cubic01(double x) {
		final double A = -0.75;
		return ((A + 2.0) * x - (A + 3.0)) * x * x + 1.0;
	}

	// 1 <= x < 2
	static double cubic12(double x) {
		final double A = -0.75;
		return ((A * x - 5.0 * A) * x + 8.0 * A) * x - 4.0 * A;

	}
	
	static void CUBIC(double x, double a[]) {
		a[3] = cubic12(2.0 - x);
		a[2] = cubic01(1.0 - x);
		a[1] = cubic01(x);
		a[0] = cubic12(x + 1.0);
	}

	static void poly3(double x, double y, int color, byte rgb[]) {
		int ndim = 4;
		int SamplesPerPixel = 3;
		double threshold = 255.0 / 16.0;
		
		double w[] = new double[ndim];
		CUBIC(x, w);
		if (color == 0) {
			for (int k = 0; k < ndim; k++) {
				double r = (double) rgb[k];
				double ad = 0.0;
				double rd = 0.0;
				double gd = 0.0;
				double bd = 0.0;
				
				for (int i = 0; i < ndim; i++) {
					double weight = w[i];
					double ri = r + i * SamplesPerPixel;
//					rd += weight * rgb[]
				}
			}
		}
		
	}
	
	public static class Box extends Interpolator{
		public Box() {
			super(0.5);
		}
		
		public double filter(double x) {
			if (x < -0.5)
				return (0.0);
			if (x < 0.5)
				return (1.0);
			return (0.0);
		}
	}

	public static class Triangle extends Interpolator{
		public Triangle() {
			super(1.0);
		}
		
		public double filter(double x) {
			if (x < -1.0)
				return (0.0);
			if (x < 0.0)
				return (1.0 + x);
			if (x < 1.0)
				return (1.0 - x);
			return (0.0);
		}
	}

	public static class Hermite extends Interpolator{
		public Hermite() {
			super(1.0);
		}
		
		public double filter(double x) {
			if (x < -1.0)
				return (0.0);
			if (x < 0.0)
				return ((2.0 * (-x) - 3.0) * (-x) * (-x) + 1.0);
			if (x < 1.0)
				return ((2.0 * x - 3.0) * x * x + 1.0);
			return (0.0);
		}
	}

	public static class Hanning extends Interpolator{
		public Hanning() {
			super(1.0);
		}
		
		public double filter(double x) {
			if (Math.abs(x) > 1.0)
				return 0;
			return (0.5 + 0.5 * Math.cos(Math.PI * x));
		}
	}

	public static class Hamming extends Interpolator{
		public Hamming() {
			super(1.0);
		}
		
		public double filter(double x) {
			if (Math.abs(x) > 1.0)
				return 0;
			return (0.54 + 0.46 * Math.cos(Math.PI * x));
		}
	}

	public static class Blackman extends Interpolator{
		public Blackman() {
			super(1.0);
		}
		
		public double filter(double x) {
			if (x < -1.0)
				return (0.0);
			if (x > 1.0)
				return (0.0);
			return (0.42 + 0.5 * Math.cos(Math.PI * x) + 0.08 * Math.cos(2 * Math.PI * x));
		}
	}

	public static class Gaussian extends Interpolator{
		public Gaussian() {
			super(1.25);
		}
		
		public double filter(double x) {
			// Gaussian 1/sqrt(2)
			return (Math.sqrt(2.0 / Math.PI) * Math.exp(-2.0 * x * x));
		}
	}

	public static class Gaussian_2 extends Interpolator{
		public Gaussian_2() {
			super(1.0);
		}
		
		public double filter(double x) {
			// Gaussian 1/2
			// double d=0.5;
			// return ( 1.0/(2.0*d*sqrt(2.0*PI)) * exp(-2.0*x*x/(2*d*d)) );
			return (1.0 / Math.sqrt(2.0 * Math.PI) * Math.exp(-4.0 * x * x));
		}
	}

	public static class Quadratic extends Interpolator{
		public Quadratic() {
			super(1.5);
		}
		
		public double filter(double x) {
			if (x < -1.5)
				return (0.0);
			if (x < -0.5)
				return (0.5 * (x + 1.5) * (x + 1.5));
			if (x < 0.5)
				return (0.75 - x * x);
			if (x < 1.5)
				return (0.5 * (x - 1.5) * (x - 1.5));
			return (0.0);
		}
	}

	public static class Cubic extends Interpolator{
		public Cubic() {
			super(2.0);
		}
		
		public double filter(double x) {
			if (x < -2.0)
				return (0.0);
			if (x < -1.0)
				return ((2.0 + x) * (2.0 + x) * (2.0 + x) / 6.0);
			if (x < 0.0)
				return ((4.0 + x * x * (-6.0 - 3.0 * x)) / 6.0);
			if (x < 1.0)
				return ((4.0 + x * x * (-6.0 + 3.0 * x)) / 6.0);
			if (x < 2.0)
				return ((2.0 - x) * (2.0 - x) * (2.0 - x) / 6.0);
			return (0.0);
		}
	}
	
	public static class Catrom extends Interpolator{
		public Catrom() {
			super(2.0);
		}
		
		public double filter(double x) {
			if (x < -2.0)
				return (0.0);
			if (x < -1.0)
				return (0.5 * (4.0 + x * (8.0 + x * (5.0 + x))));
			if (x < 0.0)
				return (0.5 * (2.0 + x * x * (-5.0 - 3.0 * x)));
			if (x < 1.0)
				return (0.5 * (2.0 + x * x * (-5.0 + 3.0 * x)));
			if (x < 2.0)
				return (0.5 * (4.0 + x * (-8.0 + x * (5.0 - x))));
			return (0.0);
		}
	}

	public static class Mitchell extends Interpolator{
		public Mitchell() {
			super(2.0);
		}
		
		public double filter(double x) {
			if (x < -2.0)
				return(0.0);
	
			double B = 1.0 / 3.0;
			double C = 1.0 / 3.0;
	
			double P0 = (  6.0- 2.0*B       )/6.0;
			double P2 = (-18.0+12.0*B+ 6.0*C)/6.0;
			double P3 = ( 12.0- 9.0*B- 6.0*C)/6.0;
			double Q0 = (       8.0*B+24.0*C)/6.0;
			double Q1 = (     -12.0*B-48.0*C)/6.0;
			double Q2 = (       6.0*B+30.0*C)/6.0;
			double Q3 = (     - 1.0*B- 6.0*C)/6.0;
	
			if (x < -1.0)
				return Q0 - x * (Q1 - x * (Q2 - x * Q3));
			if (x < 0.0)
				return P0 + x * x * (P2 - x * P3);
			if (x < 1.0)
				return P0 + x * x * (P2 + x * P3);
			if (x < 2.0)
				return Q0 + x * (Q1 + x * (Q2 + x * Q3));
			return 0.0;
		}
	}
	
	// antialias lanczos2
	public static class Lanczos2 extends Interpolator{
		public Lanczos2() {
			super(2.0);
		}
		
		public double filter(double x) {
			if (Math.abs(x) >= 2)
				return 0;
			return sinc(x) * sinc(x / 2);
		}
	}

	// antialias lanczos3
	public static class Lanczos3 extends Interpolator{
		public Lanczos3() {
			super(3.0);
		}
		
		public double filter(double x) {
			if (Math.abs(x) >= 3)
				return 0;
			return sinc(x) * sinc(x / 3);
		}
	}

	static double J1(double x) {
		final double Pone[] = { 
				0.581199354001606143928050809e+21, 
				-0.6672106568924916298020941484e+20,
				0.2316433580634002297931815435e+19, 
				-0.3588817569910106050743641413e+17,
				0.2908795263834775409737601689e+15, 
				-0.1322983480332126453125473247e+13,
				0.3413234182301700539091292655e+10, 
				-0.4695753530642995859767162166e+7,
				0.270112271089232341485679099e+4 }; 
		final double Qone[] = { 
				0.11623987080032122878585294e+22,
				0.1185770712190320999837113348e+20, 
				0.6092061398917521746105196863e+17,
				0.2081661221307607351240184229e+15, 
				0.5243710262167649715406728642e+12,
				0.1013863514358673989967045588e+10, 
				0.1501793594998585505921097578e+7,
				0.1606931573481487801970916749e+4, 
				0.1e+1 };
		double p = Pone[8];
		double q = Qone[8];
		for (int i = 7; i >= 0; i--) {
			p = p * x * x + Pone[i];
			q = q * x * x + Qone[i];
		}
		return (p / q);
	}

	static double P1(double x)	{
		double Pone[] = {
				0.352246649133679798341724373e+5,
				0.62758845247161281269005675e+5,
				0.313539631109159574238669888e+5,
				0.49854832060594338434500455e+4,
				0.2111529182853962382105718e+3,
				0.12571716929145341558495e+1 };
		double Qone[] = {
				0.352246649133679798068390431e+5,
				0.626943469593560511888833731e+5,
				0.312404063819041039923015703e+5,
				0.4930396490181088979386097e+4,
				0.2030775189134759322293574e+3,
				0.1e+1 };
		double p = Pone[5];
		double q = Qone[5];
		for (int i = 4; i >= 0; i--) {
			p = p * (8.0 / x) * (8.0 / x) + Pone[i];
			q = q * (8.0 / x) * (8.0 / x) + Qone[i];
		}
		return (p / q);
	}

	static double Q1(double x) {
		double Pone[] = {
				0.3511751914303552822533318e+3,
				0.7210391804904475039280863e+3,
				0.4259873011654442389886993e+3,
				0.831898957673850827325226e+2,
				0.45681716295512267064405e+1,
				0.3532840052740123642735e-1 };
		double Qone[] = {
				0.74917374171809127714519505e+4,
				0.154141773392650970499848051e+5,
				0.91522317015169922705904727e+4,
				0.18111867005523513506724158e+4,
				0.1038187585462133728776636e+3,
				0.1e+1 };
		double p = Pone[5];
		double q = Qone[5];
		for (int i = 4; i >= 0; i--) {
			p = p * (8.0 / x) * (8.0 / x) + Pone[i];
			q = q * (8.0 / x) * (8.0 / x) + Qone[i];
		}
		return (p / q);
	}
	
	static double BesselOrderOne(double x) {
		double p, q;

		if (x == 0.0)
			return (0.0);
		p = x;
		if (x < 0.0)
			x = (-x);
		if (x < 8.0)
			return (p * J1(x));
		q = Math.sqrt(2.0 / (Math.PI * x))
				* (P1(x) * (1.0 / Math.sqrt(2.0) * (Math.sin(x) - Math.cos(x))) - 8.0 / x * Q1(x)
						* (-1.0 / Math.sqrt(2.0) * (Math.sin(x) + Math.cos(x))));
		if (p < 0.0)
			q = (-q);
		return (q);
	}
	
	public static double Bessel(double x) {
		if (x == 0.0)
			return (Math.PI / 4.0);
		return (BesselOrderOne(Math.PI * x) / (2.0 * x));
	}

	public static class BlackmanBessel extends Interpolator{
		public BlackmanBessel() {
			super(3.2383);
		}
		
		Blackman blackman = new Blackman();
		
		public double filter(double x) {
			return blackman.filter(x / support) * Bessel(x);
		}
	}

	public static class BlackmanSinc extends Interpolator{
		public BlackmanSinc() {
			super(4.0);
		}
		
		Blackman blackman = new Blackman();
		
		public double filter(double x) {
			return (blackman.filter(x / support) * sinc(x));
		}
	}
}
