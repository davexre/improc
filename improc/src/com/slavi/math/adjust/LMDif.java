package com.slavi.math.adjust;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

import com.slavi.math.matrix.Matrix;

public class LMDif {
	
	public static boolean showDetails = false;
	
	public interface LMDifFcn {
		public void fcn(Matrix x, Matrix fvec);
	}
	
	/* resolution of arithmetic */
	static final double MACHEP = 1.2e-16;  	
	static final double DBL_EPSILON = 2220446049.250313 / 1e25; // 1.0e-14;
	static final double gtol = DBL_EPSILON;
	static final double ftol = DBL_EPSILON;
	static final double xtol = DBL_EPSILON;
	static final double epsfcn = DBL_EPSILON * 10.0;//1.0e-15;
	
	/**
	 * subroutine fdjac2
	 * 
	 * this subroutine computes a forward-difference approximation
	 * to the m by n jacobian matrix associated with a specified
	 * problem of m functions in n variables.
	 */
	static Matrix fdjac2(LMDifFcn fcn, Matrix x, Matrix fvec) {
		int m = fvec.getSizeX();	// the number of functions.
		int n = x.getSizeX();		// the number of variables. n must not exceed m.
		double eps = Math.sqrt(epsfcn);
		
//		System.out.printf("x=%12.8f fvec=%12.8f\n", x.getForbeniusNorm(), fvec.getForbeniusNorm());
//		System.out.printf("eps=%20.18f\n", eps);
		
		Matrix fjac = new Matrix(m, n);
		Matrix wa = new Matrix(m, 1);
		Matrix tmp = new Matrix(m, 1);
		for (int j = 0; j < n; j++) {
			double temp = x.getItem(j, 0);
			double h = eps * Math.abs(temp); // TODO: Проблеми с точността!!!???
			if (h == 0.0)
				h = eps;
			h = 0.1;		// TODO: REMOVE THIS LINE!!!
//			System.out.printf("**** TEMP = %f\n", temp);
//			if (j == 25)
//				System.out.printf("**** H    = %12.8f\n", h);
//			System.out.printf("**** EPS  = %f\n", eps * 1e15);
//			System.out.printf("**** EPSFCN= %f\n", epsfcn * 1e15);
			x.setItem(j, 0, temp + h);
			fcn.fcn(x, wa);
//			System.out.printf("j=%d xnorm=%12.8f wanorm=%12.8f\n", j, x.getForbeniusNorm(), wa.getForbeniusNorm());

			x.setItem(j, 0, temp);
			for (int i = 0; i < m; i++) {
				temp = (wa.getItem(i, 0) - fvec.getItem(i, 0)) / h;
				fjac.setItem(i, j, temp);
				tmp.setItem(i, 0, temp);
			}
//			System.out.printf("fjac()=%12.8f\n", tmp.getForbeniusNorm());
		}
//		System.out.printf("fjac=%12.8f\n", fjac.getForbeniusNorm());
		return fjac;
	}
	
