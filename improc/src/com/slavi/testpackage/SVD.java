package com.slavi.testpackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;

import com.slavi.matrix.Matrix;

public class SVD {
	public static void svd(Matrix a, Matrix w, Matrix v) {
		int i, its, j, jj, k, l = 0, nm = 0;
		boolean flag;
		int m = a.getSizeX();
		int n = a.getSizeY();
		double c, f, h, s, x, y, z;
		double anorm = 0., g = 0., scale = 0.;
		if (m < n)
			throw new Error("m < n");
		// zliberror._assert(m>=n) ;
		double[] rv1 = new double[n];

		System.out.println("SVD beware results may not be sorted!");

		for (i = 0; i < n; i++) {
			l = i + 1;
			rv1[i] = scale * g;
			g = s = scale = 0.;
			if (i < m) {
				for (k = i; k < m; k++)
					scale += Math.abs(a.getItem(k, i));
				if (scale != 0.0) {
					for (k = i; k < m; k++) {
						a.setItem(k, i, a.getItem(k, i) / scale);
						s += a.getItem(k, i) * a.getItem(k, i);
					}
					f = a.getItem(i, i);
					g = -SIGN(Math.sqrt(s), f);
					h = f * g - s;
					a.setItem(i, i, f - g);
					// if (i!=(n-1)) { // CHECK
					for (j = l; j < n; j++) {
						for (s = 0, k = i; k < m; k++)
							s += a.getItem(k, i) * a.getItem(k, j);
						f = s / h;
						for (k = i; k < m; k++)
							a.setItem(k, j, a.getItem(k, j) + f * a.getItem(k, i));
					}
					// }
					for (k = i; k < m; k++)
						a.setItem(k, i, a.getItem(k, i) * scale);
				}
			}
			w.setItem(i, 0, scale * g);
			g = s = scale = 0.0;
			if (i < m && i != n - 1) { //
				for (k = l; k < n; k++)
					scale += Math.abs(a.getItem(i, k));
				if (scale != 0.) {
					for (k = l; k < n; k++) { //
						a.setItem(i, k, a.getItem(i, k) / scale);
						s += a.getItem(i, k) * a.getItem(i, k);
					}
					f = a.getItem(i, l);
					g = -SIGN(Math.sqrt(s), f);
					h = f * g - s;
					a.setItem(i, l, f - g);
					for (k = l; k < n; k++)
						rv1[k] = a.getItem(i, k) / h;
					if (i != m - 1) { //
						for (j = l; j < m; j++) { //
							for (s = 0, k = l; k < n; k++)
								s += a.getItem(j, k) * a.getItem(i, k);
							for (k = l; k < n; k++)
								a.setItem(j, k, a.getItem(j, k) + s * rv1[k]);
						}
					}
					for (k = l; k < n; k++)
						a.setItem(i, k, a.getItem(i, k) * scale);
				}
			} // i<m && i!=n-1
			anorm = Math.max(anorm, (Math.abs(w.getItem(i, 0)) + Math.abs(rv1[i])));
		} // i
		for (i = n - 1; i >= 0; --i) {
			if (i < n - 1) { //
				if (g != 0.) {
					for (j = l; j < n; j++)
						v.setItem(j, i, (a.getItem(i, j) / a.getItem(i, l)) / g);
					for (j = l; j < n; j++) {
						for (s = 0, k = l; k < n; k++)
							s += a.getItem(i, k) * v.getItem(k, j);
						for (k = l; k < n; k++)
							v.setItem(k, j, v.getItem(k, j) + s * v.getItem(k, i));
					}
				}
				for (j = l; j < n; j++) {
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
		for (i = Math.min(m - 1, n - 1); i >= 0; --i) {
			l = i + 1;
			g = w.getItem(i, 0);
			if (i < n - 1) //
				for (j = l; j < n; j++)
					//
					a.setItem(i, j, 0.0);
			if (g != 0.) {
				g = 1. / g;
				if (i != n - 1) {
					for (j = l; j < n; j++) {
						for (s = 0, k = l; k < m; k++)
							s += a.getItem(k, i) * a.getItem(k, j);
						f = (s / a.getItem(i, i)) * g;
						for (k = i; k < m; k++)
							a.setItem(k, j, a.getItem(k, j) + f * a.getItem(k, i));
					}
				}
				for (j = i; j < m; j++)
					a.setItem(j, i, a.getItem(j, i) * g);
			} else {
				for (j = i; j < m; j++)
					a.setItem(j, i, 0.0);
			}
			a.setItem(i, i, a.getItem(i, i) + 1.0);
		}
		for (k = n - 1; k >= 0; --k) {
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
						h = pythag(f, g);
						w.setItem(i, 0, h);
						h = 1.0 / h;
						c = g * h;
						s = -f * h;
						for (j = 0; j < m; j++) {
							y = a.getItem(j, nm);
							z = a.getItem(j, i);
							a.setItem(j, nm, y * c + z * s);
							a.setItem(j, i, z * c - y * s);
						}
					}
				} // flag
				z = w.getItem(k, 0);
				if (l == k) {
					if (z < 0.) {
						w.setItem(k, 0, -z);
						for (j = 0; j < n; j++)
							v.setItem(j, k, -v.getItem(j, k));
					}
					break;
				} // l==k
				if (its >= 50)
					throw new Error("no svd convergence in 50 iterations");
				// zliberror._assert(its<50, "no svd convergence in 50
				// iterations");
				x = w.getItem(l, 0);
				nm = k - 1;
				y = w.getItem(nm, 0);
				g = rv1[nm];
				h = rv1[k];
				f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2 * h * y);
				g = pythag(f, 1.0);
				f = ((x - z) * (x + z) + h * ((y / (f + SIGN(g, f))) - h)) / x;
				c = s = 1.0;
				for (j = l; j <= nm; j++) {
					i = j + 1;
					g = rv1[i];
					y = w.getItem(i, 0);
					h = s * g;
					g = c * g;
					z = pythag(f, h);
					rv1[j] = z;
					c = f / z;
					s = h / z;
					f = x * c + g * s;
					g = g * c - x * s;
					h = y * s;
					y *= c;
					for (jj = 0; jj < n; jj++) {
						x = v.getItem(jj, j);
						z = v.getItem(jj, i);
						v.setItem(jj, j, x * c + z * s);
						v.setItem(jj, i, z * c - x * s);
					}
					z = pythag(f, h);
					w.setItem(j, 0, z);
					if (z != 0.0) {
						z = 1.0 / z;
						c = f * z;
						s = h * z;
					}
					f = c * g + s * y;
					x = c * y - s * g;
					for (jj = 0; jj < m; ++jj) {
						y = a.getItem(jj, j);
						z = a.getItem(jj, i);
						a.setItem(jj, j, y * c + z * s);
						a.setItem(jj, i, z * c - y * s);
					}
				} // j<nm
				rv1[l] = 0.0;
				rv1[k] = f;
				w.setItem(k, 0, x);
			} // its
		} // k
		// free rv1
	} // svd

	static final double pythag(double a, double b) {
		return Math.sqrt(a * a + b * b);
	}

	static final double SIGN(double a, double b) {
		return (b >= 0. ? Math.abs(a) : -Math.abs(a));
	}
	
	//////////////////////////////////////////////////
	
	public static Matrix jamaMat2slaviMat(jama.Matrix ja) {
		Matrix r = new Matrix(ja.getColumnDimension(), ja.getRowDimension());
		for (int i = r.getSizeX() - 1; i >= 0; i--)
			for (int j = r.getSizeY() - 1; j >= 0; j--)
				r.setItem(i, j, ja.get(j, i));
		return r;
	}
	
	public static jama.Matrix slaviMat2jamaMat(Matrix sa) {
		jama.Matrix ja = new jama.Matrix(sa.getSizeY(), sa.getSizeX());
		for (int i = sa.getSizeX() - 1; i >= 0; i--)
			for (int j = sa.getSizeY() - 1; j >= 0; j--)
				ja.set(j, i, sa.getItem(i, j));
		return ja;
	}
	
	public static void printM(Matrix m, String title) {
		System.out.println(title);
		System.out.println(m.toString());
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(
				SVD.class.getResource(
					"SVD-A.txt").getFile()));

		Matrix a = new Matrix(3, 4);
		a.load(fin);
		fin.close();
		Matrix aa = a.makeCopy();
		printM(a, "A");
		a.transpose(aa);
		aa.copyTo(a);
		
