package example.java.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

public class PropertiesEncodings {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Properties p = new Properties();
		p.setProperty("ItemName", "Това е на кирилица");
		p.store(new PrintWriter(System.out), "Коментари на кирилица");
		System.out.println("------------");
		p.store(System.out, "Коментари на кирилица");
	}
}
