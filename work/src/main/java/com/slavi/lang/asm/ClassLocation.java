package com.slavi.lang.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.objectweb.asm.ClassReader;

public class ClassLocation {
	public File location;
	public Map<String, AsmClass> declaredClasses = new HashMap();

	public ClassLocation(File location) {
		this.location = location;
	}

	private void processOneClass(InputStream classBytes) throws IOException {
		AsmClass r = new AsmClass();
		r.location = this;
		ClassReader cr = new ClassReader(classBytes);
		cr.accept(r.new ClassPrinter(), 0);
		declaredClasses.put(r.className, r);
	}

	public void processClassLocation() throws Exception {
		if (location.isDirectory()) {
			for (File i : FileUtils.listFiles(location, null, true)) {
				if (i.isDirectory() || !i.getName().endsWith(".class"))
					continue;
				try (FileInputStream is = new FileInputStream(i)) {
					processOneClass(is);
				}
			}
		} else {
			FileInputStream is = new FileInputStream(location);
			try (ZipInputStream zin = new ZipInputStream(is)) {
				ZipEntry entry = null;
				while ((entry = zin.getNextEntry()) != null) {
					String name = entry.getName();
					if (entry.isDirectory() || !name.endsWith(".class"))
						continue;
					processOneClass(new CloseShieldInputStream(zin));
				}
			}
		}
	}
}
