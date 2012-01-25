package com.slavi.util;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.slavi.TestUtils;

public class PropertyUtilTest {

	Properties p;
	
	@Before
	public void prepare() {
		p = new Properties();
	    p.setProperty("V.0", "Value 0");
		p.setProperty("V.1", "Value 1");
		p.setProperty("V.2", "Value 2");

		p.setProperty("Value", "123");
		p.setProperty("VAL$0", "V.1");
		p.setProperty("VAL$2", "A ${V.2} A");
		p.setProperty("VAL$02", "V.1");
		p.setProperty("Кирилица$0", "Value 1");
		p.setProperty("UseVal0", "0");
		p.setProperty("UseVal2", "2");
		p.setProperty("Recursive", "${Recursive}");
		p.setProperty("Z", "|${ Кирилица$${UseVal2}:L   }|");
		p.setProperty("W", "${\nVAL$${UseVal2}\n} = ${UseVal2}");
	}
	
	@Test 
	public void testSubstituteVars() {
		TestUtils.assertEqual("", "", PropertyUtil.substituteVars("", p));
		TestUtils.assertEqual("", "Static text Кирилица", PropertyUtil.substituteVars("Static text Кирилица", p));
		TestUtils.assertEqual("", "text${}text", PropertyUtil.substituteVars("text${}text", p));
		TestUtils.assertEqual("", "text${invalidVariable:10}text", PropertyUtil.substituteVars("text${invalidVariable:10}text", p));
		TestUtils.assertEqual("", "text  123text", PropertyUtil.substituteVars("text${  Value  :  5  }text", p));
		TestUtils.assertEqual("", "text123         text", PropertyUtil.substituteVars("text${ Value:L }text", p));
		String str = "${ Value:L }";
		TestUtils.assertEqual("", str.length(), PropertyUtil.substituteVars(str, p).length());
		str = "${ Value:R }";
		TestUtils.assertEqual("", str.length(), PropertyUtil.substituteVars(str, p).length());
		TestUtils.assertEqual("", "text     A Value 2 Atext", PropertyUtil.substituteVars("text${ VAL$${UseVal2}:15 }text", p));
		TestUtils.assertEqual("", "text     A Value 2 Atext", PropertyUtil.substituteVars("text${VAL$${UseVal2}:15}text", p));
		TestUtils.assertEqual("", "textValue 1text", PropertyUtil.substituteVars("text${ ${VAL$${ UseVal0:L }} }text", p));
		TestUtils.assertEqual("", "textValue 1text", PropertyUtil.substituteVars("text${${VAL$${ UseVal0:L }}}text", p));
		TestUtils.assertEqual("", "textValue 1text", PropertyUtil.substituteVars("text${${VAL$${ UseVal0 }${ UseVal2 }}}text", p));
		TestUtils.assertEqual("", "${Recursive}", PropertyUtil.substituteVars("${Recursive}", p));
		TestUtils.assertEqual("", "text${}text", PropertyUtil.substituteVars("text${}text", p));
	}
/*
	void dummy() {
		final Pattern vars = Pattern.compile("\\$\\{(((?!\\$\\{)[^}])+)\\}");
		String str = "text${ ${VAL$0    }}text";
		Matcher m = vars.matcher(str);
		System.out.println(m.find());
		System.out.println("Start:     |" + str.substring(0, m.start()) + "|");
		for (int i = 0; i < m.groupCount(); i++) {
			System.out.println("Group[" + i + "]: |" + m.group(i) + "|");
		}
		System.out.println("End:       |" + str.substring(m.end()) + "|");
	}
	
	public static void main(String[] args) {
		PropertyUtilTest test = new PropertyUtilTest();
		test.prepare();
//		test.dummy();
		test.testSubstituteVars();
	}
*/
}
