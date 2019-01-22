package com.slavi.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(namespace = "http://com.slavi/xml")
public class MyComplexType {

	@XmlAttribute
	public int id;

	@XmlAttribute
	public String type;

	@XmlValue
	public String text;
}
