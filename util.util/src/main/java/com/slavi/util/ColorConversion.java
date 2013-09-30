package com.slavi.util;

public class ColorConversion {
	public static final double piOver3 = Math.PI / 3.0;
	public static final double C2PI = Math.PI * 2.0;

	/**
	 * Returns the value if min <= value <= max else returns min or max respecively.
	 */
	public static double clipValue(double value, double min, double max) {
		return Math.min(max, Math.max(min, value));
	}

	/**
	 * Returns the specified angle in the range [0..2*pi)
	 */
	public static double fixAngle2PI(double angle) {
		angle %= C2PI;
		return (angle < 0) ? C2PI + angle : angle;
//		return Math.abs(angle - Math.floor(angle / C2PI) * C2PI);
	}


	/**
	 * @param r		Value for RED [0..255] 
	 * @param g		Value for GREEN [0..255] 
	 * @param b		Value for BLUE [0..255]
	 */
	public static void clipRGB(double r, double g, double b, double dest[]) {
		dest[0] = clipValue(r, 0.0, 255.0);
		dest[1] = clipValue(g, 0.0, 255.0);
		dest[2] = clipValue(b, 0.0, 255.0);
	}

	/**
	 * DR - Red [0..1]
	 * DG - Green [0..1]
	 * DB - Blue [0..1]
	 */
	public static void clipDRGB(double DR, double DG, double DB, double dest[]) {
		dest[0] = clipValue(DR, 0.0, 1.0);
		dest[1] = clipValue(DG, 0.0, 1.0);
		dest[2] = clipValue(DB, 0.0, 1.0);
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
			return toRGB(DRGB[0], DRGB[1], DRGB[2]);
		}
		
		public static int toRGB(double DR, double DG, double DB) {
			return (int) (
				((int) clipValue(Math.round(DR * 255.0), 0.0, 255.0) << 16) | 
				((int) clipValue(Math.round(DG * 255.0), 0.0, 255.0) << 8) | 
				((int) clipValue(Math.round(DB * 255.0), 0.0, 255.0)));
		}
		
