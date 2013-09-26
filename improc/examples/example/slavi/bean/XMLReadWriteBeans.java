package example.slavi.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.jdom.Element;

import com.slavi.util.io.ObjectRead;
import com.slavi.util.io.ObjectWrite;
import com.slavi.util.io.SimpleBeanToXML;
import com.slavi.util.testUtil.TestUtil;
import com.slavi.util.xml.XMLHelper;

public class XMLReadWriteBeans {
	static void doTest(Object myBean) throws Exception {
		Element root = new Element("obj");
		
		ObjectWrite write = new SimpleBeanToXML.Write(root);
		write.write(myBean);
		ByteArrayOutputStream fou = new ByteArrayOutputStream();
		XMLHelper.writeXML(fou, root, null); //, "dummy.xml");
		String s1 = new String(fou.toByteArray());

		ByteArrayInputStream fin = new ByteArrayInputStream(fou.toByteArray());
		root = XMLHelper.readXML(fin);
		ObjectRead read = new SimpleBeanToXML.Read(root);
		myBean = read.read();

		root = new Element("obj");
		write = new SimpleBeanToXML.Write(root);
		write.write(myBean);
		fou = new ByteArrayOutputStream();
		XMLHelper.writeXML(fou, root, null); //, "dummy.xml");
		String s2 = new String(fou.toByteArray());
		TestUtil.assertTrue("First and second time conversion not equal", s1.equals(s2));
		System.out.println(s1);
	}
	
	public static void main(String[] args) throws Exception {
		MyBean myBean;

		doTest(null);
		
		Element root = null;
		myBean = SimpleBeanToXML.xmlToObject(root, MyBean.class, true);
		TestUtil.assertTrue("Expected null object", myBean == null);
		root = new Element("obj");
		SimpleBeanToXML.objectToXml(root, myBean);
		TestUtil.assertTrue("Expected no children in xml element", root.getChildren().size() == 0);
		
		myBean = new MyBean();
		doTest(myBean);
		
		myBean = new MyBean();
		myBean.initialize();
		doTest(myBean);

		myBean = new MyBean();
		myBean.initialize();
		myBean.getMyDataArray()[1] = null;
		myBean.setMyDataIndexPropertyNoArrayWrite(2, null);
		myBean.setMyData(null);
		myBean.setMyEnum(null);
		doTest(myBean);

		myBean = new MyBean();
		myBean.initialize();
		myBean.setMyDataArray(null);
		doTest(myBean);

		System.out.println("Done.");
	}
}
