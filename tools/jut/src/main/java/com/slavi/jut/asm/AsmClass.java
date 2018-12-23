package com.slavi.jut.asm;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashSet;

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

import com.slavi.jut.asm.parser.AsmParser;
import com.slavi.jut.cfg.Location;

public class AsmClass {
	static final int ASM = Opcodes.ASM7;

	public Location location;
	public String className;
	public HashSet<String> usedClasses = new HashSet();

	public void addClassName(String className) {
		if (StringUtils.isEmpty(className)) return;
		usedClasses.add(className);
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
			Type t = (Type) i;
			switch (t.getSort()) {
			case Type.METHOD:
				addMethod(t.getDescriptor());
				break;
			case Type.OBJECT:
				addClass(t.getDescriptor());
				break;
			default:
				addType(t.getDescriptor());
				break;
			}
		} else if (i instanceof Handle) {
			addMethod(((Handle) i).getDesc());
		} else if (i instanceof ConstantDynamic) {
			addType(((ConstantDynamic) i).getDescriptor());
		// } else if (i instanceof ) { // More MethodVisitor.visitLdcInsn
		}
	}

	protected class AsmSignatureParserImpl extends AsmParser {

		public AsmSignatureParserImpl(String str) {
			super(new StringReader(str));
		}

		@Override
		public void addClass(String className) {
			addClassName(className);
		}
	}

	class ModulePrinter extends ModuleVisitor {
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

	class AnnotationPrinter extends AnnotationVisitor {
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

	class MethodPrinter extends MethodVisitor {
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
			addMethod(descriptor);
			addMethod(bootstrapMethodHandle.getDesc());
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

	class FieldPrinter extends FieldVisitor {
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
			className = name;
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
			addMethod(desc);
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

	public static AsmClass loadOneClass(InputStream classBytes) throws IOException {
		AsmClass r = new AsmClass();
		ClassReader cr = new ClassReader(classBytes);
		cr.accept(r.new ClassPrinter(), 0);
		return r;
	}
}
