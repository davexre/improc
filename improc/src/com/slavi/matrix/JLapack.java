package com.slavi.matrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

import com.slavi.testpackage.MatrixTest;

public class JLapack {
	static final double EPS = 1.11022302E-016;

	/**
	 * Transfers the sign of the first parameter (a) to 
	 * the second parameter (b) and returns the result.
	 */
	public static final double SIGN(double a, double b) {
		return ((b) >= 0.0 ? Math.abs(a) : -Math.abs(a));
	}

	/** 
	 * LAPACK: DOUBLE PRECISION FUNCTION DLAPY2( X, Y )
	 * sqrt(a^2 + b^2) without under/overflow. 
	 */
	private static double hypot(double a, double b) {
		double r;
		double absA = Math.abs(a);
		double absB = Math.abs(b);
		if (absA > absB) {
			r = b / a;
			r = absA * Math.sqrt(1 + Math.pow(r, 2.0));
		} else if (b != 0) {
			r = a / b;
			r = absB * Math.sqrt(1 + r * r);
		} else {
			r = 0.0;
		}
		return r;
	}
	
	public double DLARFG_Beta;
	public double DLARFG_Tau;
	/**
	 * LAPACK: SUBROUTINE DLARFG( N, ALPHA, X, INCX, TAU )
	 * 
 	 * DLARFG generates a real elementary reflector H of order n, such that
	 * <br><tt>
	 *       H * ( alpha ) = ( beta ),   H' * H = I.<br>
	 *           (   x   )   (   0  )<br>
	 * </tt>
	 * where alpha and beta are scalars, and x is an (n-1)-element real
	 * vector. H is represented in the form
	 * <br><tt>
	 *       H = I - tau * ( 1 ) * ( 1 v' ) ,<br>
	 *                     ( v )<br>
	 * </tt>
	 * where tau is a real scalar and v is a real (n-1)-element
	 * vector.
	 * 
	 * If the elements of x are all zero, then tau = 0 and H is taken to be
	 * the unit matrix.
	 * 
	 * DLARFG -> INCX=1
	 */
	public void DLARFG_X(Matrix Src, int atX, int atY, int width, double alpha) {
		double xnorm = 0.0;
		for (int i = atX + width - 1; i > atX; i--)
			xnorm = hypot(xnorm, Src.getItem(i, atY));
		if (xnorm == 0.0) {
			DLARFG_Beta = alpha;
			DLARFG_Tau = 0.0;
			return;
		}

		//DLARFG_Beta = -SIGN(Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)), alpha);
		DLARFG_Beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
		if (DLARFG_Beta == 0.0) {
			DLARFG_Tau = 0.0;
			return;
		}
		if (alpha >= 0.0)
			DLARFG_Beta = -DLARFG_Beta;
		DLARFG_Tau = (DLARFG_Beta - alpha) / DLARFG_Beta;
		double scale = 1.0 / (alpha - DLARFG_Beta);
		for (int i = atX + width - 1; i > atX; i--) 
			Src.setItem(i, atY, scale * Src.getItem(i, atY));
	}
	
	/**
	 * @see #DLARFG_X(Matrix, int, int, int, double)
	 * DLARFG -> INCX=Src.GetSizeY() 
	 */
	public void DLARFG_Y(Matrix Src, int atX, int atY, int height, double alpha) {
		double xnorm = 0.0;
		for (int j = atY + height - 1; j > atY; j--)
			xnorm = hypot(xnorm, Src.getItem(atX, j));
		if (xnorm == 0.0) {
			DLARFG_Beta = alpha;
			DLARFG_Tau = 0.0;
			return;
		}

		//DLARFG_Beta = -SIGN(Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)), alpha);
		DLARFG_Beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
		if (DLARFG_Beta == 0.0) {
			DLARFG_Tau = 0.0;
			return;
		}
		if (alpha >= 0.0)
			DLARFG_Beta = -DLARFG_Beta;
		DLARFG_Tau = (DLARFG_Beta - alpha) / DLARFG_Beta;
		double scale = 1.0 / (alpha - DLARFG_Beta);
		for (int j = atY + height - 1; j > atY; j--) 
			Src.setItem(atX, j, scale * Src.getItem(atX, j));
	}
	
	/**
	 * DLARF applies a real elementary reflector H to a real m by n matrix
	 * C, from either the left or the right. H is represented in the form
	 *       H = I - tau * v * v'
	 * where tau is a real scalar and v is a real vector.
	 * If tau = 0, then H is taken to be the unit matrix.
	 * DLARF -> SIDE="Left"
	 */
	public static void DLARF_X(Matrix Src, int atX, double tau) {

		for (int i = Src.getSizeX() - 1; i > atX; i--) {
			double sum = 0.0;
			for (int j = Src.getSizeY() - 1; j >= atX; j--) 
				sum += Src.getItem(i, j) * Src.getItem(atX, j);
			for (int j = Src.getSizeY() - 1; j >= atX; j--) 
				Src.setItem(i, j, Src.getItem(i, j) - tau * sum * Src.getItem(atX, j));
		}
	}

	/**
	 * @see #DLARF_X(Matrix, int, double)
	 * DLARF -> SIDE="Right"
	 */
	public static void DLARF_Y(Matrix Src, int atY, double tau) {
		for (int j = Src.getSizeY() - 1; j > atY; j--) {
			double sum = 0.0;
			for (int i = Src.getSizeX() - 1; i >= atY; i--) 
				sum += Src.getItem(i, j) * Src.getItem(i, atY);
			for (int i = Src.getSizeX() - 1; i >= atY; i--) 
				Src.setItem(i, j, Src.getItem(i, j) - tau * sum * Src.getItem(i, atY));
		}
	}
	
	/**
	 * LAPACK: SUBROUTINE DGEQRF( M, N, A, LDA, TAU, WORK, LWORK, INFO )
	 * 
	 * DGEQRF computes a QR factorization of a real M-by-N matrix A: A = Q * R.
	 * 
	 * A - On entry, the M-by-N matrix A.
	 * 
	 * A - On exit, the elements on and above the diagonal of the array
	 * 		contain the min(M,N)-by-N upper trapezoidal matrix R (R is
	 * 		upper triangular if m >= n); the elements below the diagonal,
	 * 		with the array TAU, represent the orthogonal matrix Q as a
	 * 		product of min(m,n) elementary reflectors.
	 * 
	 * The matrix Q is represented as a product of elementary reflectors
	 * 
	 * Q = H(1) H(2) . . . H(k), where k = min(m,n).
	 * 
	 * Each H(i) has the form
	 * 
	 * H(i) = I - tau * v * v'
	 * 
	 * where tau is a real scalar, and v is a real vector with
	 * v(1:i-1) = 0 and v(i) = 1; v(i+1:m) is stored on exit in A(i+1:m,i),
	 * and tau in TAU(i).
	 */
	public void qrDecomposition(Matrix Src, Matrix tau) {
		int minXY = Math.min(Src.getSizeX(), Src.getSizeY());
		
		if ((tau.getSizeX() < minXY) || (tau.getSizeY() < 1))
			tau.resize(minXY, 1);

		for (int atIndex = 0; atIndex < minXY; atIndex++) {
			if (atIndex >= Src.getSizeY() - 1) {
				tau.setItem(atIndex, 0, 0.0);
				continue;
			}
			// Generate elementary reflector H(i) to annihilate A(i+1:m,i)
			DLARFG_Y(Src, atIndex, atIndex, Src.getSizeY() - atIndex, Src.getItem(atIndex, atIndex));
			tau.setItem(atIndex, 0, DLARFG_Tau);

			// DGEQR2:109 Apply H(i) to A(i:m,i+1:n) from the left
			Src.setItem(atIndex, atIndex, 1.0);
			DLARF_X(Src, atIndex, DLARFG_Tau);
			Src.setItem(atIndex, atIndex, DLARFG_Beta);
		}
	}
	
	/**
	 * LAPACK SUBROUTINE DORGBR( VECT, M, N, K, A, LDA, TAU, WORK, LWORK, INFO )
	 * 
	 * DORGBR generates one of the real orthogonal matrices Q or P**T
	 * determined by DGEBRD when reducing a real matrix A to bidiagonal
	 * form: A = Q * B * P**T.  Q and P**T are defined as products of
	 * elementary reflectors H(i) or G(i) respectively.
	 * 
	 * DORGBR -> VECT="P"
	 */
	public static void qrDecomositionGetR(Matrix Src, Matrix r) {
		r.resize(Src.getSizeX(), Src.getSizeY());
		for (int i = Src.getSizeX() - 1; i >= 0; i--)
			for (int j = Src.getSizeY() - 1; j >= 0; j--)
				r.setItem(i, j, i < j ? 0.0 : Src.getItem(i, j));
	}
	
