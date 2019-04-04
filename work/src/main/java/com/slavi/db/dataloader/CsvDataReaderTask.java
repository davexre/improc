package com.slavi.db.dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slavi.db.dataloader.cfg.Config;
import com.slavi.util.concurrent.CloseableBlockingQueue;

public class CsvDataReaderTask implements Callable<Void> {
	static Logger log = LoggerFactory.getLogger(DataLoader.class);

	Config cfg;
	CloseableBlockingQueue<Map> rows;
	CSVParser parser = null;

	public CsvDataReaderTask(Config cfg, CloseableBlockingQueue<Map> rows, InputStream is) throws IOException {
		this.cfg = cfg;
		this.rows = rows;
		parser = CSVFormat.EXCEL.parse(new InputStreamReader(is));
	}

	@Override
	public Void call() throws Exception {
		try (AutoCloseable dummy = rows) {
			List<String> columnNames = new ArrayList<>();
			var header = parser.getHeaderMap();
			if (header != null)
				for (String i : header.keySet()) {
					String ii = StringUtils.trimToNull(i);
					if (StringUtils.startsWith(ii, "_")) {
						ii = "_" + ii;
					}
					columnNames.add(ii);
				}
			header = null;

			var iter = parser.iterator();
			while (iter.hasNext()) {
				var rec = iter.next();
				Map cur = new HashMap<>();
				for (int i = 0; i < rec.size(); i++) {
					String col = i < columnNames.size() ? columnNames.get(i) : null;
					String val = rec.get(i);
					if (col != null)
						cur.put(col, val);
					cur.put(i, val);
				}
				cur.put(DataLoader.tagLine, parser.getCurrentLineNumber());
				cur.put(DataLoader.tagId, parser.getRecordNumber());
				rows.put(cur);
			}
		}
		return null;
	}
}
