package com.slavi.db.dataloader.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.velocity.Template;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.slavi.util.jackson.PostLoad;

public class EntityDef implements Serializable {
	String name;
	String path;
	String before;
	String sql;
	ArrayList<String> params;

	@XmlTransient
	Pattern pathPattern;

	@XmlTransient
	ArrayList<Template> paramTemplates;
	
	@XmlTransient
	Template beforeTemplate;

	@XmlTransient
	Template sqlTemplate;

	public EntityDef() {
	}

	public EntityDef(String name, String path) {
		this.name = name;
		this.path = path;
		postLoad();
	}

	@PostLoad
	public void postLoad() {
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
		beforeTemplate = Config.velocity.get().getTemplate(template);
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String template) throws ResourceNotFoundException, ParseErrorException, Exception {
		sql = template;
		sqlTemplate = Config.velocity.get().getTemplate(template);
	}

	public ArrayList<String> getParams() {
		return params;
	}

	public void setParams(ArrayList<String> params) {
		this.params = params;
	}

	public Pattern getPathPattern() {
		return pathPattern;
	}

	public Template getBeforeTemplate() {
		return beforeTemplate;
	}

	public Template getSqlTemplate() {
		return sqlTemplate;
	}
}
