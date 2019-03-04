package com.slavi.jdbcspy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;

public class SpyTimer implements AutoCloseable {

	String msg;
	long start;
	Logger log;
	Map params;

	public SpyTimer(Logger log, String msg) {
		this(log, msg, null);
	}

	public SpyTimer(Logger log, String msg, Map params) {
		this.log = log;
		this.msg = msg;
		this.params = params;
		start = System.currentTimeMillis();
	}

	@Override
	public void close() {
		long end = System.currentTimeMillis();
		if (log.isInfoEnabled())
			log.info("Spent {} ms on: {}", (end - start), msg);
		if (log.isDebugEnabled() && params != null && !params.isEmpty()) {
			ArrayList<Map.Entry> items = new ArrayList(params.entrySet());
			Collections.sort(items, (a, b) -> {
				Object keyA = a.getKey();
				Object keyB = b.getKey();
				if (keyA instanceof Integer) {
					return keyB instanceof Integer ? Integer.compare((Integer) keyA, (Integer) keyB) : -1;
				} else {
					return keyB instanceof Integer ? 1 : ((String)keyA).compareTo((String) keyB);
				}
			});
			StringBuilder sb = new StringBuilder("Params [");
			String prefix = "";
			for (Map.Entry i : items) {
				sb.append(prefix);
				Object key = i.getKey();
				if (!(key instanceof Integer))
					sb.append(key).append('=');
				try {
					Object v = i.getValue();
					String vv = v == null ? "null" : v.toString();
					if (vv.length() <= 20)
						sb.append(vv);
					else
						sb.append(vv.substring(0, 20)).append("...");
				} catch (Throwable t) {
					sb.append("err");
				}
				prefix = ", ";
			}
			sb.append(']');
			log.debug(sb.toString());
		}
		if (params != null)
			params.clear();
	}
}
