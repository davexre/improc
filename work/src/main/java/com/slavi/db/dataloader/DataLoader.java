package com.slavi.db.dataloader;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.slavi.db.dataloader.cfg.Config;
import com.slavi.util.CEncoder;
import com.slavi.util.Util;

public class DataLoader {

	public static final String tagName = "_NAME";
	public static final String tagValue = "_VALUE";
	public static final String tagParent = "_PARENT";
	public static final String tagIndex = "_INDEX";
	public static final String tagLine = "_LINE";
	public static final String tagCol = "_COL";
	public static final String tagId = "_ID";
	public static final String tagPath = "_PATH";

	Map<String, Object> root;
	Map<String, Object> cur;
	JsonParser p;
	int id = 1;

	Logger log = LoggerFactory.getLogger(getClass());

	void debugPrint() {
		if (!log.isTraceEnabled())
			return;
		StringBuilder r = new StringBuilder("Parser leaf");
		var l = new ArrayList<String>();
		var loop = root;
		while (loop != null) {
			var ll = loop;
			loop = null;
			l.clear();
			for (var i : ll.entrySet()) {
				if (tagParent.equals(i.getKey()) ||
					tagPath.equals(i.getKey()))
					continue;
				if (i.getValue() instanceof Map) {
					loop = (Map<String, Object>) i.getValue();
//					l.add(CEncoder.encode(i.getKey() + "= <map>"));
					continue;
				}
				l.add(CEncoder.encode(i.getKey() + "=" + StringUtils.abbreviate(i.getValue().toString(), 20)));
			}
			Collections.sort(l);
			r.append("\n").append("  Path").append("=").append(ll.get(tagPath));
			for (var i : l)
				r.append("; ").append(i);
		}
		log.trace(r.toString());
	}

	String applyTemplate(Template t) {
		if (t == null)
			return null;
		Writer out = new StringWriter();
		t.merge(ctx, out);
		return out.toString();
	}

	void processLeaf() throws JsonProcessingException {
		if (log.isTraceEnabled()) {
			debugPrint();
		} else if (log.isDebugEnabled()) {
			log.debug("Parser leaf: _ID:{} _LINE:{} _COL:{} _INDEX:{} _NAME:{} _PATH:{} _VALUE:{} ",
					cur.get(tagId), cur.get(tagLine), cur.get(tagCol), cur.get(tagIndex), cur.get(tagName), cur.get(tagPath), cur.get(tagValue));
		}
		String path = (String) cur.get(tagPath);
		ctx.put("rec", cur);
		ctx.put("root", root);

		for (var def : cfg.defs) {
			if (def.getPathPattern().matcher(path).matches()) {
				ArrayList<String> params = new ArrayList<>();
				if (def.getParamTemplates() != null) {
					for (var t : def.getParamTemplates()) {
						params.add(applyTemplate(t));
					}
				}
				if (log.isInfoEnabled()) {
					StringBuilder sb = new StringBuilder();
					sb.append("Matched def ").append(def.getName())
						.append("\n  SQL: ").append(applyTemplate(def.getSqlTemplate()));
					for (var i : params)
						sb.append("\n  Param: ").append(CEncoder.encode(StringUtils.abbreviate(i, 40)));
					log.info(sb.toString());
				}
				// TODO:
			}
		}
	}

	void push() throws Exception {
		String name = p.getCurrentName();
		if (name == null && cur != null) {
			Integer index = (Integer) cur.get(tagIndex);
			name = index == null ? null : index.toString();
		}
		if (name == null)
			name = "/";
		if (name.startsWith("_"))
			name = "_" + name;

		Map<String, Object> parent = cur;
		cur = new HashMap<>();
		cur.put(tagParent, parent);
		cur.put(tagName, name);
		cur.put(tagId, id++);
		JsonLocation loc = p.getTokenLocation();
		cur.put(tagCol, loc.getColumnNr());
		cur.put(tagLine, loc.getLineNr());
		if (parent != null) {
			cur.put(tagPath, (parent == root ? "" : parent.get(tagPath)) + "/" + name);
			parent.put(name, cur);
		} else {
			cur.put(tagPath, name);
			root = cur;
		}
	}

