package com.slavi.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class LoadDemoSystems {

	public void doIt(String[] args) throws Exception {
		XMLInputFactory f = XMLInputFactory.newInstance();
		XMLEventReader r = f.createXMLEventReader(getClass().getResourceAsStream("demo-systems.xml"));
		Map<String, String> map = new HashMap<>();
		boolean inQueryTag = false;
		while (r.hasNext()) {
			XMLEvent event = r.nextEvent();
			if (event.isStartElement()) {
				StartElement e = event.asStartElement();
				String name = e.getName().getLocalPart();
				if ("query".equals(name)) {
					inQueryTag = true;
					map.clear();
				} else if (inQueryTag) {
					map.put(name, r.getElementText());
				}
			} else if (event.isEndElement()) {
				EndElement e = event.asEndElement();
				String name = e.getName().getLocalPart();
				if ("query".equals(name)) {
					System.out.println(map);
					inQueryTag = false;
					map.clear();
				}
			} else if (event.isEndDocument()) {
				break;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new LoadDemoSystems().doIt(args);
		System.out.println("Done.");
	}
}
