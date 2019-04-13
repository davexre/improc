package com.slavi.db.dataloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

	public void loadData(Config cfg, InputStream is, String dataFile) throws Exception {
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


	public void doIt(String config, String dataFile) throws Exception {
		VelocityEngine ve = makeVelocityEngine();
		cfg = loadConfig(ve , getClass().getResourceAsStream(config));

		InputStream is = getClass().getResourceAsStream(dataFile);
		loadData(cfg, is, dataFile);
/*
		try (Connection conn = DriverManager.getConnection(cfg.getUrl(), cfg.getUsername(), cfg.getPassword())) {
			PreparedStatement ps = conn.prepareStatement("select * from dest_pattern");
			ps.execute();
			System.out.println(ResultSetToString.resultSetToString(ps.getResultSet()));
		}*/
	}

	public void doIt2() throws Exception {
		var ve = makeVelocityEngine();

		var file = IOUtils.toString(getClass().getResourceAsStream("/bg/elections/pv/data/2013/pe2013_pe_candidates.txt"));
		var os = new ByteArrayOutputStream();
		var zos = new ZipOutputStream(os);
		var entry = new ZipEntry("data.txt");
		zos.putNextEntry(entry);
		IOUtils.copy(new StringReader(file), zos);
		zos.closeEntry();
		zos.close();

		var ctx = new VelocityContext();
		var osBytes = os.toByteArray();
		ctx.put("rec", osBytes);
		// Idea borrowed from: https://grokbase.com/t/velocity/user/05bw3e8akp/image-data-from-byte-array-merging#20051128jgcg6bpk5rvtbzmbgw66qc4pvm
		Template t = ve.getTemplate("#set ($dummy = ${os.write(${rec})})\n\n\n");
		var os2 = new ByteArrayOutputStream();
		Writer out = new OutputStreamWriter(os2);
		ctx.put("os", os2);
		ctx.put("out", out);

		t.merge(ctx, out);
		out.flush();
		var osBytes2 = os2.toByteArray();
		//String ttt = applyTemplate(ctx, t);

		System.out.println(StringUtils.abbreviate(file, 50));
//		System.out.println(StringUtils.abbreviate(ttt, 50));
		System.out.println(StringUtils.abbreviate(new String(osBytes), 50));
		System.out.println(StringUtils.abbreviate(new String(osBytes2), 50));
		System.out.println(osBytes.length);
		System.out.println(osBytes2.length);
		System.out.println(Arrays.equals(osBytes, osBytes2));


		var is = new ByteArrayInputStream(os2.toByteArray());
		var zis = new ZipInputStream(is);
		entry = zis.getNextEntry();
		String file2 = IOUtils.toString(zis);
		zis.closeEntry();
		zis.close();

		System.out.println(StringUtils.abbreviate(file2, 50));
		System.out.println(file.equals(file2));
	}

	public static void main(String[] args) throws Exception {
		Marker.mark();
//		new DataLoader().doIt("TestData-jut-config.yml", "TestData-jut.json");
//		new DataLoader().doIt("TestData-City-config.yml", "TestData-City.xml");
//		new DataLoader().doIt("TestData-CSV-config.yml", "/bg/elections/pv/data/2013/pe2013_pe_candidates.txt");
		new DataLoader().doIt2();
		Marker.release();
		System.out.println("Done.");
	}
}
