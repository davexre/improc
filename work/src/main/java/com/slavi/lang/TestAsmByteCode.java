package com.slavi.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

import com.slavi.lang.asmParser.AsmSignatureParser;

public class TestAsmByteCode {

	static final int ASM = Opcodes.ASM7;

	public HashSet<String> classes = new HashSet();

	public void addClassName(String className) {
		if (StringUtils.isEmpty(className)) return;
		classes.add(className);
	}

	public void addType(String desc) {
		if (StringUtils.isEmpty(desc)) return;
		try {
			new AsmSignatureParserImpl(desc).parseTypeSignature();
		} catch (Exception e) {
			throw new Error(desc, e);
		}
	}

	public void addMethod(String desc) {
		if (StringUtils.isEmpty(desc)) return;
		try {
			new AsmSignatureParserImpl(desc).parseMethodSignature();
		} catch (Exception e) {
			throw new Error(desc, e);
		}
	}

	public void addClass(String desc) {
		if (StringUtils.isEmpty(desc)) return;
		try {
			new AsmSignatureParserImpl(desc).parseClassSignature();
		} catch (Exception e) {
			throw new Error(desc, e);
		}
	}

	public void addObject(Object i) {
		if (i instanceof Type) {
			addType(((Type) i).getDescriptor());
		} else if (i instanceof Handle) {
			addType(((Handle) i).getDesc());
		} else if (i instanceof ConstantDynamic) {
			addType(((ConstantDynamic) i).getDescriptor());
		// } else if (i instanceof ) { // More MethodVisitor.visitLdcInsn
		}
	}

	class AsmSignatureParserImpl extends AsmSignatureParser {

		public AsmSignatureParserImpl(String str) {
			super(new StringReader(str));
		}

		@Override
		public void addClass(String className) {
			addClassName(className);
		}
	}

	public class ModulePrinter extends ModuleVisitor {
		public ModulePrinter(int api) {
			super(ASM);
		}

		public void visitMainClass(final String mainClass) {
			addClassName(mainClass);
		}

		public void visitPackage(final String packaze) {
		}

		public void visitRequire(final String module, final int access, final String version) {
		}

		public void visitExport(final String packaze, final int access, final String... modules) {
		}

		public void visitOpen(final String packaze, final int access, final String... modules) {
		}

		public void visitUse(final String service) {
		}

		public void visitProvide(final String service, final String... providers) {
		}

		public void visitEnd() {
		}
	}

	public class AnnotationPrinter extends AnnotationVisitor {
		public AnnotationPrinter() {
			super(ASM);
		}

		public void visit(final String name, final Object value) {
			addObject(value);
		}

		public void visitEnum(final String name, final String descriptor, final String value) {
			addClassName(descriptor);
		}

		public AnnotationVisitor visitAnnotation(final String name, final String descriptor) {
			return new AnnotationPrinter();
		}

		public AnnotationVisitor visitArray(final String name) {
			return new AnnotationPrinter();
		}

		public void visitEnd() {
		}
	}

	public class MethodPrinter extends MethodVisitor {
		public MethodPrinter() {
			super(ASM);
		}

		public void visitParameter(String name, int access) {
		}

		public AnnotationVisitor visitAnnotationDefault() {
			return new AnnotationPrinter();
		}

		public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
			addType(descriptor);
			return new AnnotationPrinter();
		}