	static int[] qrfac(Matrix a, Matrix rdiag, Matrix acnorm) {
		int m = a.getSizeX();
		int n = a.getSizeY();
		
		rdiag.resize(n, 1);
		int ipvt[] = new int[n];
		for (int j = 0; j < n; j++) {
			// Compute enorm for each row
			double scale = 0.0;
			double sum = 1.0;
			for (int i = 0; i < m; i++) {
				double d = Math.abs(a.getItem(i, j));
				if (d != 0.0) {
					if (scale < d) {
						double d1 = scale / d;
						sum = 1.0 + sum * d1 * d1;
						scale = d;
					} else {
						double d1 = d / scale;
						sum += d1 * d1;
					}
				}
			}
			rdiag.setItem(j, 0, scale * Math.sqrt(sum));
//			System.out.printf("r[%d]=%12.8f\n", j, rdiag.getItem(j, 0));
			ipvt[j] = j;
		}
		
//		for (int i = 0; i < n; i++)
//			System.out.printf("acnorm=%12.8f\n", rdiag.getItem(i, 0));
		rdiag.copyTo(acnorm);
		Matrix wa = rdiag.makeCopy();
		// reduce a to r with householder transformations.
		int minmn = Math.min(m, n);
		for (int j = 0; j < minmn; j++) {
			// bring the column of largest norm into the pivot position.
			int kmax = j;
			for (int k = j; k < n; k++)
				if (rdiag.getItem(k, 0) > rdiag.getItem(kmax, 0))
					kmax = k;
			if (kmax != j) {
//				System.out.printf("j=%d kmax=%d r[j]=%f r[kmax]=%f\n", j, kmax, rdiag.getItem(j, 0), rdiag.getItem(kmax, 0));
				for (int i = 0; i < m; i++) {
					double tmp = a.getItem(i, j);
					a.setItem(i, j, a.getItem(i, kmax));
					a.setItem(i, kmax, tmp);
				}
				rdiag.setItem(kmax, 0, rdiag.getItem(j, 0));
				wa.setItem(kmax, 0, wa.getItem(j, 0));
				int k = ipvt[j];
				ipvt[j] = ipvt[kmax];
				ipvt[kmax] = k;
			}
			
			// compute the householder transformation to reduce the
			// j-th column of a to a multiple of the j-th unit vector.
			double scale = 0.0;
			double sum = 1.0;
			for (int i = j; i < m; i++) {
				double d = Math.abs(a.getItem(i, j));
				if (d != 0.0) {
					if (scale < d) {
						double d1 = scale / d;
						sum = 1.0 + sum * d1 * d1;
						scale = d;
					} else {
						double d1 = d / scale;
						sum += d1 * d1;
					}
				}
			}
			double ajnorm = scale * Math.sqrt(sum);
//			System.out.printf("ajnorm=%12.8f\n", ajnorm);
			if (ajnorm != 0.0) {
				if (a.getItem(j, j) < 0.0) {
//					System.out.printf("inverted at j=%d a(j,j)=%12.8f\n", j, a.getItem(j, j) * 1000000000);
					ajnorm = -ajnorm;
				}
				for (int i = j; i < m; i++) {
					a.setItem(i, j, a.getItem(i, j) / ajnorm);
				}
				a.setItem(j, j, a.getItem(j, j) + 1.0);
				// apply the transformation to the remaining columns
				// and update the norms.
				int jp1 = j + 1;
				for (int k = jp1; k < n; k++) {
					sum = 0.0;
					for (int i = j; i < m; i++) {
						sum += a.getItem(i, j) * a.getItem(i, k);
					}
					double temp = sum / a.getItem(j, j);
					for (int i = j; i < m; i++) {
						a.setItem(i, k, a.getItem(i, k) - temp * a.getItem(i, j));
					}
					
					if (rdiag.getItem(k, 0) != 0.0) {
						temp = a.getItem(j, k) / rdiag.getItem(k, 0);
						temp = Math.max(0.0, 1.0 - temp * temp);
						rdiag.setItem(k, 0, rdiag.getItem(k, 0) * Math.sqrt(temp));
						temp = rdiag.getItem(k, 0) / wa.getItem(k, 0);
						if (temp * temp * 0.05 <= MACHEP) {
							scale = 0.0;
							sum = 1.0;
							for (int i = jp1; i < m; i++) { 
								double d = Math.abs(a.getItem(i, k));
								if (d != 0.0) {
									if (scale < d) {
										double d1 = scale / d;
										sum = 1.0 + sum * d1 * d1;
										scale = d;
									} else {
										double d1 = d / scale;
										sum += d1 * d1;
									}
								}
							}
							temp = scale * Math.sqrt(sum);
							rdiag.setItem(k, 0, temp);
							wa.setItem(k, 0, temp);
						}
					}
				}
			}
			rdiag.setItem(j, 0, -ajnorm);
//			System.exit(0);
		}
		return ipvt;
	}
	