	void pop() {
		var old = (String) cur.get(tagName);
		cur = (Map) cur.get(tagParent);
		if (cur != null)
			cur.remove(old);
	}

	void process() throws Exception {
		JsonToken t = p.nextToken();
		while (t != null) {
			switch (t.id()) {
			case JsonTokenId.ID_START_ARRAY:
				push();
				cur.put(tagIndex, 0);
				break;

			case JsonTokenId.ID_EMBEDDED_OBJECT:
			case JsonTokenId.ID_START_OBJECT:
				push();
				break;

			case JsonTokenId.ID_END_ARRAY:
			case JsonTokenId.ID_END_OBJECT:
				processLeaf();
				pop();
				if (cur != null) {
					Integer index = (Integer) cur.get(tagIndex);
					if (index != null) {
						cur.put(tagIndex, index + 1);
					}
				}
				break;

			case JsonTokenId.ID_STRING:
			case JsonTokenId.ID_NUMBER_INT:
			case JsonTokenId.ID_NUMBER_FLOAT:
			case JsonTokenId.ID_TRUE:
			case JsonTokenId.ID_FALSE:
			case JsonTokenId.ID_NULL: {
				Integer index = (Integer) cur.get(tagIndex);
				if (index == null) {
					push();
					cur.put(tagValue, p.getText());
					processLeaf();
					pop();
					String name = p.getCurrentName();
					if (name == null)
						name = "_";
					if (name.startsWith("_"))
						name = "_" + name;
					cur.put(name, p.getText());
				} else {
					JsonLocation loc = p.getTokenLocation();
					Object old_col = cur.put(tagCol, loc.getColumnNr());
					Object old_line = cur.put(tagLine, loc.getLineNr());
					String name = index.toString();
					cur.put(name, p.getText());
					processLeaf();
					cur.remove(name);
					cur.put(tagIndex, index + 1);
					cur.put(tagCol, old_col);
					cur.put(tagLine, old_line);
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

	//ArrayList<EntityDef> defs = new ArrayList<>();
	Config cfg;
	VelocityEngine ve;
	VelocityContext ctx;

	public void doIt() throws Exception {
		ve = Config.velocity.get();
		ctx = new VelocityContext();
		Map env = new HashMap(System.getenv());
		env.putAll(System.getProperties());
		ctx.put("env", env);
		ctx.put("bu", BooleanUtils.class);
		ctx.put("lu", LocaleUtils.class);
		ctx.put("du", DateUtils.class);
		ctx.put("nu", NumberUtils.class);
		ctx.put("su", StringUtils.class);
		ctx.put("seu", StringEscapeUtils.class);
		ctx.put("re", RegExUtils.class);

		ObjectMapper m = new YAMLMapper();
		Util.configureMapper(m);
		cfg = m.readValue(getClass().getResourceAsStream("config.yml"), Config.class);

//		String fname = "TestData.xml";
		String fname = "TestData.json";
		InputStream is = getClass().getResourceAsStream(fname);
		//p = new XmlFactory().createParser(is);
		p = new JsonFactory().createParser(is);

		process();
	}

	public void doIt2() throws Exception {
		VelocityEngine ve = Config.velocity.get();
		ObjectMapper m = new YAMLMapper();
		Util.configureMapper(m);

		cfg = m.readValue(getClass().getResourceAsStream("config.yml"), Config.class);
		System.out.println(m.writeValueAsString(cfg));
		System.out.println("-----------");
		System.out.println(cfg.defs.get(0).getSql());

		Map map = new HashMap();
		map.put("asd", "qwe");
		VelocityContext ctx = new VelocityContext(map);
		Writer out = new StringWriter();
		Template t = cfg.defs.get(0).getParamTemplates().get(0);
		t.merge(ctx, out);
		System.out.println(out.toString());
	}

	public static void main(String[] args) throws Exception {
		new DataLoader().doIt();
		System.out.println("Done.");
	}
}
