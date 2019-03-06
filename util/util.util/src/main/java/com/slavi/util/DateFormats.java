package com.slavi.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormats extends DateFormat {

	DateFormat[] dateFormats;

	public DateFormats(String... dateFormatPatterns) {
		this(Locale.getDefault(Locale.Category.FORMAT), dateFormatPatterns);
	}

	public DateFormats(Locale locale, String... dateFormatPatterns) {
		dateFormats = new DateFormat[dateFormatPatterns.length];
		for (int i = 0; i < dateFormatPatterns.length; i++)
			dateFormats[i] = new SimpleDateFormat(dateFormatPatterns[i], locale);
	}

	public DateFormats(DateFormat... dateFormats) {
		this.dateFormats = dateFormats;
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		return dateFormats[0].format(date, toAppendTo, fieldPosition);
	}

	@Override
	public Date parse(String source, ParsePosition pos) {
		if (source == null || "".equals(source))
			return null;
		for (DateFormat df : dateFormats) {
			try {
				Date r = df.parse(source, pos);
				if (r != null)
					return r;
			} catch (Throwable t) {
				// Ignore the error, try next format.
			}
		}
		return null;
	}
}
