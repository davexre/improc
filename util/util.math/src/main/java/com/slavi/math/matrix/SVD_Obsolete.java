package com.slavi.math.matrix;

import com.slavi.math.MathUtil;

public class SVD_Obsolete extends Matrix{

	public SVD_Obsolete(int aSizeX, int aSizeY) {
		super(aSizeX, aSizeY);
	}

	/**
	 * NOT OK!
	 */
	public void svd(Matrix w, Matrix v) {
		double anorm = 0., g = 0., scale = 0.;
		if (getSizeX() < getSizeY())
			throw new IllegalArgumentException("m < n");
		// zliberror._assert(m>=n) ;
		double[] rv1 = new double[getSizeY()];
		w.resize(Math.min(getSizeX(), getSizeY()), 1);
		v.resize(getSizeY(), getSizeY());

		System.out.println("SVD beware results may not be sorted!");

		for (int J = 0; J < getSizeY(); J++) {
			rv1[J] = scale * g;
			g = scale = 0.;
			if (J < getSizeX()) {
				for (int i = J; i < getSizeX(); i++) {
					scale += Math.abs(this.getItem(i, J));
				}
				if (scale != 0.0) {
					double s = 0;
					for (int i = J; i < getSizeX(); i++) {
						double tmp = this.itemMul(i, J, 1.0 / scale);
						s += tmp * tmp;
					}
					double f = this.getItem(J, J);
					g = -MathUtil.SIGN(Math.sqrt(s), f);
					double h = f * g - s;
					this.setItem(J, J, f - g);
					// if (i!=(n-1)) { // CHECK
					for (int j = getSizeY() - 1; j > J; j--) {
						int k;
						double SS = 0;
						for (k = J; k < getSizeX(); k++)
							SS += this.getItem(k, J) * this.getItem(k, j);
						f = SS / h;
						for (k = J; k < getSizeX(); k++)
							this.itemAdd(k, j, f * this.getItem(k, J));
					}
					// }
					for (int k = J; k < getSizeX(); k++)
						this.itemMul(k, J, scale);
				}
			}
			w.setItem(J, 0, scale * g);
			g = scale = 0.0;
			if (J < getSizeX() && J != getSizeY() - 1) { //
				for (int k = J + 1; k < getSizeY(); k++)
					scale += Math.abs(this.getItem(J, k));
				if (scale != 0.) {
					double S = 0;
					for (int k = J + 1; k < getSizeY(); k++) { //
						this.itemMul(J, k, 1.0 / scale);
						S += this.getItem(J, k) * this.getItem(J, k);
					}
					double f = this.getItem(J, J + 1);
					g = -MathUtil.SIGN(Math.sqrt(S), f);
					double h = f * g - S;
					this.setItem(J, J + 1, f - g);
					for (int k = J + 1; k < getSizeY(); k++)
						rv1[k] = this.getItem(J, k) / h;
					if (J != getSizeX() - 1) { //
						for (int j = J + 1; j < getSizeX(); j++) { //
							int k;
							double SS = 0;
							for (k = J + 1; k < getSizeY(); k++)
								SS += this.getItem(j, k) * this.getItem(J, k);
							for (k = J + 1; k < getSizeY(); k++)
								this.itemAdd(j, k, SS * rv1[k]);
						}
					}
					for (int k = J + 1; k < getSizeY(); k++)
						this.itemMul(J, k, scale);
				}
			} // i<m && i!=n-1
			anorm = Math.max(anorm, (Math.abs(w.getItem(J, 0)) + Math.abs(rv1[J])));
		}

		printM("DIAG2");

		for (int i = getSizeY() - 1; i >= 0; --i) {
			if (i < getSizeY() - 1) { //
				if (g != 0.) {
					for (int j = i + 1; j < getSizeY(); j++)
						v.setItem(j, i, (this.getItem(i, j) / this.getItem(i, i + 1)) / g);
					for (int j = i + 1; j < getSizeY(); j++) {
						int k;
						double S = 0;
						for (k = i + 1; k < getSizeY(); k++)
							S += this.getItem(i, k) * v.getItem(k, j);
						for (k = i + 1; k < getSizeY(); k++)
							v.itemAdd(k, j, S * v.getItem(k, i));
					}
				}
				for (int j = i + 1; j < getSizeY(); j++) {
					//
					v.setItem(i, j, 0.0);
					v.setItem(j, i, 0.0);
				}
			}
			v.setItem(i, i, 1.0);
			g = rv1[i];
		}

		v.printM("V0");

		// for (i=IMIN(m,n);i>=1;i--) { // !
		// for (i = n-1; i>=0; --i) {
		for (int i = Math.min(getSizeX() - 1, getSizeY() - 1); i >= 0; --i) {
			g = w.getItem(i, 0);
			if (i < getSizeY() - 1) //
				for (int j = i + 1; j < getSizeY(); j++)
					//
					this.setItem(i, j, 0.0);
			if (g != 0.) {
				g = 1. / g;
				if (i != getSizeY() - 1) {
					for (int j = i + 1; j < getSizeY(); j++) {
						int k;
						double S = 0;
						for (k = i + 1; k < getSizeX(); k++)
							S += this.getItem(k, i) * this.getItem(k, j);
						double f = (S / this.getItem(i, i)) * g;
						for (k = i; k < getSizeX(); k++)
							this.itemAdd(k, j, f * this.getItem(k, i));
					}
				}
				for (int j = i; j < getSizeX(); j++)
					this.itemMul(j, i, g);
			} else {
				for (int j = i; j < getSizeX(); j++)
					this.setItem(j, i, 0.0);
			}
			this.itemAdd(i, i, 1.0);
		}

		printM("U0");
		w.printM("Q");
		Matrix.fromArray(new double[][] {rv1}).transpose().printM("E");

		for (int k = getSizeY() - 1; k >= 0; --k) {
			for (int its = 1; its <= 30; ++its) {
				boolean flag = true;
				int l = k;
				for (; l >= 0; --l) {
					if ((Math.abs(rv1[l]) + anorm) == anorm) {
						flag = false;
						break;
					}
					if ((Math.abs(w.getItem(l - 1, 0)) + anorm) == anorm)
						break;
				}
				if (flag) {
					double c = 0.0;
					double S = 1.0;
					for (int i = l; i <= k; i++) { //
						double f = S * rv1[i];
						rv1[i] = c * rv1[i];
						if ((Math.abs(f) + anorm) == anorm)
							break;
						g = w.getItem(i, 0);
						double h = MathUtil.hypot(f, g);
						w.setItem(i, 0, h);
						h = 1.0 / h;
						c = g * h;
						S = -f * h;
						for (int j = 0; j < getSizeX(); j++) {
							double y = this.getItem(j, l - 1);
							double z = this.getItem(j, i);
							this.setItem(j, l - 1, y * c + z * S);
							this.setItem(j, i, z * c - y * S);
						}
					}
				} // flag
				double z = w.getItem(k, 0);
				if (l == k) {
					if (z < 0.) {
						w.setItem(k, 0, -z);
						for (int j = 0; j < getSizeY(); j++)
							v.itemMul(j, k, -1.0);
					}
					break;
				} // l==k
				if (its >= 50)
					throw new ArithmeticException("no svd convergence in 50 iterations");
				// zliberror._assert(its<50, "no svd convergence in 50
				// iterations");

				// shift from bottom 2X2 minor;
				double x = w.getItem(l, 0);
				double y = w.getItem(k - 1, 0);
				g = rv1[k - 1];
				double h = rv1[k];
				double f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2 * h * y);
				g = MathUtil.hypot(f, 1.0);
				f = ((x - z) * (x + z) + h * ((y / (f + MathUtil.SIGN(g, f))) - h)) / x;

				System.out.println();
				System.out.println("K:" + k + " L:" + l);
				printM("U0");
				w.printM("Q");
				Matrix.fromArray(new double[][] {rv1}).transpose().printM("E");

				// next QR transformation
				double c = 1.0;
				double S = 1.0;
				for (int j = l; j < k; j++) {
					int i = j + 1;
					g = rv1[i];
					y = w.getItem(i, 0);
					h = S * g;
					g = c * g;
					z = MathUtil.hypot(f, h);
					rv1[j] = z;
					c = f / z;
					S = h / z;
					f = x * c + g * S;
					g = g * c - x * S;
					h = y * S;
					y *= c;
					for (int jj = 0; jj < getSizeY(); jj++) {
						x = v.getItem(jj, j);
						z = v.getItem(jj, i);
						v.setItem(jj, j, x * c + z * S);
						v.setItem(jj, i, z * c - x * S);
					}
					z = MathUtil.hypot(f, h);
					w.setItem(j, 0, z);
					if (z != 0.0) {
						z = 1.0 / z;
						c = f * z;
						S = h * z;
					}
					f = c * g + S * y;
					x = c * y - S * g;
					for (int jj = 0; jj < getSizeX(); ++jj) {
						y = this.getItem(jj, j);
						z = this.getItem(jj, i);
						this.setItem(jj, j, y * c + z * S);
						this.setItem(jj, i, z * c - y * S);
					}
				} // j<nm
				rv1[l] = 0.0;
				rv1[k] = f;
				w.setItem(k, 0, x);
			} // its
		} // k
		// free rv1
	}

	/**
	 * OK
	 */
	public void svd1(Matrix w, Matrix v) {
		int i, its, j, jj, k, l = 0, nm = 0;
		boolean flag;
		double c, f, h, s, x, y, z;
		double anorm = 0., g = 0., scale = 0.;
		if (getSizeX() < getSizeY())
			throw new IllegalArgumentException("m < n");
		// zliberror._assert(m>=n) ;
		double[] rv1 = new double[getSizeY()];
		w.resize(Math.min(getSizeX(), getSizeY()), 1);
		v.resize(getSizeY(), getSizeY());

		System.out.println("SVD beware results may not be sorted!");

		for (i = 0; i < getSizeY(); i++) {
			l = i + 1;
			rv1[i] = scale * g;
			g = s = scale = 0.;
			if (i < getSizeX()) {
				for (k = i; k < getSizeX(); k++)
					scale += Math.abs(this.getItem(k, i));
				if (scale != 0.0) {
					for (k = i; k < getSizeX(); k++) {
						this.itemMul(k, i, 1.0 / scale);
						s += this.getItem(k, i) * this.getItem(k, i);
					}
					f = this.getItem(i, i);
					g = -MathUtil.SIGN(Math.sqrt(s), f);
					h = f * g - s;
					this.setItem(i, i, f - g);
					// if (i!=(n-1)) { // CHECK
					for (j = l; j < getSizeY(); j++) {
						for (s = 0, k = i; k < getSizeX(); k++)
							s += this.getItem(k, i) * this.getItem(k, j);
						f = s / h;
						for (k = i; k < getSizeX(); k++)
							this.itemAdd(k, j, f * this.getItem(k, i));
					}
					// }
					for (k = i; k < getSizeX(); k++)
						this.itemMul(k, i, scale);
				}
			}
			w.setItem(i, 0, scale * g);
			g = s = scale = 0.0;
			if (i < getSizeX() && i != getSizeY() - 1) { //
				for (k = l; k < getSizeY(); k++)
					scale += Math.abs(this.getItem(i, k));
				if (scale != 0.) {
					for (k = l; k < getSizeY(); k++) { //
						this.itemMul(i, k, 1.0 / scale);
						s += this.getItem(i, k) * this.getItem(i, k);
					}
					f = this.getItem(i, l);
					g = -MathUtil.SIGN(Math.sqrt(s), f);
					h = f * g - s;
					this.setItem(i, l, f - g);
					for (k = l; k < getSizeY(); k++)
						rv1[k] = this.getItem(i, k) / h;
					if (i != getSizeX() - 1) { //
						for (j = l; j < getSizeX(); j++) { //
							for (s = 0, k = l; k < getSizeY(); k++)
								s += this.getItem(j, k) * this.getItem(i, k);
							for (k = l; k < getSizeY(); k++)
								this.itemAdd(j, k, s * rv1[k]);
						}
					}
					for (k = l; k < getSizeY(); k++)
						this.itemMul(i, k, scale);
				}
			} // i<m && i!=n-1
			anorm = Math.max(anorm, (Math.abs(w.getItem(i, 0)) + Math.abs(rv1[i])));
		} // i


		for (i = getSizeY() - 1; i >= 0; --i) {
			if (i < getSizeY() - 1) { //
				if (g != 0.) {
					for (j = l; j < getSizeY(); j++)
						v.setItem(j, i, (this.getItem(i, j) / this.getItem(i, l)) / g);
					for (j = l; j < getSizeY(); j++) {
						for (s = 0, k = l; k < getSizeY(); k++)
							s += this.getItem(i, k) * v.getItem(k, j);
						for (k = l; k < getSizeY(); k++)
							v.itemAdd(k, j, s * v.getItem(k, i));
					}
				}
				for (j = l; j < getSizeY(); j++) {
					//
					v.setItem(i, j, 0.0);
					v.setItem(j, i, 0.0);
				}
			}
			v.setItem(i, i, 1.0);
			g = rv1[i];
			l = i;
		}
		// for (i=IMIN(m,n);i>=1;i--) { // !
		// for (i = n-1; i>=0; --i) {
		for (i = Math.min(getSizeX() - 1, getSizeY() - 1); i >= 0; --i) {
			l = i + 1;
			g = w.getItem(i, 0);
			if (i < getSizeY() - 1) //
				for (j = l; j < getSizeY(); j++)
					//
					this.setItem(i, j, 0.0);
			if (g != 0.) {
				g = 1. / g;
				if (i != getSizeY() - 1) {
					for (j = l; j < getSizeY(); j++) {
						for (s = 0, k = l; k < getSizeX(); k++)
							s += this.getItem(k, i) * this.getItem(k, j);
						f = (s / this.getItem(i, i)) * g;
						for (k = i; k < getSizeX(); k++)
							this.itemAdd(k, j, f * this.getItem(k, i));
					}
				}
				for (j = i; j < getSizeX(); j++)
					this.itemMul(j, i, g);
			} else {
				for (j = i; j < getSizeX(); j++)
					this.setItem(j, i, 0.0);
			}
			this.itemAdd(i, i, 1.0);
		}
		for (k = getSizeY() - 1; k >= 0; --k) {
			for (its = 1; its <= 30; ++its) {
				flag = true;
				for (l = k; l >= 0; --l) {
					nm = l - 1;
					if ((Math.abs(rv1[l]) + anorm) == anorm) {
						flag = false;
						break;
					}
					if ((Math.abs(w.getItem(nm, 0)) + anorm) == anorm)
						break;
				}
				if (flag) {
					c = 0.0;
					s = 1.0;
					for (i = l; i <= k; i++) { //
						f = s * rv1[i];
						rv1[i] = c * rv1[i];
						if ((Math.abs(f) + anorm) == anorm)
							break;
						g = w.getItem(i, 0);
						h = MathUtil.hypot(f, g);
						w.setItem(i, 0, h);
						h = 1.0 / h;
						c = g * h;
						s = -f * h;
						for (j = 0; j < getSizeX(); j++) {
							y = this.getItem(j, nm);
							z = this.getItem(j, i);
							this.setItem(j, nm, y * c + z * s);
							this.setItem(j, i, z * c - y * s);
						}
					}
				} // flag
				z = w.getItem(k, 0);
				if (l == k) {
					if (z < 0.) {
						w.setItem(k, 0, -z);
						for (j = 0; j < getSizeY(); j++)
							v.itemMul(j, k, -1.0);
					}
					break;
				} // l==k
				if (its >= 50)
					throw new ArithmeticException("no svd convergence in 50 iterations");
				// zliberror._assert(its<50, "no svd convergence in 50
				// iterations");
				x = w.getItem(l, 0);
				nm = k - 1;
				y = w.getItem(nm, 0);
				g = rv1[nm];
				h = rv1[k];
				f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2 * h * y);
				g = MathUtil.hypot(f, 1.0);
				f = ((x - z) * (x + z) + h * ((y / (f + MathUtil.SIGN(g, f))) - h)) / x;
				c = s = 1.0;
				for (j = l; j <= nm; j++) {
					i = j + 1;
					g = rv1[i];
					y = w.getItem(i, 0);
					h = s * g;
					g = c * g;
					z = MathUtil.hypot(f, h);
					rv1[j] = z;
					c = f / z;
					s = h / z;
					f = x * c + g * s;
					g = g * c - x * s;
					h = y * s;
					y *= c;
					for (jj = 0; jj < getSizeY(); jj++) {
						x = v.getItem(jj, j);
						z = v.getItem(jj, i);
						v.setItem(jj, j, x * c + z * s);
						v.setItem(jj, i, z * c - x * s);
					}
					z = MathUtil.hypot(f, h);
					w.setItem(j, 0, z);
					if (z != 0.0) {
						z = 1.0 / z;
						c = f * z;
						s = h * z;
					}
					f = c * g + s * y;
					x = c * y - s * g;
					for (jj = 0; jj < getSizeX(); ++jj) {
						y = this.getItem(jj, j);
						z = this.getItem(jj, i);
						this.setItem(jj, j, y * c + z * s);
						this.setItem(jj, i, z * c - y * s);
					}
				} // j<nm
				rv1[l] = 0.0;
				rv1[k] = f;
				w.setItem(k, 0, x);
			} // its
		} // k
		// free rv1
	} // svd
}
