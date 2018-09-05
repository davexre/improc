package com.slavi.xml;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class StAX_Test {

	enum State {
		initial,
		VFPData,
		query,
		data
	}

	public void doIt(String[] args) throws Exception {
		XMLInputFactory f = XMLInputFactory.newInstance();
		XMLStreamReader r = f.createXMLStreamReader(new FileInputStream("/home/spetrov/temp/billinglabel_demo.xml"));
		State state = State.initial;
		HashMap<String, String> item = new HashMap<>();
		String lastElement = "";

		HashSet<String> columns = new HashSet();

		for (int event = r.next();
				event != XMLStreamConstants.END_DOCUMENT;
				event = r.next()) {
			switch (event) {
			case XMLStreamConstants.START_ELEMENT: {
				String name = r.getLocalName();
				if ("VFPData".equals(name) && state == State.initial) {
					state = State.VFPData;
				} else if ("query".equals(name) && state == State.VFPData) {
					state = State.query;
				} else if (state == State.query) {
					state = State.data;
					lastElement = name;
					columns.add(name);
				}
				break;
			}

			case XMLStreamConstants.END_ELEMENT: {
				String name = r.getLocalName();
				if ("VFPData".equals(name) && state == State.query) {
					state = State.initial;
				} else if ("query".equals(name) && state == State.query) {
					state = State.VFPData;
					System.out.println(
						item.get("label_id") + " " +
						item.get("name") + " " +
						item.get("demo")
						);
/*
	label_id
	serverid
	name
	urlhome
	cytric_portal
	issublabel
	origin_id
	demo
	corporatecode
	businessowner
	cytric_easy
	expense_one
	expense_bus
	expense_ent
	expense_id
	vegas_reporting


activ, an_partner, autocreate, best_id, billingfrom, businessowner,
corporatecode, created, createremail, creatername, creatervorname,
cytric_easy, cytric_portal, deletedate, demo, expense_bus, expense_ent,
expense_id, expense_one, grouping, groupname, history, info, issublabel,
label_id, land, minvolume, name, origin_id, ort, parent_id, part_fax,
part_mail, part_mobil, part_tel, plz, productfacility, promotion,
serverid, strasse, urlhome, vegas_reporting

 */

					item.clear();
				} else if (state == State.data) {
					state = State.query;
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
		new StAX_Test().doIt(args);
		System.out.println("Done.");
	}
}
