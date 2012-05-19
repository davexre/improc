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
	
	public static void main(String[] args) throws Exception {
		String prefix = "";
		MyBean myBean = new MyBean(111, "kuku", new MyData(123, "alabala"), MyEnum.MyEnum2);
		for (int i = 0; i < myBean.intArray.length; i++) {
			myBean.intArray[i] = i;
		}
		
		Properties properties = new Properties();
		SimpleBeanToProperties.objectToProperties(properties, prefix, myBean);
		String s1 = propertiesToString(properties);
		System.out.println(s1);
		
		ByteArrayInputStream fin = new ByteArrayInputStream(s1.getBytes());
		properties.clear();
		properties.load(fin);
//		myBean = new MyBean();
		myBean = SimpleBeanToProperties.propertiesToObject(properties, prefix, MyBean.class);

		properties.clear();
		SimpleBeanToProperties.objectToProperties(properties, prefix, myBean);

		String s2 = propertiesToString(properties);
		System.out.println(s2);
		System.out.println(myBean.getMyEnum());
		TestUtils.assertTrue("First and second time conversion not equal", s1.equals(s2));
	}
}
