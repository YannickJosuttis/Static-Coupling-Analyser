/*
 * Copyright [2021] [Hannah S. Fischer und Yannick Josuttis]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cau.bytecode.visitor;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.INVOKEDYNAMIC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;

import de.cau.monitor.metrics.MethodToMethodCoupling;
import de.cau.monitor.metrics.MethodVisitorRegisterStrategy;
import de.cau.tools.Tag;

/**
 * Visitor class that extends {@link EmptyVisitor} for visiting all parts of
 * bytecode that are useful for method coupling.
 *
 */
public class MethodVisitor extends EmptyVisitor {

	private final ConstantPoolGen constantPoolGen;
	private final MethodGen methodGen;
	private final MethodVisitorRegisterStrategy strategy;

	public MethodVisitor(final Method method, final String className, final ConstantPoolGen constantPoolGen,
			final MethodVisitorRegisterStrategy strategy) {

		this.constantPoolGen = constantPoolGen;
		this.methodGen = new MethodGen(method, className, constantPoolGen);
		this.strategy = strategy;
	}

	/**
	 * Visit all instructions of the method.
	 */
	public void visitMethod() {
		if (!methodGen.isAbstract() && !methodGen.isNative()) {
			final InstructionList instructions = methodGen.getInstructionList();
			InstructionHandle handle = instructions.getStart();
			while (handle != null) {
				final Instruction i = handle.getInstruction();
				i.accept(this);
				handle = handle.getNext();
			}
		}
	}

	/**
	 * Register the coupling.
	 * 
	 * @param classTo      to which class there is a coupling.
	 * @param methodNameTo to which method there is a coupling.
	 */
	private void registerCoupling(final String classTo, final String methodNameTo) {
		this.registerCoupling(methodGen.getClassName(), classTo, methodNameTo);
	}

	/**
	 * Register the coupling.
	 * 
	 * @param classFrom from which the coupling is counted.
	 * @param classTo   to which class there is a coupling.
	 * @param methodTo  to which method there is a coupling.
	 */
	private void registerCoupling(final String classFrom, final String classTo, final String methodTo) {
		strategy.registerCoupling(classFrom, classTo, methodTo);
	}

	/**
	 * Catches the invoke instructions by generalization.
	 * 
	 * @param invokeInstruction like static, virtual, dynamic, special...
	 */
	public void visitINVOKEMethodCall(final InvokeInstruction invokeInstruction) {

		String classTo = invokeInstruction.getClassName(constantPoolGen);
		final String methodName = invokeInstruction.getMethodName(constantPoolGen);

		if (!classTo.contains(".") && Character.isLowerCase(classTo.charAt(0))) {
			classTo = Tag.LAMBDA.toString();
		}

		// Make sure that we do not count internal parts, e.g. control structures.
		if (!MethodToMethodCoupling.isIgnored(classTo, methodName)) {
			registerCoupling(classTo, methodName);
		}
	}

	/**
	 * Catches the static calls.
	 */
	@Override
	public void visitINVOKESTATIC(final INVOKESTATIC obj) {
		visitINVOKEMethodCall(obj);
	}

	/**
	 * Catches the special calls e.g. super calls.
	 */
	@Override
	public void visitINVOKESPECIAL(final INVOKESPECIAL obj) {

		// Do not care of initialization.
		if (!obj.getMethodName(constantPoolGen).equals("<init>")) {
			visitINVOKEMethodCall(obj);
		}
	}

	/**
	 * Catches the virtual calls.
	 */
	@Override
	public void visitINVOKEVIRTUAL(final INVOKEVIRTUAL obj) {
		visitINVOKEMethodCall(obj);
	}

	/**
	 * Catches the dynamic calls.
	 */
	@Override
	public void visitINVOKEDYNAMIC(final INVOKEDYNAMIC obj) {
		visitINVOKEMethodCall(obj);
	}

	/**
	 * Catches the interface calls.
	 */
	@Override
	public void visitINVOKEINTERFACE(final INVOKEINTERFACE obj) {
		visitINVOKEMethodCall(obj);
	}
}
