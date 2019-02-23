package com.slavi.jdbcspy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spy<TT> {

	TT t;

	static Logger log = LoggerFactory.getLogger("jdbc.spy");
	static Map<Integer, String> sqlTypes = new HashMap();

	static {
		for (Field f : Types.class.getDeclaredFields()) {
			int mod = f.getModifiers();
			if (Modifier.isStatic(mod) &&
				Modifier.isFinal(mod) &&
				f.getType().equals(int.class))
				try {
					sqlTypes.put(f.getInt(null), f.getName());
				} catch (Throwable t) {
					// ignore
				}
		}
	}

	public Spy(TT delegate) {
		this.t = delegate;
	}

	public TT unspy() {
		return t;
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	// https://stackoverflow.com/a/9855338/2243209
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
