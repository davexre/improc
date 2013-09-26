package example.slavi.bean;

import java.io.Serializable;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import com.slavi.util.PropertyUtil;
import com.slavi.util.io.ObjectRead;
import com.slavi.util.io.ObjectToProperties;
import com.slavi.util.io.ObjectWrite;
import com.slavi.util.testUtil.TestUtil;

public class ObjectToPropertiesTest {
	static Object doTest(String prefix, Object o) throws Exception {
		Properties properties = new Properties();
		
		ObjectWrite write = new ObjectToProperties.Write(properties, prefix);
		write.write(o);
		String s1 = PropertyUtil.propertiesToString(properties);
		System.out.println(s1);
		
		ObjectRead read = new ObjectToProperties.Read(properties, prefix);
		o = read.read();

		properties.clear();
		write = new ObjectToProperties.Write(properties, prefix);
		write.write(o);
		String s2 = PropertyUtil.propertiesToString(properties);
		TestUtil.assertEqual("First and second time conversion not equal", s1, s2);
		
//		System.out.println(s1);
		return o;
	}
	
	public String dummyProperty = "Dummy string property";
	
	public class MyBeanNested extends MyBean {
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

	public void doIt() throws Exception {
		MyBeanNested myBean;
		String prefix = "111";

		Properties properties = new Properties();
		myBean = (MyBeanNested) new ObjectToProperties.Read(properties, prefix).read();
		TestUtil.assertTrue("Expected null object", myBean == null);
		new ObjectToProperties.Write(properties, prefix).write(myBean);
		String s1 = PropertyUtil.propertiesToString(properties);
		TestUtil.assertEqual("Expected empty string", s1, "");
		
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
	}
	
	public void doHardTest() throws Exception {
		String prefix = "";

//		ArrayList list = new ArrayList();
//		for (int i = 0; i < 5; i++) {
//			myBean = new MyBeanNested();
//			myBean.initialize();
//			list.add(myBean);
//			list.add(myBean);
//		}
//		doTest(prefix, new BaseClassForSerialization().getTestObjects());

		System.out.println("-------------------------------------");
		ArrayList<Serializable> list = new BaseClassForSerialization().getTestObjects();
		System.out.println("-------------------------------------");
		Object o = doTest(prefix, list);
		System.out.println("-------------------------------------");
		BaseClassForSerialization.dumpObjects((ArrayList) o);
	}
	
	public void partialHardTest() throws Exception {
		System.out.println("*** Creating test classes");
		BaseClassForSerialization baseClass = new BaseClassForSerialization();
		BaseClassForSerialization.Class1 class1 = new BaseClassForSerialization.Class1();
		BaseClassForSerialization.Class2 class2 = baseClass.new Class2();
		BaseClassForSerialization.Class2.Class2InnerExtendsNonStatic 
			c = class2.new Class2InnerExtendsNonStatic(class1);
		
		System.out.println();
		System.out.println("*** Making copy");
		BaseClassForSerialization.Class2.Class2InnerExtendsNonStatic c2 = 
			(BaseClassForSerialization.Class2.Class2InnerExtendsNonStatic) doTest("", c);

		System.out.println();
		System.out.println("*** Invoking dump");
		c2.nestedDump("");
	}
	
	public static class BaseClass {
		public String str = "baseClass";
	}
	
	public static class ExtendingClass extends BaseClass {
		public String str = "extendingClass";
	}
	
	public void simplifiedHardTest() throws Exception {
		ExtendingClass ec1 = new ExtendingClass();
		BaseClass bc1 = ec1;
		
		System.out.println("EC.str " + ec1.str);
		System.out.println("BC.str " + bc1.str);
		
		ExtendingClass ec2 = (ExtendingClass) doTest("", ec1);
		BaseClass bc2 = ec2;
		
		System.out.println("EC.str " + ec2.str);
		System.out.println("BC.str " + bc2.str);
	}

	static void dumpgen(Object o) {
		Class c = o.getClass();
		TypeVariable vars[] = c.getTypeParameters();
		System.out.println(Arrays.toString(vars.getClass().getTypeParameters()));
	}
	
	public void typedArrayTest() throws Exception {
		ArrayList<String> str = new ArrayList<String>();
		str.add("asdqwe");
		System.out.println(Arrays.toString(str.getClass().getTypeParameters()));
		str = (ArrayList) doTest("", str);
		System.out.println(str);
		str.add("zxczxc");
		System.out.println(str);
		dumpgen(str);
	}
	
	public void serializeClass() throws Exception {
		Class ec1 = ExtendingClass.class;
		System.out.println("EC.str " + ec1);
		
		Class ec2 = (Class) doTest("", ec1);
		System.out.println("EC.str " + ec2);
		System.out.println(ec1 == ec2);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("ObjectToPropertiesTest2");
//		new ObjectToPropertiesTest2().serializeClass();
//		new ObjectToPropertiesTest2().typedArrayTest();
		new ObjectToPropertiesTest().doHardTest();
//		new ObjectToPropertiesTest2().partialHardTest();
//		new ObjectToPropertiesTest2().simplifiedHardTest();
		System.out.println("Done.");
	}
}
