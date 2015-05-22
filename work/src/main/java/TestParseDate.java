import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.DateUtils;


public class TestParseDate {

	public static void main(String[] args) throws ParseException {
		String format = "yyyy-MM-dd"; //'T'HH:mm:ss]";
		String str = "2015-8-27T23:34:45";
		
		System.out.println(DateUtils.parseDate(str, new String[] { "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd" }));
		
		SimpleDateFormat df = new SimpleDateFormat(format);
		System.out.println(df.parse(str).toString());
		
	}
}
