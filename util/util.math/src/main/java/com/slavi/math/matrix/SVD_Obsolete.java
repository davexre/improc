package com.slavi.math.matrix;

import com.slavi.math.MathUtil;

public class SVD_Obsolete extends Matrix{

	public SVD_Obsolete(int aSizeX, int aSizeY) {
		super(aSizeX, aSizeY);
	}

	public void svd(Matrix w, Matrix v) {
		int i, j, k, l = 0;
		double f, h, s;
		double anorm = 0., g = 0., scale = 0.;
//		if (sizeX < sizeY)
//			throw new IllegalArgumentException("m < n");
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
					this.setItem(i, l,  f - g);
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
	}	
	
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