	static double factor = 100.0;
	/**
	 * subroutine lmdif
	 * 
	 * the purpose of lmdif is to minimize the sum of the squares of
	 * m nonlinear functions in n variables by a modification of
	 * the levenberg-marquardt algorithm. the user must provide a
	 * subroutine which calculates the functions. the jacobian is
	 * then calculated by a forward-difference approximation.
	 * 
	 * @param x		is an array of length n. on input x must contain
	 * 				an initial estimate of the solution vector. on output x
	 * 				contains the final estimate of the solution vector.
	 * @param fvec	is an output array of length m which contains
	 * 				the functions evaluated at the output x.
	 * @throws Exception 
	 */
	public static void lmdif(LMDifFcn fcn, Matrix x, Matrix fvec) throws Exception {
		int m = fvec.getSizeX();	// the number of functions.
		int n = x.getSizeX();		// the number of variables. n must not exceed m.

		// evaluate the function at the starting point and calculate its norm.
		fcn.fcn(x, fvec);
		double fnorm = fvec.getForbeniusNorm();
//		System.out.printf("fnorm=%12.8f\n", fnorm);
//		System.exit(0);
		
		double par = 0.0;
		double xnorm = 0.0;
		double delta = 0.0;
		int iter = 1;
		while (true) {
			// calculate the jacobian matrix.
			Matrix fdjac = fdjac2(fcn, x, fvec);
//			Matrix ft = new Matrix();
//			fdjac.transpose(ft);
//			System.out.println(ft.toMatlabString("mf"));

//			System.out.println("CHEAT!");
//			Matrix fdjacCheat = new Matrix(30, 411);
//			BufferedReader fin = new BufferedReader(new InputStreamReader(LMDif.class.getResourceAsStream("FJAC.txt")));
//			fin.readLine();
//			fdjacCheat.load(fin);
//			fin.close();
//			Matrix fdjac = new Matrix();
//			fdjacCheat.transpose(fdjac);
			
			
//			System.out.println(fdjac.toMatlabString("FDJAC"));
			//fdjac.printM("FDJAC");
//			System.out.println("---------");
//			for (int i = 0; i < fdjac.getSizeX(); i++) {
//				for (int j = 0; j < fdjac.getSizeY(); j++) {
//					System.out.printf(Locale.US, "%12.8f\t", fdjac.getItem(i, j));
//				}
//				System.out.println();
//			}
//			System.exit(0);
			// compute the qr factorization of the jacobian.

			// compute the initial column norms
			Matrix diag = new Matrix(n, 1);
			Matrix wa2 = new Matrix(n, 1);
			for (int j = 0; j < n; j++) {
				double scale = 0.0;
				double sum = 1.0;
				for (int i = 0; i < m; i++) {
					double d = Math.abs(fdjac.getItem(i, j));
					if (d != 0.0) {
						if (scale < d) {
							double d1 = scale / d;
							sum = 1.0 + sum * d1 * d1;
							scale = d;
						} else {
							double d1 = d / scale;
							sum += d1 * d1;
						}
					}
					d = scale * Math.sqrt(sum);
					wa2.setItem(j, 0, d);
					if (d == 0.0)
						d = 1.0;
					diag.setItem(j, 0, d);
				}
			}
			
//			Matrix tau = new Matrix();
//			q.qrDecomposition(tau);
//			fdjac.qr(q, tau);
//			System.out.println(q.toMatlabString("Q"));
//			System.out.println(fdjac.toMatlabString("r"));

//			Matrix wa1 = new Matrix(n, 1);
//			for (int i = 0; i < n; i++)
//				wa1.setItem(i, 0, fdjac.getItem(i, i));
			
			Matrix wa1 = new Matrix();
//			fdjac.transpose(wa1);
//			System.out.println(wa1.toMatlabString("mf"));
//			System.exit(0);

			int[] ipvt = qrfac(fdjac, wa1, wa2);
//			System.out.println(q.toMatlabString("MQ"));
//			System.out.println(wa1.toMatlabString("MWA1"));
//			System.out.println(wa2.toMatlabString("MWA2"));
//			System.out.println("IPVT=");
//			for (int i = 0; i < ipvt.length; i++)
//				System.out.println(ipvt[i]);
//			System.exit(0);

//			fin = new BufferedReader(new InputStreamReader(LMDif.class.getResourceAsStream("WA.txt")));
//			fin.readLine();
//			wa1.load(fin);
//			fin.readLine();
//			wa2.load(fin);
//			fin.readLine();
//			fdjacCheat.load(fin);
//			fdjacCheat.transpose(fdjac);
//			fin.close();

			// on the first iteration and if mode is 1, scale according
			// to the norms of the columns of the initial jacobian.
			if (iter == 1) {
				// on the first iteration, calculate the norm of the scaled x
				// and initialize the step bound delta.
				Matrix wa3 = new Matrix(n, 1);
				for (int j = 0; j < n; j++) {
					wa3.setItem(j, 0, diag.getItem(j, 0) * x.getItem(j, 0));
				}
				xnorm = wa3.getForbeniusNorm();
				delta = (xnorm == 0.0 ? factor : factor * xnorm);
//				System.out.printf("DELTA=%12.8f\n", delta);
			}
				
			// form (q transpose)*fvec and store the first n components in qtf.
			Matrix wa4 = fvec.makeCopy();
//			System.out.printf("wa4 norm=%12.8f\n", wa4.getForbeniusNorm());
//			System.out.println(fdjac.toMatlabString("mf"));
			Matrix qtf = new Matrix(n, 1);
			for (int j = 0; j < n; j++) {
				double temp3 = fdjac.getItem(j, j);
				if (temp3 != 0.0) {
					double sum = 0.0;
					for (int i = j; i < m; i++) {
						sum += fdjac.getItem(i, j) * wa4.getItem(i, 0);
					}
					double temp = -sum / temp3;
					for (int i = j; i < m; i++) {
						wa4.setItem(i, 0, wa4.getItem(i, 0) + fdjac.getItem(i, j) * temp);
					}
				}
				fdjac.setItem(j, j, wa1.getItem(j, 0));
				qtf.setItem(j, 0, wa4.getItem(j, 0));
			}

//			System.out.println(fdjac.toMatlabString("mf"));
//			System.out.println(qtf.toMatlabString("mqtf"));
//			System.exit(0);
			
			
			// compute the norm of the scaled gradient.
			double gnorm = 0.0;
			if (fnorm != 0.0) {
				for (int j = 0; j < n; j++) {
					double d = wa2.getItem(ipvt[j], 0);
					if (d != 0.0) {
						double sum = 0.0;
						for (int i = 0; i <= j; i++) {
							sum += fdjac.getItem(i, j) * (qtf.getItem(i, 0) / fnorm);
						}
						gnorm = Math.max(gnorm, Math.abs(sum / d)); 
					}
				}
			}

//			System.out.printf("gnorm=%12.8f\n", gnorm);
//			System.out.printf("fnorm=%12.8f\n", fnorm);
//			System.exit(0);
			
			// test for convergence of the gradient norm.
			if (gnorm <= gtol)
				return; 
			
			// rescale if necessary.
			for (int j = 0; j < n; j++) {
				diag.setItem(j, 0, Math.max(diag.getItem(j, 0), wa2.getItem(j, 0)));
			}
			
//			System.out.println(diag.toMatlabString("mdiag"));
//			System.out.printf("delta=%12.8f\n", delta);
//			System.exit(0);
			
			while (true) {
				// determine the levenberg-marquardt parameter.
//				System.out.println(wa1.toMatlabString("mwa1"));
				par = lmpar(par, fdjac, diag, wa1, qtf, delta, wa2, ipvt);
				System.out.printf("LMPAR=%12.8f  iter=%d\n", par, iter);
//				System.out.println(wa1.toMatlabString("mwa1"));
//				System.exit(0);
				
				// store the direction p and x + p. calculate the norm of p.
				Matrix wa3 = new Matrix(n, 1);
		//		System.out.printf("wa1 norm=%12.8f  xnorm=%12.8f\n", wa1.getForbeniusNorm(), x.getForbeniusNorm());
				for (int j = 0; j < n; j++) {
					wa1.setItem(j, 0, -wa1.getItem(j, 0));
					wa2.setItem(j, 0, x.getItem(j, 0) + wa1.getItem(j, 0));
					wa3.setItem(j, 0, diag.getItem(j, 0) * wa1.getItem(j, 0));
				}
//				System.out.println(wa1.toMatlabString("mwa1"));

				double pnorm = wa3.getForbeniusNorm();
				// on the first iteration, adjust the initial step bound.
				if (iter == 1) 
					delta = Math.min(delta, pnorm);
//				System.out.printf("PNORM=%12.8f\n", pnorm);
//				System.out.printf("delta=%12.8f\n", delta);

				// evaluate the function at x + p and calculate its norm.
				fcn.fcn(wa2, wa4);
//				System.out.println(wa4.toMatlabString("mwa4"));
//				System.exit(0);
				
				double fnorm1 = wa4.getForbeniusNorm();
//				System.out.printf("pnorm %12.8f  fnorm1 %12.8f\n", pnorm, fnorm1);
//				System.exit(0);
				// compute the qr factorization of the jacobian.
				double actred = -1.0;
				if (p1 * fnorm1 < fnorm) {
					double temp = fnorm1 / fnorm;
					actred = 1.0 - temp * temp;
				}
//				System.out.printf("fnorm1=%12.8f\n", fnorm1);
//				System.out.printf("actred=%12.8f\n", actred);
				// compute the scaled predicted reduction and
				// the scaled directional derivative.
				for (int j = 0; j < n; j++) {
					wa3.setItem(j, 0, 0.0);
					double temp = wa1.getItem(ipvt[j], 0);
					for (int i = 0; i <= j; i++) {
						wa3.setItem(i, 0, wa3.getItem(i, 0) + fdjac.getItem(i, j) * temp);
					}
				}
//				System.out.println(wa3.toMatlabString("mwa3"));
				
				double temp1 = wa3.getForbeniusNorm() / fnorm;
				double temp2 = (Math.sqrt(par) * pnorm) / fnorm;
				double prered = temp1 * temp1 + (temp2 * temp2) / 0.5;
				double dirder = -(temp1 * temp1 + temp2 * temp2);
				// compute the ratio of the actual to the predicted reduction.
				double ratio = 0.0;
				if (prered != 0.0)
					ratio = actred / prered;
				
//				System.out.printf("temp1=%12.8f\n", temp1);
//				System.out.printf("temp2=%12.8f\n", temp2);
//				System.out.printf("prered=%12.8f\n", prered);
//				System.out.printf("dirder=%12.8f\n", dirder);
//				System.out.printf("ratio =%12.8f\n", ratio);
//				System.exit(0);

				// update the step bound.
				if (ratio <= 0.25) {
					double temp;
					if (actred >= 0.0) {
						temp = 0.5;
					} else {
						temp = 0.5 * dirder / (dirder + 0.5 * actred);
					}
					if ( ((p1*fnorm1) >= fnorm) || (temp < p1) ) {
						temp = p1;
					} 
					delta = temp * Math.min(delta, pnorm / p1);
					par = par / temp;
				} else {
					if ((par == 0.0) || (ratio >= 0.75)) {
						delta = pnorm / 0.5;
						par = 0.5 * par;
					}
				}
				
				// test for successful iteration.
				if (ratio >= 0.0001) {
					// successful iteration. update x, fvec, and their norms.
					for (int j = 0; j < n; j++) {
						x.setItem(j, 0, wa2.getItem(j, 0));
						wa2.setItem(j, 0, diag.getItem(j, 0) * x.getItem(j, 0));
					}
					for (int i = 0; i < m; i++) {
						fvec.setItem(i, 0, wa4.getItem(i, 0));
					}
					xnorm = wa2.getForbeniusNorm();
					fnorm = fnorm1;
					iter++;
				}

				if (iter > 7)
					System.exit(0);
				
				// tests for convergence.
				if ( (Math.abs(actred) <= ftol) && (prered <= ftol) && (0.5 * ratio <= 1.0)) {
					// both actual and predicted relative reduction in the sum of squares are at most ftol.
					return; 
				}
				if (delta <= xtol * xnorm) {
					if ( (Math.abs(actred) <= ftol) && (prered <= ftol) && (0.5 * ratio <= 1.0)) {
						// conditions for info = 1 and info = 2 both hold.
						return;
					}							
					// relative error between two consecutive iterates is at most xtol.
					return;
				}
				// tests for termination and stringent tolerances.
				if ( (Math.abs(actred) <= MACHEP) && (prered <= MACHEP) && (0.5 * ratio <= 1.0)) {
					// ftol is too small. no further reduction in the sum of squares is possible.
					return;
				}
				if (delta <= MACHEP * xnorm) {
					// xtol is too small. no further improvement in the approximate solution x is possible.
					return;
				}
				if (gnorm <= MACHEP) {
					// gtol is too small. fvec is orthogonal to the columns of the jacobian to machine precision.
					return;
				}
				
				if (ratio >= 0.0001)
					break;
			}
		}
		
	}	
	
