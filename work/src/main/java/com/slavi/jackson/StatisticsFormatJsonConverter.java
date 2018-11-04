package com.slavi.jackson;

public class StatisticsFormatJsonConverter {
	final static String[] ITEMS = {
		"Count",
		"Avg",
		"J",
		"AE",
		"MinMax",
		"Abs",
		"Delta",
		"MD",
		"StdDev",
		"Errors",
	};

	public static class Serialize extends BitJsonConverterBase.Serialize {
		public Serialize() {
			super(ITEMS, true);
		}
	}

	public static class Deserialize extends BitJsonConverterBase.Deserialize {
		public Deserialize() {
			super(ITEMS, true, true);
		}
	}

}
