package example.slavi.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.jdom.Element;

import com.slavi.TestUtils;
import com.slavi.io.SimpleBeanToXML;
import com.slavi.io.xml.XMLHelper;

public class XMLReadWriteBeans {
	public static void main(String[] args) throws Exception {
		MyBean myBean = new MyBean(111, "kuku", new MyData(123, "alabala"));
		
		Element root = new Element("obj");
		SimpleBeanToXML.objectToXml(root, myBean);
		ByteArrayOutputStream fou = new ByteArrayOutputStream();
		XMLHelper.writeXML(fou, root, null); //, "dummy.xml");

		String s1 = new String(fou.toByteArray());
		System.out.println(s1);
		
		ByteArrayInputStream fin = new ByteArrayInputStream(fou.toByteArray());
		root = XMLHelper.readXML(fin);
		myBean = new MyBean();
		SimpleBeanToXML.xmlToObject(root, myBean);

		root = new Element("obj");
		SimpleBeanToXML.objectToXml(root, myBean);
		fou = new ByteArrayOutputStream();
		XMLHelper.writeXML(fou, root, null); //, "dummy.xml");

		String s2 = new String(fou.toByteArray());
		System.out.println(s2);
		
		TestUtils.assertTrue("First and second time conversion not equal", s1.equals(s2));
	}
}
