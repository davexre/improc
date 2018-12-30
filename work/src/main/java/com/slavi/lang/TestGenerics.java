package com.slavi.lang;

public class TestGenerics {
	interface Organism<T extends Organism<T>> {
		default T organism() {
			System.out.println("Organism");
			return (T) this;
		}
	}

	static class Animal<T extends Animal<T>> implements Organism<T> {
		public T animal() {
			System.out.println("Animal");
			return (T) this;
		}
	}

	static class Pet<T extends Pet<T>> extends Animal<T> {
		public T pet() {
			System.out.println("Pet");
			return (T) this;
		}
	}

	static class Dog<T extends Dog<T>> extends Pet<T> {
		public T dog() {
			System.out.println("Dog");
			return (T) this;
		}
	}

	public static void main(String[] args) {
		Dog<?> d = new Dog();
		d.organism().animal().pet().dog();

		Dog d2 = new Dog();
		//d2.organism().animal().pet().dog();
	}
}
