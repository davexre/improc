package com.slavi.math.adjust;

import java.util.Random;

import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class TestMatrixStatistics {

	void doIt() throws Exception {
		Matrix m = new Matrix(1, 1);
		Statistics s = new Statistics();
		MatrixStatistics ms = new MatrixStatistics();
		
		s.start();
		ms.start();
		Random rnd = new Random();
		for (int c = 0; c < 100; c++) {
			double d = rnd.nextDouble();
			s.addValue(d);
			m.setVectorItem(0, d);
			for (int i = 1; i < m.getVectorSize(); i++)
				m.setVectorItem(i, rnd.nextDouble());
			ms.addValue(m);
		}
		s.stop();
		ms.stop();
		
		System.out.println(ms.toString(Statistics.CStatAll));
		System.out.println(s.toString(Statistics.CStatAll));
	}

	public static void main(String[] args) throws Exception {
		new TestMatrixStatistics().doIt();
		System.out.println("Done.");
	}
}
