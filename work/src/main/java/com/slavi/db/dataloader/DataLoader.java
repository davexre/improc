package com.slavi.db.dataloader;

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
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.slavi.db.dataloader.cfg.Config;
import com.slavi.dbutil.ScriptRunner2;
import com.slavi.util.Marker;
import com.slavi.util.Util;
import com.slavi.util.concurrent.CloseableBlockingQueue;
import com.slavi.util.concurrent.TaskSet;

public class DataLoader {

	public static final String tagName = "_NAME";
	public static final String tagValue = "_VALUE";
	public static final String tagParent = "_PARENT";
	public static final String tagIndex = "_INDEX";
	public static final String tagLine = "_LINE";
	public static final String tagCol = "_COL";
	public static final String tagId = "_ID";
	public static final String tagPath = "_PATH";

	static Logger log = LoggerFactory.getLogger("DataLoader");

	static String applyTemplate(VelocityContext ctx, Template t) {
		if (t == null)
			return null;
		Writer out = new StringWriter();
		t.merge(ctx, out);
		return out.toString();
	}

	static VelocityContext makeContext() {
		var ctx = new VelocityContext();
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
		return ctx;
	}

	Config cfg;
	CloseableBlockingQueue<Map> rows;

	void runBeforeScripts() throws Exception {
		VelocityContext ctx = makeContext();
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
/*
			var meta = conn.getMetaData();
			// Check the definitions
			for (var def : cfg.defs) {
				if (def.getSql() == null) {
					def.setSql(def.getName());
				}
				String sql;
				try {
					sql = applyTemplate(ctx, def.getSqlTemplate());
				} catch (Throwable t) {
					log.trace("Error applying sql template for definition {}", def.getName(), t);
					sql = null;
				}
				if (sql != null) {
					try (ResultSet rs = meta.getColumns(null, null, sql, null)) {
						if (rs.next()) {
							String tableName = rs.getString(3);
							def.setSql("insert into \"" + tableName + "\" ");
						}
					}
				}
			}
			*/
		}
	}

	void runAfterScripts() throws Exception {
		VelocityContext ctx = makeContext();
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

	public void doIt(String config, String dataFile) throws Exception {
		Properties velocityParams = new Properties();
		velocityParams.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		velocityParams.put("string.resource.loader.class", VelocityStringResourceLoader.class.getName());
		velocityParams.put("output.encoding", "UTF-8");
		velocityParams.put(RuntimeConstants.INPUT_ENCODING, "UTF-8");
		velocityParams.put(RuntimeConstants.RUNTIME_REFERENCES_STRICT, "true");
		velocityParams.put(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
		velocityParams.put(RuntimeConstants.RESOURCE_LOADER, List.of("classpath", "string"));
		VelocityEngine ve = new VelocityEngine(velocityParams);

		ObjectMapper m = new YAMLMapper();
		Util.configureMapper(m);
		Map<String, Object> jsonInject = new HashMap<>();
		jsonInject.put(Config.JacksonInjectTag, ve);
		m.setInjectableValues(new InjectableValues.Std(jsonInject));

		cfg = m.readValue(getClass().getResourceAsStream(config), Config.class);
		if (cfg.defs == null)
			cfg.defs = new ArrayList();
		for (var i : cfg.defs)
			if (i.getParams() == null)
				i.setParams(new ArrayList());

		rows = new CloseableBlockingQueue<>(10);

		InputStream is = getClass().getResourceAsStream(dataFile);

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
/*
		try (Connection conn = DriverManager.getConnection(cfg.getUrl(), cfg.getUsername(), cfg.getPassword())) {
			PreparedStatement ps = conn.prepareStatement("select * from dest_pattern");
			ps.execute();
			System.out.println(ResultSetToString.resultSetToString(ps.getResultSet()));
		}*/
	}

	public static class Data {
		public String asd;
		public Map options;
	}

	public void doIt2() throws Exception {
		Properties p = new Properties();
		p.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		p.put("string.resource.loader.class", VelocityStringResourceLoader.class.getName());
		p.put(RuntimeConstants.INPUT_ENCODING, "UTF-8");
		p.put("output.encoding", "UTF-8");
		p.put(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
		p.put(RuntimeConstants.RESOURCE_LOADER, List.of("classpath", "string"));
		VelocityEngine ve = new VelocityEngine(p);

		ObjectMapper m = new YAMLMapper();
		Util.configureMapper(m);
		Map<String, Object> jsonInject = new HashMap<>();
		jsonInject.put(Config.JacksonInjectTag, ve);
		m.setInjectableValues(new InjectableValues.Std(jsonInject));

		Data d = m.readValue(getClass().getResourceAsStream("dummy.yml"), Data.class);
		System.out.println(d.asd);
		System.out.println(d.options);
	}

	public static void main(String[] args) throws Exception {
		Marker.mark();
//		new DataLoader().doIt("TestData-jut-config.yml", "TestData-jut.json");
//		new DataLoader().doIt("TestData-City-config.yml", "TestData-City.xml");
		new DataLoader().doIt("TestData-CSV-config.yml", "/bg/elections/pv/data/2013/pe2013_pe_candidates.txt");
		Marker.release();
		System.out.println("Done.");
	}
}
