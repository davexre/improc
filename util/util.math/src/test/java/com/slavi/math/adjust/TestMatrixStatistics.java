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
		Matrix tmp = new Matrix(10, 1);
		Random rnd = new Random();
		for (int c = 0; c < tmp.getSizeX(); c++) {
			double d = rnd.nextDouble();
			tmp.setItem(c, 0, d);
			s.addValue(d);
			m.setVectorItem(0, d);
			for (int i = 1; i < m.getVectorSize(); i++)
				m.setVectorItem(i, rnd.nextDouble());
			ms.addValue(m);
		}
		System.out.println(tmp.toMatlabString("m"));
		s.stop();
		ms.stop();
		
		System.out.println(ms.sumValues1);
		System.out.println(ms.sumValues2);
		System.out.println(ms.toString(Statistics.CStatAll));
		System.out.println(s.toString(Statistics.CStatAll));
	}

	public static void main(String[] args) throws Exception {
		new TestMatrixStatistics().doIt();
		System.out.println("Done.");
	}
}
