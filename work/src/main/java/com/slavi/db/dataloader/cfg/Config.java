package com.slavi.db.dataloader.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.slavi.db.dataloader.VelocityStringResourceLoader;
import com.slavi.util.ComputableSoftReference;

public class Config implements Serializable {

	public static final ComputableSoftReference<VelocityEngine> velocity = new ComputableSoftReference<>() {
		@Override
		protected VelocityEngine compute() throws Exception {
			Properties p = new Properties();
			p.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			p.put("string.resource.loader.class", VelocityStringResourceLoader.class.getName());
			p.put(RuntimeConstants.INPUT_ENCODING, "UTF-8");
			p.put("output.encoding", "UTF-8");
			p.put(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
			p.put(RuntimeConstants.RESOURCE_LOADER, List.of("classpath", "string"));
			VelocityEngine ve = new VelocityEngine(p);
			return ve;
		}
	};

	public ArrayList<EntityDef> defs;
}
