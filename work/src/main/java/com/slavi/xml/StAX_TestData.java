package com.slavi.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;

public class StAX_TestData {

	enum State {
		initial,
		tests,
		test,
		data
	}

	public void doIt(String[] args) throws Exception {
		XMLInputFactory f = XMLInputFactory.newInstance();
		MyXMLStreamReader r = new MyXMLStreamReader(f.createXMLStreamReader(getClass().getResourceAsStream("StAX_testData.xml")));
		State state = State.initial;
		HashMap<String, String> item = new HashMap<>();
		String lastElement = "";

		HashSet<String> columns = new HashSet();

		for (int event = r.next();
				event != XMLStreamConstants.END_DOCUMENT;
				event = r.next()) {
			System.out.println(r.code2String.get(event) + " " + r.getCurrentPathWithLastAttr());
			switch (event) {
			case XMLStreamConstants.ATTRIBUTE:
				System.out.println("aaa");
			case XMLStreamConstants.START_ELEMENT: {
				String name = r.getLocalName();
				if ("tests".equals(name) && state == State.initial) {
					state = State.tests;
				} else if ("test".equals(name) && state == State.tests) {
					state = State.test;
				} else if (state == State.test) {
					state = State.data;
					lastElement = name;
					columns.add(name);
				}
				break;
			}

			case XMLStreamConstants.END_ELEMENT: {
				String name = r.getLocalName();
				if ("tests".equals(name) && state == State.test) {
					state = State.initial;
				} else if ("test".equals(name) && state == State.test) {
					state = State.tests;
					System.out.println(
						item.get("name") + " " +
						item.get("points")
						);
					item.clear();
				} else if (state == State.data) {
					state = State.test;
				}
				break;
			}

			case XMLStreamConstants.CHARACTERS: {
				if (state == State.data) {
					item.put(lastElement, r.getText());
				}
			}
			}
		}

		ArrayList<String> c = new ArrayList<>(columns);
		Collections.sort(c);
		System.out.println(c);
	}

	public static void main(String[] args) throws Exception {
		new StAX_TestData().doIt(args);
		System.out.println("Done.");
	}
}
