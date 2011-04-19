package example.javax;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

public class EnumartePrinters {
	public static void main(String[] args) {
		String tName = "";
		PrintService[] pServices = PrintServiceLookup.lookupPrintServices(null, null);
		System.out.println("Service count: " + pServices.length);
		System.out.println("----------");
		for (int i = 0; i < pServices.length; i++) {
			tName = pServices[i].getName();
			System.out.println("Printer: " + tName);
		}
	}
}
