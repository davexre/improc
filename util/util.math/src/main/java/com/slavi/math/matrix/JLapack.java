package com.slavi.math.matrix;

import com.slavi.math.MathUtil;


public class JLapack {
	public static final double EPS = 1.11022302E-016;

	public double DLARFG_Beta;
	public double DLARFG_Tau;
	/**
	 * <b>LAPACK:</b> SUBROUTINE DLARFG( N, ALPHA, X, INCX, TAU )
	 * <p>
 	 * DLARFG generates a real elementary reflector H of order n, such that
	 * <blockquote><pre>
	 *       H * ( alpha ) = ( beta ),   H' * H = I.
	 *           (   x   )   (   0  )
	 * </pre></blockquote>
	 * where alpha and beta are scalars, and x is an (n-1)-element real
	 * vector. H is represented in the form
	 * <blockquote><pre>
	 *       H = I - tau * ( 1 ) * ( 1 v' ) ,
	 *                     ( v )
	 * </pre></blockquote>
	 * where tau is a real scalar and v is a real (n-1)-element
	 * vector.
	 * <p>
	 * If the elements of x are all zero, then tau = 0 and H is taken to be
	 * the unit matrix.
	 * <p>
	 * DLARFG - INCX=1
	 */
	public void DLARFG_X(Matrix Src, int atX, int atY, int width, double alpha) {
		double xnorm = 0.0;
		for (int i = atX + width - 1; i >= atX; i--)
			xnorm = MathUtil.hypot(xnorm, Src.getItem(i, atY));
		if (xnorm == 0.0) {
			DLARFG_Beta = alpha;
			DLARFG_Tau = 0.0;
			return;
		}

		//DLARFG_Beta = -SIGN(Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)), alpha);
		//DLARFG_Beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
		DLARFG_Beta = MathUtil.hypot(alpha, xnorm);
		if (DLARFG_Beta == 0.0) {
			DLARFG_Tau = 0.0;
			return;
		}
		if (alpha >= 0.0)
			DLARFG_Beta = -DLARFG_Beta;
		DLARFG_Tau = (DLARFG_Beta - alpha) / DLARFG_Beta;
		double scale = 1.0 / (alpha - DLARFG_Beta);
		for (int i = atX + width - 1; i >= atX; i--)
			Src.setItem(i, atY, scale * Src.getItem(i, atY));
	}

	/**
	 * @see #DLARFG_X(Matrix, int, int, int, double)
	 * DLARFG - INCX=Src.GetSizeY()
	 */
	public void DLARFG_Y(Matrix Src, int atX, int atY, int height, double alpha) {
		double xnorm = 0.0;
		for (int j = atY + height - 1; j >= atY; j--)
			xnorm = MathUtil.hypot(xnorm, Src.getItem(atX, j));
		if (xnorm == 0.0) {
			DLARFG_Beta = alpha;
			DLARFG_Tau = 0.0;
			return;
		}

		//DLARFG_Beta = -SIGN(Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2)), alpha);
		//DLARFG_Beta = Math.sqrt(Math.pow(alpha, 2) + Math.pow(xnorm, 2));
		DLARFG_Beta = MathUtil.hypot(alpha, xnorm);
		if (DLARFG_Beta == 0.0) {
			DLARFG_Tau = 0.0;
			return;
		}
		if (alpha >= 0.0)
			DLARFG_Beta = -DLARFG_Beta;
		DLARFG_Tau = (DLARFG_Beta - alpha) / DLARFG_Beta;
		double scale = 1.0 / (alpha - DLARFG_Beta);
		for (int j = atY + height - 1; j >= atY; j--)
			Src.setItem(atX, j, scale * Src.getItem(atX, j));
	}

	/**
	 * <b>LAPACK:</b> SUBROUTINE DLARF( SIDE, M, N, V, INCV, TAU, C, LDC, WORK )
	 * <p>
	 * DLARF applies a real elementary reflector H to a real m by n matrix
	 * C, from either the left or the right. H is represented in the form
	 * <blockquote><pre>
	 *       H = I - tau * v * v'
	 * </pre></blockquote>
	 * where tau is a real scalar and v is a real vector.
	 * <p>
	 * If tau = 0, then H is taken to be the unit matrix.
	 * <p>
	 * DLARF - SIDE="Left"
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
	 * DLARF - SIDE="Right"
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
	 * <b>LAPACK:</b> SUBROUTINE DGEQRF( M, N, A, LDA, TAU, WORK, LWORK, INFO )
	 * <p>
	 * DGEQRF computes a QR factorization of a real M-by-N matrix A: A = Q * R.
	 * <p>
	 * A - On entry, the M-by-N matrix A.
	 * <p>
	 * A - On exit, the elements on and above the diagonal of the array
	 * 		contain the min(M,N)-by-N upper trapezoidal matrix R (R is
	 * 		upper triangular if m &gt;= n); the elements below the diagonal,
	 * 		with the array TAU, represent the orthogonal matrix Q as a
	 * 		product of min(m,n) elementary reflectors.
	 * <p>
	 * The matrix Q is represented as a product of elementary reflectors
	 * <p>
	 * Q = H(1) H(2) . . . H(k), where k = min(m,n).
	 * <p>
	 * Each H(i) has the form
	 * <p>
	 * H(i) = I - tau * v * v'
	 * <p>
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
			DLARFG_Y(Src, atIndex, atIndex + 1, Src.getSizeY() - atIndex - 1, Src.getItem(atIndex, atIndex));
			tau.setItem(atIndex, 0, DLARFG_Tau);

			// DGEQR2:109 Apply H(i) to A(i:m,i+1:n) from the left
			Src.setItem(atIndex, atIndex, 1.0);
			DLARF_X(Src, atIndex, DLARFG_Tau);
			Src.setItem(atIndex, atIndex, DLARFG_Beta);
		}
	}

	/**
	 * <b>LAPACK:</b> SUBROUTINE DORGBR( VECT, M, N, K, A, LDA, TAU, WORK, LWORK, INFO )
	 * <p>
	 * DORGBR generates one of the real orthogonal matrices Q or P**T
	 * determined by DGEBRD when reducing a real matrix A to bidiagonal
	 * form: A = Q * B * P**T.  Q and P**T are defined as products of
	 * elementary reflectors H(i) or G(i) respectively.
	 * <p>
	 * DORGBR - VECT="P"
	 */
	public static void qrDecomositionGetR(Matrix Src, Matrix r) {
		r.resize(Src.getSizeX(), Src.getSizeY());
		for (int i = Src.getSizeX() - 1; i >= 0; i--)
			for (int j = Src.getSizeY() - 1; j >= 0; j--)
				r.setItem(i, j, i < j ? 0.0 : Src.getItem(i, j));
	}

