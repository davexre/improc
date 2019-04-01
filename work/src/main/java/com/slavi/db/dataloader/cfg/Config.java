package com.slavi.db.dataloader.cfg;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.velocity.Template;
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

	String format;

	String url;
	String username;
	String password;
	int commitEveryNumSqls = 1;

	String before;
	String after;
	List<String> dateFormats;

	@XmlTransient
	Template beforeTemplate;

	@XmlTransient
	Template afterTemplate;

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
}
