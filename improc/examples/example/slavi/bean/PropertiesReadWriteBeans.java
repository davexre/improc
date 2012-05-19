package example.slavi.bean;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import com.slavi.TestUtils;
import com.slavi.io.SimpleBeanToProperties;

public class PropertiesReadWriteBeans {
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
	
	public static MyBean createMyBean() {
		MyBean r = new MyBean();
		r.id = 111;
		r.name = "kuku";
		r.myEnum = MyEnum.MyEnum2;
		r.myData = new MyData(123, "alabala");

		int intArray[] = new int[5];
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = i;
		}
		r.arrayProperty = intArray;

		intArray = new int[3];
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = i + 2;
		}
		r.indexProperty = intArray;

		MyData myDataArray[] = new MyData[5];
		for (int i = 0; i < myDataArray.length; i++) {
			myDataArray[i] = new MyData(i, "ID " + i);
		}
		r.objectArray = myDataArray;
		
		myDataArray = new MyData[3];
		for (int i = 0; i < myDataArray.length; i++) {
			myDataArray[i] = new MyData(i + 3, "ID " + (i + 3));
		}
		r.objectArray = myDataArray;
		
		return r;
	}
	
	static void doTest(String prefix, MyBean myBean) throws Exception {
		Properties properties = new Properties();
		SimpleBeanToProperties.objectToProperties(properties, prefix, myBean);
		String s1 = propertiesToString(properties);
		
		ByteArrayInputStream fin = new ByteArrayInputStream(s1.getBytes());
		properties.clear();
		properties.load(fin);
		myBean = SimpleBeanToProperties.propertiesToObject(properties, prefix, MyBean.class, true);

		properties.clear();
		SimpleBeanToProperties.objectToProperties(properties, prefix, myBean);

		String s2 = propertiesToString(properties);
		TestUtils.assertTrue("First and second time conversion not equal", s1.equals(s2));

		System.out.println(s1);
	}
	
	public static void main(String[] args) throws Exception {
		MyBean myBean;
		String prefix = "111";
		
		myBean = new MyBean();
		doTest(prefix, myBean);

		myBean = createMyBean();
		doTest(prefix, myBean);
		
		myBean = createMyBean();
		myBean.objectArray[1] = null;
		myBean.myEnum = null;
		doTest(prefix, myBean);
		
		myBean = createMyBean();
		myBean.objectArray = null;
		doTest(prefix, myBean);

		System.out.println("Done.");
	}
}
