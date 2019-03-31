package com.slavi.db.dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.slavi.db.dataloader.cfg.Config;
import com.slavi.util.concurrent.CloseableBlockingQueue;

public class DataReaderTask implements Callable<Void> {
	static Logger log = LoggerFactory.getLogger(DataLoader.class);

	Config cfg;
	CloseableBlockingQueue<Map<String, Object>> rows;
	JsonParser p;
	int id = 1;
	Map<String, Object> cur = null;

	public DataReaderTask(Config cfg, CloseableBlockingQueue<Map<String, Object>> rows, InputStream is) throws JsonParseException, IOException {
		this.cfg = cfg;
		this.rows = rows;
		switch (cfg.getFormat()) {
		case "json":
			p = new JsonFactory().createParser(is);
			p.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
			break;
		case "yml":
			p = new YAMLFactory().createParser(is);
			break;
		case "xml":
		default:
			p = new XmlFactory().createParser(is);
			break;
		}
	}

	void push() throws Exception {
		String name = p.getCurrentName();
		if (name == null && cur != null) {
			Integer index = (Integer) cur.get(DataLoader.tagIndex);
			name = index == null ? null : index.toString();
		}
		if (name == null)
			name = "/";
		if (name.startsWith("_"))
			name = "_" + name;

		Map<String, Object> parent = cur;
		cur = new HashMap<>();
		cur.put(DataLoader.tagName, name);
		cur.put(DataLoader.tagId, id++);
		JsonLocation loc = p.getTokenLocation();
		cur.put(DataLoader.tagCol, loc.getColumnNr());
		cur.put(DataLoader.tagLine, loc.getLineNr());
		if (parent != null) {
			cur.put(DataLoader.tagParent, parent);
			String path = (String) parent.get(DataLoader.tagPath);
			cur.put(DataLoader.tagPath, path + (path.endsWith("/") ? "" : "/") + name);
		} else {
			cur.put(DataLoader.tagPath, name);
		}
	}

	void pop() {
		cur = (Map) cur.get(DataLoader.tagParent);
	}

	public Void call() throws Exception {
		try (AutoCloseable dummy = rows) {
			JsonToken t = p.nextToken();
			Integer index;
			while (t != null) {
				switch (t.id()) {
				case JsonTokenId.ID_START_ARRAY:
					push();
					cur.put(DataLoader.tagIndex, 0);
					break;

				case JsonTokenId.ID_EMBEDDED_OBJECT:
				case JsonTokenId.ID_START_OBJECT:
					push();
					break;

				case JsonTokenId.ID_END_ARRAY:
				case JsonTokenId.ID_END_OBJECT:
					cur = new HashMap(cur);
					index = (Integer) cur.get(DataLoader.tagIndex);
					if (index != null) {
						cur.put(DataLoader.tagIndex, index + 1);
					}
					rows.put(cur);
					pop();
					break;

				case JsonTokenId.ID_STRING:
				case JsonTokenId.ID_NUMBER_INT:
				case JsonTokenId.ID_NUMBER_FLOAT:
				case JsonTokenId.ID_TRUE:
				case JsonTokenId.ID_FALSE:
				case JsonTokenId.ID_NULL: {
					index = (Integer) cur.get(DataLoader.tagIndex);
					if (index == null) {
						push();
						cur.put(DataLoader.tagValue, p.getText());
						rows.put(cur);
						pop();
						String name = p.getCurrentName();
						if (name == null)
							name = "_";
						if (name.startsWith("_"))
							name = "_" + name;
						cur = new HashMap(cur);
						cur.put(name, p.getText());
					} else {
						JsonLocation loc = p.getTokenLocation();
						var cur_bak = cur;
						cur = new HashMap(cur);
						cur.put(DataLoader.tagCol, loc.getColumnNr());
						cur.put(DataLoader.tagLine, loc.getLineNr());
						cur.put(DataLoader.tagValue, p.getText());
						rows.put(cur);
						cur = new HashMap(cur_bak);
						cur.put(DataLoader.tagIndex, index + 1);
					}
					break;
				}

				case JsonTokenId.ID_FIELD_NAME:
				case JsonTokenId.ID_NOT_AVAILABLE:
				case JsonTokenId.ID_NO_TOKEN:
				default:
					break;
				}

				t = p.nextToken();
			}
		}
		return null;
	}
}
