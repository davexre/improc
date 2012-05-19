package example.slavi.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.jdom.Element;

import com.slavi.TestUtils;
import com.slavi.io.SimpleBeanToXML;
import com.slavi.io.xml.XMLHelper;

public class XMLReadWriteBeans {
	static void doTest(MyBean myBean) throws Exception {
		Element root = new Element("obj");
		SimpleBeanToXML.objectToXml(root, myBean);
		ByteArrayOutputStream fou = new ByteArrayOutputStream();
		XMLHelper.writeXML(fou, root, null); //, "dummy.xml");
		String s1 = new String(fou.toByteArray());
		
		ByteArrayInputStream fin = new ByteArrayInputStream(fou.toByteArray());
		root = XMLHelper.readXML(fin);
		myBean = SimpleBeanToXML.xmlToObject(root, MyBean.class, true);

		root = new Element("obj");
		SimpleBeanToXML.objectToXml(root, myBean);
		fou = new ByteArrayOutputStream();
		XMLHelper.writeXML(fou, root, null); //, "dummy.xml");

		String s2 = new String(fou.toByteArray());
		TestUtils.assertTrue("First and second time conversion not equal", s1.equals(s2));
		
		System.out.println(s1);
	}
	
	public static void main(String[] args) throws Exception {
		MyBean myBean;
		
		myBean = new MyBean();
		doTest(myBean);
		
		myBean = PropertiesReadWriteBeans.createMyBean();
		doTest(myBean);

		myBean = PropertiesReadWriteBeans.createMyBean();
		myBean.objectArray[1] = null;
		myBean.myEnum = null;
		doTest(myBean);
		
		System.out.println("Done.");
	}
}
