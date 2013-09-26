package example.slavi.bean;

import java.util.Properties;

import com.slavi.util.PropertyUtil;
import com.slavi.util.io.ObjectRead;
import com.slavi.util.io.ObjectWrite;
import com.slavi.util.io.SimpleBeanToProperties;
import com.slavi.util.testUtil.TestUtil;

public class SimpleBeanToPropertiesTest {
	static void doTest(String prefix, Object myBean) throws Exception {
		Properties properties = new Properties();
		
		ObjectWrite write = new SimpleBeanToProperties.Write(properties);
		write.write(myBean);
		String s1 = PropertyUtil.propertiesToString(properties);
		
		ObjectRead read = new SimpleBeanToProperties.Read(properties);
		myBean = read.read();

		properties.clear();
		write = new SimpleBeanToProperties.Write(properties);
		write.write(myBean);
		String s2 = PropertyUtil.propertiesToString(properties);
		TestUtil.assertEqual("First and second time conversion not equal", s1, s2);
		
		System.out.println(s1);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("SimpleBeanToPropertiesTest");
		
		MyBean myBean;
		String prefix = "111";
		
		Properties properties = new Properties();
		myBean = SimpleBeanToProperties.propertiesToObject(properties, prefix, MyBean.class, true);
		TestUtil.assertTrue("Expected null object", myBean == null);
		SimpleBeanToProperties.objectToProperties(properties, prefix, myBean);
		String s1 = PropertyUtil.propertiesToString(properties);
		TestUtil.assertEqual("Expected empty string", s1, "");
		
		myBean = new MyBean();
		doTest(prefix, myBean);

		myBean = new MyBean();
		myBean.initialize();
		doTest(prefix, myBean);
		
		myBean = new MyBean();
		myBean.initialize();
		myBean.getMyDataArray()[1] = null;
		myBean.setMyDataIndexPropertyNoArrayWrite(2, null);
		myBean.setMyData(null);
		myBean.setMyEnum(null);
		doTest(prefix, myBean);
		
		myBean = new MyBean();
		myBean.initialize();
		myBean.setMyDataArray(null);
		doTest(prefix, myBean);

		System.out.println("Done.");
	}
}