		public static void fromRGB(int rgb, double DRGB[]) {
			DRGB[0] = ((rgb >> 16) & 255) / 255.0;
			DRGB[1] = ((rgb >> 8) & 255) / 255.0;
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
			clipDRGB(DR, DG, DB, hsv);
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
				hsv[0] = fixAngle2PI(piOver3 * (hsv[1] - hsv[2]) / chroma);
			} else if (hsv[1] == max) {
				hsv[0] = piOver3 * (2.0 + (hsv[2] - hsv[0]) / chroma);
			} else {
				hsv[0] = piOver3 * (4.0 + (hsv[0] - hsv[1]) / chroma);
			}
			hsv[0] = fixAngle2PI(hsv[0]); // TODO: Obsolete
			hsv[1] = chroma / max;
			hsv[2] = max;
		}
		
		public static void toDRGB(double h, double s, double v, double DRGB[]) {
			h = fixAngle2PI(h);
			s = clipValue(s, 0.0, 1.0);
			v = clipValue(v, 0.0, 1.0);
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

	/**
	 * http://en.wikipedia.org/wiki/HSL_and_HSV
	 * 
	 * H - Hue [0..2*pi]
	 * S - Saturation [0..1]
	 * L - Lightness [0..1]
	 * 
	 * DR - Red [0..1]
	 * DG - Green [0..1]
	 * DB - Blue [0..1]
	 */
	public static class HSL {
		public static void fromDRGB(double DRGB[], double hsl[]) {
			fromDRGB(DRGB[0], DRGB[1], DRGB[2], hsl);
		}
		
		public static void toDRGB(double hsl[], double DRGB[]) {
			toDRGB(hsl[0], hsl[1], hsl[2], DRGB);
		}
		
		/**
		 * @param hsl	dest[0] = Hue
		 * 				dest[1] = Saturation
		 * 				dest[2] = Lightness
		 */
		public static void fromDRGB(double DR, double DG, double DB, double hsl[]) {
			clipDRGB(DR, DG, DB, hsl);
			double max = Math.max(Math.max(hsl[0], hsl[1]), hsl[2]);
			double min = Math.min(Math.min(hsl[0], hsl[1]), hsl[2]);
			double chroma = max - min;
			if ((max == 0.0) || (chroma == 0.0)) {
				hsl[0] = 0.0;
				hsl[1] = 0.0;
				hsl[2] = (max + min) / 2.0;
				return;
			}
			if (hsl[0] == max) {
				hsl[0] = fixAngle2PI(piOver3 * (hsl[1] - hsl[2]) / chroma);
			} else if (hsl[1] == max) {
				hsl[0] = piOver3 * (2.0 + (hsl[2] - hsl[0]) / chroma);
			} else {
				hsl[0] = piOver3 * (4.0 + (hsl[0] - hsl[1]) / chroma);
			}
			double L2 = max + min;
			hsl[2] = L2 * 0.5;
			if (hsl[2] <= 0.5) {
				hsl[1] = chroma / L2;
			} else {
				hsl[1] = chroma / (2.0 - L2);
			}
		}
		
		public static void toDRGB(double h, double s, double l, double DRGB[]) {
			h = fixAngle2PI(h);
			s = clipValue(s, 0.0, 1.0);
			l = clipValue(l, 0.0, 1.0);
			double C = s * (l <= 0.5 ? l : 1.0 - l);
			double m = l - C;
			C *= 2.0;
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
	
	/**
	 * http://bytes.com/topic/java/answers/16180-rgb-cmyk
	 * 
	 * C - Cyan [0..1]
	 * M - Magenta [0..1]
	 * Y - Yellow [0..1]
	 * K - Key (blacK) [0..1]
	 * 
	 * DR - Red [0..1]
	 * DG - Green [0..1]
	 * DB - Blue [0..1]
	 */
	public static class CMYK {
		public static void fromDRGB(double DRGB[], double cmyk[]) {
			fromDRGB(DRGB[0], DRGB[1], DRGB[2], cmyk);
		}
		
		public static void toDRGB(double cmyk[], double DRGB[]) {
			toDRGB(cmyk[0], cmyk[1], cmyk[2], cmyk[3], DRGB);
		}
		
		/**
		 * @param cmyk	dest[0] = Cyan
		 * 				dest[1] = Magenta
		 * 				dest[2] = Yellow
		 * 				dest[3] = Key/blacK
		 */
		public static void fromDRGB(double DR, double DG, double DB, double cmyk[]) {
			clipDRGB(1.0 - DR, 1.0 - DG, 1.0 - DB, cmyk);
			cmyk[3] = Math.min(cmyk[0], Math.min(cmyk[1], cmyk[2]));
			if (cmyk[3] == 1.0) {
				cmyk[0] = cmyk[1] = cmyk[2] = 0.0;
			} else {
				double scale = 1.0 / (1.0 - cmyk[3]);
				cmyk[0] = scale * (cmyk[0] - cmyk[3]);  
				cmyk[1] = scale * (cmyk[1] - cmyk[3]);  
				cmyk[2] = scale * (cmyk[2] - cmyk[3]);  
			}
		}
		
		public static void toDRGB(double c, double m, double y, double k, double DRGB[]) {
			c = clipValue(1.0 - c, 0.0, 1.0);
			m = clipValue(1.0 - m, 0.0, 1.0);
			y = clipValue(1.0 - y, 0.0, 1.0);
			k = clipValue(1.0 - k, 0.0, 1.0);
			DRGB[0] = k * c;
			DRGB[1] = k * m;
			DRGB[2] = k * y;
		}
	}
	
	/**
	 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.56.4151&rep=rep1&type=pdf
	 * R - Red (1) /green (-1)
	 * B - Blue (1) /yellow (-1)
	 * W - White (1) /black (-1)
	 */
	public static class RBW {
		public static void fromDRGB(double DRGB[], double rbw[]) {
			fromDRGB(DRGB[0], DRGB[1], DRGB[2], rbw);
		}
		
		public static void toDRGB(double rbw[], double DRGB[]) {
			toDRGB(rbw[0], rbw[1], rbw[2], DRGB);
		}
		
		/**
		 * @param rbw	dest[0] = Red
		 * 				dest[1] = Blue
		 * 				dest[2] = White
		 */
		public static void fromDRGB(double DR, double DG, double DB, double rbw[]) {
			HSV.fromDRGB(DR, DG, DB, rbw);
			double h = 6.0 * rbw[0] / C2PI;
			double s = rbw[1];
			double v = rbw[2];
			
			double a, r, b, w;
			if (v * (2 - s) > 1) {
				a = 1.5 - v + v * s;
				w = 1;
			} else {
				a = 0.5 + v;
				w = -1;
			}
			double t = Math.sqrt(a*a - 2*v*s);
			double c = a - t;
			w = w * (2-a-t);
			
			if (c == 0) {
				r = 0;
				b = 0;
			} else {
				double f;
				if (h <= 1) {
					f = 2*h-1;
					r = c;
					b = -c;
				} else if (h <= 2) {
					f = 3-2*h;
					r = -c;
					b = -c;
				} else if (h <= 4) {
					f = h-3;
					r = -c;
					b = c;
				} else {
					f = 5-h;
					r = c;
					b = c;
				}
				if (f >= 0) {
					r = (1-f)*r;
				} else {
					b = (1+f)*b;
				}
			}
			rbw[0] = r;
			rbw[1] = b;
			rbw[2] = w;
		}
		
		public static void fromDRGB_NO(double DR, double DG, double DB, double rbw[]) {
			HSV.fromDRGB(DR, DG, DB, rbw);
			double h, s, v;
			double a, k, t, c, vs, f = 0, sr = 0, sb = 0, r, b;
			h = rbw[0] / C2PI;
			s = rbw[1];
			v = rbw[2];
			
			vs = v * s;
			if (v - .5 > vs / 2) {
				a = 1.5 + vs - v;
				k = 1;
			} else {
				a = .5 + v;
				k = -1;
			}
			t = Math.sqrt(a * a - 2 * vs);
			c = a - t;
			k = k * (2 - a - t);
			h = h * 6;

			/*
			 * Figure out the hue.
			 */
			if (h <= 1) {
				sr = 1;
				sb = -1;
				f = 2 * h - 1;
			} else if (h <= 2) {
				sr = -1;
				sb = -1;
				f = 3 - 2 * h;
			} else if (h <= 4) {
				sr = -1;
				sb = 1;
				f = h - 3;
			} else if (h <= 6) {
				sr = 1;
				sb = 1;
				f = 5 - h;
			}
			if (f >= 0) {
				b = sb;
				r = (1 - f) * sr;
			} else {
				r = sr;
				b = (f + 1) * sb;
			}

			rbw[0] = r * c / 2.0;
			rbw[1] = b * c / 2.0;
			rbw[2] = k;
		}
		
		public static void toDRGB(double r, double b, double w, double DRGB[]) {
			r = clipValue(r, -1.0, 1.0);
			b = clipValue(b, -1.0, 1.0);
			w = clipValue(w, -1.0, 1.0);

			double h;
			if (r == 0 && b == 0) {
				h = 0;
			} else {
				double f;
				if (Math.abs(b) > Math.abs(r)) {
					f = 1 - Math.abs(r / b);
				} else {
					f = Math.abs(b / r) - 1;
				}
				if (r >= 0 && b <= 0) {
					h = 0.5 + f / 2;
				} else if (r <= 0 && b <= 0) {
					h = 1.5 - f / 2;
				} else if (r <= 0 && b >= 0) {
					h = 3 + f;
				} else {
					h = 5 - f;
				}
			}
			h /= 6.;
			/*
			 * Figure out the value and saturation.
			 */
			double v, vs, s;
			double c = Math.max(Math.abs(r), Math.abs(b));
			if (w >= 0) {
				v = (w + 1 + c * (1 - w)) / 2;
				vs = c * (2 - w) / 2;
			} else {
				v = (w + 1 + c) / 2;
				vs = c * (w + 2) / 2;
			}
			if (v == 0) {
				s = 0;
			} else {
				s = vs / v;
			}
			DRGB[0] = h * C2PI;
			DRGB[1] = s;
			DRGB[2] = v;
			HSV.toDRGB(DRGB, DRGB);
		}
	}
}
