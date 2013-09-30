package com.test.image;

/*
 * Source borrowed from
 * http://www.righto.com/java/colorselector.html
 */

public class ColorConversion {
	abstract class ColorInput {
		abstract void toRGB(int vals[]);

		abstract void fromRGB(int vals[]);

		abstract String[] labels();

		final int RED = 0;

		final int GRN = 1;

		final int BLU = 2;

		public double abs(double v) {
			return v > 0 ? v : -v;
		}

		public double max(double a, double b) {
			return a > b ? a : b;
		}

		public double min(double a, double b) {
			return a < b ? a : b;
		}

		public double clip(double v) {
			// Clip to 0..1
			return min(1, max(0, v));
		}

	}

	class HSV extends ColorInput {
		final int HUE = 3;

		final int SAT = 4;

		final int VAL = 5;

		public void toRGB(int vals[]) {
			if (vals[SAT] == 0) {
				vals[RED] = vals[VAL];
				vals[GRN] = vals[VAL];
				vals[BLU] = vals[VAL];
				return;
			}
			double f = vals[HUE] * 6 / 256.;
			int i = (int) f;
			f = f - i;
			int p = (int) (vals[VAL] * (1. - vals[SAT] / 255.));
			int q = (int) (vals[VAL] * (1. - f * vals[SAT] / 255.));
			int t = (int) (vals[VAL] * (1. - (1 - f) * vals[SAT] / 255.));
			switch (i) {
			case 0:
				vals[RED] = vals[VAL];
				vals[GRN] = t;
				vals[BLU] = p;
				break;
			case 1:
				vals[RED] = q;
				vals[GRN] = vals[VAL];
				vals[BLU] = p;
				break;
			case 2:
				vals[RED] = p;
				vals[GRN] = vals[VAL];
				vals[BLU] = t;
				break;
			case 3:
				vals[RED] = p;
				vals[GRN] = q;
				vals[BLU] = vals[VAL];
				break;
			case 4:
				vals[RED] = t;
				vals[GRN] = p;
				vals[BLU] = vals[VAL];
				break;
			case 5:
				vals[RED] = vals[VAL];
				vals[GRN] = p;
				vals[BLU] = q;
				break;
			}
		}

		public void fromRGB(int vals[]) {
//			float v;
			int max = (int) max(max(vals[RED], vals[GRN]), vals[BLU]);
			int min = (int) min(min(vals[RED], vals[GRN]), vals[BLU]);
			vals[VAL] = max;
			if (max == 0) {
				vals[SAT] = 0;
			} else {
				vals[SAT] = (int) ((max - min) * 255 / (float) max);
			}
			if (vals[SAT] == 0) {
				vals[HUE] = 0;
			} else {
				float rc, gc, bc;
				rc = (max - vals[RED]) / (float) (max - min);
				gc = (max - vals[GRN]) / (float) (max - min);
				bc = (max - vals[BLU]) / (float) (max - min);
				float h;
				if (vals[RED] == max) {
					h = bc - gc;
				} else if (vals[GRN] == max) {
					h = 2 + rc - bc;
				} else {
					h = 4 + gc - rc;
				}
				if (h < 0) {
					h += 6;
				}
				vals[HUE] = (int) (h / 6. * 255);
			}
		}

		public String[] labels() {
			String s[] = new String[6];
			s[0] = "Hue";
			s[1] = "";
			s[2] = "Saturation";
			s[3] = "";
			s[4] = "Value";
			s[5] = "";
			return s;
		}
	}

	class RBW extends ColorInput {
		final int R = 3;

		final int B = 4;

		final int W = 5;

		final int HUE = 3;

		final int SAT = 4;

		final int VAL = 5;

		HSV hsv = null;

