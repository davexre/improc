package com.slavi.jut;

public class TestJut {

//	"${user.home}/.m2/repository/com/slavi/util.math/1.0.0-SNAPSHOT/util.math-1.0.0-SNAPSHOT.jar",
//	"${user.home}/.m2/repository/com/slavi/util.util/1.0.0-SNAPSHOT/util.util-1.0.0-SNAPSHOT.jar",
//	"${user.home}/.m2/repository/com/slavi/util.dbutil/1.0.0-SNAPSHOT/util.dbutil-1.0.0-SNAPSHOT.jar",
//	"${user.home}/.m2/repository/com/slavi/util.io/1.0.0-SNAPSHOT/util.io-1.0.0-SNAPSHOT.jar",

	static String[] test1 = new String[] {
		"-m", "split",
		"-l", "${IMPROC_HOME}/work/target/classes=${IMPROC_HOME}/work/src/main/java",
		"-t", "${IMPROC_HOME}/work/target/extract=com/slavi/ann/test/v2/connection/ConvolutionLayer",
		"-t", "${IMPROC_HOME}/work/target/extract=com/slavi/ann/test/v2/connection/Bias.*",
	};

	static String[] test2 = new String[] {
			"-m", "common",
			"-l", "${IMPROC_HOME}/work/target/classes=${IMPROC_HOME}/work/src/main/java",
			"-t", "${IMPROC_HOME}/work/target/extract=com/slavi/ann/.*",
			"-c", "${IMPROC_HOME}/work/target/common"
		};

	public static void main(String[] args) throws Exception {
		Main.main(test1);
	}
}
