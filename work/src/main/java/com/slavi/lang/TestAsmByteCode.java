package com.slavi.lang;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TestAsmByteCode {

/*
	https://asm.ow2.io/asm4-guide.pdf

	Java type	Type descriptor
	boolean		Z
	char		C
	byte		B
	short		S
	int			I
	float		F
	long		J
	double		D
	Object		Ljava/lang/Object;
	int[]		[I
	Object[][]	[[Ljava/lang/Object;

	A method descriptor is a list of type descriptors that describe the parameter
	types and the return type of a method, in a single string. A method descriptor
	starts with a left parenthesis, followed by the type descriptors of each formal
	parameter, followed by a right parenthesis, followed by the type descriptor of
	the return type, or V if the method returns void (a method descriptor does
	not contain the method’s name or the argument names).

	Method declaration				Method descriptor
	in source file
	void m(int i, float f)			(IF)V
	int m(Object o)					(Ljava/lang/Object;)I
	int[] m(int i, String s)		(ILjava/lang/String;)[I
	Object m(int[] i)				([I)Ljava/lang/Object;
*/
	static final int ASM = Opcodes.ASM4;

	public static class MethodPrinter extends MethodVisitor {
		public MethodPrinter() {
			super(ASM);
		}

		public void visitTypeInsn(int opcode, String type) {
			System.out.println("visitTypeInsn: " + type);
		}

		public void visitParameter(String name, int access) {
			System.out.println("visitParameter: " + name);
		}

	}

	public static class ClassPrinter extends ClassVisitor {
		public ClassPrinter() {
			super(ASM);
		}

		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			System.out.println(name + " extends " + superName + " {");
		}

		public void visitSource(String source, String debug) {
			System.out.println("visitSource " + source + " (" + debug + ")");
		}

		public void visitOuterClass(String owner, String name, String desc) {
			System.out.println("visitOuterClass " + owner + ":" + name + " (" + desc + ")");
		}

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return null;
		}

		public void visitAttribute(Attribute attr) {
		}

		public void visitInnerClass(String name, String outerName, String innerName, int access) {
		}

		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			System.out.println("    " + desc + " " + name);
			return null;
		}

		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			System.out.println("    " + signature + " " + name + " " + desc);
			return new MethodPrinter();
		}

		public void visitEnd() {
			System.out.println("}");
		}
	}

	List<String> myDummy() { return null; }

	File dir = new File("target/classes/");
	void doIt() throws Exception {

		ClassPrinter cp = new ClassPrinter();
		File f = new File(dir, "com/slavi/lang/TestAsmByteCode.class");
		try (FileInputStream is = new FileInputStream(f)) {
			ClassReader cr = new ClassReader(is);
			cr.accept(cp, 0);
		}
	}

	public static void main(String[] args) throws Exception {
		new TestAsmByteCode().doIt();
		System.out.println("Done.");
	}
}
