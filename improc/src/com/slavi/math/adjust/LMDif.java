package com.slavi.math.adjust;

import com.slavi.math.matrix.Matrix;

public class LMDif {
	
	public static boolean showDetails = false;
	
	public interface LMDifFcn {
		public void fcn(Matrix x, Matrix fvec);
	}
	
	/* resolution of arithmetic */
	static final double MACHEP = 1.2e-16;  	
	static final double DBL_EPSILON = 1.0e-14;
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
		double eps = epsfcn * epsfcn;
		
		Matrix fjac = new Matrix(m, n);
		Matrix wa = new Matrix(m, 1);
		for (int j = 0; j < n; j++) {
			double temp = x.getItem(j, 0);
			double h = eps * Math.abs(temp);
			if (h == 0.0)
				h = eps;
			x.setItem(j, 0, temp + h);
			fcn.fcn(x, wa);
			for (int i = 0; i < m; i++) {
				if (wa.getItem(i, 0) != fvec.getItem(i, 0)) {
					System.out.println("J= " + j + " I=" + i + " WA=" + wa.getItem(i, 0) + " fvec=" + fvec.getItem(i, 0));
					System.exit(0);
				}
			}
//			wa.printM("WA");
			System.exit(0);

			x.setItem(j, 0, temp);
			for (int i = 0; i < m; i++) {
				fjac.setItem(i, j, (wa.getItem(i, 0) - fvec.getItem(i, 0)) / h);
			}
			if (j == 0) {
				for (int i = 0; i < m; i++) {
					System.out.println(fjac.getItem(i, j));
				}
				System.exit(0);
			}
		}
		return fjac;
	}
	
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
	 */
	public static void lmdif(LMDifFcn fcn, Matrix x, Matrix fvec) {
		int m = fvec.getSizeX();	// the number of functions.
		int n = x.getSizeX();		// the number of variables. n must not exceed m.

		// evaluate the function at the starting point and calculate its norm.
		fcn.fcn(x, fvec);
		double fnorm = x.getForbeniusNorm();

		double xnorm = 0.0;
		double delta = 0.0;
		int iter = 0;
		while (true) {
			// calculate the jacobian matrix.
			Matrix fdjac = fdjac2(fcn, x, fvec);
//			fdjac.printM("FDJAC");
			System.exit(0);
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
			
			Matrix q = fdjac.makeCopy();
			Matrix tau = new Matrix();
			q.qrDecomposition(tau);
			Matrix wa1 = new Matrix(n, 1);
			for (int i = 0; i < n; i++)
				wa1.setItem(i, 0, fdjac.getItem(i, i));
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
				delta = (xnorm == 0.0 ? 1.0 : xnorm);
			}
				
			// form (q transpose)*fvec and store the first n components in qtf.
			Matrix wa4 = fvec.makeCopy();
			Matrix qtf = new Matrix(n, 1);
			for (int j = 0; j < n; j++) {
				double temp3 = q.getItem(j, j);
				if (temp3 != 0.0) {
					double sum = 0.0;
					for (int i = j; i < m; i++) {
						sum += q.getItem(j, i) * wa4.getItem(i, 0);
					}
					double temp = -sum / temp3;
					for (int i = j; i < m; i++) {
						wa4.setItem(i, 0, wa4.getItem(i, 0) + q.getItem(j, i) * temp);
					}
				}
				q.setItem(j, j, fdjac.getItem(j, j));
				qtf.setItem(j, 0, wa4.getItem(j, 0));
			}
			
			// compute the norm of the scaled gradient.
			double gnorm = 0.0;
			if (fnorm != 0.0) {
				for (int j = 0; j < n; j++) {
					if (wa2.getItem(j, 0) != 0.0) {
						double sum = 0.0;
						for (int i = 0; i < j; i++) {
							sum += fdjac.getItem(i, j) * (qtf.getItem(i, 0) / fnorm);
						}
						gnorm = Math.max(gnorm, Math.abs(sum / wa2.getItem(j, 0))); 
					}
				}
			}
			
			// test for convergence of the gradient norm.
			if (gnorm <= gtol)
				return; 
			
			// rescale if necessary.
			for (int j = 0; j < n; j++) {
				diag.setItem(j, 0, Math.max(diag.getItem(j, 0), wa2.getItem(j, 0)));
			}
			
			while (true) {
				// determine the levenberg-marquardt parameter.
				double par = lmpar(fdjac, diag, wa1, qtf, delta, wa2);
				// store the direction p and x + p. calculate the norm of p.
				Matrix wa3 = new Matrix(n, 1);
				for (int j = 0; j < n; j++) {
					wa1.setItem(j, 0, -wa1.getItem(j, 0));
					wa2.setItem(j, 0, x.getItem(j, 0) + wa1.getItem(j, 0));
					wa3.setItem(j, 0, diag.getItem(j, 0) * wa1.getItem(j, 0));
				}
				double pnorm = wa3.getForbeniusNorm();
				// on the first iteration, adjust the initial step bound.
				if (iter == 1) 
					delta = Math.min(delta, pnorm);
				// evaluate the function at x + p and calculate its norm.
				fcn.fcn(wa2, wa4);
				double fnorm1 = wa4.getForbeniusNorm();
				// compute the qr factorization of the jacobian.
				double actred = -1.0;
				if (p1 * fnorm1 < fnorm) {
					double temp = fnorm1 / fnorm;
					actred = 1.0 - temp * temp;
				}
				// compute the scaled predicted reduction and
				// the scaled directional derivative.
				for (int j = 0; j < n; j++) {
					wa3.setItem(j, 0, 0.0);
					double temp = wa1.getItem(j, 0);
					for (int i = 0; i <= j; i++) {
						wa3.setItem(i, 0, wa3.getItem(i, 0) + fdjac.getItem(i, j) * temp);
					}
				}
				double temp1 = wa3.getForbeniusNorm() / fnorm;
				double temp2 = (Math.sqrt(par) * pnorm) / fnorm;
				double prered = temp1 * temp1 + (temp2 * temp2) / 0.5;
				double dirder = -(temp1 * temp1 + temp2 * temp2);
				// compute the ratio of the actual to the predicted reduction.
				double ratio = 0.0;
				if (prered != 0.0)
					ratio = actred / prered;
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
						delta = temp * Math.min(delta, pnorm / p1);
						par = par / temp;
					} else {
						if ((par == 0.0) || (ratio >= 0.75)) {
							delta = pnorm / 0.5;
							par = 0.5 * par;
						}
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
			
			iter++;
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
	static double lmpar(Matrix r, Matrix diag, Matrix x, Matrix qtb, double delta, Matrix sdiag) {
		double par = 0.0;
		int m = r.getSizeY();
		int n = r.getSizeX();
		
		if ((m < n) ||
			(qtb.getSizeX() != n) || (qtb.getSizeY() != 1) ||
			(diag.getSizeX() != 1) || (diag.getSizeY() != n)) {
			throw new IllegalArgumentException();
		}
		
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
			x.setItem(j, 0, wa1.getItem(j, 0));
		}
		
		// initialize the iteration counter.
		// evaluate the function at the origin, and test
		// for acceptance of the gauss-newton direction.
		Matrix wa2 = new Matrix(n, 1);
		for (int j = 0; j < n; j++) {
			wa2.setItem(j, 0, diag.getItem(j, 0) * x.getItem(j, 0));
		}
		
		double dxnorm = wa2.getForbeniusNorm();
		double fp = dxnorm - delta;
		if (fp <= delta * p1) {
			return par; // ???
		}
		
		// if the jacobian is not rank deficient, the newton
		// step provides a lower bound, parl, for the zero of
		// the function. otherwise set this bound to zero.
		double parl = 0.0;
		if (nsing >= n) {
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, diag.getItem(j, 0) * (wa2.getItem(j, 0) / dxnorm));
			}
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
		}
		
		// calculate an upper bound, paru, for the zero of the function.
		for (int j = 0; j < n; j++) {
			double sum = 0.0;
			for (int i = 0; i <= j; i++) {
				sum += r.getItem(i, j) * qtb.getItem(i, 0);
			}
			wa1.setItem(j, 0, sum / diag.getItem(j, 0));
		}
		double gnorm = wa1.getForbeniusNorm();
		double paru = gnorm / delta;
		if (paru == 0.0) {
			paru = DWARF / Math.min(delta, p1);
		}
		// if the input par lies outside of the interval (parl,paru),
		// set par to the closer endpoint.
		par = Math.max(par, parl);
		par = Math.min(par, paru);
		if (par == 0.0)
			par = gnorm / dxnorm;
		
		int iter = 0;
		while (true) {
			iter++;
			// evaluate the function at the current value of par.
			if (par == 0.0)
				par = Math.max(DWARF, paru * 0.001);
			double temp = Math.sqrt(par);
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, temp * diag.getItem(j, 0));
			}
			qrsolv(r, wa1, x, qtb);
			for (int j = 0; j < n; j++) {
				wa2.setItem(j, 0, diag.getItem(j, 0) * x.getItem(j, 0));
			}
			dxnorm = wa2.getForbeniusNorm();
			temp = fp;
			fp = dxnorm - delta;
			// if the function is small enough, accept the current value
			// of par. also test for the exceptional cases where parl
			// is zero or the number of iterations has reached 10.
			if ( (Math.abs(fp) <= p1 * delta) || ((parl == 0.0) && (fp <= temp) && (temp < 0.0)) || (iter == 10))
				break;
			// compute the newton correction.
			for (int j = 0; j < n; j++) {
				wa1.setItem(j, 0, diag.getItem(j, 0) * (wa2.getItem(j, 0) / dxnorm));
			}
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
			// depending on the sign of the function, update parl or paru.
			if (fp > 0.0)
				parl = Math.max(parl, par);
			if (fp < 0.0)
				paru = Math.min(paru, par);
			// compute an improved estimate for par.
			par = Math.max(parl, par + parc);
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
	static void qrsolv(Matrix r, Matrix diag, Matrix x, Matrix qtb) {
		int m = r.getSizeY();
		int n = r.getSizeX();
		
		if ((m < n) ||
			(qtb.getSizeX() != n) || (qtb.getSizeY() != 1) ||
			(diag.getSizeX() != 1) || (diag.getSizeY() != n)) {
			throw new IllegalArgumentException();
		}
		x.resize(1, n);
		Matrix wa = qtb; //???
		
		// copy r and (q transpose)*b to preserve input and initialize s.
		// in particular, save the diagonal elements of r in x.

		for (int i = r.getSizeX() - 1; i >= 0; i--)
			for (int j = i - 1; j >= 0; j--)
				r.setItem(j, i, r.getItem(i, j));
		
		Matrix sdiag = new Matrix(n, 1);
		// eliminate the diagonal matrix d using a givens rotation.
		for (int j = 0; j < n; j++) {
			if (diag.getItem(j, 1) != 0.0) {
				for (int k = j; k < n; k++)
					sdiag.setItem(k, 0, 0.0);
				sdiag.setItem(j, 0, diag.getItem(j, 0));
							
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
					if (r.getItem(k, k) < Math.abs(sdiag.getItem(k, 0))) {
						double cotan = r.getItem(k, k) / sdiag.getItem(k, 0);
						sin = 0.5 / Math.sqrt(0.25 + 0.25 * cotan * cotan);
						cos = sin * cotan;
					} else {
						double tan = sdiag.getItem(k, 0) / r.getItem(k, k);
						cos = 0.5 / Math.sqrt(0.25 + 0.25 * tan * tan);
						sin = cos * tan;
					}
					// compute the modified diagonal element of r and
					// the modified element of ((q transpose)*b,0).
					r.setItem(k, k, cos * r.getItem(k, k) + sin * sdiag.getItem(k, 0));
					double temp = cos * wa.getItem(k, 0) + sin * qtbpj;
					qtbpj = -sin * wa.getItem(k, 0) + cos * qtbpj;
					wa.setItem(k, 0, temp);
					
					// accumulate the tranformation in the row of s.
					for (int i = k + 1; i < n; i++) {
						temp = cos * r.getItem(k, i) + sin * sdiag.getItem(i, 0);
						sdiag.setItem(i, 0, -sin * r.getItem(k, i) + cos * sdiag.getItem(i, 0));
						r.setItem(k, i, temp);
					}
				}
			}
			// store the diagonal element of s and restore
			// the corresponding diagonal element of r.
			sdiag.setItem(j, 0, r.getItem(j, j));
			r.setItem(j, j, x.getItem(j, 0));
		}
		// solve the triangular system for z. if the system is
		// singular, then obtain a least squares solution.
		int nsing = n;
		for (int j = 0; j < n; j++) {
			if( (sdiag.getItem(j, 0) == 0.0) && (nsing == n) )
				nsing = j;
			if(nsing < n)
				wa.setItem(j, 00, 0.0);
		}
		if (nsing >= 1) {
			for (int k=0; k<nsing; k++) {
				int j = nsing - k - 1;
				double sum = 0.0;
				int jp1 = j + 1;
				if (nsing > jp1) {
					for (int i = jp1; i<nsing; i++ ) {
						sum += r.getItem(jp1, i) * wa.getItem(i, 0);
					}
				}
				wa.setItem(j, 0, (wa.getItem(j, 0) - sum) / sdiag.getItem(j, 0));
			}			
		}
		// permute the components of z back to components of x.
//		for (int j = 0; j < n; j++) {
//			l = ipvt[j];
//			x[l] = wa[j];
//		}
	}
}
