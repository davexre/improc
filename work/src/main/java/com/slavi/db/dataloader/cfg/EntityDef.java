package com.slavi.db.dataloader.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.slavi.util.DateFormats;

public class EntityDef implements Serializable {
	public final VelocityEngine velocity;

	String name;
	String path;
	String before;
	String after;
	String sql;
	List<String> params;
	List<String> dateFormats;

	@XmlTransient
	Pattern pathPattern;

	@XmlTransient
	List<Template> paramTemplates = new ArrayList<>();

	@XmlTransient
	Template beforeTemplate;

	@XmlTransient
	Template afterTemplate;

	@XmlTransient
	Template sqlTemplate;

	public EntityDef(@JacksonInject(value=Config.JacksonInjectTag) VelocityEngine velocity) {
		this.velocity = velocity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
		this.pathPattern = Pattern.compile(path);
	}

	public String getBefore() {
		return before;
	}

	public void setBefore(String template) throws ResourceNotFoundException, ParseErrorException, Exception {
		before = template;
		beforeTemplate = velocity.getTemplate(template);
	}

	public String getAfter() {
		return after;
	}

	public void setAfter(String template) throws ResourceNotFoundException, ParseErrorException, Exception {
		after = template;
		afterTemplate = velocity.getTemplate(template);
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String template) throws ResourceNotFoundException, ParseErrorException, Exception {
		sql = template;
		sqlTemplate = velocity.getTemplate(template);
	}

	public List<String> getParams() {
		return params;
	}

	public void setParams(List<String> params) throws Exception {
		this.params = params;
		paramTemplates.clear();
		if (params != null) {
			for (String i : params)
				paramTemplates.add(velocity.getTemplate(i));
		}
	}

	public Pattern getPathPattern() {
		return pathPattern;
	}

	public Template getBeforeTemplate() {
		return beforeTemplate;
	}

	public Template getAfterTemplate() {
		return afterTemplate;
	}

	public Template getSqlTemplate() {
		return sqlTemplate;
	}

	public List<Template> getParamTemplates() {
		return paramTemplates;
	}

	public List<String> getDateFormats() {
		return dateFormats;
	}

	public void setDateFormats(List<String> dateFormats) {
		this.dateFormats = dateFormats;
		if (dateFormats != null)
			new DateFormats(dateFormats);
	}
}
