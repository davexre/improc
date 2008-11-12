package com.test.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformException;
import org.jdom.transform.XSLTransformer;

import com.slavi.math.matrix.Matrix;
import com.slavi.util.ui.SwtUtil;

public class SwtUtilsTest {

	void showHtmlTest() throws XSLTransformException, IOException {
		Matrix m = new Matrix(3, 3);
		int count = 1;
		for (int j = 0; j < m.getSizeY(); j++)
			for (int i = 0; i < m.getSizeX(); i++)
				m.setItem(i, j, count++);
		
		XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());

		Element root = new Element("matrix");  
		m.toXML(root);
		
		XSLTransformer transformer = new XSLTransformer(SwtUtilsTest.class.getResourceAsStream("SwtUtilsTest.xsl"));
		Document doc = transformer.transform(new Document(root));
		ByteArrayOutputStream fou = new ByteArrayOutputStream();
		xout.output(doc.getRootElement(), fou);
		String html = fou.toString();
		fou.close();
		SwtUtil.showHTML(null, "", html);
	}
	
	void openFileTest() {
		System.out.println(SwtUtil.openFile(null, "Some title", null, null));
		System.out.println(SwtUtil.openFile(null, "Some title", null, null));
		System.out.println(SwtUtil.openFile(null, "Some title", null, null));
	}
	
	public static void main(String[] args) throws Exception {
		SwtUtilsTest test = new SwtUtilsTest();
		test.showHtmlTest();
		test.openFileTest();
	}
}
