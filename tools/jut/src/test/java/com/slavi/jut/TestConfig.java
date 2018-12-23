package com.slavi.jut;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.slavi.jut.cfg.Config;
import com.slavi.jut.cfg.Destination;
import com.slavi.jut.cfg.Location;

public class TestConfig {

	int counter = 1;

	Destination makeDestination() {
		Destination r = new Destination();
		r.patterns.add("pattern " + (counter++));
		r.patterns.add("pattern " + (counter++));
		r.patterns.add("pattern " + (counter++));
		r.sources = "sources " + (counter++);
		return r;
	}

	Location makeLocation() {
		Location r = new Location();
		r.classes = "classes " + (counter++);
		r.sources = "sources " + (counter++);
		return r;
	}

	void doIt() throws Exception {
		Config cfg = new Config();
		cfg.common = makeDestination();
		cfg.destinations.add(makeDestination());
		cfg.destinations.add(makeDestination());
		cfg.destinations.add(makeDestination());
		cfg.locations.add(makeLocation());

		ObjectMapper m = new ObjectMapper();
		m.enable(SerializationFeature.INDENT_OUTPUT);
		String s = m.writeValueAsString(cfg);
		System.out.println(s);

		cfg = m.readValue(s, Config.class);
		String s2 = m.writeValueAsString(cfg);
		System.out.println(s.equals(s2));
	}

	public static void main(String[] args) throws Exception {
		new TestConfig().doIt();
		System.out.println("Done.");
	}
}
