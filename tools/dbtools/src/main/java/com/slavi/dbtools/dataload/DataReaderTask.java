package com.slavi.dbtools.dataload;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.slavi.util.concurrent.CloseableBlockingQueue;

public class DataReaderTask implements Callable<Void> {
	Config cfg;
	CloseableBlockingQueue<Map> rows;
	JsonParser p;
	int id = 1;
	Map cur = null;

	public DataReaderTask(Config cfg, CloseableBlockingQueue<Map> rows, InputStream is) throws JsonParseException, IOException {
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
		String name = null;
		if (cur != null) {
			name = p.getCurrentName();
			if (name == null) {
				Integer index = (Integer) cur.get(DataLoad.tagIndex);
				name = index == null ? null : index.toString();
			}
		}
		if (name == null)
			name = "/";
		if (name.startsWith("_"))
			name = "_" + name;

		Map parent = cur;
		cur = new HashMap<>();
		cur.put(DataLoad.tagName, name);
		cur.put(DataLoad.tagId, id++);
		JsonLocation loc = p.getTokenLocation();
		cur.put(DataLoad.tagCol, loc.getColumnNr());
		cur.put(DataLoad.tagLine, loc.getLineNr());
		if (parent != null) {
			cur.put(DataLoad.tagParent, parent);
			String path = (String) parent.get(DataLoad.tagPath);
			cur.put(DataLoad.tagPath, path + (path.endsWith("/") ? "" : "/") + name);
		} else {
			cur.put(DataLoad.tagPath, name);
		}
	}

	void pop() {
		Map tmp = new HashMap(cur);
		cur = (Map) cur.get(DataLoad.tagParent);
		if (cur != null) {
			cur = new HashMap(cur);
			tmp.remove(DataLoad.tagParent);
			cur.put(tmp.get(DataLoad.tagName), tmp);
		}
	}

	public Void call() throws Exception {
		try (AutoCloseable dummy = rows) {
			JsonToken t = p.nextToken();
			Integer index;
			while (t != null) {
				switch (t.id()) {
				case JsonTokenId.ID_START_ARRAY:
					push();
					cur.put(DataLoad.tagIndex, 0);
					break;

				case JsonTokenId.ID_EMBEDDED_OBJECT:
				case JsonTokenId.ID_START_OBJECT:
					push();
					break;

				case JsonTokenId.ID_END_ARRAY:
				case JsonTokenId.ID_END_OBJECT:
					cur = new HashMap(cur);
					index = (Integer) cur.get(DataLoad.tagIndex);
					if (index != null) {
						cur.put(DataLoad.tagIndex, index + 1);
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
					index = (Integer) cur.get(DataLoad.tagIndex);
					if (index == null) {
						push();
						cur.put(DataLoad.tagValue, p.getText());
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
						push();
						cur.put(DataLoad.tagValue, p.getText());
						rows.put(cur);
						pop();
						cur = new HashMap(cur);
						cur.put(DataLoad.tagIndex, index + 1);
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
