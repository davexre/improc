package com.slavi.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import org.jdom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.slavi.io.xml.XMLMatrix;
import com.slavi.math.matrix.Matrix;
import com.slavi.util.xml.XMLHelper;

public class XMLMatrixTest {
	public static double precision = 1.0 / 10000.0;

	Matrix m;

	@Before
	public void setUp() throws Exception {
		BufferedReader fin = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("SymmetricMatrixTest.txt")));
		m = new Matrix(3, 3);
		m.load(fin);
		fin.readLine();
		fin.close();
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
		Assert.assertTrue(m2.is0(precision));
	}
}
