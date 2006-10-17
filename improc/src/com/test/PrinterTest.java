package com.test;

import java.applet.Applet;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

public class PrinterTest extends Applet{

	private static final long serialVersionUID = -419865865922648829L;
	Button pushMe;
	
	public PrinterTest() {
		pushMe = new Button("Push me");
		pushMe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				StringBuffer buf = new StringBuffer();
				PrintService[] pServices = PrintServiceLookup
						.lookupPrintServices(null, null);
				for (int i = 0; i < pServices.length; i++) {
					buf.append(pServices[i].getName() + ", ");
				}
				pushMe.setLabel(buf.toString());				
			}			
		});
	}
	
	public void PrintNames() {
		String tName = "";
		PrintService[] pServices = PrintServiceLookup.lookupPrintServices(null,
				null);
		System.out.println("Service count: " + pServices.length);
		for (int i = 0; i < pServices.length; i++) {
			tName = pServices[i].getName();
			System.out.println("Printer: " + tName);
		}
		return;
	}

	public void doIt() {
		PrintNames();
	}

	public static void main(String[] args) {
		PrinterTest pt = new PrinterTest();
		pt.doIt();
	}

}