	/**
	 * @see #qrDecomositionGetR(Matrix, Matrix)
	 * DORGBR -> VECT="Q"
	 */
	public static void qrDecomositionGetQ(Matrix Src, Matrix tau, Matrix q) {
		if ((tau.getSizeX() < Src.getSizeX()) || (tau.getSizeY() < 1))
			throw new Error("Invalid parameter");
		q.resize(Src.getSizeY(), Src.getSizeY());
		for (int i = Src.getSizeY() - 1; i >= 0; i--)
			for (int j = Src.getSizeY() - 1; j >= 0; j--)
				q.setItem(i, j, (i <= j) && (i < Src.getSizeX()) ? Src.getItem(i, j) : i == j ? 1.0 : 0.0);

		for (int atIndex = Src.getSizeX() - 1; atIndex >= 0; atIndex--) {
			if (atIndex < q.getSizeY()) {
				double tmp_tau = tau.getItem(atIndex, 0);
				q.setItem(atIndex, atIndex, 1.0);
				DLARF_X(q, atIndex, tmp_tau);

				for (int j = q.getSizeY() - 1; j > atIndex; j--)
					q.setItem(atIndex, j, q.getItem(atIndex, j) * (-tmp_tau));
				q.setItem(atIndex, atIndex, 1.0 - tmp_tau);
				for (int j = atIndex - 1; j >= 0; j--)
					q.setItem(atIndex, j, 0.0);				
			}
		}
	}

	/**
	 * LAPACK: SUBROUTINE DGELQF( M, N, A, LDA, TAU, WORK, LWORK, INFO )
	 * 
	 * DGELQF computes an LQ factorization of a real M-by-N matrix A: A = L * Q.
	 * 
	 * The matrix Q is represented as a product of elementary reflectors
	 * 
	 * Q = H(k) . . . H(2) H(1), where k = min(m,n).
	 * 
	 * Each H(i) has the form
	 * 
	 * H(i) = I - tau * v * v'
	 * 
	 * where tau is a real scalar, and v is a real vector with
	 * v(1:i-1) = 0 and v(i) = 1; v(i+1:n) is stored on exit in A(i,i+1:n),
	 * and tau in TAU(i).
	 */
	public void lqDecomposition(Matrix Src, Matrix tau) {
		int minXY = Math.min(Src.getSizeX(), Src.getSizeY());
		if ((tau.getSizeX() < minXY) || (tau.getSizeY() < 1))
			tau.resize(minXY, 1);

		for (int atIndex = 0; atIndex < minXY; atIndex++) {
			if (atIndex >= Src.getSizeX() - 1) {
				tau.setItem(atIndex, 0, 0.0);
				continue;
			}
			// Generate elementary reflector H(i) to annihilate A(i,i+1:n)
			DLARFG_X(Src, atIndex, atIndex, Src.getSizeX() - atIndex, Src.getItem(atIndex, atIndex));
			tau.setItem(atIndex, 0, DLARFG_Tau);
			
			// DGELQ2:109 Apply H(i) to A(i+1:m,i:n) from the right
			Src.setItem(atIndex, atIndex, 1.0);
			DLARF_Y(Src, atIndex, DLARFG_Tau);
			Src.setItem(atIndex, atIndex, DLARFG_Beta);
		}
	}
	
	/**
	 * LAPACK: DORGBR( VECT, M, N, K, A, LDA, TAU, WORK, LWORK, INFO )
	 * 
	 * @see #qrDecomositionGetR(Matrix, Matrix)
	 * DORGBR -> VECT="P"
	 */
	public static void lqDecomositionGetL(Matrix Src, Matrix l) {
		l.resize(Src.getSizeX(), Src.getSizeY());
		for (int i = Src.getSizeX() - 1; i >= 0; i--)
			for (int j = Src.getSizeY() - 1; j >= 0; j--)
				l.setItem(i, j, i > j ? 0.0 : Src.getItem(i, j));
	}

	/**
	 * @see #lqDecomositionGetL(Matrix, Matrix)
	 * DORGBR -> VECT="Q"
	 */
	public static void lqDecomositionGetQ(Matrix Src, Matrix tau, Matrix q) {
		if ((tau.getSizeX() < Src.getSizeY()) || (tau.getSizeY() != 1))
			throw new Error("Invalid parameter");
		
		q.resize(Src.getSizeX(), Src.getSizeX());
		for (int i = Src.getSizeX() - 1; i >= 0; i--)
			for (int j = Src.getSizeX() - 1; j >= 0; j--)
				q.setItem(i, j, (i >= j) && (j < Src.getSizeY()) ? Src.getItem(i, j) : i == j ? 1.0 : 0.0);

		for (int atIndex = Src.getSizeY() - 1; atIndex >= 0; atIndex--) {
			if (atIndex < q.getSizeX()) {
				double tmp_tau = tau.getItem(atIndex, 0);
				q.setItem(atIndex, atIndex, 1.0);
				DLARF_Y(q, atIndex, tmp_tau);

				for (int i = q.getSizeX() - 1; i > atIndex; i--)
					q.setItem(i, atIndex, q.getItem(i, atIndex) * (-tmp_tau));
				q.setItem(atIndex, atIndex, 1.0 - tmp_tau);
				for (int i = atIndex - 1; i >= 0; i--)
					q.setItem(i, atIndex, 0.0);				
			}
		}
	}
	
