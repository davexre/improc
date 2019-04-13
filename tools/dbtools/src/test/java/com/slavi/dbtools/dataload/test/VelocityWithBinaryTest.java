package com.slavi.dbtools.dataload.test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.junit.Assert;

import com.slavi.dbtools.dataload.DataLoad;
import com.slavi.dbtools.dataload.VelocityStringResourceLoader;

public class VelocityWithBinaryTest {

	byte [] makeByteArray() throws Exception {
		var file = IOUtils.toString(getClass().getResourceAsStream("TestData-CSV.csv"));
		var os = new ByteArrayOutputStream();
		var zos = new ZipOutputStream(os);
		var entry = new ZipEntry("data.txt");
		zos.putNextEntry(entry);
		IOUtils.copy(new StringReader(file), zos);
		zos.closeEntry();
		zos.close();
		return os.toByteArray();
	}

	void doIt() throws Exception {
		var ve = DataLoad.makeVelocityEngine();
		var ctx = new VelocityContext();

		var osBytes = makeByteArray();
		ctx.put("rec", osBytes);
		// Idea borrowed from: https://grokbase.com/t/velocity/user/05bw3e8akp/image-data-from-byte-array-merging#20051128jgcg6bpk5rvtbzmbgw66qc4pvm
		Template t = ve.getTemplate("#set ($dummy = ${os.write(${rec})})");
		var os2 = new ByteArrayOutputStream();
		Writer out = new OutputStreamWriter(os2);
		ctx.put("os", os2);
		ctx.put("out", out);

		t.merge(ctx, out);
		out.flush();
		var osBytes2 = os2.toByteArray();
		String ttt = new String(osBytes2);
		//String ttt = applyTemplate(ctx, t);

		Assert.assertArrayEquals(osBytes, osBytes2);
		var osBytes3 = ttt.getBytes();
		Assert.assertArrayEquals(osBytes, osBytes3); // Does not work
	}

	void doIt2() throws Exception {
		Properties velocityParams = new Properties();
		velocityParams.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		velocityParams.put("string.resource.loader.class", VelocityStringResourceLoader.class.getName());
		velocityParams.put("output.encoding", "UTF-8");
		velocityParams.put(RuntimeConstants.INPUT_ENCODING, "UTF-8");
//		velocityParams.put(RuntimeConstants.RUNTIME_REFERENCES_STRICT, "true");
		velocityParams.put(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
		velocityParams.put(RuntimeConstants.RESOURCE_LOADER, List.of("classpath", "string"));
		VelocityEngine ve = new VelocityEngine(velocityParams);

		var ctx = new VelocityContext();
		var s = DataLoad.applyTemplate(ctx, ve.getTemplate("${does['not'].exist}"));
		System.out.println(s);
		s = DataLoad.applyTemplate(ctx, ve.getTemplate("$!{does['not'].exist}"));
		System.out.println(s);
	}

	public static void main(String[] args) throws Exception {
		new VelocityWithBinaryTest().doIt2();
		System.out.println("Done.");
	}
}
