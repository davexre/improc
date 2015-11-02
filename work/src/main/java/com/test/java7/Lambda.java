package com.test.java7;

public class Lambda {

	interface MyInterface {
		void doIt(int x, int y);
		default int getme(int x) {
			return x;
		}
	}

	void callMyInterface(MyInterface a) {
		a.doIt(1, 2);
	}

	void doIt() throws Exception {
		callMyInterface((x, y) -> {
			System.out.println(x);
			System.out.println(y);
		});
	}

	public static void main(String[] args) throws Exception {
		new Lambda().doIt();
		System.out.println("Done.");
	}
}