		public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
			addType(descriptor);
			return new AnnotationPrinter();
		}

		public void visitAnnotableParameterCount(final int parameterCount, final boolean visible) {
		}

		public AnnotationVisitor visitParameterAnnotation(final int parameter, final String descriptor, final boolean visible) {
			addType(descriptor);
			return new AnnotationPrinter();
		}

		public void visitAttribute(final Attribute attribute) {
			addClassName(attribute.type);
		}

		public void visitCode() {
		}

		public void visitFrame(final int type, final int numLocal, final Object[] local, final int numStack, final Object[] stack) {
		}

		public void visitInsn(final int opcode) {
		}

		public void visitIntInsn(final int opcode, final int operand) {
		}

		public void visitVarInsn(final int opcode, final int var) {
		}

		public void visitTypeInsn(int opcode, String type) {
			addClassName(type);
		}

		public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
			addClassName(owner);
			addType(descriptor);
		}

		public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor) {
			addClassName(owner);
			addMethod(descriptor);
		}

		public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
			addClassName(owner);
			addMethod(descriptor);
		}

		public void visitInvokeDynamicInsn(final String name, final String descriptor,
				final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
			addType(descriptor);
			addType(bootstrapMethodHandle.getDesc());
			if (bootstrapMethodArguments != null)
				for (Object i : bootstrapMethodArguments) {
					addObject(i);
			}
		}

		public void visitJumpInsn(final int opcode, final Label label) {
		}

		public void visitLabel(final Label label) {
		}

		public void visitLdcInsn(final Object value) {
			addObject(value);
		}

		public void visitIincInsn(final int var, final int increment) {
		}

		public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
		}

		public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
		}

		public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
			addType(descriptor);
		}

		public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
			addType(descriptor);
			return new AnnotationPrinter();
		}

		public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
			addClassName(type);
		}

		public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
			addType(descriptor);
			return new AnnotationPrinter();
		}

		public void visitLocalVariable(final String name, final String descriptor, final String signature,
				final Label start, final Label end, final int index) {
			addType(descriptor);
			addType(signature);
		}

		public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath,
				final Label[] start, final Label[] end, final int[] index, final String descriptor,
				final boolean visible) {
			addType(descriptor);
			return new AnnotationPrinter();
		}

		public void visitLineNumber(final int line, final Label start) {
		}

		public void visitMaxs(final int maxStack, final int maxLocals) {
		}

		public void visitEnd() {
		}
	}

	public class FieldPrinter extends FieldVisitor {
		public FieldPrinter() {
			super(ASM);
		}

		public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
			addType(descriptor);
			return new AnnotationPrinter();
		}

		public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
			addType(descriptor);
			return new AnnotationPrinter();
		}

		public void visitAttribute(final Attribute attribute) {
			addClassName(attribute.type);
		}
	}

	public class ClassPrinter extends ClassVisitor {
		public ClassPrinter() {
			super(ASM);
		}

		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			addClassName(name);
			addClassName(superName);
			addClass(signature);
			if (interfaces != null)
				for (String i : interfaces)
					addClassName(i);
		}

		public void visitSource(String source, String debug) {
		}

		public ModuleVisitor visitModule(final String name, final int access, final String version) {
			return new ModulePrinter(ASM);
		}

		public void visitNestHost(final String nestHost) {
		}

		public void visitOuterClass(String owner, String name, String desc) {
			addClassName(owner);
			addClass(desc);
		}

		public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
			addClass(descriptor);
			return new AnnotationPrinter();
		}

		public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath,
				final String descriptor, final boolean visible) {
			addClass(descriptor);
			return new AnnotationPrinter();
		}

		public void visitAttribute(Attribute attr) {
			addClassName(attr.type);
		}

		public void visitNestMember(final String nestMember) {
		}

		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			addClassName(name);
		}

		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			addType(desc);
			addType(signature);
			return new FieldPrinter();
		}

		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			addMethod(desc);
			addMethod(signature);
			if (exceptions != null)
				for (String e : exceptions) {
					addClassName(e);
				}
			return new MethodPrinter();
		}

		public void visitEnd() {
		}
	}

	List<String> myDummy() { return null; }

	void doIt2() throws Exception {
		File dir = new File("target/classes/");
		ClassPrinter cp = new ClassPrinter();
		File f = new File(dir, "com/slavi/lang/TestAsmByteCode.class");
		try (FileInputStream is = new FileInputStream(f)) {
			ClassReader cr = new ClassReader(is);
			cr.accept(cp, 0);
		}

		System.out.println("--------- Referenced classes ----------");
		ArrayList<String> classes = new ArrayList(this.classes);
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
	};

	String[] classTests = {
		"<E:Ljava/lang/Object;T:Ljava/lang/Object;>Ljava/util/List;",
		"<E:Ljava/lang/Object;>Ljava/util/List<TE;>;",
		"<InputType:Ljava/lang/Object;OutputType:Ljava/lang/Object;>Lcom/slavi/math/transform/BaseTransformer<TInputType;TOutputType;>;"
	};

	void doIt() throws Exception {
/*		for (String t: typeTests) {
			AsmSignatureParserImpl p = new AsmSignatureParserImpl(t);
			p.parseTypeSignature();
			System.out.println(classes);
			classes.clear();
		}

		for (String t: methodTests) {
			AsmSignatureParserImpl p = new AsmSignatureParserImpl(t);
			p.parseMethodSignature();
			System.out.println(classes);
			classes.clear();
		}
*/
		for (String t: classTests) {
			System.out.println(">>> " + t);
			AsmSignatureParserImpl p = new AsmSignatureParserImpl(t);
			p.parseClassSignature();
			System.out.println(classes);
			classes.clear();
		}
	}

	void doIt3() throws Exception {
		Path f;
		if (true) {
			String dn = "jar:file:/home/spetrov/.m2/repository/com/slavi/util.math/1.0.0-SNAPSHOT/util.math-1.0.0-SNAPSHOT.jar";
			URI uri = URI.create(dn);
			FileSystem fs = FileSystems.newFileSystem(uri, Collections.EMPTY_MAP);
			f = fs.getPath("/");
		} else {
			String dn = "/home/spetrov/.S/git/improc/work/target/classes";
			f = Paths.get(dn);
		}

		PathMatcher matcher = f.getFileSystem().getPathMatcher("glob:**.class");
		Files.walkFileTree(f, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				if (matcher.matches(file)) {
//					System.out.println(file);
					try (InputStream is = Files.newInputStream(file)) {
						ClassReader cr = new ClassReader(is);
						cr.accept(new ClassPrinter(), 0);
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static void main(String[] args) throws Exception {
		new TestAsmByteCode().doIt();
		System.out.println("Done.");
	}
}
