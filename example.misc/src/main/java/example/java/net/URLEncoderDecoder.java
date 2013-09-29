package example.java.net;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.slavi.util.testUtil.TestUtil;

public class URLEncoderDecoder {
	public static void main(String[] args) throws UnsupportedEncodingException {
		String encoding = "UTF-8";
		String str = "asd:QWE!@$#@%$#%^&&%*\"\"\\//:; това е на кирилица";
		String encoded = URLEncoder.encode(str, encoding);
		String decoded = URLDecoder.decode(encoded, encoding);
		System.out.println(str);
		System.out.println(decoded);
		System.out.println(encoded);
		TestUtil.assertEqual("", str, decoded);
	}
}