	/**
	 * LAPACK: SUBROUTINE DGEBRD( M, N, A, LDA, D, E, TAUQ, TAUP, WORK, LWORK, INFO )
	 * 
	 * DGEBRD reduces a general real M-by-N matrix A to upper or lower
	 * bidiagonal form B by an orthogonal transformation: Q**T * A * P = B.
	 * 
	 * If m >= n, B is upper bidiagonal; if m < n, B is lower bidiagonal.
	 * 
	 * The matrices Q and P are represented as products of elementary reflectors:
	 * 
	 * If m >= n,
	 * 
	 * Q = H(1) H(2) . . . H(n)  and  P = G(1) G(2) . . . G(n-1)
	 * 
	 * Each H(i) and G(i) has the form:
	 * 
	 * H(i) = I - tauq * v * v'  and G(i) = I - taup * u * u'
	 * 
	 * where tauq and taup are real scalars, and v and u are real vectors;
	 * v(1:i-1) = 0, v(i) = 1, and v(i+1:m) is stored on exit in A(i+1:m,i);
	 * u(1:i) = 0, u(i+1) = 1, and u(i+2:n) is stored on exit in A(i,i+2:n);
	 * tauq is stored in TAUQ(i) and taup in TAUP(i).
	 * 
	 * If m < n,
	 * 
	 * Q = H(1) H(2) . . . H(m-1)  and  P = G(1) G(2) . . . G(m)
	 * 
	 * Each H(i) and G(i) has the form:
	 * 
	 * H(i) = I - tauq * v * v'  and G(i) = I - taup * u * u'
	 * 
	 * where tauq and taup are real scalars, and v and u are real vectors;
	 * v(1:i) = 0, v(i+1) = 1, and v(i+2:m) is stored on exit in A(i+2:m,i);
	 * u(1:i-1) = 0, u(i) = 1, and u(i+1:n) is stored on exit in A(i,i+1:n);
	 * tauq is stored in TAUQ(i) and taup in TAUP(i).
	 * 
	 * The contents of A on exit are illustrated by the following examples:
	 * <br><tt>
	 * m = 6 and n = 5 (m > n):          m = 5 and n = 6 (m < n):<br>
	 * <br>
	 *   (  d   e   u1  u1  u1 )           (  d   u1  u1  u1  u1  u1 )<br>
	 *   (  v1  d   e   u2  u2 )           (  e   d   u2  u2  u2  u2 )<br>
	 *   (  v1  v2  d   e   u3 )           (  v1  e   d   u3  u3  u3 )<br>
	 *   (  v1  v2  v3  d   e  )           (  v1  v2  e   d   u4  u4 )<br>
	 *   (  v1  v2  v3  v4  d  )           (  v1  v2  v3  e   d   u5 )<br>
	 *   (  v1  v2  v3  v4  v5 )<br>
	 * </tt>
	 * where d and e denote diagonal and off-diagonal elements of B, vi
	 * denotes an element of the vector defining H(i), and ui an element of
	 * the vector defining G(i).
	 */
	public void DGEBRD(Matrix Src, Matrix tauP, Matrix tauQ) {
		boolean upper = true; //getSizeX() <= getSizeY();
		//int minXY = upper ? getSizeX() : getSizeY();
		int minXY = Math.min(Src.getSizeX(), Src.getSizeY());
		if ((tauQ.getSizeX() < minXY) || (tauQ.getSizeY() < 1))
			tauQ.resize(minXY, 1);
		if ((tauP.getSizeX() < minXY) || (tauP.getSizeY() < 1))
			tauP.resize(minXY, 1);
//		if (!upper) {
//			Matrix tmp = tauP;
//			tauP = tauQ;
//			tauQ = tmp;
//		}
		
		for (int atIndex = 0; atIndex < minXY; atIndex++) {
			// Generate elementary reflector H(i) to annihilate A(i+1:m,i)
			if (upper)
				DLARFG_Y(Src, atIndex, atIndex, Src.getSizeY() - atIndex, Src.getItem(atIndex, atIndex));
			else
				DLARFG_X(Src, atIndex, atIndex, Src.getSizeX() - atIndex, Src.getItem(atIndex, atIndex));
			tauQ.setItem(atIndex, 0, DLARFG_Tau);
			Src.setItem(atIndex, atIndex, DLARFG_Beta);

			System.out.println("atIndex = " + atIndex + " DLARFG_Beta = " + DLARFG_Beta + " DLARFG_Tau = " + DLARFG_Tau);

			// DGEQR2:109 Apply H(i) to A(i:m,i+1:n) from the left
			Src.setItem(atIndex, atIndex, 1.0);
			if (upper) 
				DLARF_X(Src, atIndex, DLARFG_Tau);
			else 
				DLARF_Y(Src, atIndex, DLARFG_Tau);
			Src.setItem(atIndex, atIndex, DLARFG_Beta);
			
			// DGEBD2:176
			if (atIndex >= minXY - 1) {
				tauP.setItem(atIndex, 0, 0.0); // DGEBD2:192
				continue;
			}
			
			// DGEBD2:178 Generate elementary reflector G(i) to annihilate A(i,i+2:n)
			if (upper)
				DLARFG_X(Src, atIndex + 1, atIndex, Src.getSizeX() - atIndex - 1, Src.getItem(atIndex + 1, atIndex));
			else
				DLARFG_Y(Src, atIndex, atIndex + 1, Src.getSizeY() - atIndex - 1, Src.getItem(atIndex, atIndex + 1));
			tauP.setItem(atIndex, 0, DLARFG_Tau);
			
			if (upper) {
				// DGEBD2:186 Apply G(i) to A(i+1:m,i+1:n) from the right
				Src.setItem(atIndex + 1, atIndex, 1.0);
//				DLARF_Y(Src, atIndex, DLARFG_Tau);
				for (int j = Src.getSizeY() - 1; j > atIndex; j--) {
					double sum = 0.0;
					for (int i = Src.getSizeX() - 1; i > atIndex; i--) 
						sum += Src.getItem(i, j) * Src.getItem(i, atIndex);
					for (int i = Src.getSizeX() - 1; i > atIndex; i--) 
						Src.setItem(i, j, Src.getItem(i, j) - DLARFG_Tau * sum * Src.getItem(i, atIndex));
				}
				Src.setItem(atIndex + 1, atIndex, DLARFG_Beta);
			} else {
				// DGEBD2:186 Apply G(i) to A(i+1:m,i+1:n) from the right
				Src.setItem(atIndex, atIndex + 1, 1.0);
				DLARF_X(Src, atIndex, DLARFG_Tau);
				Src.setItem(atIndex, atIndex + 1, DLARFG_Beta);
			}
		}
	}

	public static class DLARTG_Result {
		double CS, SN, r;
	}
	
	public DLARTG_Result DLARTG_1 = new DLARTG_Result();
	public DLARTG_Result DLARTG_2 = new DLARTG_Result();
	
	/**
	 * LAPACK: SUBROUTINE DLARTG( F, G, CS, SN, R )
	 * 
	 * DLARTG generate a plane rotation so that
	 * <br><tt>
	 * [  CS  SN  ]  .  [ F ]  =  [ R ]   where CS**2 + SN**2 = 1.<br>
	 * [ -SN  CS  ]     [ G ]     [ 0 ]<br>
	 * </tt>
	 */
	public static void DLARTG(double F, double G, DLARTG_Result Result) {
		Result.r = hypot(F, G);
		Result.CS = F / Result.r;
		Result.SN = G / Result.r;
		if ((Result.CS < 0.0) && (Math.abs(F) > Math.abs(G))) {
			Result.CS = -Result.CS;
			Result.SN = -Result.SN;
			Result.r = -Result.r;
		}
	}	
	
	/**
	 * SSMIN - abs(SSMIN) is the smaller singular value
	 * SSMAX - abs(SSMAX) is the larger singular value.
	 * SINL, COSL - The vector (CSL, SNL) is a unit left singular vector for the singular value abs(SSMAX).
	 * SINR, COSR - The vector (CSR, SNR) is a unit right singular vector for the singular value abs(SSMAX).
	 */
	public static class DLASV2_Result {
		double SSMIN, SSMAX, SINR, COSR, SINL, COSL;
	}
	
	public DLASV2_Result DLASV2_result = new DLASV2_Result();
	
	/**
	 * LAPACK: SUBROUTINE DLASV2( F, G, H, SSMIN, SSMAX, SNR, CSR, SNL, CSL )
	 * 
	 * DLASV2 computes the singular value decomposition of a 2-by-2 triangular matrix
	 * <br><tt>
	 * [  F   G  ]<br>
	 * [  0   H  ]<br>
	 * </tt>
	 * On return, abs(SSMAX) is the larger singular value, abs(SSMIN) is the
	 * smaller singular value, and (CSL,SNL) and (CSR,SNR) are the left and
	 * right singular vectors for abs(SSMAX), giving the decomposition
	 * <br><tt>
	 * [ CSL  SNL ] [  F   G  ] [ CSR -SNR ]  =  [ SSMAX   0   ]<br>
	 * [-SNL  CSL ] [  0   H  ] [ SNR  CSR ]     [  0    SSMIN ]<br>
	 * </tt>
	 * Barring over/underflow and assuming a guard digit in subtraction, all
	 * output quantities are correct to within a few units in the last place (ulps).
	 * 
	 * Overflow will not occur unless the largest singular value itself
	 * overflows or is within a few ulps of overflow. (On machines with
	 * partial overflow, like the Cray, overflow may occur if the largest
	 * singular value is within a factor of 2 of overflow.)
	 * 
	 * Underflow is harmless if underflow is gradual. Otherwise, results
	 * may correspond to a matrix modified by perturbations of size near
	 * the underflow threshold.
	 */
	public static void DLASV2(double F, double G, double H, DLASV2_Result result) {
		// DLASV2
		double FT = F;
		double GT = G;
		double HT = H;
		
		double FA = Math.abs(FT);
		double HA = Math.abs(HT);
		double GA = Math.abs(GT);
		
		int pmax = 1;
		boolean swap = HA > FA;
		
		if (swap) {
			pmax = 3;
			double tmp = FT;
			FT = HT;
			HT = tmp;
			tmp = FA;
			FA = HA;
			HA = tmp;
		}
		double CLT = 0, CRT = 0, SLT = 0, SRT = 0;
		if (GA == 0.0) {
			// DLASV:132 Diagonal matrix
			result.SSMIN = HA;
			result.SSMAX = FA;
			CRT = CLT = 1.0;
			SRT = SLT = 0.0;
		} else {
			boolean gasmal = true;
			if (GA > FA) {
				pmax = 2;
				if ((FA / GA) < EPS) {  
					// DLASV2:146 Case of very large GA
					gasmal = false;
					result.SSMIN = HA > 1.0 ? FA / (GA / HA) : (FA / GA) * HA;
					result.SSMAX = GA;
					SRT = CLT = 1.0;
					SLT = HT / GT;
					CRT = FT / GT;								
				}
			}
			if (gasmal) {
				// DLASV2:163 Normal case
				double tmpD = FA - HA;
				double L;
				if (tmpD == FA)
					// DLASV2:168 Copes with infinite F or H
					L = 1.0;
				else
					L = tmpD / FA;  
				double M = GT / FT;
				double T = 2.0 - L;
				double MM = M * M;
				double TT = T * T;
				double _S = Math.sqrt(TT + MM);
				double _R = L == 0.0 ? Math.abs(M) : Math.sqrt(L * L + MM);
				double A = (_S + _R) * 0.5;
				result.SSMIN = HA / A;
				result.SSMAX = FA * A;
				if (MM == 0.0) {
					// DLASV:207 Note that M is very tiny
					if (L == 0.0)
						T = SIGN(2.0, FT) * SIGN(1.0, GT);
					else
						T = GT / SIGN(tmpD, FT) + M / T;
				} else
					T = (M / (_S + T) + M / (_R + L)) * (1.0 + A);
				L = Math.sqrt(T * T + 4.0);
				CRT = 2.0 / L;
				SRT = T / L;
				CLT = (CRT + SRT * M) / A;
				SLT = (HT / FT ) * SRT / A;
			}
		}
		if (swap) {
			result.COSL = SRT;
			result.SINL = CRT;
			result.COSR = SLT;
			result.SINR = CLT;
		} else {
			result.COSL = CLT;
			result.SINL = SLT;
			result.COSR = CRT;
			result.SINR = SRT;
		};
		// DLASV2:236 Correct signs of SSMAX and SSMIN
		double tsign = 0;
		if (pmax == 1)
			tsign = SIGN(1.0, result.COSR) * SIGN(1.0, result.COSL) * SIGN(1.0, F);
		if (pmax == 2)
			tsign = SIGN(1.0, result.SINR) * SIGN(1.0, result.COSL) * SIGN(1.0, G);
		if (pmax == 3)
			tsign = SIGN(1.0, result.SINR) * SIGN(1.0, result.SINL) * SIGN(1.0, H);
		result.SSMAX = SIGN(result.SSMAX, tsign);
		result.SSMIN = SIGN(result.SSMIN, tsign * SIGN(1.0, F) * SIGN(1.0, H));
		// End DLASV2
	}
	