//		jama.Matrix ja = slaviMat2jamaMat(a);
//		jama.SingularValueDecomposition svd = ja.svd();
		
		Matrix u = new Matrix(10, 10);
		Matrix v = new Matrix(10, 10);
		Matrix s = new Matrix(10, 10);

		Matrix tmp1 = new Matrix();
		Matrix tmp2 = new Matrix();
		Matrix tmp3 = new Matrix();
//		Matrix tmp4 = new Matrix();

		a.svd2(u, v, s);
		//a.svd(s,v);
		tmp1.resize(aa.getSizeX(), aa.getSizeY());
		for (int i = Math.min(aa.getSizeX(), aa.getSizeY()) - 1; i >= 0; i--)
			tmp1.setItem(i, i, s.getItem(i, 0));
		tmp1.copyTo(s);
		a.copyTo(u);

		a.save(new PrintStream("c:/temp/a.txt"));
		u.save(new PrintStream("c:/temp/u.txt"));
		s.save(new PrintStream("c:/temp/s.txt"));
		v.save(new PrintStream("c:/temp/v.txt"));
//		Matrix ia = aa.makeCopy();
//		ia.inverse();
//		ia.save(new PrintStream("c:/temp/ia.txt"));
		
		
		printM(u, "U");
		printM(v, "V");
		printM(s, "S");
		
