package com.slavi.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class StringPrintStream extends PrintStream {

	public StringPrintStream() {
		super(new ByteArrayOutputStream());
	}
	
	public String toString() {
		return new String(((ByteArrayOutputStream) out).toByteArray(), Charset.forName("UTF-8"));
	}

	public String toString(Charset cs) {
		return new String(((ByteArrayOutputStream) out).toByteArray(), cs);
	}
	
	public byte[] toByteArray() {
		return ((ByteArrayOutputStream) out).toByteArray();
	}
	
	public OutputStream getOutputStream() {
		return out;
	}
}