	/**
	 * LAPACK: SUBROUTINE DBDSQR( UPLO, N, NCVT, NRU, NCC, D, E, VT, LDVT, U, LDU, C, LDC, WORK, INFO )
	 * 
	 * DBDSQR computes the singular value decomposition (SVD) of a real
	 * N-by-N (upper or lower) bidiagonal matrix B:  B = Q * S * P' (P'
	 * denotes the transpose of P), where S is a diagonal matrix with
	 * non-negative diagonal elements (the singular values of B), and Q
	 * and P are orthogonal matrices.
	 * 
	 * The routine computes S, and optionally computes U * Q, P' * VT,
	 * or Q' * C, for given real input matrices U, VT, and C.
	 * 
	 * See "Computing  Small Singular Values of Bidiagonal Matrices With
	 * Guaranteed High Relative Accuracy," by J. Demmel and W. Kahan,
	 * LAPACK Working Note #3 (or SIAM J. Sci. Statist. Comput. vol. 11,
	 * no. 5, pp. 873-912, Sept 1990) and
	 * "Accurate singular values and differential qd algorithms," by
	 * B. Parlett and V. Fernando, Technical Report CPAM-554, Mathematics
	 * Department, University of California at Berkeley, July 1992
	 * for a detailed description of the algorithm.
	 */
	public void DBDSQR(Matrix U, Matrix s, Matrix V, Matrix E, Matrix work) {
		// DBDSQR
		// Perform bidiagonal QR iteration, computing left singular vectors 
		// of R in WORK(IU) and computing right singular vectors of R in VT
		
		// DBDSQR:258 Compute approximate maximum, minimum singular values
		int atIndex = Math.min(U.getSizeX(), V.getSizeY()) - 1;
		int maxit = 6 * atIndex * atIndex;
		int iter = maxit;
		double TOL = 1.0958067E-014; // ?????
		double threshold = 2.59371421E-015;
		double EPS = 1.11022302E-016;
		
		int old_atIndex = -1;
		int old_ll = -1;
		double SMINL = 0.0;
		
		work.resize(V.getSizeX(), 4);
		int idir = 0;
		
		while ((atIndex > 0) && (iter >= 0)) {
			// DBDSQR:316 Find diagonal block of matrix to work on
			double smax = Math.abs(s.getItem(atIndex, atIndex));
			double smin = smax;
			int ll = atIndex - 1;
			boolean found_it = false; 
			while (ll >= 0) {
				double abss = Math.abs(s.getItem(ll, ll));
				double abse = Math.abs(E.getItem(ll, 0));
				if (abse <= threshold) {
					E.setItem(ll, 0, 0.0);
					// Matrix splits since E(LL) = 0
					if (ll == atIndex - 1) {
						// Convergence of bottom singular value, return to top of loop
						atIndex--;
						found_it = true;
					}
					break;
				}
				smin = Math.min(smin, abss);
				smax = Math.max(smax, Math.max(abss, abse));
				ll--;
			}
			if (found_it)
				continue;
			ll++;
			// E(LL) through E(M-1) are nonzero, E(LL-1) is zero
			if (ll == atIndex - 1) {
				// DBDSQR:354 2 by 2 block, handle separately
				DLASV2(s.getItem(atIndex - 1, atIndex - 1), E.getItem(atIndex - 1, 0), s.getItem(atIndex, atIndex), DLASV2_result);
				s.setItem(atIndex, atIndex, DLASV2_result.SSMIN);
				s.setItem(atIndex - 1, atIndex - 1, DLASV2_result.SSMAX);
				
				E.setItem(atIndex - 1, 0, 0.0);
				
				// DBDSQR:362 Compute singular vectors
				for (int i = V.getSizeX() - 1; i >= 0; i--) {
					double tmp = DLASV2_result.COSR * V.getItem(i, atIndex - 1) + DLASV2_result.SINR * V.getItem(i, atIndex);
					V.setItem(i, atIndex, DLASV2_result.COSR * V.getItem(i, atIndex) - DLASV2_result.SINR * V.getItem(i, atIndex - 1));
					V.setItem(i, atIndex - 1, tmp);
				}
				for (int j = U.getSizeY() - 1; j >= 0; j--) {
					double tmp = DLASV2_result.COSL * U.getItem(atIndex - 1, j) + DLASV2_result.SINL * U.getItem(atIndex, j);
					U.setItem(atIndex, j, DLASV2_result.COSL * U.getItem(atIndex, j) - DLASV2_result.SINL * U.getItem(atIndex - 1, j));
					U.setItem(atIndex - 1, j, tmp);
				}
				atIndex -= 2;
				continue;
			}
			
			// DBDSQR:376 If working on new submatrix, choose shift direction (from larger end diagonal element towards smaller)
			if ((ll > old_atIndex) || (atIndex < old_ll)) {
				if (Math.abs(s.getItem(ll, ll)) >= Math.abs(s.getItem(atIndex, atIndex)))
					// DBDSQR:382 Chase bulge from top (big end) to bottom (small end)
					idir = 1;
				else
					// DBDSQR:387 Chase bulge from bottom (big end) to top (small end)
					idir = 2;
			}
			System.out.println("IDIR=" + idir);
			// DBDSQR:393 Apply convergence tests
			if (idir == 1) {
				// DBDSQR:397 Run convergence test in forward direction. First apply standard test to bottom of matrix
				if ((Math.abs(E.getItem(atIndex - 1, 0)) <= Math.abs(TOL * s.getItem(atIndex, atIndex))) ||
					((TOL < 0.0) && (Math.abs(E.getItem(atIndex - 1, 0)) < threshold ))) {
					E.setItem(atIndex - 1, 0, 0.0);
					continue;
				}
				if (TOL >= 0.0) {
					// DBDSQR:408 If relative accuracy desired, apply convergence criterion forward
					double MU = Math.abs(s.getItem(ll, ll));
					SMINL = MU;
					boolean found = false;
					for (int lll = ll; lll < atIndex; lll++) {
						if (Math.abs(E.getItem(lll, 0)) <= (TOL * MU)) {
							E.setItem(lll, 0, 0.0);
							found = true;
							break;
						}
						MU = Math.abs(s.getItem(lll + 1, lll + 1)) * (MU / (MU + Math.abs(E.getItem(lll, 0))));
						SMINL = Math.min(SMINL, MU);
					}
					if (found)
						continue;					
				}
			} else {
				// DBDSQR:426 Run convergence test in backward direction First apply standard test to top of matrix
				if ((Math.abs(E.getItem(ll, 0)) <= Math.abs(TOL * s.getItem(ll, ll))) || 
					((TOL < 0.0) && (Math.abs(E.getItem(ll, 0)) <= threshold))) {   
					E.setItem(ll, 0, 0.0);
					continue;
				}
				if (TOL > 0.0) {
					// DBDSQR:437 If relative accuracy desired, apply convergence criterion backward
					double MU = Math.abs(s.getItem(atIndex, atIndex));
					SMINL = MU;
					boolean found = false;
					for (int lll = atIndex - 1; lll >= ll; lll--) {
						if (Math.abs(E.getItem(lll, 0)) <= (TOL * MU)) {
							E.setItem(lll, 0, 0.0);
							found = true;
							break;
						}
						MU = Math.abs(s.getItem(lll, lll)) * (MU / (MU + Math.abs(E.getItem(lll, 0))));
						SMINL = Math.min(SMINL, MU);
					}
					if (found)
						continue;					
				}
			}
			old_ll = ll;
			old_atIndex = atIndex;
			
			double shift = 0.0;
			
			// DBDQR:456 Compute shift.  First, test if shifting would ruin relative accuracy, and if so set the shift to zero.
			if ((TOL >= 0.0) && (V.getSizeX() * TOL * (SMINL / smax) <= Math.max(EPS, TOL * 0.01))) { 
				// DBDSQR:462 Use a zero shift to avoid loss of relative accuracy
				shift = 0.0;
			} else {
				// DBDSQR:467 Compute the shift from 2-by-2 block at end of matrix
				double sll, FA, GA, HA;				
				if (idir == 1) {
					sll = Math.abs(s.getItem(ll, ll));
					FA = Math.abs(s.getItem(atIndex - 1, atIndex - 1));
					GA = Math.abs(E.getItem(atIndex - 1, 0));
					HA = Math.abs(s.getItem(atIndex, atIndex));
				} else {
					sll = Math.abs(s.getItem(atIndex, atIndex));
					FA = Math.abs(s.getItem(ll, ll));
					GA = Math.abs(E.getItem(ll, 0));
					HA = Math.abs(s.getItem(ll + 1, ll + 1));
				}
				
				// DLAS2:15 computes the singular values of the 2-by-2 matrix
				// [  F   G  ]
				// [  0   H  ]
				if (FA < HA) {
					// FA => FHMX; HA => FHMN
					double tmp = FA;
					FA = HA;
					HA = tmp;
				}
				System.out.println("HA=" + HA);
				if (HA == 0.0) {
					shift = 0.0;
				} else {
					if (GA < FA) {
						double AS = 1.0 + HA / FA;
						double AT = (FA - HA) / FA;
						double AU = Math.pow((GA / FA), 2.0);
						double C = 2.0 / (Math.sqrt(AS * AS + AU) + Math.sqrt(AT * AT + AU));
						shift = HA * C;
					} else {
						double AU = FA / GA;
						if (AU == 0.0) {
							// DLAS2:101 Avoid possible harmful underflow if exponent range
							// asymmetric (true SSMIN may not underflow even if AU underflows)
							shift = (FA + HA) / GA;
						} else {
							double AS = 1.0 + HA / FA;
							double AT = (FA - HA) / FA;
							double C = 1.0 / (Math.sqrt(1.0 + Math.pow(AS * AU, 2.0)) + Math.sqrt(1.0 + Math.pow(AT * AU, 2.0)));
							shift = 2.0 * (HA * C * AU);
						}
					}
				}
				// End of DLAS2
				
				// DBDSQR:477 Test if shift negligible, and if so set to zero
				if ((sll > 0.0) && (Math.pow(shift / sll, 2.0) < EPS))
					shift = 0.0;
			}
			
			// DBDSQR:485 Increment iteration count
			iter -= atIndex - ll;
			// DBDSQR:489 If SHIFT = 0, do simplified QR iteration
//			System.out.println("SHIFT=" + shift);
			
			if (shift == 0.0) {
				if (idir == 1) {
					// DBDSQR:570 Use nonzero shift
					if (idir == 1) {
						System.out.println("use path 01");
						// DBDSQR:494 Chase bulge from top to bottom
						// Save cosines and sines for later singular vector updates
						DLARTG_1.CS = 1.0;
						DLARTG_2.CS = 1.0;
						DLARTG_2.SN = 0.0;

						for (int lll = ll; lll < atIndex; lll++) {
							DLARTG(s.getItem(lll, lll) * DLARTG_1.CS, E.getItem(lll, 0), DLARTG_1); 
							if (lll > ll)
								E.setItem(lll - 1, 0, DLARTG_2.SN * DLARTG_1.r);
							
							DLARTG(DLARTG_2.CS * DLARTG_1.r, s.getItem(lll + 1, lll + 1) * DLARTG_1.SN, DLARTG_2);							
							s.setItem(lll, lll, DLARTG_2.r);

							work.setItem(lll, 0, DLARTG_1.CS);
							work.setItem(lll, 1, DLARTG_1.SN);
							work.setItem(lll, 2, DLARTG_2.CS);
							work.setItem(lll, 3, DLARTG_2.SN);
						}
						double H = s.getItem(atIndex, atIndex) * DLARTG_1.CS;
						s.setItem(atIndex, atIndex, H * DLARTG_2.CS);
						E.setItem(atIndex - 1, 0, H * DLARTG_2.SN);
						// DBDSQR:513 Update singular vectors
						for (int lll = ll; lll < atIndex; lll++) {
							DLARTG_1.CS = work.getItem(lll, 0);
							DLARTG_1.SN = work.getItem(lll, 1);
							DLARTG_2.CS = work.getItem(lll, 2);
							DLARTG_2.SN = work.getItem(lll, 3);
							
							if ((DLARTG_1.CS != 1.0) || (DLARTG_1.SN != 0.0)) 
								for (int indx = 0; indx < V.getSizeX(); indx++) {
									double tmp1 = V.getItem(indx, lll + 1);
									double tmp2 = V.getItem(indx, lll);
									V.setItem(indx, lll + 1, DLARTG_1.CS * tmp1 - DLARTG_1.SN * tmp2);
									V.setItem(indx, lll,     DLARTG_1.SN * tmp1 + DLARTG_1.CS * tmp2);
								}
							if ((DLARTG_2.CS != 1.0) || (DLARTG_2.SN != 0.0)) 
								for (int indy = 0; indy < U.getSizeY(); indy++) {
									double tmp1 = U.getItem(lll + 1, indy);
									double tmp2 = U.getItem(lll, indy);
									U.setItem(lll + 1, indy, DLARTG_2.CS * tmp1 - DLARTG_2.SN * tmp2);
									U.setItem(lll, indy,     DLARTG_2.SN * tmp1 + DLARTG_2.CS * tmp2);
								}
						}
						// DBDSQR:525 Test convergence
						if (Math.abs(E.getItem(atIndex - 1, 0)) < threshold)
							E.setItem(atIndex - 1, 0, 0.0);
					}
				} else {
					System.out.println("use path 02");
					// DBDSQR:532 Chase bulge from bottom to top
					// Save cosines and sines for later singular vector updates
					DLARTG_1.CS = 1.0;
					DLARTG_2.CS = 1.0;
					DLARTG_2.SN = 0.0;
					
					for (int lll = atIndex; lll > ll; lll--) {
						DLARTG(s.getItem(lll, lll) * DLARTG_1.CS, E.getItem(lll - 1, 0), DLARTG_1);
						if (lll < atIndex)
							E.setItem(lll, 0, DLARTG_2.SN * DLARTG_1.r);

						DLARTG(DLARTG_2.CS * DLARTG_1.r, s.getItem(lll - 1, lll - 1) * DLARTG_1.SN, DLARTG_2);
						s.setItem(lll, lll, DLARTG_2.r);

						work.setItem(lll, 0, DLARTG_1.CS);
						work.setItem(lll, 1, -DLARTG_1.SN);
						work.setItem(lll, 2, DLARTG_2.CS);
						work.setItem(lll, 3, -DLARTG_2.SN);
					}
					double H = s.getItem(ll, ll) * DLARTG_1.CS;
					s.setItem(ll, ll, H * DLARTG_2.CS);
					E.setItem(ll, 0, H * DLARTG_2.SN);
					// DBDSQR:551 Update singular vectors
					for (int lll = ll; lll < atIndex; lll++) {
						DLARTG_1.CS = work.getItem(lll, 0);
						DLARTG_1.SN = work.getItem(lll, 1);
						DLARTG_2.CS = work.getItem(lll, 2);
						DLARTG_2.SN = work.getItem(lll, 3);
						
						if ((DLARTG_1.CS != 1.0) || (DLARTG_1.SN != 0.0)) 
							for (int indx = V.getSizeX() - 1; indx >= 0; indx--) {
								double tmp1 = V.getItem(indx, lll + 1);
								double tmp2 = V.getItem(indx, lll);
								V.setItem(indx, lll + 1, DLARTG_1.CS * tmp1 - DLARTG_1.SN * tmp2);
								V.setItem(indx, lll,     DLARTG_1.SN * tmp1 + DLARTG_1.CS * tmp2);
							}
						if ((DLARTG_2.CS != 1.0) || (DLARTG_2.SN != 0.0)) 
							for (int indy = U.getSizeY() - 1; indy >= 0; indy--) {
								double tmp1 = U.getItem(lll + 1, indy);
								double tmp2 = U.getItem(lll, indy);
								U.setItem(lll + 1, indy, DLARTG_2.CS * tmp1 - DLARTG_2.SN * tmp2);
								U.setItem(lll, indy,     DLARTG_2.SN * tmp1 + DLARTG_2.CS * tmp2);
							}
					}
					// DBDSQR:563 Test convergence
					if (Math.abs(E.getItem(ll, 0)) < threshold)
						E.setItem(ll, 0, 0.0);
				}
			} else {
				// DBDSQR:570 Use nonzero shift
				if (idir == 1) {
					System.out.println("use path 11");
					// DBDSQR:574 Chase bulge from top to bottom
					// Save cosines and sines for later singular vector updates
					double F = (Math.abs(s.getItem(ll, ll)) - shift) *
						(SIGN(1.0, s.getItem(ll, ll)) + shift / s.getItem(ll, ll));
					double G = E.getItem(ll, 0);
					DLARTG_1.CS = 1.0;
					DLARTG_2.CS = 1.0;
					DLARTG_2.SN = 0.0;

					for (int lll = ll; lll < atIndex; lll++) {
						DLARTG(F, G, DLARTG_1);
						if (lll > ll)
							E.setItem(lll - 1, 0, DLARTG_1.r);
						
						F = DLARTG_1.CS * s.getItem(lll, lll) + DLARTG_1.SN * E.getItem(lll, 0);
						E.setItem(lll, 0, DLARTG_1.CS * E.getItem(lll, 0) - DLARTG_1.SN * s.getItem(lll, lll));
						G = s.getItem(lll + 1, lll + 1) * DLARTG_1.SN;
						s.setItem(lll + 1, lll + 1, DLARTG_1.CS * s.getItem(lll + 1, lll + 1));
						
						DLARTG(F, G, DLARTG_2);
						s.setItem(lll, lll, DLARTG_2.r);

						F = DLARTG_2.CS * E.getItem(lll, 0) + DLARTG_2.SN * s.getItem(lll + 1, lll + 1);
						s.setItem(lll + 1, lll + 1, DLARTG_2.CS * s.getItem(lll + 1, lll + 1) - DLARTG_2.SN * E.getItem(lll, 0));
						if (lll < atIndex - 1) {
							G = DLARTG_2.SN * E.getItem(lll + 1, 0); 
							E.setItem(lll + 1, 0, DLARTG_2.CS * E.getItem(lll + 1, 0));
						}
						work.setItem(lll, 0, DLARTG_1.CS);
						work.setItem(lll, 1, DLARTG_1.SN);
						work.setItem(lll, 2, DLARTG_2.CS);
						work.setItem(lll, 3, DLARTG_2.SN);
					}
					E.setItem(atIndex - 1, 0, F);
					// DBDSQR:603 Update singular vectors
					for (int lll = ll; lll < atIndex; lll++) {
						DLARTG_1.CS = work.getItem(lll, 0);
						DLARTG_1.SN = work.getItem(lll, 1);
						DLARTG_2.CS = work.getItem(lll, 2);
						DLARTG_2.SN = work.getItem(lll, 3);
						
						if ((DLARTG_1.CS != 1.0) || (DLARTG_1.SN != 0.0)) 
							for (int indx = 0; indx < V.getSizeX(); indx++) {
								double tmp1 = V.getItem(indx, lll + 1);
								double tmp2 = V.getItem(indx, lll);
								V.setItem(indx, lll + 1, DLARTG_1.CS * tmp1 - DLARTG_1.SN * tmp2);
								V.setItem(indx, lll,     DLARTG_1.SN * tmp1 + DLARTG_1.CS * tmp2);
							}
						if ((DLARTG_2.CS != 1.0) || (DLARTG_2.SN != 0.0)) 
							for (int indy = 0; indy < U.getSizeY(); indy++) {
								double tmp1 = U.getItem(lll + 1, indy);
								double tmp2 = U.getItem(lll, indy);
								U.setItem(lll + 1, indy, DLARTG_2.CS * tmp1 - DLARTG_2.SN * tmp2);
								U.setItem(lll, indy,     DLARTG_2.SN * tmp1 + DLARTG_2.CS * tmp2);
							}
					}
					// DBDSQR:615 Test convergence
					if (Math.abs(E.getItem(atIndex - 1, 0)) < threshold)
						E.setItem(atIndex - 1, 0, 0.0);
				} else {
					System.out.println("use path 12");
					// DBDSQR:622 Chase bulge from bottom to top
					// Save cosines and sines for later singular vector updates
					double F = (Math.abs(s.getItem(atIndex, atIndex)) - shift) *
						(SIGN(1.0, s.getItem(atIndex, atIndex)) + shift / s.getItem(atIndex, atIndex));
					double G = E.getItem(atIndex - 1, 0);
					
					DLARTG_1.CS = 1.0;
					DLARTG_2.CS = 1.0;
					DLARTG_2.SN = 0.0;
					
					for (int lll = atIndex; lll > ll; lll--) {
						DLARTG(F, G, DLARTG_1);
						if (lll < atIndex)
							E.setItem(lll, 0, DLARTG_1.r);
						
						F = DLARTG_1.CS * s.getItem(lll, lll) + DLARTG_1.SN * E.getItem(lll - 1, 0);
						E.setItem(lll - 1, 0, DLARTG_1.CS * E.getItem(lll - 1, 0) - DLARTG_1.SN * s.getItem(lll, lll));
						G = s.getItem(lll - 1, lll - 1) * DLARTG_1.SN;
						s.setItem(lll - 1, lll - 1, DLARTG_1.CS * s.getItem(lll - 1, lll - 1));

						DLARTG(F, G, DLARTG_2);
						s.setItem(lll, lll, DLARTG_2.r);

						F = DLARTG_2.CS * E.getItem(lll - 1, 0) + DLARTG_2.SN * s.getItem(lll - 1, lll - 1);
						s.setItem(lll - 1, lll - 1, DLARTG_2.CS * s.getItem(lll - 1, lll - 1) - DLARTG_2.SN * E.getItem(lll - 1, 0));
						if (lll > ll + 1) {
							G = DLARTG_2.SN * E.getItem(lll - 2, 0); 
							E.setItem(lll - 2, 0, DLARTG_2.CS * E.getItem(lll - 2, 0));
						}
						work.setItem(lll, 0, DLARTG_1.CS);
						work.setItem(lll, 1, -DLARTG_1.SN);
						work.setItem(lll, 2, DLARTG_2.CS);
						work.setItem(lll, 3, -DLARTG_2.SN);
					}
					E.setItem(ll, 0, F);
					// DBDSQR:656 Update singular vectors
					
					for (int lll = atIndex; lll > ll; lll--) {
						DLARTG_1.CS = work.getItem(lll, 0);
						DLARTG_1.SN = work.getItem(lll, 1);
						DLARTG_2.CS = work.getItem(lll, 2);
						DLARTG_2.SN = work.getItem(lll, 3);
	
						if ((DLARTG_2.CS != 1.0) || (DLARTG_2.SN != 0.0))
							for (int indx = V.getSizeX() - 1; indx >= 0; indx--) {
								double tmp1 = V.getItem(indx, lll);
								double tmp2 = V.getItem(indx, lll - 1);
								V.setItem(indx, lll,     DLARTG_2.CS * tmp1 - DLARTG_2.SN * tmp2);
								V.setItem(indx, lll - 1, DLARTG_2.SN * tmp1 + DLARTG_2.CS * tmp2);
							}
						if ((DLARTG_1.CS != 1.0) || (DLARTG_1.SN != 0.0)) 
							for (int indy = 0; indy < U.getSizeY(); indy++) {
								double tmp1 = U.getItem(lll, indy);
								double tmp2 = U.getItem(lll - 1, indy);
								U.setItem(lll, indy,     DLARTG_1.CS * tmp1 - DLARTG_1.SN * tmp2);
								U.setItem(lll - 1, indy, DLARTG_1.SN * tmp1 + DLARTG_1.CS * tmp2);
							}
					}
					// DBDSQR:615 Test convergence
					if (Math.abs(E.getItem(ll, 0)) < threshold)
						E.setItem(ll, 0, 0.0);
				}
			}
			// DBDSQR:670 QR iteration finished, go back and check convergence
		}

		if ((atIndex <= 0) && (iter >= 0)) 
			svdSortResult(U, s, V);
		else 
			// DBDSQR:720 Maximum number of iterations exceeded, failure to converge
			throw new Error("Maximum number of iterations exceeded, failure to converge");
	}
	
