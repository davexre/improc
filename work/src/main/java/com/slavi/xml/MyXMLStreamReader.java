package com.slavi.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

public class MyXMLStreamReader extends StreamReaderDelegate {

	public Map<Integer, String> code2String = new HashMap<>();

	public static final String tagName = "<tagName>";

	public Stack<Map<String, String>> path = new Stack<>();

	CharSequenceTranslator ESCAPE;

	public MyXMLStreamReader(XMLStreamReader reader) {
		super(reader);
		code2String.put(XMLStreamConstants.START_ELEMENT         , "START_ELEMENT");
		code2String.put(XMLStreamConstants.END_ELEMENT           , "END_ELEMENT");
		code2String.put(XMLStreamConstants.SPACE                 , "SPACE");
		code2String.put(XMLStreamConstants.START_DOCUMENT        , "START_DOCUMENT");
		code2String.put(XMLStreamConstants.ENTITY_REFERENCE      , "ENTITY_REFERENCE");
		code2String.put(XMLStreamConstants.ATTRIBUTE             , "ATTRIBUTE");
		code2String.put(XMLStreamConstants.PROCESSING_INSTRUCTION, "PROCESSING_INSTRUCTION");
		code2String.put(XMLStreamConstants.CHARACTERS            , "CHARACTERS");
		code2String.put(XMLStreamConstants.COMMENT               , "COMMENT");
		code2String.put(XMLStreamConstants.END_DOCUMENT          , "END_DOCUMENT");
		code2String.put(XMLStreamConstants.DTD                   , "DTD");
		code2String.put(XMLStreamConstants.CDATA                 , "CDATA");
		code2String.put(XMLStreamConstants.NAMESPACE             , "NAMESPACE");
		code2String.put(XMLStreamConstants.NOTATION_DECLARATION  , "NOTATION_DECLARATION");
		code2String.put(XMLStreamConstants.ENTITY_DECLARATION    , "ENTITY_DECLARATION");

		Map<CharSequence, CharSequence> escapeMap = new HashMap<>();
		escapeMap.put("\b", "\\b");
		escapeMap.put("\n", "\\n");
		escapeMap.put("\t", "\\t");
		escapeMap.put("\f", "\\f");
		escapeMap.put("\r", "\\r");
		escapeMap.put("\0", "\\0");
		escapeMap.put("\"", "\\\"");
		escapeMap.put("\\", "\\\\");
		ESCAPE = new LookupTranslator(escapeMap);
	}

	private Comparator<Map.Entry<String, String>> compare = (a, b) -> a.getKey().compareToIgnoreCase(b.getKey());

	void attributesToString(StringBuilder r, Map<String, String> map) {
		if (map == null || map.size() == 1)
			return;
		r.append('[');
		ArrayList<Map.Entry<String, String>> items = new ArrayList<>(map.entrySet());
		Collections.sort(items, compare);
		String prefix = "";
		for (Map.Entry<String, String> i : items) {
			if (tagName == i.getKey()) // This comparison is ok.
				continue;
			r.append(prefix).append(i.getKey()).append("=\"").append(ESCAPE.translate(i.getValue())).append("\"");
			prefix = " ";
		}
		r.append(']');
	}

	public String getCurrentPath() {
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < path.size(); i++)
			r.append('/').append(path.get(i).get(tagName));
		return r.toString();
	}

	public String getCurrentPathWithLastAttr() {
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < path.size(); i++)
			r.append('/').append(path.get(i).get(tagName));
		if (!path.isEmpty())
			attributesToString(r, path.peek());
		return r.toString();
	}

	private void processAttibutes() {
		Map<String, String> map = path.peek();
		for (int i = super.getAttributeCount() - 1; i >= 0; i--)
			map.put(super.getAttributeLocalName(i), super.getAttributeValue(i));
	}

	public int next() throws XMLStreamException {
		int r = super.next();
		switch (r) {
			case XMLStreamConstants.START_ELEMENT:
				Map<String, String> map = new HashMap<>();
				map.put(tagName, super.getLocalName());
				path.push(map);
				processAttibutes();
				break;
			case XMLStreamConstants.END_ELEMENT:
				path.pop();
				break;
			case XMLStreamConstants.SPACE:
				break;
			case XMLStreamConstants.START_DOCUMENT:
				path.clear();
				break;
			case XMLStreamConstants.ENTITY_REFERENCE:
				break;
			case XMLStreamConstants.ATTRIBUTE:
				processAttibutes();
				break;
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
			case XMLStreamConstants.CHARACTERS:
			case XMLStreamConstants.COMMENT:
			case XMLStreamConstants.END_DOCUMENT:
			case XMLStreamConstants.DTD:
			case XMLStreamConstants.CDATA:
			case XMLStreamConstants.NAMESPACE:
			case XMLStreamConstants.NOTATION_DECLARATION:
			case XMLStreamConstants.ENTITY_DECLARATION:
			default:
				break;
		}
		return r;
	}
}
