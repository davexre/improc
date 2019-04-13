package com.slavi.dbtools.dataload;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.slavi.dbutil.ScriptRunner2;
import com.slavi.util.Util;
import com.slavi.util.concurrent.CloseableBlockingQueue;
import com.slavi.util.concurrent.TaskSet;

public class DataLoad {

	public static final String tagName = "_NAME";
	public static final String tagValue = "_VALUE";
	public static final String tagParent = "_PARENT";
	public static final String tagIndex = "_INDEX";
	public static final String tagLine = "_LINE";
	public static final String tagCol = "_COL";
	public static final String tagId = "_ID";
	public static final String tagPath = "_PATH";

	static Logger log = LoggerFactory.getLogger("DataLoader");

	public static String applyTemplate(VelocityContext ctx, Template t) {
		if (t == null)
			return null;
		Writer out = new StringWriter();
		t.merge(ctx, out);
		return out.toString();
	}

	public static VelocityEngine makeVelocityEngine() {
		Properties velocityParams = new Properties();
		velocityParams.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		velocityParams.put("string.resource.loader.class", VelocityStringResourceLoader.class.getName());
		velocityParams.put("output.encoding", "UTF-8");
		velocityParams.put(RuntimeConstants.INPUT_ENCODING, "UTF-8");
		velocityParams.put(RuntimeConstants.RUNTIME_REFERENCES_STRICT, "true");
		velocityParams.put(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
		velocityParams.put(RuntimeConstants.RESOURCE_LOADER, List.of("classpath", "string"));
		VelocityEngine ve = new VelocityEngine(velocityParams);
		return ve;
	}

	public static Config loadConfig(VelocityEngine ve, InputStream cfgInputStream) throws Exception {
		ObjectMapper m = new YAMLMapper();
		Util.configureMapper(m);
		Map<String, Object> jsonInject = new HashMap<>();
		jsonInject.put(Config.JacksonInjectTag, ve);
		m.setInjectableValues(new InjectableValues.Std(jsonInject));

		Config cfg = m.readValue(cfgInputStream, Config.class);
		if (cfg.defs == null)
			cfg.defs = new ArrayList();
		for (var i : cfg.defs)
			if (i.getParams() == null)
				i.setParams(new ArrayList());
		return cfg;
	}

	Config cfg;
	CloseableBlockingQueue<Map> rows;

	void runBeforeScripts() throws Exception {
		VelocityContext ctx = cfg.makeContext();
		try (Connection conn = DriverManager.getConnection(cfg.getUrl(), cfg.getUsername(), cfg.getPassword())) {
			ScriptRunner2 scriptRunner = new ScriptRunner2(conn);
			// Execute befre scripts
			String script = applyTemplate(ctx, cfg.getBeforeTemplate());
			if (script != null) {
				log.debug("Running script config.before");
				log.trace("Expanded script is\n{}", script);
				scriptRunner.runScript(new StringReader(script));
			}
			for (var def : cfg.defs) {
				script = applyTemplate(ctx, def.getBeforeTemplate());
				if (script != null) {
					log.debug("Running script {} before", def.getName());
					log.trace("Expanded script is\n{}", script);
					scriptRunner.runScript(new StringReader(script));
				}
			}
		}
	}

	void runAfterScripts() throws Exception {
		VelocityContext ctx = cfg.makeContext();
		try (Connection conn = DriverManager.getConnection(cfg.getUrl(), cfg.getUsername(), cfg.getPassword())) {
			ScriptRunner2 scriptRunner = new ScriptRunner2(conn);
			// Execute after scripts
			for (var def : cfg.defs) {
				String script = applyTemplate(ctx, def.getAfterTemplate());
				if (script != null) {
					log.debug("Running script {} after", def.getName());
					log.trace("Expanded script is\n{}", script);
					scriptRunner.runScript(new StringReader(script));
				}
			}
			String script = applyTemplate(ctx, cfg.getAfterTemplate());
			if (script != null) {
				log.debug("Running script config.after");
				log.trace("Expanded script is\n{}", script);
				scriptRunner.runScript(new StringReader(script));
			}
		}
	}

	public void loadData(Config cfg, InputStream is, String dataFile) throws Exception {
		this.cfg = cfg;
		rows = new CloseableBlockingQueue<>(10);
		runBeforeScripts();
		Runtime runtime = Runtime.getRuntime();
		int nThreads = runtime.availableProcessors() * 2;
		if (nThreads < 4)
			nThreads = 4;
		ExecutorService exec = Util.newBlockingThreadPoolExecutor(nThreads);
		try {
			TaskSet task = new TaskSet(exec);
			switch (cfg.getFormat()) {
			case "json":
			case "yml":
			case "xml":
				task.add(new DataReaderTask(cfg, rows, is));
				break;
			case "csv":
			case "csv-excel":
			case "csv-informix-unload":
			case "csv-informix-unload-csv":
			case "csv-mysql":
			case "csv-oracle":
			case "csv-postgresql-csv":
			case "csv-postgresql-text":
			case "csv-rfc4180":
			case "csv-tdf":
				task.add(new CsvDataReaderTask(cfg, rows, is, dataFile));
				break;
			default:
				throw new InvalidParameterException("Invalid file format " + cfg.getFormat());
			}
			for (int i = 1; i < nThreads; i++)
				task.add(new RowProcessTask(cfg, rows));
			task.run().get();
		} finally {
			exec.shutdownNow();
		}
		runAfterScripts();
	}
}
