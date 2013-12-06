package com.slavi.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class StringPrintStream extends PrintStream {

	public StringPrintStream() {
		this(new ByteArrayOutputStream());
	}

	ByteArrayOutputStream localOutput;
	
	public StringPrintStream(ByteArrayOutputStream out) {
		super(out);
		localOutput = out;
	}

	public String toString() {
		return new String(localOutput.toByteArray(), Charset.forName("UTF-8"));
	}

	public String toString(Charset cs) {
		return new String(localOutput.toByteArray(), cs);
	}
	
	public byte[] toByteArray() {
		return localOutput.toByteArray();
	}
	
	public OutputStream getOutputStream() {
		return localOutput;
	}
}