		public void toRGB(int vals[]) {
			double r, b, w;
			double h = 0, s = 0, v = 0;
			double c, vs, f = 0;
			int vals2[] = new int[6];

			// -1 to 1
			r = vals[R] / 128. - 1.;
			b = vals[B] / 128. - 1;
			w = vals[W] / 128. - 1;
			if (r == 0 && b == 0) {
				h = 0;
			} else {
				if (abs(b) > abs(r)) {
					f = 1 - abs(r / b);
				} else {
					f = abs(b / r) - 1;
				}
				if (r >= 0 && b <= 0) {
					h = .5 + f / 2;
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
			c = max(abs(r), abs(b));
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

			vals2[HUE] = (int) (h * 255.);
			vals2[SAT] = (int) (s * 255.);
			vals2[VAL] = (int) (v * 255.);
			hsv = new HSV();
			hsv.toRGB(vals2);
			vals[0] = vals2[0];
			vals[1] = vals2[1];
			vals[2] = vals2[2];
		}

		public void fromRGB(int vals[]) {
			hsv = new HSV();
			hsv.fromRGB(vals);
			double h, s, v;
			double a, k, t, c, vs, f = 0, sr = 0, sb = 0, r, b;
			h = vals[HUE] / 255.;
			s = vals[SAT] / 255.;
			v = vals[VAL] / 255.;
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

			vals[R] = (int) ((r * c / 2 + .5) * 255);
			vals[B] = (int) ((b * c / 2 + .5) * 255);
			vals[W] = (int) (((k + 1) / 2) * 255);

		}

		public String[] labels() {
			String s[] = new String[6];
			s[0] = "Green";
			s[1] = "Red";
			s[2] = "Yellow";
			s[3] = "Blue";
			s[4] = "Black";
			s[5] = "White";
			return s;
		}
	}

	class RGYB extends ColorInput {
		final int RG = 3;

		final int YB = 4;

		final int BW = 5;

		public void toRGB(int vals[]) {
			double rg, yb, bw;
			double r, g, b;
			rg = vals[RG] / 255.;
			yb = vals[YB] / 255.;
			bw = vals[BW] / 255.;
			r = rg * bw * 2;
			g = yb * bw * 2;
			b = (1 - max(rg, yb)) * bw * 2;
			r = clip(r);
			g = clip(g);
			b = clip(b);
			vals[RED] = (int) (r * 255);
			vals[GRN] = (int) (g * 255);
			vals[BLU] = (int) (b * 255);
		}

		public void fromRGB(int vals[]) {
			double r, g, b;
			double rg = 0, yb = 0, bw;
			r = vals[RED] / 255.;
			g = vals[GRN] / 255.;
			b = vals[BLU] / 255.;
			if (r > g) {
				bw = (r + b) / 2;
			} else {
				bw = (g + b) / 2;
			}
			if (bw != 0) {
				rg = r / bw / 2;
				yb = g / bw / 2;
			}
			vals[RG] = (int) (rg * 255);
			vals[YB] = (int) (yb * 255);
			vals[BW] = (int) (bw * 255);
		}

		public String[] labels() {
			String s[] = new String[6];
			s[0] = "BG";
			s[1] = "RY";
			s[2] = "RB";
			s[3] = "YG";
			s[4] = "Black";
			s[5] = "White";
			return s;
		}
	}

	class HLS extends ColorInput {
		final int H = 3;

		final int L = 4;

		final int S = 5;

		// From Computer Graphics Principles and Practice, Foley, van Dam,
		// Feiner, Hughes
		public double value(double n1, double n2, double hue) {
			if (hue < 0) {
				hue += 360;
			}
			if (hue > 360) {
				hue -= 360;
			}
			if (hue < 60) {
				return n1 + (n2 - n1) * hue / 60.;
			} else if (hue < 180) {
				return n2;
			} else if (hue < 240) {
				return n1 + (n2 - n1) * (240 - hue) / 60.;
			} else {
				return n1;
			}
		}

		public void toRGB(int vals[]) {
			double h, l, s;
			double r, g, b;
			h = vals[H] / 255. * 360;
			l = vals[L] / 255.;
			s = vals[S] / 255.;
			double m1, m2;
			if (l < .5) {
				m2 = l * (1 + s);
			} else {
				m2 = l + s - l * s;
			}
			m1 = 2 * l - m2;
			if (s == 0) {
				r = l;
				g = l;
				b = l;
			} else {
				r = value(m1, m2, h + 120);
				g = value(m1, m2, h);
				b = value(m1, m2, h - 120);
			}
			vals[RED] = (int) (r * 255);
			vals[GRN] = (int) (g * 255);
			vals[BLU] = (int) (b * 255);
		}

		public void fromRGB(int vals[]) {
			double r, g, b;
			double h, l, s;
			r = vals[RED] / 255.;
			g = vals[GRN] / 255.;
			b = vals[BLU] / 255.;
			double min = min(min(r, g), b);
			double max = max(max(r, g), b);
			l = (max + min) / 2;
			if (max == min) {
				s = 0;
				h = 0;
			} else {
				if (l <= 0.5) {
					s = (max - min) / (max + min);
				} else {
					s = (max - min) / (2 - max - min);
				}
				double delta = max - min;
				if (r == max) {
					h = (g - b) / delta;
				} else if (g == max) {
					h = 2 + (b - r) / delta;
				} else {
					h = 4 + (r - g) / delta;
				}
				h /= 6.;
				if (h < 0) {
					h += 1;
				}
			}
			vals[H] = (int) (h * 255);
			vals[L] = (int) (l * 255);
			vals[S] = (int) (s * 255);
		}

		public String[] labels() {
			String s[] = new String[6];
			s[0] = "Hue";
			s[1] = "";
			s[2] = "Lightness";
			s[3] = "";
			s[4] = "Saturation";
			s[5] = "";
			return s;
		}
	}

	class YIQ extends ColorInput {
		final int Y = 3;

		final int I = 4;

		final int Q = 5;

		public void toRGB(int vals[]) {
			double y, i, q;
			double r, g, b;
			y = vals[Y] / 255.; // 0 to 1
			i = (vals[I] / 255. * 2 - 1) * .596; // -.596 to .596
			q = (vals[Q] / 255. * 2 - 1) * .522; // -.522 to .522
			r = clip(y + .956 * i + .623 * q);
			g = clip(y - .272 * i - .648 * q);
			b = clip(y - 1.105 * i + .705 * q);

			vals[RED] = (int) (r * 255);
			vals[GRN] = (int) (g * 255);
			vals[BLU] = (int) (b * 255);
		}

		public void fromRGB(int vals[]) {
			double r, g, b;
			double y, i, q;
			r = vals[RED] / 255.;
			g = vals[GRN] / 255.;
			b = vals[BLU] / 255.;
			y = .299 * r + .587 * g + .114 * b;
			i = .596 * r - .274 * g - .322 * b;
			q = .211 * r - .522 * g + .311 * b;
			i = (i / .596 + 1) / 2; // 0 to 1
			q = (q / .522 + 1) / 2; // 0 to 1

			vals[Y] = (int) (y * 255);
			vals[I] = (int) (i * 255);
			vals[Q] = (int) (q * 255);
		}

		public String[] labels() {
			String s[] = new String[6];
			s[0] = "Y";
			s[1] = "";
			s[2] = "I";
			s[3] = "";
			s[4] = "Q";
			s[5] = "";
			return s;
		}
	}
}