	public static void svdSortResult(Matrix U, Matrix s, Matrix V) {
		// Check data
//		if ((U.getSizeX() != U.getSizeY()) ||		// TODO: Enable this check
//			(V.getSizeX() != V.getSizeY()) ||
//			(V.getSizeX() != s.getSizeX()) ||
//			(U.getSizeY() != s.getSizeY()) )
//			throw new Error("Invalid arguments");
		int minXY = Math.min(U.getSizeX(), V.getSizeX());
		// Make all singular values positive
		for (int j = 0; j < minXY; j++) {
			if (s.getItem(j, j) < 0.0) {
				s.setItem(j, j, -s.getItem(j, j));
				for (int i = V.getSizeX() - 1; i >= 0; i--)
					V.setItem(i, j, -V.getItem(i, j));
			}
		}
		// Sort the singular values into decreasing order (insertion sort on
		// singular values, but only one transposition per singular vector)
		for (int j = 0; j < minXY; j++) {
			// Scan for smallest D(I)
			int minJindex = j;
			double minJvalue = s.getItem(j, j);
			for (int i = j + 1; i < minXY; i++) {
				if (s.getItem(i, i) > minJvalue) {
					minJindex = i;
					minJvalue = s.getItem(i, i);						
				}				
			}
			if (minJindex != j) {
				// Swap singular values and vectors
				double tmp = s.getItem(j, j);
				s.setItem(j, j, s.getItem(minJindex, minJindex));
				s.setItem(minJindex, minJindex, tmp);
				for (int i = V.getSizeX() - 1; i >= 0; i--) {
					tmp = V.getItem(i, j);
					V.setItem(i, j, V.getItem(i, minJindex));
					V.setItem(i, minJindex, tmp);
				}
				for (int i = U.getSizeX() - 1; i >= 0; i--) {   
					tmp = U.getItem(j, i);
					U.setItem(j, i, U.getItem(minJindex, i));
					U.setItem(minJindex, i, tmp);
				}
			}
		}
	}

