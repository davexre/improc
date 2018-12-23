package com.slavi.jut.cfg;

import java.util.ArrayList;
import java.util.List;

public class Config {

	public Mode mode = Mode.split;

	public List<Location> locations = new ArrayList();

	public List<Destination> destinations = new ArrayList();

	public Destination common = new Destination();
}
