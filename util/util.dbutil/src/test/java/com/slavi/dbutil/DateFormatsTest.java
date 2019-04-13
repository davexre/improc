package com.slavi.dbutil;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import com.slavi.dbutil.DateFormats;

public class DateFormatsTest {

	static final String dfs[] = { "yyyy-MM-dd", "yyyy/MM/dd" };

	@Test(expected = ParseException.class)
	public void testInvalid() throws ParseException {
		DateFormats df = new DateFormats(dfs);
		df.setThrowsException(true);
		df.parse("2019-01-44");
		df.parse(" ");
		df.parse("");
		df.parse(null);
	}

	@Test
	public void testFormats() throws ParseException {
		DateFormats df = new DateFormats(dfs);
		df.parse("2019-01-01");
		df.parse("2019/01/01");
		Assert.assertNull(df.parse("2019/01/44"));
		Assert.assertNull(df.parse(" "));
		Assert.assertNull(df.parse(""));
		Assert.assertNull(df.parse(null));
	}
}