	public void mysvd(Matrix Src, Matrix U, Matrix V, Matrix s) {
		Src.printM("A");
		U.resize(Src.getSizeY(), Src.getSizeY());
		s.resize(Src.getSizeX(), Src.getSizeY());
		V.resize(Src.getSizeX(), Src.getSizeX());
		s.make0();
		int minXY = Math.min(Src.getSizeX(), Src.getSizeY());

		Matrix RL = new Matrix(Src.getSizeX(), Src.getSizeY());
		Matrix tauP = new Matrix(minXY, 1);
		Matrix tauQ = new Matrix(Src.getSizeX(), 1);
		Matrix E = new Matrix(minXY - 1, 1);
		Matrix Q = new Matrix(Src.getSizeY(), Src.getSizeY());
		Matrix work = new Matrix();

		Matrix tmpV;
		Matrix tmpU;
		Matrix tmpQ;
		
		boolean upper = Src.getSizeX() <= Src.getSizeY();

		if (upper) {
			Src.qrDecomposition(tauP);
			Src.qrDecomositionGetQ(tauP, U);
			Src.qrDecomositionGetR(RL);
			tmpU = U;
			tmpV = V;
			tmpQ = Q;
		} else {
			Src.lqDecomposition(tauP);
			Src.lqDecomositionGetQ(tauP, V);
			Src.lqDecomositionGetL(RL);
			tmpU = V;
			tmpV = Q;
			tmpQ = U;
		}

		// DGESVD:1769 Bidiagonalize R in WORK(IU), copying result to VT
		DGEBRD(RL, tauP, tauQ);
		for (int i = minXY - 1; i >= 0; i--)
			s.setItem(i, i, RL.getItem(i, i));
		for (int i = minXY - 2; i >= 0; i--)
			E.setItem(i, 0, RL.getItem(i + 1, i));
		
		// DGESVD:1807
		for (int i = tmpV.getSizeX() - 1; i >= 0; i--)
			for (int j = i; j >= 0; j--)
				tmpV.setItem(i, j, RL.getItem(i, j));
		
		// DGESVD:1817 Generate left bidiagonalizing vectors in WORK(IU)
		RL.qrDecomositionGetQ(tauQ, tmpQ);
		
		// DORGBR:217 Shift the vectors which define the elementary reflectors one
		// row downward, and set the first row and column of P' to those of the unit matrix
		for (int j = tmpV.getSizeY() - 1; j > 0; j--) {
			for (int i = j - 1; i > 0; i--)
				tmpV.setItem(j, i, tmpV.getItem(j, i - 1));
			tmpV.setItem(0, j, 0.0);
			tmpV.setItem(j, 0, 0.0);
		}
		tmpV.setItem(0, 0, 1.0);

		// DORGL2
		for (int atIndex = tmpV.getSizeX() - 1; atIndex > 0; atIndex--) {
			double tmp_tau = tauP.getItem(atIndex - 1, 0);
			if (atIndex < tmpV.getSizeX() - 1) {
				// Apply H(i) to A(i:m,i:n) from the right
				tmpV.setItem(atIndex, atIndex, 1.0);
				DLARF_Y(tmpV, atIndex, tmp_tau);
				for (int i = tmpV.getSizeX() - 1; i > atIndex; i--) 
					tmpV.setItem(i, atIndex, -tmp_tau * tmpV.getItem(i, atIndex));
			}
			tmpV.setItem(atIndex, atIndex, 1.0 - tmp_tau);
			// DORGL2:124 Set A(i,1:i-1) to zero 
			for (int i = atIndex - 1; i > 0; i--)
				tmpV.setItem(i, atIndex, 0.0);
		}
		// End DORGL2
		DBDSQR(tmpQ, s, tmpV, E, work);

		if (upper) {
			// DGESVD:1850 Multiply Q in U by left singular vectors of R in WORK(IU), storing result in A
			for (int i = Src.getSizeX() - 1; i >= 0; i--) {
				for (int j = Src.getSizeY() - 1; j >= 0; j--) {
					double sum = 0.0;	
					for (int k = Src.getSizeY() - 1; k >= 0; k--) {
						double a = tmpU.getItem(k, j);
						double b = tmpQ.getItem(i, k);
						sum += a * b;
					}
					Src.setItem(i, j, sum);
				}
			}
		} else {
			// DGESVD:3246 Multiply right singular vectors of L in WORK(IU) by Q in VT, storing result in A 
			for (int i = Src.getSizeX() - 1; i >= 0; i--) {
				for (int j = Src.getSizeY() - 1; j >= 0; j--) {
					double sum = 0.0;	
					for (int k = Src.getSizeY() - 1; k >= 0; k--) {
						double a = tmpV.getItem(k, j);
						double b = tmpU.getItem(i, k);
						sum += a * b;
					}
					Src.setItem(i, j, sum);
				}
			}
		}

		for (int i = Src.getSizeX() - 1; i >= 0; i--) 
			for (int j = Src.getSizeY() - 1; j >= 0; j--) 
				tmpU.setItem(i, j, Src.getItem(i, j));
	}

