package bg.elections.pv;

import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class TestCSV {

	void doIt() throws Exception {
		String data = "asd,q\\rwe,123\nrrr,444,ttt\n#fg,hrty,456";
		for (CSVRecord i : CSVFormat.DEFAULT.withEscape('\\').withCommentMarker('#').parse(new StringReader(data))) {
			System.out.print("Rec: " + i.getRecordNumber() + ": ");
			for (String c : i) {
				System.out.print(c);
				System.out.print("\t");
			}
			System.out.println();
		}
	}

	public static void main(String[] args) throws Exception {
		new TestCSV().doIt();
		System.out.println("Done.");
	}
}