	static final double p1 = 0.1;
	static final double DWARF = 1.0e-38;
	/**
	 * subroutine lmpar
	 * 
	 * given an m by n matrix a, an n by n nonsingular diagonal
	 * matrix d, an m-vector b, and a positive number delta,
	 * the problem is to determine a value for the parameter
	 * par such that if x solves the system
	 * 
	 * ...
	 */
	static double lmpar(double par, Matrix r, Matrix diag, Matrix x, Matrix qtb, double delta, Matrix sdiag, int ipvt[]) {
		int m = r.getSizeX();
		int n = r.getSizeY();
		
		if ((m < n) ||
			(qtb.getSizeX() != n) || (qtb.getSizeY() != 1) ||
			(diag.getSizeX() != n) || (diag.getSizeY() != 1)) {
			throw new IllegalArgumentException();
		}
		
//		System.out.printf("mr=[\n");
//		for (int j = 0; j < n; j++) {
//			for (int i = 0; i < n; i++) {
//				System.out.printf(Locale.US, "%12.8f\t", r.getItem(i, j));
//			}
//			System.out.printf(";\n");
//		}
//		System.out.printf("];\n");
		
//		System.out.println(qtb.toMatlabString("md"));
		
		// compute and store in x the gauss-newton direction. if the
		// jacobian is rank-deficient, obtain a least squares solution.

		int nsing = n;
		Matrix wa1 = new Matrix(n, 1);
		for (int j = 0; j < n; j++) {
			wa1.setItem(j, 0, qtb.getItem(j, 0));
			if ((r.getItem(j, j) == 0.0) && (nsing == n))
				nsing = j;
			if (nsing < n)
				wa1.setItem(j, 0, 0.0);
		}
//		System.out.println(nsing);

		for (int k = 0; k < nsing; k++) {
			int j = nsing - k - 1;
			wa1.setItem(j, 0, wa1.getItem(j, 0) / r.getItem(j, j));
			double temp = wa1.getItem(j, 0);
			int jm1 = j - 1;
			for (int i = 0; i <= jm1; i++) {
				wa1.setItem(i, 0, wa1.getItem(i, 0) - temp * r.getItem(i, j));
			}
		}

		for (int j = 0; j < n; j++) {
			x.setItem(ipvt[j], 0, wa1.getItem(j, 0));
		}
		
		// initialize the iteration counter.
		// evaluate the function at the origin, and test
		// for acceptance of the gauss-newton direction.
		Matrix wa2 = new Matrix(n, 1);
		for (int j = 0; j < n; j++) {
			wa2.setItem(j, 0, diag.getItem(j, 0) * x.getItem(j, 0));
		}
//		System.out.println(wa2.toMatlabString("wa2"));
		
		double dxnorm = wa2.getForbeniusNorm();
		double fp = dxnorm - delta;
//		System.out.printf("dxnorm=%12.8f\n", dxnorm);
//		System.out.printf("delta =%12.8f\n", delta);
//		System.out.printf("fp    =%12.8f\n", fp);
		if (fp <= delta * p1) {
			return par;
		}
		
		// if the jacobian is not rank deficient, the newton
		// step provides a lower bound, parl, for the zero of
		// the function. otherwise set this bound to zero.
		double parl = 0.0;
		if (nsing >= n) {
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, diag.getItem(ipvt[j], 0) * (wa2.getItem(ipvt[j], 0) / dxnorm));
			}
//			System.out.printf("dxnorm=%12.8f\n", dxnorm);
//			System.out.println(wa1.toMatlabString("mwa1"));

