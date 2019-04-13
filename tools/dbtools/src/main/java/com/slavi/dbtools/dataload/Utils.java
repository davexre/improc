package com.slavi.dbtools.dataload;

import com.slavi.util.Util;

public class Utils {
	public static String bytesToHex(byte[] bytes) {
		return Util.bytesToHex(bytes);
	}

	public static byte[] hexToBytes(String hexStr) throws NumberFormatException {
		return Util.hexToBytes(hexStr);
	}
}
