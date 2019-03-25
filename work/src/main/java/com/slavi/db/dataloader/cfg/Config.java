package com.slavi.db.dataloader.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
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

	String format;

	String url;
	String username;
	String password;

	String before;
	String after;

	@XmlTransient
	Template beforeTemplate;

	@XmlTransient
	Template afterTemplate;

	public String getBefore() {
		return before;
	}

	public void setBefore(String template) throws ResourceNotFoundException, ParseErrorException, Exception {
		this.before = template;
		beforeTemplate = velocity.get().getTemplate(template);
	}

	public String getAfter() {
		return after;
	}

	public void setAfter(String template) throws ResourceNotFoundException, ParseErrorException, Exception {
		this.after = template;
		afterTemplate = velocity.get().getTemplate(template);
	}

	public Template getBeforeTemplate() {
		return beforeTemplate;
	}

	public Template getAfterTemplate() {
		return afterTemplate;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