			for (int j = 0; j < n; j++) {
				double sum = 0.0;
				int jm1 = j - 1;
				if (jm1 >= 0) {
					for (int i = 0; i <= jm1; i++) {
						sum += r.getItem(i, j) * wa1.getItem(i, 0);
					}
				}
				wa1.setItem(j, 0, (wa1.getItem(j, 0) - sum) / r.getItem(j, j));
			}
			double temp = wa1.getForbeniusNorm();
			parl = ((fp / delta) / temp) / temp;
//			System.out.printf("TEMP=%12.8f\n", temp);
//			System.out.printf("DELTA=%12.8f\n", delta);
//			System.out.printf("FP  =%12.8f\n", fp);
//			System.out.printf("PARL=%12.8f\n", parl);
		}
//		System.out.println(paru);
		
		// calculate an upper bound, paru, for the zero of the function.
		for (int j = 0; j < n; j++) {
			double sum = 0.0;
			for (int i = 0; i <= j; i++) {
				sum += r.getItem(i, j) * qtb.getItem(i, 0);
			}
			wa1.setItem(j, 0, sum / diag.getItem(ipvt[j], 0));
		}
		double gnorm = wa1.getForbeniusNorm();
		double paru = gnorm / delta;
		if (paru == 0.0) {
			paru = DWARF / Math.min(delta, p1);
		}
