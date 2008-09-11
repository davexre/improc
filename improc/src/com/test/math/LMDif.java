package com.test.math;

import com.slavi.math.matrix.Matrix;

public class LMDif {

	public void qrsolv(Matrix r, Matrix b, Matrix d, Matrix x) {
		int m = r.getSizeY();
		int n = r.getSizeX();
		
		if ((m < n) ||
			(b.getSizeX() != 1) || (b.getSizeY() != m) ||
			(d.getSizeX() != 1) || (d.getSizeY() != n)) {
			throw new IllegalArgumentException();
		}
		x.resize(1, n);
		
		// copy r and (q transpose)*b to preserve input and initialize s.
		// in particular, save the diagonal elements of r in x.

		for (int i = r.getSizeX() - 1; i >= 0; i--)
			for (int j = i - 1; j >= 0; j--)
				r.setItem(j, i, r.getItem(i, j));
	}
	
	
}
