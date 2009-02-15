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
					scale += Math.abs(this.m[k][i]);
				if (scale != 0.0) {
					for (k = i; k < getSizeX(); k++) {
						this.m[k][i] /= scale;
						s += this.m[k][i] * this.m[k][i];
					}
					f = this.m[i][i];
					g = -MathUtil.SIGN(Math.sqrt(s), f);
					h = f * g - s;
					this.m[i][i] = f - g;
					// if (i!=(n-1)) { // CHECK
					for (j = l; j < getSizeY(); j++) {
						for (s = 0, k = i; k < getSizeX(); k++)
							s += this.m[k][i] * this.m[k][j];
						f = s / h;
						for (k = i; k < getSizeX(); k++)
							this.m[k][j] += f * this.m[k][i];
					}
					// }
					for (k = i; k < getSizeX(); k++)
						this.m[k][i] *= scale;
				}
			}
			w.m[i][0] = scale * g;
			g = s = scale = 0.0;
			if (i < getSizeX() && i != getSizeY() - 1) { //
				for (k = l; k < getSizeY(); k++)
					scale += Math.abs(this.m[i][k]);
				if (scale != 0.) {
					for (k = l; k < getSizeY(); k++) { //
						this.m[i][k] /= scale;
						s += this.m[i][k] * this.m[i][k];
					}
					f = this.m[i][l];
					g = -MathUtil.SIGN(Math.sqrt(s), f);
					h = f * g - s;
					this.m[i][l] = f - g;
					for (k = l; k < getSizeY(); k++)
						rv1[k] = this.m[i][k] / h;
					if (i != getSizeX() - 1) { //
						for (j = l; j < getSizeX(); j++) { //
							for (s = 0, k = l; k < getSizeY(); k++)
								s += this.m[j][k] * this.m[i][k];
							for (k = l; k < getSizeY(); k++)
								this.m[j][k] += s * rv1[k];
						}
					}
					for (k = l; k < getSizeY(); k++)
						this.m[i][k] *= scale;
				}
			} // i<m && i!=n-1
			anorm = Math.max(anorm, (Math.abs(w.m[i][0]) + Math.abs(rv1[i])));
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
					scale += Math.abs(this.m[k][i]);
				if (scale != 0.0) {
					for (k = i; k < getSizeX(); k++) {
						this.m[k][i] /= scale;
						s += this.m[k][i] * this.m[k][i];
					}
					f = this.m[i][i];
					g = -MathUtil.SIGN(Math.sqrt(s), f);
					h = f * g - s;
					this.m[i][i] = f - g;
					// if (i!=(n-1)) { // CHECK
					for (j = l; j < getSizeY(); j++) {
						for (s = 0, k = i; k < getSizeX(); k++)
							s += this.m[k][i] * this.m[k][j];
						f = s / h;
						for (k = i; k < getSizeX(); k++)
							this.m[k][j] += f * this.m[k][i];
					}
					// }
					for (k = i; k < getSizeX(); k++)
						this.m[k][i] *= scale;
				}
			}
			w.m[i][0] = scale * g;
			g = s = scale = 0.0;
			if (i < getSizeX() && i != getSizeY() - 1) { //
				for (k = l; k < getSizeY(); k++)
					scale += Math.abs(this.m[i][k]);
				if (scale != 0.) {
					for (k = l; k < getSizeY(); k++) { //
						this.m[i][k] /= scale;
						s += this.m[i][k] * this.m[i][k];
					}
					f = this.m[i][l];
					g = -MathUtil.SIGN(Math.sqrt(s), f);
					h = f * g - s;
					this.m[i][l] = f - g;
					for (k = l; k < getSizeY(); k++)
						rv1[k] = this.m[i][k] / h;
					if (i != getSizeX() - 1) { //
						for (j = l; j < getSizeX(); j++) { //
							for (s = 0, k = l; k < getSizeY(); k++)
								s += this.m[j][k] * this.m[i][k];
							for (k = l; k < getSizeY(); k++)
								this.m[j][k] += s * rv1[k];
						}
					}
					for (k = l; k < getSizeY(); k++)
						this.m[i][k] *= scale;
				}
			} // i<m && i!=n-1
			anorm = Math.max(anorm, (Math.abs(w.m[i][0]) + Math.abs(rv1[i])));
		} // i
		
		
		for (i = getSizeY() - 1; i >= 0; --i) {
			if (i < getSizeY() - 1) { //
				if (g != 0.) {
					for (j = l; j < getSizeY(); j++)
						v.m[j][i] = (this.m[i][j] / this.m[i][l]) / g;
					for (j = l; j < getSizeY(); j++) {
						for (s = 0, k = l; k < getSizeY(); k++)
							s += this.m[i][k] * v.m[k][j];
						for (k = l; k < getSizeY(); k++)
							v.m[k][j] += s * v.m[k][i];
					}
				}
				for (j = l; j < getSizeY(); j++)
					//
					v.m[i][j] = v.m[j][i] = 0.0;
			}
			v.m[i][i] = 1.0;
			g = rv1[i];
			l = i;
		}
		// for (i=IMIN(m,n);i>=1;i--) { // !
		// for (i = n-1; i>=0; --i) {
		for (i = Math.min(getSizeX() - 1, getSizeY() - 1); i >= 0; --i) {
			l = i + 1;
			g = w.m[i][0];
			if (i < getSizeY() - 1) //
				for (j = l; j < getSizeY(); j++)
					//
					this.m[i][j] = 0.0;
			if (g != 0.) {
				g = 1. / g;
				if (i != getSizeY() - 1) {
					for (j = l; j < getSizeY(); j++) {
						for (s = 0, k = l; k < getSizeX(); k++)
							s += this.m[k][i] * this.m[k][j];
						f = (s / this.m[i][i]) * g;
						for (k = i; k < getSizeX(); k++)
							this.m[k][j] += f * this.m[k][i];
					}
				}
				for (j = i; j < getSizeX(); j++)
					this.m[j][i] *= g;
			} else {
				for (j = i; j < getSizeX(); j++)
					this.m[j][i] = 0.0;
			}
			this.m[i][i] += 1.0;
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
					if ((Math.abs(w.m[nm][0]) + anorm) == anorm)
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
						g = w.m[i][0];
						h = MathUtil.hypot(f, g);
						w.m[i][0] = h;
						h = 1.0 / h;
						c = g * h;
						s = -f * h;
						for (j = 0; j < getSizeX(); j++) {
							y = this.m[j][nm];
							z = this.m[j][i];
							this.m[j][nm] = y * c + z * s;
							this.m[j][i] = z * c - y * s;
						}
					}
				} // flag
				z = w.m[k][0];
				if (l == k) {
					if (z < 0.) {
						w.m[k][0] = -z;
						for (j = 0; j < getSizeY(); j++)
							v.m[j][k] = -v.m[j][k];
					}
					break;
				} // l==k
				if (its >= 50)
					throw new ArithmeticException("no svd convergence in 50 iterations");
				// zliberror._assert(its<50, "no svd convergence in 50
				// iterations");
				x = w.m[l][0];
				nm = k - 1;
				y = w.m[nm][0];
				g = rv1[nm];
				h = rv1[k];
				f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2 * h * y);
				g = MathUtil.hypot(f, 1.0);
				f = ((x - z) * (x + z) + h * ((y / (f + MathUtil.SIGN(g, f))) - h)) / x;
				c = s = 1.0;
				for (j = l; j <= nm; j++) {
					i = j + 1;
					g = rv1[i];
					y = w.m[i][0];
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
						x = v.m[jj][j];
						z = v.m[jj][i];
						v.m[jj][j] = x * c + z * s;
						v.m[jj][i] = z * c - x * s;
					}
					z = MathUtil.hypot(f, h);
					w.m[j][0] = z;
					if (z != 0.0) {
						z = 1.0 / z;
						c = f * z;
						s = h * z;
					}
					f = c * g + s * y;
					x = c * y - s * g;
					for (jj = 0; jj < getSizeX(); ++jj) {
						y = this.m[jj][j];
						z = this.m[jj][i];
						this.m[jj][j] = y * c + z * s;
						this.m[jj][i] = z * c - y * s;
					}
				} // j<nm
				rv1[l] = 0.0;
				rv1[k] = f;
				w.m[k][0] = x;
			} // its
		} // k
		// free rv1
	} // svd

}
