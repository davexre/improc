package com.slavi.util;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
		Assert.assertEquals("", "", PropertyUtil.substituteVars("", p));
		Assert.assertEquals("", "Static text Кирилица", PropertyUtil.substituteVars("Static text Кирилица", p));
		Assert.assertEquals("", "text${}text", PropertyUtil.substituteVars("text${}text", p));
		Assert.assertEquals("", "text${invalidVariable:10}text", PropertyUtil.substituteVars("text${invalidVariable:10}text", p));
		Assert.assertEquals("", "text  123text", PropertyUtil.substituteVars("text${  Value  :  5  }text", p));
		Assert.assertEquals("", "text123         text", PropertyUtil.substituteVars("text${ Value:L }text", p));
		String str = "${ Value:L }";
		Assert.assertEquals("", str.length(), PropertyUtil.substituteVars(str, p).length());
		str = "${ Value:R }";
		Assert.assertEquals("", str.length(), PropertyUtil.substituteVars(str, p).length());
		Assert.assertEquals("", "text     A Value 2 Atext", PropertyUtil.substituteVars("text${ VAL$${UseVal2}:15 }text", p));
		Assert.assertEquals("", "text     A Value 2 Atext", PropertyUtil.substituteVars("text${VAL$${UseVal2}:15}text", p));
		Assert.assertEquals("", "textValue 1text", PropertyUtil.substituteVars("text${ ${VAL$${ UseVal0:L }} }text", p));
		Assert.assertEquals("", "textValue 1text", PropertyUtil.substituteVars("text${${VAL$${ UseVal0:L }}}text", p));
		Assert.assertEquals("", "textValue 1text", PropertyUtil.substituteVars("text${${VAL$${ UseVal0 }${ UseVal2 }}}text", p));
		Assert.assertEquals("", "${Recursive}", PropertyUtil.substituteVars("${Recursive}", p));
		Assert.assertEquals("", "text${}text", PropertyUtil.substituteVars("text${}text", p));
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
