package example.java.lang;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.junit.Assert;

import com.slavi.util.Marker;
import com.slavi.util.testUtil.TestUtil;

public class EnumerationVsIterator {
	public static void main(String[] args) {
		Vector<String> data = new Vector<String>();
//		ArrayList<String> data = new ArrayList<String>();
		Enumeration<?> enum1;
		Iterator<?> iter;

		for (int i = 0; i < 1000000; i++) {
			data.add("New Element");
		}

		// --------------
		int count1 = 0;
		Marker.mark("ITERATOR 1");
		for (int i = 0; i < 100; i++) {
			iter = data.iterator();
			while (iter.hasNext()) {
				Object dummy = iter.next();
				if (dummy != null)
					count1++;
			}
		}
		Marker.release();

		// --------------
		int count2 = 0;
		Marker.mark("ITERATOR 2");
		for (int i = 0; i < 100; i++) {
			for (Object dummy : data) {
				if (dummy != null)
					count2++;
			}
		}
		Marker.release();
		Assert.assertTrue("", count1 == count2);

		// --------------
		int count3 = 0;
		Marker.mark("ENUMERATION");
		for (int i = 0; i < 100; i++) {
			enum1 = data.elements();
			while (enum1.hasMoreElements()) {
				Object dummy = enum1.nextElement();
				if (dummy != null)
					count3++;
			}
		}
		Marker.release();
		
		Assert.assertTrue("", count3 == count2);
	}
}