	public static class DLANV2_Result {
		double AA, BB, CC, DD, RT1R, RT1I, RT2R, RT2I, CS, SN;
	}
	
	public DLANV2_Result DLANV2_result = new DLANV2_Result();
	
	/**
	 * LAPACK: SUBROUTINE DLANV2( A, B, C, D, RT1R, RT1I, RT2R, RT2I, CS, SN )
	 * 
	 * DLANV2 computes the Schur factorization of a real 2-by-2 nonsymmetric
	 * matrix in standard form:
	 * <br><tt>
	 * [ A  B ] = [ CS -SN ] [ AA  BB ] [ CS  SN ]<br>
	 * [ C  D ]   [ SN  CS ] [ CC  DD ] [-SN  CS ]<br>
	 * </tt>
	 * where either<br>
	 * 1) CC = 0 so that AA and DD are real eigenvalues of the matrix, or<br>
	 * 2) AA = DD and BB*CC < 0, so that AA + or - sqrt(BB*CC) are complex
	 * conjugate eigenvalues.
	 */
	public static void DLANV2(double A, double B, double C, double D, DLANV2_Result result) {
		if (C == 0.0) {
			result.CS = 1.0;
			result.SN = 0.0;
		} else if (B == 0.0) {
			// Swap rows and columns
			result.CS = 0.0;
			result.SN = 1.0;
			double temp = D;
			D = A;
			A = temp;
			B = -C;
			C = 0.0;
		} else if ( (A-D == 0.0) && (SIGN(1.0, B) != SIGN(1.0, C)) ) {
			result.CS = 1.0;
			result.SN = 0.0;
		} else {
			double temp = A - D;
			double p = temp * 0.5;
			double BCmax = Math.max(Math.abs(B), Math.abs(C));
			double BCmin = Math.min(Math.abs(B), Math.abs(C)) * SIGN(1.0, B) * SIGN(1.0, C);
			double scale = Math.max(Math.abs(p), BCmax);
			double z = (p / scale) * p + (BCmax / scale) * BCmin; 
			// If Z is of the order of the machine accuracy, postpone the
			// decision on the nature of eigenvalues
			if (z >= 4.0 * EPS) {
				// Real eigenvalues. Compute A and D.
				z = p + SIGN( Math.sqrt(scale) * Math.sqrt(z), p);
				A = D + z;
				D = D - (BCmax / z) * BCmin;
				// Compute B and the rotation matrix
				double tau = hypot(C, z);
				result.CS = z / tau;
				result.SN = C / tau;
				B = B - C;
				C = 0.0;
			} else {
				// Complex eigenvalues, or real (almost) equal eigenvalues.
				// Make diagonal elements equal.
				double sigma = B + C;
				double tau = hypot(sigma, temp);
				result.CS = Math.sqrt(0.5 * (1.0 + Math.abs(sigma) / tau) );
				result.SN = - (p / (tau * result.CS)) * SIGN(1.0, sigma);
				
				// Compute [ AA  BB ] = [ A  B ] [ CS -SN ]
				//         [ CC  DD ]   [ C  D ] [ SN  CS ]
				result.AA =  A * result.CS + B * result.SN;
				result.BB = -A * result.SN + B * result.CS;
				result.CC =  C * result.CS + D * result.SN;
				result.DD = -C * result.SN + D * result.CS;
				// Compute [ A  B ] = [ CS  SN ] [ AA  BB ]
				//         [ C  D ]   [-SN  CS ] [ CC  DD ]
				A =  result.AA * result.CS + result.CC * result.SN;
				B =  result.BB * result.CS + result.DD * result.SN;
				C = -result.AA * result.SN + result.CC * result.CS;
				D = -result.BB * result.SN + result.DD * result.CS;
				
				temp = (A + D) * 0.5;
				A = temp;
				D = temp;
				if (C != 0.0) {
					if (B != 0.0) {
						if (SIGN(1.0, B) == SIGN(1.0, C)) {
							// Real eigenvalues: reduce to upper triangular form
							double SAB = Math.sqrt(Math.abs(B));
							double SAC = Math.sqrt(Math.abs(C));
							p = SIGN(SAB * SAC, C);
							tau = 1.0 / Math.sqrt(Math.abs(B + C));
							A = temp + p;
							D = temp - p;
							B = B - C;
							C = 0.0;
							double CS1 = SAB * tau;
							double SN1 = SAC * tau;
							temp = result.CS * CS1 - result.SN * CS1;
							result.SN = result.CS * SN1 + result.SN * CS1;
							result.CS = temp;
						}
					} else {
						B = -C;
						C = 0.0;
						temp = result.CS;
						result.CS = -result.SN;
						result.SN = temp;
					}
				}
			}
		}

		result.AA = A;
		result.BB = B;
		result.CC = C;
		result.DD = D;
		// Store eigenvalues in (RT1R,RT1I) and (RT2R,RT2I).
		result.RT1R = A;
		result.RT2R = D;
		if (C == 0.0) {
			result.RT1I = 0.0;
			result.RT2I = 0.0;
		} else {
			result.RT1I = Math.sqrt(Math.abs(B)) * Math.sqrt(Math.abs(C));
			result.RT2I = -result.RT2I;
		}
	}	
	
