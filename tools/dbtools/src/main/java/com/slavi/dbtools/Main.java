package com.slavi.dbtools;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class Main {
	public static void main(String[] args) {
		try {
			List<String> argsList = new ArrayList<>();
			if (args != null)
				for (String arg : args)
					argsList.add(arg);
			String cmd = argsList.size() == 0 ? "" : StringUtils.trimToEmpty(argsList.remove(0));
			args = argsList.toArray(new String[argsList.size()]);

			switch (cmd) {
				case "dbcompare": System.exit(com.slavi.tools.dbcompare.Main.main0(args));
				case "dbexport": System.exit(com.slavi.tools.dbexport.Main.main0(args));
			}
			System.out.println(IOUtils.toString(Main.class.getResourceAsStream("Main.help.txt"), "UTF8"));
			System.exit(254);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(255);
		}
	}
}