//		System.out.printf("gnorm=%12.8f\n", gnorm);
//		System.out.printf("delta=%12.8f\n", delta);
//		System.out.printf("paru =%12.8f\n", paru);
//		System.out.printf("parl =%12.8f\n", parl);
		
		// if the input par lies outside of the interval (parl,paru),
		// set par to the closer endpoint.
		par = Math.max(par, parl);
		par = Math.min(par, paru);
		if (par == 0.0)
			par = gnorm / dxnorm;
//		System.out.printf("par  =%12.8f\n", par);
		
		int iter = 0;
		while (true) {
			iter++;
//			System.out.printf("iter  =%d\n", iter);
			// evaluate the function at the current value of par.
			if (par == 0.0)
				par = Math.max(DWARF, paru * 0.001);
			double temp = Math.sqrt(par);
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, temp * diag.getItem(j, 0));
			}
//			System.out.printf("TEMP=%12.8f\n", temp);
//			System.out.println(diag.toMatlabString("md"));
//			System.out.printf("mr=[\n");
//			for (int j = 0; j < n; j++) {
//				for (int i = 0; i < n; i++) {
//					System.out.printf(Locale.US, "%12.8f\t", r.getItem(i, j));
//				}
//				System.out.printf(";\n");
//			}
//			System.out.printf("];\n");

//			System.exit(0);
//			System.out.printf("R_NORM=%12.8f\n", r.getForbeniusNorm());
//			System.out.println(qtb.toMatlabString("qtb"));

//			System.out.printf("X_NORM=%12.8f\n", x.getForbeniusNorm());
//			System.out.printf("WA1_NORM=%12.8f\n", wa1.getForbeniusNorm());
//			System.out.printf("QTB_NORM=%12.8f\n", qtb.getForbeniusNorm());
//			System.out.printf("sdiag_NORM=%12.8f\n\n", sdiag.getForbeniusNorm());
			
			qrsolv(r, wa1, x, qtb, sdiag, ipvt);
			System.out.printf("qrsolv xnorm=%12.8f\n", x.getForbeniusNorm());
			for (int j = 0; j < n; j++) {
				wa2.setItem(j, 0, diag.getItem(j, 0) * x.getItem(j, 0));
			}
			dxnorm = wa2.getForbeniusNorm();
			temp = fp;
			fp = dxnorm - delta;
			
//			System.out.printf("dxnorm=%12.8f\n", dxnorm);
//			System.out.printf("temp  =%12.8f\n", temp);
//			System.out.printf("fp    =%12.8f\n", fp);
//			System.out.printf("X_NORM=%12.8f\n", x.getForbeniusNorm());
//			System.out.printf("WA1_NORM=%12.8f\n", wa1.getForbeniusNorm());
//			System.out.printf("QTB_NORM=%12.8f\n", qtb.getForbeniusNorm());
//			System.out.printf("sdiag_NORM=%12.8f\n", sdiag.getForbeniusNorm());
//			System.out.println();
			
			
			// if the function is small enough, accept the current value
			// of par. also test for the exceptional cases where parl
			// is zero or the number of iterations has reached 10.
			if ((Math.abs(fp) <= p1 * delta) || 
				((parl == 0.0) && (fp <= temp) && (temp < 0.0)) || 
				(iter == 10))
				break;
			// compute the newton correction.
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, diag.getItem(ipvt[j], 0) * (wa2.getItem(ipvt[j], 0) / dxnorm));
			}
			
