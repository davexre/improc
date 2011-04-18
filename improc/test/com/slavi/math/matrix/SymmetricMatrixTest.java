package com.slavi.math.matrix;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;

import com.slavi.TestUtils;
import com.slavi.io.xml.XMLHelper;
import com.slavi.io.xml.XMLMatrix;

public class SymmetricMatrixTest {

	Matrix m;
	
	SymmetricMatrix tm;
	
	@Before
	public void setUp() throws Exception {
		BufferedReader fin = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("SymmetricMatrixTest.txt")));
		m = new Matrix(3, 3);
		tm = new SymmetricMatrix(3);
		m.load(fin);
		fin.readLine();
		tm.load(fin);
		fin.close();		
	}

	@Test
	public void testMatrixInverse() {
		Matrix a = m.makeCopy();
		Matrix b = m.makeCopy();
		TestUtils.assertTrue("Failed to inverse the matrix", a.inverse());
		a.mMul(m, b);
		TestUtils.assertMatrixE("Inverse matrix incorrect", b);
	}

	@Test
	public void testTriangularMatrixInverse() throws Exception {
		SymmetricMatrix ta = tm.makeCopy();
		SymmetricMatrix tb = tm.makeCopy();
		TestUtils.assertTrue("Failed to inverse the matrix", ta.inverse());
		ta.mMul(tm, tb);
		TestUtils.assertMatrixE("Inverse matrix incorrect", tb.makeSquareMatrix());
	}

	@Test
	public void matrixToXML() throws Exception {
		Element root = new Element("matrix");
		XMLMatrix.instance.toXML(m, root);
		ByteArrayOutputStream fou = new ByteArrayOutputStream();
		XMLHelper.writeXML(fou, root, "matrix.xsl");

		ByteArrayInputStream fin = new ByteArrayInputStream(fou.toByteArray());
		root = XMLHelper.readXML(fin);
		Matrix m2 = XMLMatrix.instance.fromXML(root);
		m.mSub(m2, m2);
		TestUtils.assertMatrix0("", m2);		
	}

}
