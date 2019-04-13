package com.slavi.dbutil;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateFormats extends DateFormat {

	DateFormat[] dateFormats;

	boolean throwsException = false;

	public DateFormats(List<String> dateFormatPatterns) {
		this(dateFormatPatterns.toArray(new String[dateFormatPatterns.size()]));
	}

	public DateFormats(String... dateFormatPatterns) {
		this(Locale.US, dateFormatPatterns);
	}

	public DateFormats(Locale locale, String... dateFormatPatterns) {
		dateFormats = new DateFormat[dateFormatPatterns.length];
		for (int i = 0; i < dateFormatPatterns.length; i++) {
			DateFormat df = new SimpleDateFormat(dateFormatPatterns[i], locale);
			df.setLenient(false);
			dateFormats[i] = df;
		}
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
		if (source == null || "".equals(source)) {
			if (!throwsException)
				pos.setIndex(1); // TODO: Why?
			return null;
		}
		for (DateFormat df : dateFormats) {
			try {
				Date r = df.parse(source, pos);
				if (r != null)
					return r;
			} catch (Throwable t) {
				// Ignore the error, try next format.
			}
		}
		if (throwsException)
			pos.setIndex(0);
		else
			pos.setIndex(1); // TODO: Why?
		return null;
	}

	public boolean isThrowsException() {
		return throwsException;
	}

	public void setThrowsException(boolean throwsException) {
		this.throwsException = throwsException;
	}
}
