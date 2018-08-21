package com.slavi.ann.test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.slavi.math.adjust.MatrixStatistics;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

public class Utils {
	public static final double valueLow = 0.05;
	public static final double valueHigh = 0.95;

	static final Logger log = LoggerFactory.getLogger(Utils.class);

	public static void computeDataStatistics(List<? extends DatapointPair> trainset) {
		if (!log.isDebugEnabled())
			return;
		MatrixStatistics stIn = new MatrixStatistics();
		MatrixStatistics stOut = new MatrixStatistics();
		stIn.start();
		stOut.start();
		Matrix input = new Matrix();
		Matrix output = new Matrix();

		for (DatapointPair pair : trainset) {
			pair.toInputMatrix(input);
			pair.toOutputMatrix(output);
			stIn.addValue(input);
			stOut.addValue(output);
		}

		stIn.stop();
		stOut.stop();

		log.debug("Dataset statistics" +
			"\nInput:\n" + stIn.toString(Statistics.CStatAvg | Statistics.CStatStdDev | Statistics.CStatCount //| Statistics.CStatMinMax
				) +
			"\nOutput:\n" + stOut.toString(Statistics.CStatAvg | Statistics.CStatStdDev //| Statistics.CStatMinMax
				));
	}

	public static double assertValidValue(double d) {
		if (Double.isInfinite(d) || Double.isNaN(d))
			throw new RuntimeException("Invalid value");
		return d;
	}

	public static Matrix assertValidMatrix(Matrix m) {
		for (int i = m.getSizeX() - 1; i >= 0; i--)
			for (int j = m.getSizeY() - 1; j >= 0; j--)
				assertValidValue(m.getItem(i, j));
		return m;
	}

	public static void randomMatrix(Matrix dest) {
		Random random = new Random();
		for (int i = dest.getVectorSize() - 1; i >= 0; i--)
			dest.setVectorItem(i, random.nextDouble());
	}

	public static void configureMapper(ObjectMapper m) {
		AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
		AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
		AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);

		m.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		//m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
		m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		m.setAnnotationIntrospector(pair);
		m.enable(SerializationFeature.INDENT_OUTPUT);
	}

	public static ObjectMapper xmlMapper() {
		ObjectMapper m = new XmlMapper();
		configureMapper(m);
		return m;
	}

	public static ObjectMapper jsonMapper() {
		ObjectMapper m = new ObjectMapper();
		configureMapper(m);
		return m;
	}

	public static void downloadDataFiles(String dataTargetDir, String dataUrl, String ... dataFiles ) throws Exception {
		File dir = new File(dataTargetDir);
		dir.mkdirs();
		URL url = new URL(dataUrl);
		for (String f : dataFiles) {
			File targetFile = new File(dir, f);
			if (!targetFile.isFile()) {
				log.info("Downloading file {}", targetFile);
				FileUtils.copyURLToFile(new URL(url, f), targetFile);
			}
		}
	}
}
