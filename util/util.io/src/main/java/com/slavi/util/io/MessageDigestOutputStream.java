package com.slavi.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestOutputStream extends OutputStream {
	
	OutputStream os;
	
	MessageDigest md;

	public MessageDigestOutputStream(OutputStream os) {
		this(os, "MD5");
	}

	public MessageDigestOutputStream(OutputStream os, String messageDigestAlgorithm) {
		this.os = os;
		try {
			md = MessageDigest.getInstance(messageDigestAlgorithm);
		} catch (NoSuchAlgorithmException e) {
		}
	}

	public MessageDigestOutputStream(OutputStream os, MessageDigest md) {
		this.os = os;
		this.md = md;
	}

	public void write(int b) throws IOException {
		os.write(b);
		md.update((byte) b);
	}
	
	public void write(byte b[]) throws IOException {
		os.write(b);
		md.update(b);
	}
	
	public void write(byte b[], int off, int len) throws IOException {
		os.write(b, off, len);
		md.update(b, off, len);
	}
	
	public void flush() throws IOException {
		os.flush();
	}
	
	public void close() throws IOException {
		os.close();
	}
	
	public MessageDigest getMessageDigest() {
		return md;
	}
	
	public byte[] digest() {
		return md.digest();
	}
	
	public int getDigestLength() {
		return md.getDigestLength();
	}
}
