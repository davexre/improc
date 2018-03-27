package com.test;

import java.io.FileInputStream;
import java.io.InputStream;

import org.jdom2.Document;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;

public class MavenDecodePassword {
	public static void main(String[] args) throws Exception {
		// args = new String[] { "{KobptvOIk3IHdAKzMWKuBxmwg453a63K6CHgxeUa8/Q=}" };
		String fname = "~/.m2/settings-security.xml";
		fname = fname.replaceFirst("^~", System.getProperty("user.home"));
		InputStream is = new FileInputStream(fname);
		Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(is);
		XPathExpression exp = XPathFactory.instance().compile("/settingsSecurity/master/text()");
		String masterPassword = ((Text) (exp.evaluateFirst(doc))).getTextNormalize();
		masterPassword = new DefaultPlexusCipher().decryptDecorated(masterPassword, "settings.security");
		
		System.out.println(masterPassword);
		
		if (args != null)
			for (String i : args) {
				String p = new DefaultPlexusCipher().decryptDecorated(i, masterPassword);
				System.out.println(p);
			}
	}
}