//			System.out.println(sdiag.toMatlabString("msd"));
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, wa1.getItem(j, 0) / sdiag.getItem(j, 0));
				temp = wa1.getItem(j, 0);
				int jp1 = j + 1;
				for (int i = jp1; i < n; i++) {
					wa1.setItem(i, 0, wa1.getItem(i, 0) - r.getItem(i, j) * temp);
				}
			}
			temp = wa1.getForbeniusNorm();
			double parc = ((fp / delta) / temp) / temp;
			
//			System.out.printf("parc=%12.8f\n", parc);
//			System.out.printf("temp=%12.8f\n", temp);
//			System.out.println(wa1.toMatlabString("wa1"));
//			System.exit(0);

			// depending on the sign of the function, update parl or paru.
			if (fp > 0.0)
				parl = Math.max(parl, par);
			if (fp < 0.0)
				paru = Math.min(paru, par);
			// compute an improved estimate for par.
			par = Math.max(parl, par + parc);
//			System.out.printf("paru =%12.8f\n", paru);
//			System.out.printf("parl =%12.8f\n", parl);
//			System.out.printf("fp   =%12.8f\n", fp);
			
//			if (iter > 2)
//				System.exit(0);
		}
		return par;
	}	

	/**
	 * subroutine qrsolv
	 * 
	 * given an m by n matrix a, an n by n diagonal matrix d,
	 * and an m-vector b, the problem is to determine an x which
	 * solves the system
	 * <pre>
	 *   a*x = b ,	  d*x = 0 ,
	 * </pre>
	 * in the least squares sense.
	 * 
	 * this subroutine completes the solution of the problem
	 * if it is provided with the necessary information from the
	 * qr factorization, with column pivoting, of a. that is, if
	 * a*p = q*r, where p is a permutation matrix, q has orthogonal
	 * columns, and r is an upper triangular matrix with diagonal
	 * elements of nonincreasing magnitude, then qrsolv expects
	 * the full upper triangle of r, the permutation matrix p,
	 * and the first n components of (q transpose)*b. the system
	 * a*x = b, d*x = 0, is then equivalent to
	 * <pre>
	 *    t	   t
	 * r*z = q *b ,  p *d*p*z = 0 ,
	 * </pre>
	 * where x = p*z. if this system does not have full rank,
	 * then a least squares solution is obtained. on output qrsolv
	 * also provides an upper triangular matrix s such that
	 * <pre>
	 *    t	 t		 t
	 *   p *(a *a + d*d)*p = s *s .
	 * </pre>
	 * s is computed within qrsolv and may be of separate interest.
	 * 
	 * 
	 * 
	 * @param r		is an n by n array. on input the full upper triangle
	 * 				must contain the full upper triangle of the matrix r.
	 * 				on output the full upper triangle is unaltered, and the
	 * 				strict lower triangle contains the strict upper triangle
	 * 				(transposed) of the upper triangular matrix s.
	 * @param diag	is an input array of length n which must contain the
	 * 				diagonal elements of the matrix d.
	 * @param x		is an output array of length n which contains the least
	 * 				squares solution of the system a*x = b, d*x = 0.
	 * @param qtb	is an input array of length n which must contain the first
	 * 				n elements of the vector (q transpose)*b.
	 */
	static void qrsolv(Matrix r, Matrix diag, Matrix x, Matrix qtb, Matrix sdiag, int ipvt[]) {
		int m = r.getSizeX();
		int n = r.getSizeY(); 
		if ( //(m < n) ||
			(qtb.getSizeX() != n) || (qtb.getSizeY() != 1) ||
			(diag.getSizeX() != n) || (diag.getSizeY() != 1)) {
			throw new IllegalArgumentException();
		}
		x.resize(n, 1);
		Matrix wa = qtb.makeCopy();
		
		// copy r and (q transpose)*b to preserve input and initialize s.
		// in particular, save the diagonal elements of r in x.

		for (int j = 0; j < n; j++) {
			for (int i = j; i < n; i++)
				r.setItem(i, j, r.getItem(j, i));
			x.setItem(j, 0, r.getItem(j, j));
		}
		
//		System.out.printf("mr=[\n");
//		for (int jj = 0; jj < n; jj++) {
//			for (int ii = 0; ii < n; ii++) {
//				System.out.printf(Locale.US, "%12.8f\t", r.getItem(ii, jj));
//			}
//			System.out.printf(";\n");
//		}
//		System.out.printf("];\n");
//		System.exit(0);
		
//		System.out.println(diag.toMatlabString("md"));
		// eliminate the diagonal matrix d using a givens rotation.
		for (int j = 0; j < n; j++) {
			// prepare the row of d to be eliminated, locating the
			// diagonal element using p from the qr factorization.
			if (diag.getItem(ipvt[j], 0) != 0.0) {
				for (int k = j; k < n; k++)
					sdiag.setItem(k, 0, 0.0);
				sdiag.setItem(j, 0, diag.getItem(ipvt[j], 0));
							
				// the transformations to eliminate the row of d  
				// modify only a single element of (q transpose)*b
				// beyond the first n, which is initially zero.
				double qtbpj = 0.0;
				for (int k = j; k < n; k++) {
					// determine a givens rotation which eliminates the
					// appropriate element in the current row of d.    
					if (sdiag.getItem(k, 0) == 0.0)
						continue;
					double sin, cos;
//					System.out.printf("k=%d r[kk]=%12.8f sdiag[k]=%12.8f\n", k, r.getItem(k, k), sdiag.getItem(k, 0));
					if (Math.abs(r.getItem(k, k)) < Math.abs(sdiag.getItem(k, 0))) {
						double cotan = r.getItem(k, k) / sdiag.getItem(k, 0);
						sin = 0.5 / Math.sqrt(0.25 + 0.25 * cotan * cotan);
						cos = sin * cotan;
					} else {
						double tan = sdiag.getItem(k, 0) / r.getItem(k, k);
						cos = 0.5 / Math.sqrt(0.25 + 0.25 * tan * tan);
						sin = cos * tan;
					}
//					System.out.println(sdiag.toMatlabString("msd"));
//					System.out.printf("SIN=%12.8f\n", sin);
//					System.out.printf("COS=%12.8f\n", cos);
					// compute the modified diagonal element of r and
					// the modified element of ((q transpose)*b,0).
					r.setItem(k, k, cos * r.getItem(k, k) + sin * sdiag.getItem(k, 0));
					double temp = cos * wa.getItem(k, 0) + sin * qtbpj;
					qtbpj = -sin * wa.getItem(k, 0) + cos * qtbpj;
					wa.setItem(k, 0, temp);
					
//					System.out.printf("r[kk]=%12.8f\n", r.getItem(k, k));
//					System.out.printf("temp =%12.8f\n", temp);
//					System.out.printf("qtbpj=%12.8f\n", qtbpj);
//					System.out.printf("wa[k]=%12.8f\n", wa.getItem(k, 0));
//					
//					System.out.println(sdiag.toMatlabString("msd"));
//					System.out.println(r.toMatlabString("mr"));
					// accumulate the tranformation in the row of s.
					for (int i = k + 1; i < n; i++) {
						temp = cos * r.getItem(i, k) + sin * sdiag.getItem(i, 0);
						sdiag.setItem(i, 0, -sin * r.getItem(i, k) + cos * sdiag.getItem(i, 0));
						r.setItem(i, k, temp);
					}
					
//					System.out.printf("mr=[\n");
//					for (int jj = 0; jj < n; jj++) {
//						for (int ii = 0; ii < n; ii++) {
//							System.out.printf(Locale.US, "%12.8f\t", r.getItem(ii, jj));
//						}
//						System.out.printf(";\n");
//					}
//					System.out.printf("];\n");
					
				}
			}
			// store the diagonal element of s and restore
			// the corresponding diagonal element of r.
			sdiag.setItem(j, 0, r.getItem(j, j));
			r.setItem(j, j, x.getItem(j, 0));
//			System.exit(0);
		}

//		System.out.println(sdiag.toMatlabString("msd"));
//		System.out.printf("mr=[\n");
//		for (int jj = 0; jj < n; jj++) {
//			for (int ii = 0; ii < n; ii++) {
//				System.out.printf(Locale.US, "%12.8f\t", r.getItem(ii, jj));
//			}
//			System.out.printf(";\n");
//		}
//		System.out.printf("];\n");
//		System.exit(0);
//		System.out.printf("diagNorm =%12.8f\n", diag.getForbeniusNorm());
//		System.out.printf("x norm   =%12.8f\n", x.getForbeniusNorm());
//		System.out.printf("wa norm  =%12.8f\n", wa.getForbeniusNorm());
		
		// solve the triangular system for z. if the system is
		// singular, then obtain a least squares solution.
		int nsing = n;
		for (int j = 0; j < n; j++) {
			if( (sdiag.getItem(j, 0) == 0.0) && (nsing == n) )
				nsing = j;
			if(nsing < n)
				wa.setItem(j, 0, 0.0);
		}
		if (nsing >= 1) {
			for (int k=0; k<nsing; k++) {
				int j = nsing - k - 1;
				double sum = 0.0;
				int jp1 = j + 1;
				if (nsing > jp1) {
					for (int i = jp1; i<nsing; i++ ) {
						sum += r.getItem(i, j) * wa.getItem(i, 0);
					}
				}
				wa.setItem(j, 0, (wa.getItem(j, 0) - sum) / sdiag.getItem(j, 0));
			}			
		}
		// permute the components of z back to components of x.
		for (int j = 0; j < n; j++) {
			int l = ipvt[j];
			x.setItem(l, 0, wa.getItem(j, 0));
		}

//		System.out.println(x.toMatlabString("mx"));
//		System.exit(0);
	}
}
