package com.slavi.db.dataloader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slavi.db.dataloader.cfg.Config;
import com.slavi.util.concurrent.CloseableBlockingQueue;

public class CsvDataReaderTask implements Callable<Void> {
	static Logger log = LoggerFactory.getLogger(DataLoader.class);

	Config cfg;
	CloseableBlockingQueue<Map<String, Object>> rows;
	CSVParser parser = null;

	public CsvDataReaderTask(Config cfg, CloseableBlockingQueue<Map<String, Object>> rows, InputStream is) {
		this.cfg = cfg;
		this.rows = rows;

	}

	@Override
	public Void call() throws Exception {
		try (AutoCloseable dummy = rows) {
			Map<String, Object> cur = new HashMap<>();
			var iter = parser.iterator();
			while (iter.hasNext()) {
				var rec = iter.next();
				//parser.getHeaderMap();
				rows.put(cur);
			}
		}
		return null;
	}
}