	/**
	 * LAPACK: SUBROUTINE DLAHQR( WANTT, WANTZ, N, ILO, IHI, H, LDH, WR, WI, ILOZ, IHIZ, Z, LDZ, INFO )
	 * 
	 * DLAHQR is an auxiliary routine called by DHSEQR to update the
	 * eigenvalues and Schur decomposition already computed by DHSEQR, by
	 * dealing with the Hessenberg submatrix in rows and columns ILO to IHI.
	 */
	protected void DLAHQR(int ILO, int IHI, Matrix H, Matrix WR, Matrix WI, Matrix Z) {
		// ITN is the total number of QR iterations allowed.
		int maxIterations = 30 * (IHI - ILO + 1);
		int iIndex = IHI;
		int lIndex = ILO;
		if (iIndex < lIndex)
			return;
		
		// Perform QR iterations on rows and columns ILO to I until a
		// submatrix of order 1 or 2 splits off at the bottom because a
		// subdiagonal element has become negligible.
		for (int iteration = 0; iteration < maxIterations; iteration++) {
			// Look for a single small subdiagonal element.
			for (int i = iIndex; i <= lIndex + 1; i++) {
				double tst = Math.abs(H.getItem(i - 1, i - 1)) + Math.abs(H.getItem(i, i));
				if (tst == 0.0)
					tst = 0; // ???
//				if (Math.abs(H.getItem(i, i - 1)) <= Math.max(ULP * tst, SMLNUM)) {
//					
//				}
			}
			
		}		
	}	
	
	/**
	 * LAPACK: SUBROUTINE DHSEQR( JOB, COMPZ, N, ILO, IHI, H, LDH, WR, WI, Z, LDZ, WORK, LWORK, INFO )
	 * 
	 * DHSEQR computes the eigenvalues of a real upper Hessenberg matrix H
	 * and, optionally, the matrices T and Z from the Schur decomposition
	 * H = Z T Z**T, where T is an upper quasi-triangular matrix (the Schur
	 * form), and Z is the orthogonal matrix of Schur vectors.
	 * 
	 * Optionally Z may be postmultiplied into an input orthogonal matrix Q,
	 * so that this routine can give the Schur factorization of a matrix A
	 * which has been reduced to the Hessenberg form H by the orthogonal
	 * matrix Q:  A = Q*H*Q**T = (QZ)*T*(QZ)**T.
	 * 
	 * JOB = "S" -> compute eigenvalues and the Schur form T.
	 * COMPZ = "V" -> Z must contain an orthogonal matrix Q on entry, and the product Q*Z is returned.
	 */
	public void DHSEQR(int ILO, int IHI, Matrix H, Matrix WR, Matrix WI, Matrix Z) {
		// Initialize Z, if necessary
		//if (???)
		Z.makeE();
		// Store the eigenvalues isolated by DGEBAL.
		// ???
		
		// Set rows and columns ILO to IHI to zero below the first subdiagonal.
		for (int j = ILO; j <= IHI - 2; j++)
			for (int i = j + 2; i < H.getSizeX(); i++)
				H.setItem(i, j, 0.0);
		// Determine the order of the multi-shift QR algorithm to be used.
		
	}
	
	public static void main(String[] args) throws Exception {
		JLapack jl = new JLapack();

		BufferedReader fin = new BufferedReader(new FileReader(
				MatrixTest.class.getResource(
					"SVD-A.txt").getFile()));
		StringTokenizer stt = new StringTokenizer(fin.readLine());
		Matrix a = new Matrix(Integer.parseInt(stt.nextToken()), Integer.parseInt(stt.nextToken()));
		a.load(fin);
		fin.close();

		//Matrix tmp = new Matrix();
		//a.transpose(tmp);
		//a = tmp.makeCopy();

		Matrix b = a.makeCopy();
		
		Matrix aU = new Matrix(); 
		Matrix aS = new Matrix();
		Matrix aV = new Matrix();

		Matrix bU = new Matrix();
		Matrix bS = new Matrix();
		Matrix bV = new Matrix();
		
		Matrix dU = new Matrix();
		Matrix dS = new Matrix();
		Matrix dV = new Matrix();
		
		a.mysvd(aU, aV, aS);
		jl.mysvd(b, bU, bV, bS);
		
		aU.mSub(bU, dU);
		dU.printM("Diff U");
		
		aV.mSub(bV, dV);
		dV.printM("Diff V");
		
		aS.mSub(bS, dS);
		dS.printM("Diff S");
	}
}
