package com.slavi.jackson;

import com.fasterxml.jackson.databind.util.StdConverter;

public class BitJsonConverterBase {
	public static abstract class Serialize extends StdConverter<Integer, String> {
		String[] items;
		boolean ignoreUnlabeledBits;

		public Serialize(String[] items, boolean ignoreUnlabeledBits) {
			this.items = items;
			this.ignoreUnlabeledBits = ignoreUnlabeledBits;
		}

		public String convert(Integer value) {
			if (value == null)
				return null;
			int v = value;
			StringBuilder r = new StringBuilder();
			for (int i = items.length - 1; i >= 0; i--) {
				if (items[i] == null)
					continue;
				if ((v & (1 << i)) != 0) {
					if (r.length() > 0)
						r.append(',');
					r.append(items[i]);
					v ^= 1 << i;
				}
			}
			if (v != 0 && !ignoreUnlabeledBits)
				throw new Error("Unlabeled bits " + Integer.toBinaryString(v));
			return r.toString();
		}
	}

	public static abstract class Deserialize extends StdConverter<String, Integer> {
		String[] items;
		boolean caseSensitive;
		boolean ignoreInvalidTokens;

		public Deserialize(String[] items, boolean ignoreInvalidTokens, boolean caseSensitive) {
			this.items = items;
			this.ignoreInvalidTokens = ignoreInvalidTokens;
			this.caseSensitive = caseSensitive;
		}

		public Integer convert(String value) {
			if (value == null)
				return null;
			int r = 0;
			nextToken: for (String tt : value.split(",")) {
				String t = tt.trim();
				if ("".equals(t))
					continue;
				for (int i = items.length - 1; i >= 0; i--) {
					if (items[i] == null)
						continue;
					boolean match = caseSensitive ?
							items[i].equals(t) :
							items[i].equalsIgnoreCase(t);
					if (match) {
						r |= 1 << i;
						continue nextToken;
					}
				}
				if (!ignoreInvalidTokens)
					throw new Error("Invalid token " + t);
			}
			return r;
		}
	}
}
