package com.slavi.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestInputStream extends InputStream {

	InputStream is;

	MessageDigest md;

	public MessageDigestInputStream(InputStream is) {
		this(is, "MD5");
	}

	public MessageDigestInputStream(InputStream is, String messageDigestAlgorithm) {
		this.is = is;
		try {
			md = MessageDigest.getInstance(messageDigestAlgorithm);
		} catch (NoSuchAlgorithmException e) {
		}
	}
	
	public MessageDigestInputStream(InputStream is, MessageDigest md) {
		this.is = is;
		this.md = md;
	}

	public int read() throws IOException {
		int r = is.read();
		if (r >= 0)
			md.update((byte) r);
		return r;
	}

	public int read(byte b[]) throws IOException {
		int r = is.read(b);
		md.update(b);
		return r;
	}

	public int read(byte b[], int off, int len) throws IOException {
		int r = is.read(b, off, len);
		md.update(b, off, r);
		return r;
	}

	public int available() throws IOException {
		return is.available();
	}

	public void close() throws IOException {
		is.close();
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
