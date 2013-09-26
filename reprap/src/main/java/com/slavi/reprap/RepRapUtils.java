package com.slavi.reprap;

import java.io.IOException;
import java.net.URL;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;

import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.stl.STLFileReader;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;

public class RepRapUtils {
	public static Scene load(URL url) throws IncorrectFormatException, ParsingErrorException, InvalidFormatException, IOException {
		STLFileReader reader = new STLFileReader(url);
		final SceneBase scene = new SceneBase();
		final BranchGroup bg = new BranchGroup();
		final int numOfObjects = reader.getNumOfObjects();
		final int[] numOfFacets = reader.getNumOfFacets();
		final String[] names = reader.getObjectNames();

		final double[] normal = new double[3];
		final float[] fNormal = new float[3];
		final double[][] vertices = new double[3][3];
		for (int i = 0; i < numOfObjects; i++) {
			final TriangleArray geometry = new TriangleArray(3 * numOfFacets[i], TriangleArray.NORMALS
					| TriangleArray.COORDINATES);
			int index = 0;
			for (int j = 0; j < numOfFacets[i]; j++) {
				final boolean ok = reader.getNextFacet(normal, vertices);
				if (ok) {
					fNormal[0] = (float) normal[0];
					fNormal[1] = (float) normal[1];
					fNormal[2] = (float) normal[2];
					for (int k = 0; k < 3; k++) {
						geometry.setNormal(index, fNormal);
						geometry.setCoordinate(index, vertices[k]);
						index++;
					}
				} else {
					throw new ParsingErrorException();
				}
			}
			final Shape3D shape = new Shape3D(geometry);
			bg.addChild(shape);
			String name = names[i];
			if (name == null) {
				name = new String("Unknown_" + i);
			}
			scene.addNamedObject(name, shape);
		}
		scene.setSceneGroup(bg);
		return scene;
	}
}