	/**
	 * @see #qrDecomositionGetR(Matrix, Matrix)
	 * <p>
	 * DORGBR - VECT="Q"
	 */
	public static void qrDecomositionGetQ(Matrix Src, Matrix tau, Matrix q) {
		if ((tau.getSizeX() < Src.getSizeX()) || (tau.getSizeY() < 1))
			throw new ArithmeticException("Invalid parameter");
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
	 * <b>LAPACK:</b> SUBROUTINE DGELQF( M, N, A, LDA, TAU, WORK, LWORK, INFO )
	 * <p>
	 * DGELQF computes an LQ factorization of a real M-by-N matrix A: A = L * Q.
	 * <p>
	 * The matrix Q is represented as a product of elementary reflectors
	 * <p>
	 * Q = H(k) . . . H(2) H(1), where k = min(m,n).
	 * <p>
	 * Each H(i) has the form
	 * <p>
	 * H(i) = I - tau * v * v'
	 * <p>
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
			DLARFG_X(Src, atIndex + 1, atIndex, Src.getSizeX() - atIndex - 1, Src.getItem(atIndex, atIndex));
			tau.setItem(atIndex, 0, DLARFG_Tau);

			// DGELQ2:109 Apply H(i) to A(i+1:m,i:n) from the right
			Src.setItem(atIndex, atIndex, 1.0);
			DLARF_Y(Src, atIndex, DLARFG_Tau);
			Src.setItem(atIndex, atIndex, DLARFG_Beta);
		}
	}

	/**
	 * <b>LAPACK:</b> DORGBR( VECT, M, N, K, A, LDA, TAU, WORK, LWORK, INFO )
	 * <p>
	 * @see #qrDecomositionGetR(Matrix, Matrix)
	 * <p>
	 * DORGBR - VECT="P"
	 */
	public static void lqDecomositionGetL(Matrix Src, Matrix l) {
		l.resize(Src.getSizeX(), Src.getSizeY());
		for (int i = Src.getSizeX() - 1; i >= 0; i--)
			for (int j = Src.getSizeY() - 1; j >= 0; j--)
				l.setItem(i, j, i > j ? 0.0 : Src.getItem(i, j));
	}

	/**
	 * @see #lqDecomositionGetL(Matrix, Matrix)
	 * <p>
	 * DORGBR - VECT="Q"
	 */
	public static void lqDecomositionGetQ(Matrix Src, Matrix tau, Matrix q) {
		if ((tau.getSizeX() < Src.getSizeY()) || (tau.getSizeY() != 1))
			throw new IllegalArgumentException("Invalid parameter");

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
	 * <b>LAPACK:</b> SUBROUTINE DGEBRD( M, N, A, LDA, D, E, TAUQ, TAUP, WORK, LWORK, INFO )
	 * <p>
	 * DGEBRD reduces a general real M-by-N matrix A to upper or lower
	 * bidiagonal form B by an orthogonal transformation: Q**T * A * P = B.
	 * <p>
	 * If m &gt;= n, B is upper bidiagonal; if m &lt; n, B is lower bidiagonal.
	 * <p>
	 * The matrices Q and P are represented as products of elementary reflectors:
	 * <p>
	 * If m &gt;= n,
	 * <p>
	 * Q = H(1) H(2) . . . H(n)  and  P = G(1) G(2) . . . G(n-1)
	 * <p>
	 * Each H(i) and G(i) has the form:
	 * <p>
	 * H(i) = I - tauq * v * v'  and G(i) = I - taup * u * u'
	 * <p>
	 * where tauq and taup are real scalars, and v and u are real vectors;
	 * v(1:i-1) = 0, v(i) = 1, and v(i+1:m) is stored on exit in A(i+1:m,i);
	 * u(1:i) = 0, u(i+1) = 1, and u(i+2:n) is stored on exit in A(i,i+2:n);
	 * tauq is stored in TAUQ(i) and taup in TAUP(i).
	 * <p>
	 * If m &lt; n,
	 * <p>
	 * Q = H(1) H(2) . . . H(m-1)  and  P = G(1) G(2) . . . G(m)
	 * <p>
	 * Each H(i) and G(i) has the form:
	 * <p>
	 * H(i) = I - tauq * v * v'  and G(i) = I - taup * u * u'
	 * <p>
	 * where tauq and taup are real scalars, and v and u are real vectors;
	 * v(1:i) = 0, v(i+1) = 1, and v(i+2:m) is stored on exit in A(i+2:m,i);
	 * u(1:i-1) = 0, u(i) = 1, and u(i+1:n) is stored on exit in A(i,i+1:n);
	 * tauq is stored in TAUQ(i) and taup in TAUP(i).
	 * <p>
	 * The contents of A on exit are illustrated by the following examples:
	 * <blockquote><pre>
	 * m = 6 and n = 5 (m &gt; n):          m = 5 and n = 6 (m &lt; n):
	 *
	 *   (  d   e   u1  u1  u1 )           (  d   u1  u1  u1  u1  u1 )
	 *   (  v1  d   e   u2  u2 )           (  e   d   u2  u2  u2  u2 )
	 *   (  v1  v2  d   e   u3 )           (  v1  e   d   u3  u3  u3 )
	 *   (  v1  v2  v3  d   e  )           (  v1  v2  e   d   u4  u4 )
	 *   (  v1  v2  v3  v4  d  )           (  v1  v2  v3  e   d   u5 )
	 *   (  v1  v2  v3  v4  v5 )
	 * </pre></blockquote>
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
				DLARFG_Y(Src, atIndex, atIndex + 1, Src.getSizeY() - atIndex - 1, Src.getItem(atIndex, atIndex));
			else
				DLARFG_X(Src, atIndex + 1, atIndex, Src.getSizeX() - atIndex - 1, Src.getItem(atIndex, atIndex));
			tauQ.setItem(atIndex, 0, DLARFG_Tau);
			Src.setItem(atIndex, atIndex, DLARFG_Beta);

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
				DLARFG_X(Src, atIndex + 2, atIndex, Src.getSizeX() - atIndex - 2, Src.getItem(atIndex + 1, atIndex));
			else
				DLARFG_Y(Src, atIndex, atIndex + 2, Src.getSizeY() - atIndex - 2, Src.getItem(atIndex, atIndex + 1));
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
	 * <b>LAPACK:</b> SUBROUTINE DLARTG( F, G, CS, SN, R )
	 * <p>
	 * DLARTG generate a plane rotation so that
	 * <blockquote><pre>
	 * [  CS  SN  ]  .  [ F ]  =  [ R ]   where CS**2 + SN**2 = 1.
	 * [ -SN  CS  ]     [ G ]     [ 0 ]
	 * </pre></blockquote>
	 */
	public static void DLARTG(double F, double G, DLARTG_Result Result) {
		Result.r = MathUtil.hypot(F, G);
		Result.CS = F / Result.r;
		Result.SN = G / Result.r;
		if ((Result.CS < 0.0) && (Math.abs(F) > Math.abs(G))) {
			Result.CS = -Result.CS;
			Result.SN = -Result.SN;
			Result.r = -Result.r;
		}
	}

	/**
	 * <pre>
	 * SSMIN - abs(SSMIN) is the smaller singular value
	 * SSMAX - abs(SSMAX) is the larger singular value.
	 * SINL, COSL - The vector (CSL, SNL) is a unit left singular vector for the singular value abs(SSMAX).
	 * SINR, COSR - The vector (CSR, SNR) is a unit right singular vector for the singular value abs(SSMAX).
	 * </pre>
	 */
	public static class DLASV2_Result {
		double SSMIN, SSMAX, SINR, COSR, SINL, COSL;
	}

	public DLASV2_Result DLASV2_result = new DLASV2_Result();

	/**
	 * <b>LAPACK:</b> SUBROUTINE DLASV2( F, G, H, SSMIN, SSMAX, SNR, CSR, SNL, CSL )
	 * <p>
	 * DLASV2 computes the singular value decomposition of a 2-by-2 triangular matrix
	 * <blockquote><pre>
	 * [  F   G  ]
	 * [  0   H  ]
	 * </pre></blockquote>
	 * On return, abs(SSMAX) is the larger singular value, abs(SSMIN) is the
	 * smaller singular value, and (CSL,SNL) and (CSR,SNR) are the left and
	 * right singular vectors for abs(SSMAX), giving the decomposition
	 * <blockquote><pre>
	 * [ CSL  SNL ] [  F   G  ] [ CSR -SNR ]  =  [ SSMAX   0   ]
	 * [-SNL  CSL ] [  0   H  ] [ SNR  CSR ]     [  0    SSMIN ]
	 * </pre></blockquote>
	 * Barring over/underflow and assuming a guard digit in subtraction, all
	 * output quantities are correct to within a few units in the last place (ulps).
	 * <p>
	 * Overflow will not occur unless the largest singular value itself
	 * overflows or is within a few ulps of overflow. (On machines with
	 * partial overflow, like the Cray, overflow may occur if the largest
	 * singular value is within a factor of 2 of overflow.)
	 * <p>
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
						T = MathUtil.SIGN(2.0, FT) * MathUtil.SIGN(1.0, GT);
					else
						T = GT / MathUtil.SIGN(tmpD, FT) + M / T;
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
		}
		// DLASV2:236 Correct signs of SSMAX and SSMIN
		double tsign = 0;
		if (pmax == 1)
			tsign = MathUtil.SIGN(1.0, result.COSR) * MathUtil.SIGN(1.0, result.COSL) * MathUtil.SIGN(1.0, F);
		if (pmax == 2)
			tsign = MathUtil.SIGN(1.0, result.SINR) * MathUtil.SIGN(1.0, result.COSL) * MathUtil.SIGN(1.0, G);
		if (pmax == 3)
			tsign = MathUtil.SIGN(1.0, result.SINR) * MathUtil.SIGN(1.0, result.SINL) * MathUtil.SIGN(1.0, H);
		result.SSMAX = MathUtil.SIGN(result.SSMAX, tsign);
		result.SSMIN = MathUtil.SIGN(result.SSMIN, tsign * MathUtil.SIGN(1.0, F) * MathUtil.SIGN(1.0, H));
		// End DLASV2
	}

	/**
	 * <b>LAPACK:</b> SUBROUTINE DBDSQR( UPLO, N, NCVT, NRU, NCC, D, E, VT, LDVT, U, LDU, C, LDC, WORK, INFO )
	 * <p>
	 * DBDSQR computes the singular value decomposition (SVD) of a real
	 * N-by-N (upper or lower) bidiagonal matrix B:  B = Q * S * P' (P'
	 * denotes the transpose of P), where S is a diagonal matrix with
	 * non-negative diagonal elements (the singular values of B), and Q
	 * and P are orthogonal matrices.
	 * <p>
	 * The routine computes S, and optionally computes U * Q, P' * VT,
	 * or Q' * C, for given real input matrices U, VT, and C.
	 * <p>
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

				if (HA == 0.0) {
					shift = 0.0;
				} else {
					if (GA < FA) {
						double AS = 1.0 + HA / FA;
						double AT = (FA - HA) / FA;
						double d = GA / FA;
						double AU = d * d;
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
							double d1 = AS * AU;
							double d2 = AT * AU;
							double C = 1.0 / (Math.sqrt(1.0 + d1 * d1) + Math.sqrt(1.0 + d2 * d2));
							shift = 2.0 * (HA * C * AU);
						}
					}
				}
				// End of DLAS2

				// DBDSQR:477 Test if shift negligible, and if so set to zero
				double d = shift / sll;
				if ((sll > 0.0) && (d * d < EPS))
					shift = 0.0;
			}

			// DBDSQR:485 Increment iteration count
			iter -= atIndex - ll;

			// DBDSQR:489 If SHIFT = 0, do simplified QR iteration
			if (shift == 0.0) {
				if (idir == 1) {
					// DBDSQR:570 Use nonzero shift
					if (idir == 1) {
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
					// DBDSQR:574 Chase bulge from top to bottom
					// Save cosines and sines for later singular vector updates
					double F = (Math.abs(s.getItem(ll, ll)) - shift) *
						(MathUtil.SIGN(1.0, s.getItem(ll, ll)) + shift / s.getItem(ll, ll));
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
					// DBDSQR:622 Chase bulge from bottom to top
					// Save cosines and sines for later singular vector updates
					double F = (Math.abs(s.getItem(atIndex, atIndex)) - shift) *
						(MathUtil.SIGN(1.0, s.getItem(atIndex, atIndex)) + shift / s.getItem(atIndex, atIndex));
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
			throw new ArithmeticException("Maximum number of iterations exceeded, failure to converge");
	}

	public static void svdSortResult(Matrix U, Matrix s, Matrix V) {
		// Check data
//		if ((U.getSizeX() != U.getSizeY()) ||		// TODO: Enable this check
//			(V.getSizeX() != V.getSizeY()) ||
//			(V.getSizeX() != s.getSizeX()) ||
//			(U.getSizeY() != s.getSizeY()) )
//			throw new IllegalArgumentException("Invalid arguments");
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
			Src.lqDecompositionGetQ(tauP, V);
			Src.lqDecompositionGetL(RL);
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
	 * <b>LAPACK:</b> SUBROUTINE DLANV2( A, B, C, D, RT1R, RT1I, RT2R, RT2I, CS, SN )
	 * <p>
	 * DLANV2 computes the Schur factorization of a real 2-by-2 nonsymmetric
	 * matrix in standard form:
	 * <blockquote><pre>
	 * [ A  B ] = [ CS -SN ] [ AA  BB ] [ CS  SN ]
	 * [ C  D ]   [ SN  CS ] [ CC  DD ] [-SN  CS ]
	 * </pre></blockquote>
	 * where either
	 * <ol>
	 * <li>CC = 0 so that AA and DD are real eigenvalues of the matrix, or</li>
	 * <li>AA = DD and BB*CC &lt; 0, so that AA + or - sqrt(BB*CC) are complex
	 * conjugate eigenvalues.</li>
	 * </ol>
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
		} else if ( (A-D == 0.0) && (MathUtil.SIGN(1.0, B) != MathUtil.SIGN(1.0, C)) ) {
			result.CS = 1.0;
			result.SN = 0.0;
		} else {
			double temp = A - D;
			double p = temp * 0.5;
			double BCmax = Math.max(Math.abs(B), Math.abs(C));
			double BCmin = Math.min(Math.abs(B), Math.abs(C)) * MathUtil.SIGN(1.0, B) * MathUtil.SIGN(1.0, C);
			double scale = Math.max(Math.abs(p), BCmax);
			double z = (p / scale) * p + (BCmax / scale) * BCmin;
			// If Z is of the order of the machine accuracy, postpone the
			// decision on the nature of eigenvalues
			if (z >= 4.0 * EPS) {
				// Real eigenvalues. Compute A and D.
				z = p + MathUtil.SIGN( Math.sqrt(scale) * Math.sqrt(z), p);
				A = D + z;
				D = D - (BCmax / z) * BCmin;
				// Compute B and the rotation matrix
				double tau = MathUtil.hypot(C, z);
				result.CS = z / tau;
				result.SN = C / tau;
				B = B - C;
				C = 0.0;
			} else {
				// Complex eigenvalues, or real (almost) equal eigenvalues.
				// Make diagonal elements equal.
				double sigma = B + C;
				double tau = MathUtil.hypot(sigma, temp);
				result.CS = Math.sqrt(0.5 * (1.0 + Math.abs(sigma) / tau) );
				result.SN = - (p / (tau * result.CS)) * MathUtil.SIGN(1.0, sigma);

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
						if (MathUtil.SIGN(1.0, B) == MathUtil.SIGN(1.0, C)) {
							// Real eigenvalues: reduce to upper triangular form
							double SAB = Math.sqrt(Math.abs(B));
							double SAC = Math.sqrt(Math.abs(C));
							p = MathUtil.SIGN(SAB * SAC, C);
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
			result.RT2I = -result.RT1I;
		}
	}

	public static class DGEBAL_Result {
		public int ILO;
		public int IHI;
	}

	/**
	 * <b>LAPACK:</b> SUBROUTINE DGEBAL( JOB, N, A, LDA, ILO, IHI, SCALE, INFO )
	 * <p>
	 * DGEBAL balances a general real matrix A.  This involves, first,
	 * permuting A by a similarity transformation to isolate eigenvalues
	 * in the first 1 to ILO-1 and last IHI+1 to N elements on the
	 * diagonal; and second, applying a diagonal similarity transformation
	 * to rows and columns ILO to IHI to make the rows and columns as
	 * close in norm as possible.  Both steps are optional.
	 * <p>
	 * Balancing may reduce the 1-norm of the matrix, and improve the
	 * accuracy of the computed eigenvalues and/or eigenvectors.
	 * <p>
	 * The permutations consist of row and column interchanges which put
	 * the matrix in the form
	 * <blockquote><pre>
	 *         ( T1   X   Y  )
	 * P A P = (  0   B   Z  )
	 *         (  0   0   T2 )
	 * </pre></blockquote>
	 * where T1 and T2 are upper triangular matrices whose eigenvalues lie
	 * along the diagonal.  The column indices ILO and IHI mark the starting
	 * and ending columns of the submatrix B. Balancing consists of applying
	 * a diagonal similarity transformation inv(D) * B * D to make the
	 * 1-norms of each row of B and its corresponding column nearly equal.
	 * The output matrix is
	 * <blockquote><pre>
	 * ( T1     X*D          Y    )
	 * (  0  inv(D)*B*D  inv(D)*Z )
	 * (  0      0           T2   )
	 * </pre></blockquote>
	 * Information about the permutations P and the diagonal matrix D is
	 * returned in the vector SCALE.
	 * <p>
	 * This subroutine is based on the EISPACK routine BALANC.
	 */
	public static void DGEBAL(Matrix A, Matrix scale, DGEBAL_Result result) {
		result.ILO = 0;
		result.IHI = 0;

		if (A.getSizeX() != A.getSizeY())
			throw new IllegalArgumentException("Invalid matrix size");
		if (scale.getSizeX() < A.getSizeX())
			scale.resize(A.getSizeX(), 1);

		int K = 0;
		int L = A.getSizeX() - 1;
		int M = L;

		// Permutation to isolate eigenvalues if possible

		// Search for rows isolating an eigenvalue and push them down.
		int j = L;
		while (j >= 0) {
			boolean found = false;
			for (int i = 0; i <= L; i++) {
				if ((i != j) && (A.getItem(i, j) != 0.0)) {
					found = true;
					break;
				}
			}
			if (!found) {
				M = L;
				scale.setItem(M, 0, j + 1); // ???
				if (j != M) {
					// Row and column exchange.
					for (int jj = L; jj >= 0; jj--) {
						double tmp = A.getItem(j, jj);
						A.setItem(j, jj, A.getItem(M, jj));
						A.setItem(M, jj, tmp);
					}
					for (int ii = A.getSizeX() - 1; ii >= K; ii--) {
						double tmp = A.getItem(ii, j);
						A.setItem(ii, j, A.getItem(ii, M));
						A.setItem(ii, M, tmp);
					}
				}
				if (L == 0) {
					// goto 210
					break;
				}
				j = L;
				L--;
			}
			j--;
		}

		// Search for columns isolating an eigenvalue and push them left.
		j = K;
		while (j <= L) {
			boolean found = false;
			for (int i = K; i <= L; i++) {
				if ((i != j) && (A.getItem(i, j) != 0.0)) {
					found = true;
					break;
				}
			}
			if (!found) {
				M = K;
				scale.setItem(M, 0, j + 1); // ???
				if (j != M) {
					// Row and column exchange.
					for (int jj = L; jj >= 0; jj--) {
						double tmp = A.getItem(j, jj);
						A.setItem(j, jj, A.getItem(M, jj));
						A.setItem(M, jj, tmp);
					}
					for (int ii = A.getSizeX() - 1; ii >= K; ii--) {
						double tmp = A.getItem(ii, j);
						A.setItem(ii, j, A.getItem(ii, M));
						A.setItem(ii, M, tmp);
					}
				}
				K++;
				j = K;
			}
			j++;
		}

		// Scale
		for (int i = K; i <= L; i++)
			scale.setItem(i, 0, 1.0);
		// Balance the submatrix in rows K to L.
		// Iterative loop for norm reduction
		final double SCLFAC = 8.0; // in fortran this is encoded like 0.8D+1;

		while (true) {
			boolean NOCONV = false;
			for (int i = K; i <= L; i++) {
				double c = 0.0;
				double r = 0.0;
				for (int jj = K; jj <= L; jj++)
					if (jj != i) {
						c += Math.abs(A.getItem(i, jj));
						r += Math.abs(A.getItem(jj, i));
					}
				// Guard against zero C or R due to underflow.
				if ( (c == 0.0) || (r == 0.0) )
					continue;

				// Find CA=max(abs()) in column i
				double CA = Math.abs(A.getItem(i, L));
				for (int jj = L - 1; jj >= 0; jj--) {
					double d = Math.abs(A.getItem(i, jj));
					if (d > CA)
						CA = d;
				}

				// Find RA=max(abs()) in row i
				double RA = Math.abs(A.getItem(A.getSizeX() - 1, i));
				for (int jj = A.getSizeY() - 2; jj >= K; jj--) {
					double d = Math.abs(A.getItem(jj, i));
					if (d > RA)
						RA = d;
				}

				double G = r / SCLFAC;
				double F = 1.0;
				double S = c + r;
				while ( (c < G)  ) { // ??? more
					F *= SCLFAC;
					c *= SCLFAC;
					CA *= SCLFAC;
					r /= SCLFAC;
					G /= SCLFAC;
					RA /= SCLFAC;
				}

				G = c / SCLFAC;
				while ( (G >= r)  ) { // ??? more
					F /= SCLFAC;
					c /= SCLFAC;
					G /= SCLFAC;
					CA /= SCLFAC;
					r *= SCLFAC;
					RA *= SCLFAC;
				}

				// Now balance.
				// DGEBAL:293
				final double FACTOR = 0.95;
				if (c + r >= FACTOR * S)
					continue;
				if ( (F < 1.0) && (scale.getItem(i, 0) < 1.0) ) // ??? more
					continue;
				if ( (F > 1.0) && (scale.getItem(i, 0) > 1.0) ) // ??? more
					continue;
				G = 1.0 / F;
				scale.setItem(i, 0, scale.getItem(i, 0) * F);
				NOCONV = true;

				for (int ii = A.getSizeX() - 1; ii >= K; ii--)
					A.setItem(ii, i, A.getItem(ii, i) * G);
				for (int jj = L; jj >= 0; jj--)
					A.setItem(i, jj, A.getItem(i, jj) * F);
			}
			if (!NOCONV)
				break;
		}

		result.ILO = K;
		result.IHI = L;
	}

	private int DLAHQR_FindSmallSubDiagonalElement(Matrix A, int ILO, int I) {
		final double SMLNUM = 9.01875762E-292;
		final double ULP = 2.22044605E-016;

		int result = ILO;
		// DLAHQR:184 Look for a single small subdiagonal element.
		double norm1 = -1.0;
		for (int k = I; k > ILO; k--) {
			double tst = Math.abs(A.getItem(I - 1, I - 1)) + Math.abs(A.getItem(k, k));
			if (tst == 0.0) {
				if (norm1 < 0.0) {
					// norm1 not calculated yet. Calc it.
					for (int i = I; i >= ILO; i--) {
						double sumAbs = 0.0;
						for (int j = I; j >= ILO; j--)
							sumAbs += Math.abs(A.getItem(i, j));
						if (sumAbs > norm1)
							norm1 = sumAbs;
					}
				}
				tst = norm1;
			}
			tst = Math.max(ULP * tst, SMLNUM);
			if (Math.abs(A.getItem(k - 1, k)) <= tst)  {
				result = k;
				break;
			}
		}
		return result;
	}

	/**
	 * <b>LAPACK:</b> SUBROUTINE DGEEV( JOBVL, JOBVR, N, A, LDA, WR, WI, VL, LDVL, VR, LDVR, WORK, LWORK, INFO )
	 * <p>
	 * DGEEV computes for an N-by-N real nonsymmetric matrix A, the
	 * eigenvalues and, optionally, the left and/or right eigenvectors.
	 * <p>
	 * The right eigenvector v(j) of A satisfies
	 * <p>
	 * A * v(j) = lambda(j) * v(j)
	 * <p>
	 * where lambda(j) is its eigenvalue.
	 * <p>
	 * The left eigenvector u(j) of A satisfies
	 * <p>
	 * u(j)**H * A = lambda(j) * u(j)**H
	 * <p>
	 * where u(j)**H denotes the conjugate transpose of u(j).
	 * <p>
	 * The computed eigenvectors are normalized to have Euclidean norm
	 * equal to 1 and largest component real.
	 * <p>
	 * JOBVL = "N" - left eigenvectors of A are not computed
	 * JOBVR = "N" - right eigenvectors of A are not computed
	 *
	 * @param WRI - The eigenvalues WRI.getItem(?, 0) - real part, WRI.getItem(?, 1) - imaginary part
	 */
	public void DGEEV(Matrix A, Matrix WRI) {
		if (A.getSizeX() != A.getSizeY())
			throw new IllegalArgumentException("Matrix must be square");

		WRI.resize(A.getSizeX(), 2);
		WRI.make0();

		Matrix tau = new Matrix(A.getSizeX(), 1);
		tau.make0();

//		double ANRM = A.maxAbs();
//		????
//		if (ANRM != 0.0)
//			A.rMul(1.0 / ANRM);
		Matrix scale = new Matrix(A.getSizeX(), 1);
		DGEBAL(A, scale, DGEBAL_result);

		for (int atIndex = DGEBAL_result.ILO; atIndex < DGEBAL_result.IHI; atIndex++) {
			// DGEHD2: 124 Compute elementary reflector H(i) to annihilate A(i+2:ihi,i)
			DLARFG_Y(A, atIndex, atIndex + 2, DGEBAL_result.IHI - atIndex - 1, A.getItem(atIndex, atIndex + 1));
			tau.setItem(atIndex, 0, DLARFG_Tau);
			A.setItem(atIndex, atIndex + 1, 1.0);

			// DGEHD2: 133 Apply H(i) to A(1:ihi,i+1:ihi) from the right
			for (int i = DGEBAL_result.IHI; i >= 0; i--) {
				double sum = 0.0;
				for (int j = DGEBAL_result.IHI; j > atIndex; j--)
					sum += A.getItem(j, i) * A.getItem(atIndex, j);
				for (int j = DGEBAL_result.IHI; j > atIndex; j--)
					A.setItem(j, i, A.getItem(j, i) - DLARFG_Tau * sum * A.getItem(atIndex, j));
			}
			// DGEHD2: 136 Apply H(i) to A(i+1:ihi,i+1:n) from the left
			for (int i = A.getSizeX() - 1; i > atIndex; i--) {
				double sum = 0.0;
				for (int j = DGEBAL_result.IHI; j > atIndex; j--)
					sum += A.getItem(i, j) * A.getItem(atIndex, j);
				for (int j = DGEBAL_result.IHI; j > atIndex; j--)
					A.setItem(i, j, A.getItem(i, j) - DLARFG_Tau * sum * A.getItem(atIndex, j));
			}
			A.setItem(atIndex, atIndex + 1, DLARFG_Beta);
		}

//		if (Left != null) {
//			// Want left eigenvectors
//			// DGEEV:265 Copy Householder vectors to VL
//			Left.resize(A.getSizeX(), A.getSizeY());
//			for (int i = A.getSizeX() - 1; i >= 0; i--)
//				for (int j = A.getSizeY() - 1; j >= 0; j--)
//					Left.setItem(i, j, (i <= j) ? A.getItem(i, j) : 0.0);
//			// DGEEV:270 Generate orthogonal matrix in VL
//
//			// DORGHR:125 Shift the vectors which define the elementary reflectors one
//			// column to the right, and set the first ilo and the last n-ihi
//			// rows and columns to those of the unit matrix
//			for (int i = DGEBAL_result.IHI; i > DGEBAL_result.ILO; i--) {
//				//for (int j = i - 1; j >= 0; j--)
//				//	Left.setItem(i, j, 0.0);
//				for (int j = i + 1; j <= DGEBAL_result.IHI; j++)
//					Left.setItem(i, j, Left.getItem(i - 1, j));
//				for (int j = DGEBAL_result.IHI + 1; j < Left.getSizeY(); j++)
//					Left.setItem(i, j, 0.0);
//			}
//
//			for (int i = DGEBAL_result.ILO; i >= 0; i--) {
//				for (int j = Left.getSizeY() - 1; j >= 0; j--)
//					Left.setItem(i, j, 0.0);
//				Left.setItem(i, i, 1.0);
//			}
//
//			for (int i = DGEBAL_result.IHI + 1; i < Left.getSizeX(); i++) {
//				for (int j = Left.getSizeY() - 1; j >= 0; j--)
//					Left.setItem(i, j, 0.0);
//				Left.setItem(i, i, 1.0);
//			}
//
//			//Left.printM("DORG2R: before A=");
//			// DORGHR:159 Generate Q(ilo+1:ihi,ilo+1:ihi)
//			for (int i = DGEBAL_result.IHI; i > DGEBAL_result.ILO; i--) {
//				// DORG2R:109 Apply H(i) to A(i:m,i:n) from the left
//				double TAU = tau.getItem(i - 1, 0);
//				Left.setItem(i, i, 1.0);
//				DLARF_X(Left, i, TAU);
//				for (int j = Left.getSizeY() - 1; j > i; j--)
//					Left.setItem(i, j, - TAU * Left.getItem(i, j));
//				Left.setItem(i, i, 1.0 - TAU);
//				// DORG2R:120 Set A(1:i-1,i) to zero
//				for (int j = i - 1; j >= 0; j--)
//					Left.setItem(i, j, 0.0);
//			}
//			//Left.printM("DORG2R: after A=");
//
//			// DGEEV:276 Perform QR iteration, accumulating Schur vectors in VL
//
//			A.printM("A before DHSEQR");
//			DHSEQR(DGEBAL_result.ILO, DGEBAL_result.IHI, A, WRI, Left);
//			Left.printM("Z");
//			// Left.copyTo(Right);
//
//			// DGEEV:331 Compute left and/or right eigenvectors
//		}

		DHSEQR(DGEBAL_result.ILO, DGEBAL_result.IHI, A, WRI, null);
		WRI.printM("WRI");
	}

	/**
	 * <b>LAPACK:</b> SUBROUTINE DTREVC( SIDE, HOWMNY, SELECT, N, T, LDT, VL, LDVL, VR, LDVR, MM, M, WORK, INFO )
	 * <p>
	 * DTREVC computes some or all of the right and/or left eigenvectors of
	 * a real upper quasi-triangular matrix T.
	 * <p>
	 * The right eigenvector x and the left eigenvector y of T corresponding
	 * to an eigenvalue w are defined by:
	 * <p>
	 * T*x = w*x,     y'*T = w*y'
	 * <p>
	 * where y' denotes the conjugate transpose of the vector y.
	 * <p>
	 * If all eigenvectors are requested, the routine may either return the
	 * matrices X and/or Y of right or left eigenvectors of T, or the
	 * products Q*X and/or Q*Y, where Q is an input orthogonal
	 * matrix. If T was obtained from the real-Schur factorization of an
	 * original matrix A = Q*T*Q', then Q*X and Q*Y are the matrices of
	 * right or left eigenvectors of A.
	 * <p>
	 * T must be in Schur canonical form (as returned by DHSEQR), that is,
	 * block upper triangular with 1-by-1 and 2-by-2 diagonal blocks; each
	 * 2-by-2 diagonal block has its diagonal elements equal and its
	 * off-diagonal elements of opposite sign.  Corresponding to each 2-by-2
	 * diagonal block is a complex conjugate pair of eigenvalues and
	 * eigenvectors; only one eigenvector of the pair is computed, namely
	 * the one corresponding to the eigenvalue with positive imaginary part.
	 * <p>
	 * The algorithm used in this program is basically backward (forward)
	 * substitution, with scaling to make the the code robust against
	 * possible overflow.
	 * <p>
	 * Each eigenvector is normalized so that the element of largest
	 * magnitude has magnitude 1; here the magnitude of a complex number
	 * (x,y) is taken to be |x| + |y|.
	 * <p>
	 * SIDE = "B" - compute both right and left eigenvectors.
	 * HOWMNY = "B" - compute all right and/or left eigenvectors,
	 *     and backtransform them using the input matrices supplied in VR and/or VL;
	 * SELECT = [empty]
	 */
/*	public void DTREVC(Matrix T, Matrix VL, Matrix VR) {
		// DTREVC:279 Compute 1-norm of each column of strictly upper triangular
		// part of T to control overflow in triangular solver.
		Matrix work = new Matrix(T.getSizeX(), 1);
		work.setItem(0, 0, 0.0);
		for (int i = 1; i < T.getSizeY(); i++) {
			double sumAbs = 0.0;
			for (int j = i - 1; j >= 0; j--)
				sumAbs += Math.abs(T.getItem(i, j));
			work.setItem(i, 0, sumAbs);
		}
		// DTREVC:290 Index IP is used to specify the real or complex eigenvalue:
		// IP = 0, real eigenvalue,
		//      1, first of conjugate complex pair: (wr,wi)
		//     -1, second of conjugate complex pair: (wr,wi)

//		double ULP = 0.0; // ???
//		double SMLNUM = 0.0; // ???
		int IP = 0;

		// DTREVC:299 Compute right eigenvectors.
		for (int KI = T.getSizeX() - 1; KI >= 0; KI--) {
			// IF( IP.EQ.1 ) GO TO 130

			if ( (KI != 0) && (T.getItem(KI - 1, KI) != 0.0) )
				IP = -1;
			// DTREVC:655 Compute the KI-th eigenvalue (WR,WI).
//			double WR = T.getItem(KI, KI);
//			double WI = 0.0;
//			if (IP != 0)
//				WI =
//					Math.sqrt(Math.abs(T.getItem(KI + 1, KI))) *
//					Math.sqrt(Math.abs(T.getItem(KI, KI + 1)));
//			double SMIN = Math.max(ULP * (Math.abs(WR) + Math.abs(WI)), SMLNUM);

			if (IP == 0) {
				// DTREVC:666 Real left eigenvector.
				// WORK( KI+N ) = ONE

				// DTREVC:670 Form right-hand side

				// DTREVC:676 Solve the quasi-triangular system:
				// (T(KI+1:N,KI+1:N) - WR)'*X = SCALE*WORK

			}
		}

	}
*/

	/**
	 * <b>LAPACK:</b> SUBROUTINE DHSEQR( JOB, COMPZ, N, ILO, IHI, H, LDH, WR, WI, Z, LDZ, WORK, LWORK, INFO )
	 * <p>
	 * DHSEQR computes the eigenvalues of a real upper Hessenberg matrix H
	 * and, optionally, the matrices T and Z from the Schur decomposition
	 * H = Z T Z**T, where T is an upper quasi-triangular matrix (the Schur
	 * form), and Z is the orthogonal matrix of Schur vectors.
	 * <p>
	 * Optionally Z may be postmultiplied into an input orthogonal matrix Q,
	 * so that this routine can give the Schur factorization of a matrix A
	 * which has been reduced to the Hessenberg form H by the orthogonal
	 * matrix Q:  A = Q*H*Q**T = (QZ)*T*(QZ)**T.
	 * <p>
	 * JOB = "S" - compute eigenvalues and the Schur form T.
	 * <p>
	 * COMPZ = "V" - Z must contain an orthogonal matrix Q on entry, and the product Q*Z is returned.
	 */
	public void DHSEQR(int ILO, int IHI, Matrix A, Matrix WRI, Matrix ZZZ) {
//		if (ZZZ != null)
//			ZZZ.resize(A.getSizeX(), A.getSizeY());

		// DHSEQR:187 Store the eigenvalues isolated by DGEBAL.
		for (int i = ILO - 1; i >= 0; i--)
			WRI.setItem(i, 0, A.getItem(i, i));
		for (int i = A.getSizeX() - 1; i > IHI; i--)
			WRI.setItem(i, 0, A.getItem(i, i));

		// DHSEQR:208 Set rows and columns ILO to IHI to zero below the first subdiagonal.
		for (int i = IHI - 2; i >= ILO; i--)
			for (int j = A.getSizeY() - 1; j >= i + 2; j--)
				A.setItem(i, j, 0.0);

//		A.printM("AAA before DLAHQR");

		// DLAHQR:162 the total number of QR iterations allowed.
		int I1 = 0;
		int I2 = A.getSizeX() - 1;
		int maxIterations = 30 * (IHI - ILO + 1);

		// DLAHQR:166 The main loop begins here. I is the loop index and decreases from
		// IHI to ILO in steps of 1 or 2. Each iteration of the loop works
		// with the active submatrix in rows and columns L to I.
		// Eigenvalues I+1 to IHI have already converged. Either L = ILO or
		// H(L,L-1) is negligible so that the matrix splits.
//		final double SMLNUM = 9.01875762E-292;
		final double ULP = 2.22044605E-016;

		Matrix V = new Matrix(3, 1);
		int I = IHI;
		I2 = I;

//		boolean fulldump = false;

		while (I >= ILO) {
			int L = ILO;
			// DLAHQR:178 Perform QR iterations on rows and columns ILO to I until a
			// submatrix of order 1 or 2 splits off at the bottom because a
			// subdiagonal element has become negligible.
			int iteration = 0;
			boolean converged = false;
			while (iteration <= maxIterations) {
				L = DLAHQR_FindSmallSubDiagonalElement(A, L, I);

//				if ( (L==0) && (I==8) && (iteration==0) )
//					fulldump = true;
//				else
//					fulldump = false;
//
//				if (fulldump)
//					System.out.println("******************** FULLDUMP ********************");

//				System.out.println("*** L=" + (L+1) + " I=" + (I+1) + " iteration=" + iteration);
//				A.printM("A=");

				if (L > ILO) {
					// DLAHQR:197 H(L,L-1) is negligible
					A.setItem(L - 1, L, 0.0);
				}

				// DLAHQR:202 Exit from loop if a submatrix of order 1 or 2 has split off.
				if (L >= I - 1) {
					converged = true;
					break;
				}

				// DLAHQR:207 Now the active submatrix is in rows and columns L to I. If
				// eigenvalues only are being computed, only the active submatrix
				// need be transformed.
				// DLAHQR:211 ??? IF( .NOT.WANTT ) THEN

				// DLAHQR:226 Prepare to use Francis' double shift (i.e. 2nd degree generalized Rayleigh quotient)
				double S;
				double A33;
				double A44;
				double A43A34;
				if ((iteration == 10) || (iteration == 20)) {
					// DLAHQR:218 Exceptional shift.
					S = Math.abs(A.getItem(I - 1, I)) + Math.abs(A.getItem(I - 2, I - 1));
					A33 = A44 = 0.75 * S + A.getItem(I, I);
					A43A34 = S * S * (-0.4375);
				} else {
					// DLAHQR:226 Prepare to use Francis' double shift (i.e. 2nd degree generalized Rayleigh quotient)
					A44 = A.getItem(I, I);
					A33 = A.getItem(I - 1, I - 1);
					A43A34 = A.getItem(I - 1, I) * A.getItem(I, I - 1);
					S = A.getItem(I - 2, I - 1);
					S = S * S;
					double DISC = (A33 - A44) * 0.5;
					DISC = DISC * DISC + A43A34;
					if (DISC > 0.0) {
						// DLAHQR:237 Real roots: use Wilkinson's shift twice
						DISC = Math.sqrt(DISC);
						double AVE = (A33 + A44) * 0.5;
						if (Math.abs(A33) > Math.abs(A44)) {
							A33 = A33 * A44 - A43A34;
							A44 = A33 / ( MathUtil.SIGN(DISC, AVE) + AVE );
						} else {
							A44 = MathUtil.SIGN(DISC, AVE) + AVE;
						}
						A33 = A44;
						A43A34 = 0.0;
					}
				}

//				if (fulldump) {
//					System.out.println("S=" + S);
//					System.out.println("A33=" + A33);
//					System.out.println("A44=" + A44);
//					System.out.println("A43A34=" + A43A34);
//				}

				double V1 = 0.0, V2 = 0.0, V3 = 0.0;

				// DLAHQR:252 Look for two consecutive small subdiagonal elements.
				int M = I - 2;
				while (M >= L) {
					// Determine the effect of starting the double-shift QR
					// iteration at row M, and see if this would make H(M,M-1) negligible.
					double A11 = A.getItem(M, M);
					double A22 = A.getItem(M + 1, M + 1);
					double A21 = A.getItem(M, M + 1);
					double A12 = A.getItem(M + 1, M);
					double A44S = A44 - A11;
					double A33S = A33 - A11;

					V1 = (A33S * A44S - A43A34) / A21 + A12;
					V2 = A22 - A11 - A33S - A44S;
					V3 = A.getItem(M + 1, M + 2);
					S = Math.abs(V1) + Math.abs(V2) + Math.abs(V3);
					V1 /= S;
					V2 /= S;
					V3 /= S;
					V.setItem(0, 0, V1);
					V.setItem(1, 0, V2);
					V.setItem(2, 0, V3);

//					if (fulldump) {
//						System.out.println("M=" + (M+1));
//						A.printM("HHHHHHH");
//						System.out.println("A11=" + A11);
//						System.out.println("A22=" + A22);
//						System.out.println("A21=" + A21);
//						System.out.println("A12=" + A12);
//						System.out.println("A44S=" + A44S);
//						System.out.println("A33S=" + A33S);
//						System.out.println("S=" + S);
//						V.printM("vv0");
//					}

					if (M == L)
						break;
					double A00 = A.getItem(M - 1, M - 1);
					double A10 = A.getItem(M - 1, M);
					double tst = Math.abs(V1) * ( Math.abs(A00) + Math.abs(A11) + Math.abs(A22) );
					if (Math.abs(A10) * ( Math.abs(V2) + Math.abs(V3) ) <= ULP * tst)
						break;
					M--;
				}

//				if (fulldump) {
//					System.out.println("V(1)=" + V.getItem(0, 0));
//					System.out.println("V(2)=" + V.getItem(1, 0));
//					System.out.println("V(3)=" + V.getItem(2, 0));
//				}

				// DLAHQR:285 Double-shift QR step
				for (int K = M; K < I; K++) {
					// DLAHQR:289 The first iteration of this loop determines a reflection G
					// from the vector V and applies it from left and right to H,
					// thus creating a nonzero bulge below the subdiagonal.
					//
					// Each subsequent iteration determines a reflection G to
					// restore the Hessenberg form in the (K-1)th column, and thus
					// chases the bulge one step toward the bottom of the active
					// submatrix. NR is the order of G.
					int NR = Math.min(2, I - K);

					if (K > M) {
						for (int i = NR; i >= 0; i--)
							V.setItem(i, 0, A.getItem(K - 1, K + i));
					}

//					if (fulldump) {
//						A.printM("A at V");
//						V.printM("before V");
//					}

					DLARFG_X(V, 1, 0, NR, V.getItem(0, 0));
					V.setItem(0, 0, DLARFG_Beta);

//					if (fulldump)
//						V.printM("after V");

					if (K > M) {
						A.setItem(K - 1, K, V.getItem(0, 0));
						A.setItem(K - 1, K + 1, 0.0);
						if (K < I - 1)
							A.setItem(K - 1, K + 2, 0.0);
					} else if (M > L) {
						A.setItem(K - 1, K, -A.getItem(K - 1, K));
					}
					V2 = V.getItem(1, 0);
					double T2 = DLARFG_Tau * V2;

					if (NR == 2) {
						V3 = V.getItem(2, 0);
						double T3 = DLARFG_Tau * V3;
						// DLAHQR:316 Apply G from the left to transform the rows of the matrix in columns K to I2.
						for (int j = K; j <= I2; j++) {
							double sum = A.getItem(j, K) + V2 * A.getItem(j, K + 1) + V3 * A.getItem(j, K + 2);
							A.setItem(j, K, A.getItem(j, K) - sum * DLARFG_Tau);
							A.setItem(j, K + 1, A.getItem(j, K + 1) - sum * T2);
							A.setItem(j, K + 2, A.getItem(j, K + 2) - sum * T3);
						}

//						if (fulldump) {
//							System.out.println("K=" + (K+1));
//							System.out.println("T1=" + DLARFG_Tau + " T2=" + T2 + " T3=" + T3);
//							A.printM("A at NR=2");
//						}
						// DLAHQR:326 Apply G from the right to transform the columns of the matrix in rows I1 to min(K+3,I).
						for (int j = Math.min(K + 3, I); j >= I1; j--) {
							double sum = A.getItem(K, j) + V2 * A.getItem(K + 1, j) + V3 * A.getItem(K + 2, j);
							A.setItem(K, j, A.getItem(K, j) - sum * DLARFG_Tau);
							A.setItem(K + 1, j, A.getItem(K + 1, j) - sum * T2);
							A.setItem(K + 2, j, A.getItem(K + 2, j) - sum * T3);
						}
//						if (fulldump) {
//							System.out.println("K=" + (K+1));
//							A.printM("A at NR=2");
//						}

						if (ZZZ != null) {
							// DLAHQR:338 Accumulate transformations in the matrix Z
							for (int j = ILO; j <= IHI; j++) {
								double sum = ZZZ.getItem(K, j) + V2 * ZZZ.getItem(K + 1, j) + V3 * ZZZ.getItem(K + 2, j);
								ZZZ.setItem(K, j, ZZZ.getItem(K, j) - sum * DLARFG_Tau);
								ZZZ.setItem(K + 1, j, ZZZ.getItem(K + 1, j) - sum * T2);
								ZZZ.setItem(K + 2, j, ZZZ.getItem(K + 2, j) - sum * T3);
							}
						}
					} else if (NR == 1) {
						// DLAHQR:349 Apply G from the left to transform the rows of the matrix in columns K to I2.
						for (int j = K; j <= I2; j++) {
							double sum = A.getItem(j, K) + V2 * A.getItem(j, K + 1);
							A.setItem(j, K, A.getItem(j, K) - sum * DLARFG_Tau);
							A.setItem(j, K + 1, A.getItem(j, K + 1) - sum * T2);
						}

						// DLAHQR:358 Apply G from the right to transform the columns of the matrix in rows I1 to min(K+3,I).
						for (int j = I1; j <= I; j++) {
							double sum = A.getItem(K, j) + V2 * A.getItem(K + 1, j);
							A.setItem(K, j, A.getItem(K, j) - sum * DLARFG_Tau);
							A.setItem(K + 1, j, A.getItem(K + 1, j) - sum * T2);
						}

						if (ZZZ != null) {
							// DLAHQR:369 Accumulate transformations in the matrix Z
							for (int j = ILO; j <= IHI; j++) {
								double sum = ZZZ.getItem(K, j) + V2 * ZZZ.getItem(K + 1, j);
								ZZZ.setItem(K, j, ZZZ.getItem(K, j) - sum * DLARFG_Tau);
								ZZZ.setItem(K + 1, j, ZZZ.getItem(K + 1, j) - sum * T2);
							}
						}
					}
				}
				iteration++;

//				if (fulldump) {
//					A.printM("A after QR");
//				}
			}

			// DLAHQR:382 Failure to converge in remaining number of iterations
			if (!converged) {
				throw new Error("Failure to converge");
			}

			if (L == I) {
				// DLAHQR:391 H(I,I-1) is negligible: one eigenvalue has converged.
				WRI.setItem(I, 0, A.getItem(I, I));
				WRI.setItem(I, 1, 0.0);
			} else if (L == I - 1) {
				// DLAHQR:397 H(I-1,I-2) is negligible: a pair of eigenvalues have converged.
				// Transform the 2-by-2 submatrix to standard Schur form,
				// and compute and store the eigenvalues.
//				A.printM("BEFORE DLANV2");
				DLANV2(
						A.getItem(I - 1, I - 1),
						A.getItem(I, I - 1),
						A.getItem(I - 1, I),
						A.getItem(I, I),
						DLANV2_result);
				WRI.setItem(I - 1, 0, DLANV2_result.RT1R);
				WRI.setItem(I - 1, 1, DLANV2_result.RT1I);
				WRI.setItem(I, 0, DLANV2_result.RT2R);
				WRI.setItem(I, 1, DLANV2_result.RT2I);

				// DLAHQR:406 ??? IF( WANTT ) THEN
				// DLAHQR:408 Apply the transformation to the rest of H.
//				System.out.println("I=" + I + " I2=" + I2);
//				A.printM("BEFORE DROT A=");
				for (int i = I + 1; i <= I2; i++) {
					double tmp1 =  DLANV2_result.CC * A.getItem(i, I - 1) + DLANV2_result.CS * A.getItem(i, I);
					double tmp2 = -DLANV2_result.CS * A.getItem(i, I - 1) + DLANV2_result.CC * A.getItem(i, I);
					A.setItem(i, I - 1, tmp1);
					A.setItem(i, I, tmp2);
				}
//				A.printM("AFTER DROT A=");

				for (int j = I1; j < I; j++) {
					double tmp1 =  DLANV2_result.CS * A.getItem(I - 1, j) + DLANV2_result.CC * A.getItem(I, j);
					double tmp2 = -DLANV2_result.CC * A.getItem(I - 1, j) + DLANV2_result.CS * A.getItem(I, j);
					A.setItem(I - 1, j, tmp1);
					A.setItem(I, j, tmp2);
				}

				if (ZZZ != null) {
					// DLAHQR:417 Apply the transformation to Z.
					for (int j = DGEBAL_result.ILO; j <= DGEBAL_result.IHI; j++) {
						double tmp1 =  DLANV2_result.CS * ZZZ.getItem(I - 1, j) + DLANV2_result.CC * ZZZ.getItem(I, j);
						double tmp2 = -DLANV2_result.CC * ZZZ.getItem(I - 1, j) + DLANV2_result.CS * ZZZ.getItem(I, j);
						ZZZ.setItem(I - 1, j, tmp1);
						ZZZ.setItem(I, j, tmp2);
					}
				}
			}

			// DLAHQR:423 Decrement number of remaining iterations, and return to start of
			// the main loop with new value of I.
			I = L - 1;
			maxIterations -= iteration;
		}
	}

	public DGEBAL_Result DGEBAL_result = new DGEBAL_Result();

	public void roots(Matrix p, Matrix result) {
		if (p.getSizeY() != 1)
			throw new IllegalArgumentException("Invalid matrix size");

		int n = p.getSizeX();
		int start = 0;
		double div = 0.0;
		while (start < n) {
			div = p.getItem(start, 0);
			if (div != 0.0)
				break;
			start++;
		}
		if ((start > n) || (div == 0.0))
			throw new IllegalArgumentException("Invalid polynomial");

		int size = n - start - 1;
		Matrix A = new Matrix(size, size);
		for (int i = size - 1; i >= 0; i--) {
			A.setItem(i, 0, -p.getItem(start + i + 1, 0) / div);
			for (int j = size - 1; j > 0; j--)
				A.setItem(i, j, i + 1 == j ? 1.0 : 0.0);
		}
		A.printM("companion matrix A:");
		DGEEV(A, result);
	}
}
