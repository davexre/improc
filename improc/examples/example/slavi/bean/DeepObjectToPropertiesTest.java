package example.slavi.bean;

import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import com.slavi.TestUtils;
import com.slavi.io.DeepObjectToProperties;

public class DeepObjectToPropertiesTest {
	static String propertiesToString(Properties properties) throws IOException {
		ByteArrayOutputStream fou = new ByteArrayOutputStream();
		properties.store(fou, "");
		BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fou.toByteArray())));
		ArrayList<String> lines = new ArrayList<String>();
		while (true) {
			String line = in.readLine();
			if (line == null)
				break;
			line = line.trim();
			if (line.startsWith("#"))
				continue;
			lines.add(line);
		}
		Collections.sort(lines);
		StringBuilder sb = new StringBuilder();
		for (String line : lines) { 
			sb.append(line);
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public MyBeanNested createMyBean() {
		MyBeanNested r = new MyBeanNested();
		r.setIntProperty(111);
		r.setStringProperty("kuku");
		r.setBoolProperty(true);
		r.setMyEnum(MyEnum.MyEnum2);
		r.setMyData(new MyData(123, "alabala"));

		int intArray[] = new int[5];
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = i;
		}
		r.setIntArrayProperty(intArray);

		intArray = new int[3];
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = i + 2;
		}
		r.setIntIndexProperty(intArray);

		MyData myDataArray[] = new MyData[5];
		for (int i = 0; i < myDataArray.length; i++) {
			myDataArray[i] = new MyData(i, "ID " + i);
		}
		r.setMyDataArray(myDataArray);
		
		myDataArray = new MyData[3];
		for (int i = 0; i < myDataArray.length; i++) {
			myDataArray[i] = new MyData(i + 3, "ID " + (i + 3));
		}
		r.setMyDataIndexProperty(myDataArray);
		
		myDataArray = new MyData[4];
		for (int i = 0; i < myDataArray.length; i++) {
			myDataArray[i] = new MyData(i + 5, "ID " + (i + 5));
		}
		r.setMyDataIndexPropertyNoArrayWriteSize(myDataArray.length);
		for (int i = 0; i < myDataArray.length; i++)
			r.setMyDataIndexPropertyNoArrayWrite(i, myDataArray[i]);
		
		return r;
	}
	
	void doTest(String prefix, MyBeanNested myBean) throws Exception {
		Properties properties = new Properties();
		DeepObjectToProperties dp;
		dp = new DeepObjectToProperties();
		System.out.println("=======");
		dp.objectToProperties(properties, prefix, myBean);
		Object obj[] = new Object[dp.writeObjectIds.size()];
		for (Map.Entry<Object, Integer> i : dp.writeObjectIds.entrySet()) {
			obj[i.getValue() - 1] = i.getKey();
		}
		String s1 = propertiesToString(properties);
		System.out.println(s1);
		
		ByteArrayInputStream fin = new ByteArrayInputStream(s1.getBytes());
		properties.clear();
		properties.load(fin);
		dp = new DeepObjectToProperties();
		System.out.println("-----");
		myBean = (MyBeanNested) dp.propertiesToObject(properties, prefix, true);
		System.out.println("++++++++");
		
		MyData d = (MyData) myBean.objects[1];
		System.out.println(d.getName());
		System.out.println(myBean.getMyData().getName());
		
		properties.clear();
		dp = new DeepObjectToProperties();
		dp.objectToProperties(properties, prefix, myBean);

		String s2 = propertiesToString(properties);
		TestUtils.assertEqual("First and second time conversion not equal", s1, s2);

		System.out.println(s1);
	}
	
	public static class MyBeanNested extends MyBean {
		private Object objects[] = new Object[3];
	}
	
	void doAll() throws Exception {
		MyBeanNested myBean;
		String prefix = "111";
/*
		Properties properties = new Properties();
		myBean = (MyBeanNested) DeepObjectToProperties.propertiesToObject(properties, prefix, true);
		TestUtils.assertTrue("Expected null object", myBean == null);
		DeepObjectToProperties.DeepObjectToProperties(properties, prefix, myBean);
		String s1 = propertiesToString(properties);
		TestUtils.assertEqual("Expected empty string", s1, "");
		
		myBean = new MyBeanNested();
		doTest(prefix, myBean);

		myBean = createMyBean();
		doTest(prefix, myBean);
*/
		myBean = createMyBean();
		myBean.getMyDataArray()[1] = null;
		myBean.setMyDataIndexPropertyNoArrayWrite(2, null);
		myBean.objects[0] = "123qwe";
		myBean.objects[1] = myBean.getMyData();
		myBean.setMyEnum(null);
		
		doTest(prefix, myBean);
		
		myBean = createMyBean();
		myBean.setMyDataArray(null);
		doTest(prefix, myBean);
	}
	
	public static void main(String[] args) throws Exception {
		DeepObjectToPropertiesTest test = new DeepObjectToPropertiesTest();
		test.doAll();
		System.out.println("Done.");
	}
}
