package com.slavi.jut.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CloseShieldInputStream;

import com.slavi.jut.asm.AsmClass;

public class Location {

	public String classes;

	public String sources;

	public transient String classesPath;
	public transient String sourcesPath;
	public transient Map<String, AsmClass> declaredClasses = new HashMap();

	public void loadClasses() throws Exception {
		File locationFile = new File(classesPath);
		if (locationFile.isDirectory()) {
			for (File i : FileUtils.listFiles(locationFile, null, true)) {
				if (i.isDirectory() || !i.getName().endsWith(".class"))
					continue;
				try (FileInputStream is = new FileInputStream(i)) {
					AsmClass ac = AsmClass.loadOneClass(is);
					ac.location = this;
					declaredClasses.put(ac.className, ac);
				}
			}
		} else {
			FileInputStream is = new FileInputStream(locationFile);
			try (ZipInputStream zin = new ZipInputStream(is)) {
				ZipEntry entry = null;
				while ((entry = zin.getNextEntry()) != null) {
					String name = entry.getName();
					if (entry.isDirectory() || !name.endsWith(".class"))
						continue;
					AsmClass ac = AsmClass.loadOneClass(new CloseShieldInputStream(is));
					ac.location = this;
					declaredClasses.put(ac.className, ac);
				}
			}
		}
	}
}
