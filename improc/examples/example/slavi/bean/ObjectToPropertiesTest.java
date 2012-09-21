package example.slavi.bean;

import java.util.Properties;

import com.slavi.TestUtils;
import com.slavi.io.ObjectToProperties;
import com.slavi.io.ObjectRead;
import com.slavi.io.ObjectWrite;
import com.slavi.util.PropertyUtil;

public class ObjectToPropertiesTest {
	static void doTest(String prefix, Object myBean) throws Exception {
		Properties properties = new Properties();
		
		ObjectWrite write = new ObjectToProperties.Write(properties);
		write.write(myBean);
		String s1 = PropertyUtil.propertiesToString(properties);

		ObjectRead read = new ObjectToProperties.Read(properties);
		myBean = read.read();

		properties.clear();
		write = new ObjectToProperties.Write(properties);
		write.write(myBean);
		String s2 = PropertyUtil.propertiesToString(properties);
		TestUtils.assertEqual("First and second time conversion not equal", s1, s2);
		
		System.out.println(s1);
	}
	
	public static class MyBeanNested extends MyBean {
		Object objects[] = new Object[5];
		private String strs[][] = new String[3][5];
		
		public MyBeanNested() {
			for (int i = 0; i < strs.length; i++) {
				Object ob[] = strs[i];
				for (int j = 0; j < ob.length; j++) {
					ob[j] = Integer.toString(i) + "$" + Integer.toString(j);
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("ObjectToPropertiesTest");

		MyBeanNested myBean;
		String prefix = "111";

		Properties properties = new Properties();
		myBean = (MyBeanNested) new ObjectToProperties.Read(properties).read();
		TestUtils.assertTrue("Expected null object", myBean == null);
		new ObjectToProperties.Write(properties).write(myBean);
		String s1 = PropertyUtil.propertiesToString(properties);
		TestUtils.assertEqual("Expected empty string", s1, "");
		
		myBean = new MyBeanNested();
		doTest(prefix, myBean);

		myBean = new MyBeanNested();
		myBean.initialize();
		doTest(prefix, myBean);

		myBean = new MyBeanNested();
		myBean.initialize();
		myBean.getMyDataArray()[1] = null;
		myBean.setMyDataIndexPropertyNoArrayWrite(2, null);
		myBean.objects[0] = "123qwe";
		myBean.objects[1] = myBean.getMyData();
		myBean.setMyEnum(null);
		
		doTest(prefix, myBean);
		
		myBean = new MyBeanNested();
		myBean.initialize();
		myBean.setMyDataArray(null);
		doTest(prefix, myBean);

		System.out.println("Done.");
	}
}