//		Matrix u2 = jamaMat2slaviMat(svd.getU());
//		Matrix v2 = jamaMat2slaviMat(svd.getV());
//		Matrix s2 = jamaMat2slaviMat(svd.getS());
//				
//		printM(u2, "U2");
//		printM(v2, "V2");
//		printM(s2, "S2");
//
//		u.mSub(u2, tmp1);
//		printM(tmp1, "U - U2");
//		v.mSub(v2, tmp1);
//		printM(tmp1, "V - V2");
//		s.mSub(s2, tmp1);
//		printM(tmp1, "S - S2");

		u.transpose(tmp1);
		tmp1.mMul(u, tmp2);
		printM(tmp2, "U.' * U");
		
		v.transpose(tmp1);
		v.mMul(tmp1, tmp2);
		printM(tmp2, "V.' * V");

		printM(aa, "AA");

		u.mMul(s, tmp2);
		tmp2.mMul(tmp1, tmp3);
		printM(tmp3, "U * S * V.'");
		
		aa.mSub(tmp3, tmp1);
		printM(tmp1, "AA - U * S * V.'");
	}

	public static void main2(String[] args) throws Exception {
		int nr = 6;
		int nc = 5;
		Matrix a = new Matrix(nr, nc);
		for (int r = 0; r < nr; r++) {
			float p = (float) r / (nr - 1);
			for (int c = 0; c < nc; c++) {
				float frac = (float) c / (nc - 1);
				a.setItem(r, c, Math.pow(frac, p));
			}
		}
		printM(a, "A");

		Matrix w = new Matrix(nc, 1);
		Matrix v = new Matrix(nc,nc);
		Matrix copyA = a.makeCopy();
		
		svd(a, w, v);
		printM(a, "U");
		printM(w, "W");
		printM(v, "V");
		
		Matrix s = new Matrix(w.getSizeX(), w.getSizeX());
		s.make0();
		for (int i = w.getSizeX() - 1; i >= 0; i--)
			s.setItem(i, i, w.getItem(i, 0));
		printM(s, "S");
		Matrix vt = new Matrix();
		v.transpose(vt);
		Matrix t = new Matrix();
		s.mMul(vt, t);
		//printM(t, "S * V.'");
		
		Matrix u = new Matrix(w.getSizeX(), w.getSizeX());
		for (int i = u.getSizeX() - 1; i >= 0; i--)
			for (int j = u.getSizeY() - 1; j >= 0; j--)
				u.setItem(i, j, a.getItem(i, j));
			
		u.mMul(t, s);

		for (int i = s.getSizeX() - 1; i >= 0; i--)
			for (int j = s.getSizeY() - 1; j >= 0; j--)
				s.setItem(i, j, s.getItem(i, j) - copyA.getItem(i, j));

		printM(s, "U * S * V.'");
		
	}
}
