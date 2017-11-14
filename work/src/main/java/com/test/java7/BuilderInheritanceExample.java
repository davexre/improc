package com.test.java7;

public class BuilderInheritanceExample {
	public static class Base<T extends Base<T>> {
		int b;

		T b(int b) {
			this.b = b;
			return (T) this;
		}
	}

	public static class Child<T extends Child<T>> extends Base<T> {
		int c;

		T c(int c) {
			this.c = c;
			return (T) this;
		}
	}

	public static class Last extends Child<Last> {
		int d;

		Last d(int d) {
			this.d = d;
			return this;
		}
	}

	public static void main(String[] args) {
		new Last().b(1).c(2);
	}
}
