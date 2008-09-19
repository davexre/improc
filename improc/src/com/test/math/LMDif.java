package com.test.math;

import com.slavi.math.matrix.Matrix;

public class LMDif {

	public void qrsolv(Matrix r, Matrix diag, Matrix x, Matrix qtb) {
		int m = r.getSizeY();
		int n = r.getSizeX();
		
		if ((m < n) ||
			(qtb.getSizeX() != n) || (qtb.getSizeX() != 1) ||
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
			
			// solve the triangular system for z. if the system is
			// singular, then obtain a least squares solution.
			
		}
	}
	
	
}
