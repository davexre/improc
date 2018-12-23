package com.slavi.jut;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.objectweb.asm.ClassReader;

import com.slavi.jut.asm.AsmClass;

public class TestAsmByteCode extends AsmClass {

	void doIt2() throws Exception {
		File dir = new File("target/classes/");
		ClassPrinter cp = new ClassPrinter();
		File f = new File(dir, "com/slavi/lang/TestAsmByteCode.class");
		try (FileInputStream is = new FileInputStream(f)) {
			ClassReader cr = new ClassReader(is);
			cr.accept(cp, 0);
		}

		System.out.println("--------- Referenced classes ----------");
		ArrayList<String> classes = new ArrayList(this.usedClasses);
		Collections.sort(classes);
		for (String t : classes) {
			System.out.println(t);
		}
	}

	String[] typeTests = {
		"Ljava/util/List<[Ljava/util/List<Ljava/lang/String;>;>;",
		"Ljava/util/List<TE;>;",
		"Ljava/util/List<*>;",
		"Ljava/util/List<+Ljava/lang/Number;>;",
		"Ljava/util/List<-Ljava/lang/Integer;>;",
		"Ljava/util/List<[Ljava/util/List<Ljava/lang/String;>;>;",
		"Ljava/util/HashMap<TK;TV;>.HashIterator<TK;>;",
	};

	String[] methodTests = {
		"<T:Ljava/lang/Object;>(I)Ljava/lang/Class<+TT;>;",
		"()V",
		"<SerializableObject::Ljava/io/Serializable;>(TSerializableObject;)TSerializableObject;",
	};

	String[] classTests = {
		"<E:Lajava/lang/Object;T:Lbjava/lang/Object;>Lcjava/util/List;",
		"<E:Ljava/lang/Object;>Ljava/util/List<TE;>;",
		"<InputType:Ljava/lang/Object;OutputType:Ljava/lang/Object;>Lcom/slavi/math/transform/BaseTransformer<TInputType;TOutputType;>;"
	};

	void doIt() throws Exception {
		for (String t: typeTests) {
			System.out.println(">>> " + t);
			AsmSignatureParserImpl p = new AsmSignatureParserImpl(t);
			p.parseTypeSignature();
			System.out.println(usedClasses);
			usedClasses.clear();
		}

		for (String t: methodTests) {
			System.out.println(">>> " + t);
			AsmSignatureParserImpl p = new AsmSignatureParserImpl(t);
			p.parseMethodSignature();
			System.out.println(usedClasses);
			usedClasses.clear();
		}

		for (String t: classTests) {
			System.out.println(">>> " + t);
			AsmSignatureParserImpl p = new AsmSignatureParserImpl(t);
			p.parseClassSignature();
			System.out.println(usedClasses);
			usedClasses.clear();
		}
	}

	public static void main(String[] args) throws Exception {
		new TestAsmByteCode().doIt();
		System.out.println("Done.");
	}
}
