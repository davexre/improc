package com.test.java7;

public class LoopBreakToLabel {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		label: while (args.length < 0) {
			System.out.println("Will not execute");
		};
		
		label: do {
			System.out.println("Will execute once");
		} while (args.length > 0);
		
		label: for (int i = 0; i < 3; i++) {
			System.out.println("I = " + i);
			for (int j = 0; j < 3; j++) {
				System.out.println("J = " + j);
				if (i + j == 3) {
					System.out.println("Breaking");
					break label;
				}
			}
			System.out.println("next i");
		}
		System.out.println("Done.");
	}
}
