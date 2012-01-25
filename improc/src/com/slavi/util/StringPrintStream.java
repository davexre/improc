package com.slavi.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StringPrintStream extends PrintStream {

	public StringPrintStream() {
		super(new ByteArrayOutputStream());
	}
	
	public String toString() {
		return new String(((ByteArrayOutputStream) out).toByteArray());
	}
}
