package com.slavi.util;

import com.slavi.math.MathUtil;

public class ColorConversion {
	public static final double piOver3 = Math.PI / 3.0;
	
	/**
	 * @param r		Value for RED [0..255] 
	 * @param g		Value for GREEN [0..255] 
	 * @param b		Value for BLUE [0..255]
	 */
	public static void clipRGB(double r, double g, double b, double dest[]) {
		dest[0] = MathUtil.clipValue(r, 0.0, 255.0);
		dest[1] = MathUtil.clipValue(g, 0.0, 255.0);
		dest[2] = MathUtil.clipValue(b, 0.0, 255.0);
	}

	/**
	 * DR - Red [0..1]
	 * DG - Green [0..1]
	 * DB - Blue [0..1]
	 */
	public static void clipDRGB(double DR, double DG, double DB, double dest[]) {
		dest[0] = MathUtil.clipValue(DR, 0.0, 1.0);
		dest[1] = MathUtil.clipValue(DG, 0.0, 1.0);
		dest[2] = MathUtil.clipValue(DB, 0.0, 1.0);
	}
	
	public static class RGB {
		public static void fromDRGB(double DRGB[], double rgb[]) {
			fromDRGB(DRGB[0], DRGB[1], DRGB[2], rgb);
		}
		
		public static void toDRGB(double rgb[], double DRGB[]) {
			toDRGB(rgb[0], rgb[1], rgb[2], DRGB);
		}
		
		public static void fromDRGB(double DR, double DG, double DB, double rgb[]) {
			clipDRGB(DR, DG, DB, rgb);
			rgb[0] *= 255.0;
			rgb[1] *= 255.0;
			rgb[2] *= 255.0;
		}
		
		public static void toDRGB(double r, double g, double b, double DRGB[]) {
			clipRGB(r, g, b, DRGB);
			DRGB[0] /= 255.0;
			DRGB[1] /= 255.0;
			DRGB[2] /= 255.0;
		}
		
		public static int toRGB(double DRGB[]) {
			return (int) (
					((int) MathUtil.clipValue(DRGB[0] * 255.0, 0.0, 255.0) << 16) | 
					((int) MathUtil.clipValue(DRGB[1] * 255.0, 0.0, 255.0) << 8) | 
					((int) MathUtil.clipValue(DRGB[2] * 255.0, 0.0, 255.0)));
		}
		
		public static int toRGB(double DR, double DG, double DB) {
			return (int) (
				((int) MathUtil.clipValue(DR * 255.0, 0.0, 255.0) << 16) | 
				((int) MathUtil.clipValue(DG * 255.0, 0.0, 255.0) << 8) | 
				((int) MathUtil.clipValue(DB * 255.0, 0.0, 255.0)));
		}
		
		public static void fromRGB(int rgb, double DRGB[]) {
			DRGB[0] = ((rgb >> 16) & 255) / 255.0;
			DRGB[1] = ((rgb >> 6) & 255) / 255.0;
			DRGB[2] = ((rgb) & 255) / 255.0;
		}		
	}

	/**
	 * http://en.wikipedia.org/wiki/HSL_and_HSV
	 * 
	 * H - Hue [0..2*pi]
	 * S - Saturation [0..1]
	 * V - Value [0..1]
	 * 
	 * DR - Red [0..1]
	 * DG - Green [0..1]
	 * DB - Blue [0..1]
	 */
	public static class HSV {
		public static void fromDRGB(double DRGB[], double hsv[]) {
			fromDRGB(DRGB[0], DRGB[1], DRGB[2], hsv);
		}
		
		public static void toDRGB(double hsv[], double DRGB[]) {
			toDRGB(hsv[0], hsv[1], hsv[2], DRGB);
		}
		
		/**
		 * @param hsv	dest[0] = Hue
		 * 				dest[1] = Saturation
		 * 				dest[2] = Value
		 */
		public static void fromDRGB(double DR, double DG, double DB, double hsv[]) {
			clipRGB(DR, DG, DB, hsv);
			double max = Math.max(Math.max(hsv[0], hsv[1]), hsv[2]);
			double min = Math.min(Math.min(hsv[0], hsv[1]), hsv[2]);
			double chroma = max - min;
			if ((max == 0.0) || (chroma == 0.0)) {
				hsv[0] = 0.0;
				hsv[1] = 0.0;
				hsv[2] = max;
				return;
			}
			if (hsv[0] == max) {
				hsv[0] = MathUtil.fixAngle2PI(piOver3 * (hsv[1] - hsv[2]) / chroma);
			} else if (hsv[1] == max) {
				hsv[0] = piOver3 * (2.0 + (hsv[2] - hsv[0]) / chroma);
			} else {
				hsv[0] = piOver3 * (4.0 + (hsv[0] - hsv[1]) / chroma);
			}
			hsv[1] = chroma / max;
			hsv[2] = max;
		}
		
		public static void toDRGB(double h, double s, double v, double DRGB[]) {
			h = MathUtil.fixAngle2PI(h);
			s = MathUtil.clipValue(s, 0.0, 1.0);
			v = MathUtil.clipValue(v, 0.0, 1.0);
			double C = v * s;
			double m = v - C;
			double h1 = h / piOver3;
			double X = C * (1 - Math.abs(h1 % 2 - 1));
			switch ((int) h1 % 6) {
			case 0:
				DRGB[0] = m + C;
				DRGB[1] = m + X;
				DRGB[2] = m;
				break;
			case 1:
				DRGB[0] = m + X;
				DRGB[1] = m + C;
				DRGB[2] = m;
				break;
			case 2:
				DRGB[0] = m;
				DRGB[1] = m + C;
				DRGB[2] = m + X;
				break;
			case 3:
				DRGB[0] = m;
				DRGB[1] = m + X;
				DRGB[2] = m + C;
				break;
			case 4:
				DRGB[0] = m + X;
				DRGB[1] = m;
				DRGB[2] = m + C;
				break;
			case 5:
				DRGB[0] = m + C;
				DRGB[1] = m;
				DRGB[2] = m + X;
				break;
			}
		}
	}
}
