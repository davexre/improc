package example.java.lang;

public class JavaGenerics {
	static class Animal {}

	static class Mammal extends Animal {}

	static class Dog extends Mammal {}

	static class Foo<T> {
		T t;
		void bar(T t) { this.t = t; }
		T get() { return t; }
	}
	
	static class FooMammal<T extends Mammal> {
		T t;
		void bar(T t) { this.t = t; }
		T get() { return t; }
	}

//	static class FooMammalSup<T super Mammal> {
//		void bar(T t) {
//		}
//	}

	public static void main(String... args) {
		Animal animal = new Animal();
		Mammal mammal = new Mammal();
		Dog dog = new Dog();

		Foo<? super Mammal> foo_sup;
//		foo_sup = new Foo<Dog>();
		foo_sup = new Foo<Mammal>();
		foo_sup = new Foo<Animal>();
		
		Foo<? extends Mammal> foo_ext;
		foo_ext = new Foo<Dog>();
		foo_ext = new Foo<Mammal>();
//		foo_ext = new Foo<Animal>();
		
		FooMammal foo_mammal;
		foo_mammal = new FooMammal<Dog>();
		foo_mammal = new FooMammal<Mammal>();
//		foo_mammal = new FooMammal<Animal>();

		foo_sup.bar(dog);
		foo_sup.bar(mammal);
//		foo_sup.bar(animal);

//		foo_ext.bar(dog);
//		foo_ext.bar(mammal);
//		foo_ext.bar(animal);
		
		foo_mammal.bar(dog);
		foo_mammal.bar(mammal);
//		foo_mammal.bar(animal);
		
//		mammal = foo_sup.get();
		mammal = foo_ext.get();
		mammal = foo_mammal.get();
		
//		animal = foo_sup.get();
		animal = foo_ext.get();
		animal = foo_mammal.get();
		
		Object o = foo_sup.get();
		
		System.out.println(animal);
		System.out.println(o);
	}
}
