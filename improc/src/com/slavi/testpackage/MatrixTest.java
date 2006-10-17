package com.slavi.testpackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.ProcessingInstruction;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.slavi.matrix.DiagonalMatrix;
import com.slavi.matrix.Matrix;

public class MatrixTest {

	public static MatrixTest mt;

	public Matrix m;

	public DiagonalMatrix tm;

	public MatrixTest() throws IOException {
		m = new Matrix(3, 3);
		tm = new DiagonalMatrix(3);
		BufferedReader fin = new BufferedReader(new FileReader(getClass()
				.getResource("MatrixTest.txt").getFile()));
		m.load(fin);
		fin.readLine();
		tm.load(fin);
		fin.close();
	}

	public void testTriangularMatrixInverse() throws IOException {
		DiagonalMatrix ta, tb;
		System.out.println("*** Source ***");
		tm.save(System.out);
		ta = tm.makeCopy();
		tb = tm.makeCopy();
		if (ta.inverse()) {
			ta.mMul(tm, tb);
			System.out.println("*** Inverse ***");
			ta.save(System.out);
			System.out.println("*** Source * Inverse ***");
			tb.save(System.out);
		} else
			System.out.println("*** Failed to inverse the matrix");
	}

	public void testMatrixInverse() throws IOException {
		Matrix a, b;
		System.out.println("*** Source ***");
		m.save(System.out);
		a = m.makeCopy();
		b = m.makeCopy();
		if (a.inverse()) {
			a.mMax(m, b);
			System.out.println("*** Inverse ***");
			a.save(System.out);
			System.out.println("*** Source * Inverse ***");
			b.save(System.out);
		} else
			System.out.println("*** Failed to inverse the matrix");
	}

	public static void matrixToXML() throws FileNotFoundException, IOException {
		Matrix m = new Matrix(5, 4);
		m.makeE();

		Document doc = new Document();
		Element root = new Element("matrix");
		m.toXML(root);
		doc.setRootElement(root);
		XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
		xout.output(doc, new FileOutputStream(new File("c:/asd.xml")));
	}
	
	public static void matrixFromXML() throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder(false);
		Document doc = builder.build(new File("c:/asd.xml"));  

		Element matrix = doc.getRootElement();
		Matrix m = Matrix.fromXML(matrix);
		System.out.println(m.toString());
	}
	
//	public static void main1(String[] args) throws Exception {
//		MatrixTest.mt = new MatrixTest();
//		MatrixTest.mt.testTriangularMatrixInverse();
//
//		DiagonalMatrix a;
//		a = MatrixTest.mt.tm;
//		a.save(System.out);
//		a.exchangeX(2, 1);
//		a.save(System.out);
//		a.exchangeX(2, 1);
//		a.save(System.out);
//
//		BufferedReader fin = new BufferedReader(new FileReader(MatrixTest.class
//				.getResource("MatrixTest.txt").getFile()));
//		for (int i = 0; i < 2; i++) {
//			StreamTokenizer st = new StreamTokenizer(fin);
//			for (int j = 0; j < 9; j++) {
//				if (st.nextToken() == StreamTokenizer.TT_NUMBER)
//					System.out.println(st.nval);
//				else
//					throw new Exception("BAD");
//			}
//			fin.readLine();
//		}
//	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Matrix m = new Matrix(3, 3);
		m.makeE();

		Document doc = new Document();
		Content c = new ProcessingInstruction("xml-stylesheet", "href=\"matrix.xsl\" type=\"text/xsl\"");
		doc.addContent(c);
		Element root = new Element("Matrix");
		m.toXML(root);
		doc.setRootElement(root);

		//doc.getContent().add(0,e);
		
		XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
		xout.output(doc, new FileOutputStream(new File("./../../output/matrix.xml")));
	}
}
