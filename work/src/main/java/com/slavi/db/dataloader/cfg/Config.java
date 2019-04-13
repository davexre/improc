package com.slavi.db.dataloader.cfg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

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
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.slavi.util.DateFormats;

public class Config implements Serializable {

	public static final String JacksonInjectTag = "Velocity";

	public final VelocityEngine velocity;

	public Config(@JacksonInject(value=JacksonInjectTag) VelocityEngine velocity) {
		this.velocity = velocity;
	}

	public List<EntityDef> defs;

	Map variables = new HashMap();

	String format;
	Map formatOptions;

	String url;
	String username;
	String password;
	int commitEveryNumSqls = 1;

	String before;
	String after;
	String connectionInitialize;
	String connectionFinalize;
	List<String> dateFormats;

	@XmlTransient
	Template beforeTemplate;

	@XmlTransient
	Template afterTemplate;

	@XmlTransient
	Template connectionInitializeTemplate;

	@XmlTransient
	Template connectionFinalizeTemplate;

	@XmlTransient
	Template urlTemplate;

	@XmlTransient
	Template usernameTemplate;

	@XmlTransient
	Template passwordTemplate;

	public VelocityContext makeContext() {
		var ctx = new HashMap();
		ctx.put("env", new HashMap(System.getenv()));
		ctx.putAll(getVariables());
		ctx.putAll(System.getProperties());
		ctx.put("bu", BooleanUtils.class);
		ctx.put("lu", LocaleUtils.class);
		ctx.put("du", DateUtils.class);
		ctx.put("nu", NumberUtils.class);
		ctx.put("su", StringUtils.class);
		ctx.put("seu", StringEscapeUtils.class);
		ctx.put("re", RegExUtils.class);
		return new VelocityContext(ctx);
	}

	public String getBefore() {
		return before;
	}

	public void setBefore(String template) throws ResourceNotFoundException, ParseErrorException, Exception {
		this.before = template;
		beforeTemplate = velocity.getTemplate(template);
	}

	public String getAfter() {
		return after;
	}

	public void setAfter(String template) throws ResourceNotFoundException, ParseErrorException, Exception {
		this.after = template;
		afterTemplate = velocity.getTemplate(template);
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
		urlTemplate = velocity.getTemplate(url);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
		usernameTemplate = velocity.getTemplate(username);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		passwordTemplate = velocity.getTemplate(password);
	}

	public List<String> getDateFormats() {
		return dateFormats;
	}

	public void setDateFormats(List<String> dateFormats) {
		this.dateFormats = dateFormats;
		if (dateFormats != null)
			new DateFormats(dateFormats);
	}

	public int getCommitEveryNumSqls() {
		return commitEveryNumSqls;
	}

	public void setCommitEveryNumSqls(int commitEveryNumSqls) {
		if (commitEveryNumSqls < 1)
			throw new IllegalArgumentException();
		this.commitEveryNumSqls = commitEveryNumSqls;
	}

	public Map<?,?> getFormatOptions() {
		return formatOptions;
	}

	public void setFormatOptions(Map formatOptions) {
		this.formatOptions = formatOptions;
	}

	public Template getUrlTemplate() {
		return urlTemplate;
	}

	public Template getUsernameTemplate() {
		return usernameTemplate;
	}

	public Template getPasswordTemplate() {
		return passwordTemplate;
	}

	public String getConnectionInitialize() {
		return connectionInitialize;
	}

	public void setConnectionInitialize(String connectionInitialize) {
		this.connectionInitialize = connectionInitialize;
		connectionInitializeTemplate = velocity.getTemplate(connectionInitialize);
	}

	public String getConnectionFinalize() {
		return connectionFinalize;
	}

	public void setConnectionFinalize(String connectionFinalize) {
		this.connectionFinalize = connectionFinalize;
		connectionFinalizeTemplate = velocity.getTemplate(connectionFinalize);
	}

	public Template getConnectionInitializeTemplate() {
		return connectionInitializeTemplate;
	}

	public Template getConnectionFinalizeTemplate() {
		return connectionFinalizeTemplate;
	}

	public Map getVariables() {
		return variables;
	}

	public void setVariables(Map variables) {
		this.variables = variables;
	}
}
