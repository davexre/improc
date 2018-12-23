package com.slavi.jut;

public class TestJut {

//	"${user.home}/.m2/repository/com/slavi/util.math/1.0.0-SNAPSHOT/util.math-1.0.0-SNAPSHOT.jar",
//	"${user.home}/.m2/repository/com/slavi/util.util/1.0.0-SNAPSHOT/util.util-1.0.0-SNAPSHOT.jar",
//	"${user.home}/.m2/repository/com/slavi/util.dbutil/1.0.0-SNAPSHOT/util.dbutil-1.0.0-SNAPSHOT.jar",
//	"${user.home}/.m2/repository/com/slavi/util.io/1.0.0-SNAPSHOT/util.io-1.0.0-SNAPSHOT.jar",

	static String[] testMove1 = new String[] {
		"move",
		"-m", "split",
		"-l", "${IMPROC_HOME}/work/target/classes=${IMPROC_HOME}/work/src/main/java",
		"-t", "${IMPROC_HOME}/work/target/extract=com/slavi/ann/test/v2/connection/ConvolutionLayer",
		"-t", "${IMPROC_HOME}/work/target/extract=com/slavi/ann/test/v2/connection/Bias.*",
	};

	static String[] testMove2 = new String[] {
		"move",
		"-m", "common",
		"-l", "${IMPROC_HOME}/work/target/classes=${IMPROC_HOME}/work/src/main/java",
		"-t", "${IMPROC_HOME}/work/target/extract=com/slavi/ann/.*",
		"-c", "${IMPROC_HOME}/work/target/common"
	};

	static String[] testDraw1 = new String[] {
		"draw",
		"-l", "${IMPROC_HOME}/work/target/classes=${IMPROC_HOME}/work/src/main/java",
		"-r", "com/slavi/ann/test/v2/connection/ConvolutionLayer",
		"-r", "com/slavi/ann/test/v2/connection/Bias.*",
	};

	static String[] testDraw2 = new String[] {
		"draw",
		"-m", "package",
		"-l", "${IMPROC_HOME}/work/target/classes=${IMPROC_HOME}/work/src/main/java",
		"-r", "com/slavi/ann/test/v2/connection/ConvolutionLayer",
		"-r", "com/slavi/ann/test/v2/connection/Bias.*",
	};

	public static void main(String[] args) throws Exception {
		Main.main(testDraw2);
	}
}
