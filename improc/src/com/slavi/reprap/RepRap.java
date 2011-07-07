package com.slavi.reprap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.media.j3d.BranchGroup;

import org.j3d.renderer.java3d.loaders.STLLoader;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;

public class RepRap {
	
	void loadSTL(Reader fin) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
		STLLoader loader = new STLLoader();
		Scene scene = loader.load(fin);
		BranchGroup stl = scene.getSceneGroup();
		// STLObject.loadSingleSTL
	}
	
	void doIt() throws Exception {
		String fname = "pulley-4.5-6-8-40.stl";
		Reader fin = new InputStreamReader(getClass().getResourceAsStream(fname));
		loadSTL(fin);
		fin.close();
	}

	public static void main(String[] args) throws Exception {
		new RepRap().doIt();
		System.out.println("Done.");
	}
}
