package com.slavi.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

public class DeepCopyTest {

	public static class CopyMe implements Serializable {
		final String asd = "qwe";
		
		public String aString;
		
		int aInteger;
		
		protected double aDouble;
		
		private CopyMe aObject;

		private CopyMe() {
			System.out.println("CopyMe create");
		}
		
		public CopyMe getaObject() {
			return aObject;
		}

		public void setaObject(CopyMe aObject) {
			this.aObject = aObject;
		}
	}
	
	@Test
	public void testDeepCopy() throws Exception {
		CopyMe c1 = new CopyMe();
		CopyMe c2 = new CopyMe();
		
		c1.aString = "c1";
		c1.aInteger = 123;
		c1.aDouble = 234.567;
		c1.setaObject(c2);

		c2.aString = "c2";
		c2.aInteger = 111;
		c2.aDouble = 222.333;
		
		ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
		ObjectOutputStream oos1 = new ObjectOutputStream(bos1);
		oos1.writeObject(c1);
		oos1.close();
		
		c2 = Util.deepCopy(c1);

		ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
		ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
		oos2.writeObject(c2);
		oos2.close();
	
		Charset cs = Charset.forName("ISO-8859-5");
		
		Assert.assertArrayEquals("Objects differ", bos1.toByteArray(), bos2.toByteArray());
		String str = new String(bos1.toByteArray(), cs);
		str = str.replaceAll("qwe", "zxc");
		System.out.println(bos1.toByteArray().length);
		System.out.println(str.getBytes(cs).length);
		ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes(cs));
		ObjectInputStream ois = new ObjectInputStream(bis);
		System.out.println("reading...");
		CopyMe c3 = (CopyMe) ois.readObject();
		System.out.println(c1.asd);
		System.out.println(c3.asd);
		
	}
}
