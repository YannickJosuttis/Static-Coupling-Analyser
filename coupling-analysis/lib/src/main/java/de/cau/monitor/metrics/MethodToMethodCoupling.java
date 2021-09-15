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

package de.cau.monitor.metrics;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

import de.cau.bytecode.visitor.MethodVisitor;
import de.cau.monitor.CouplingMonitor;
import de.cau.tools.Tag;

/**
 * Can calculate coupling of methods for source and bytecode.
 * 
 */
public class MethodToMethodCoupling extends ACoupling {

	public MethodToMethodCoupling() {
		super(CouplingTag.METHOD_TO_METHOD);
	}

	@Override
	public void calculateCoupling(final ClassOrInterfaceDeclaration coi, final String classFrom) {

		if (isNested(classFrom))
			return;

		for (final MethodCallExpr mce : coi.findAll(MethodCallExpr.class)) {

			final String methodName = mce.getNameAsString();
			try {

				// At this point the SymobolSolver is called.
				final ResolvedMethodDeclaration rmd = mce.resolve();
				String name = rmd.getQualifiedName();
				// ...successfully resolved...

				// cut method name
				name = name.substring(0, name.length() - methodName.length() - 1);
				logger.log(methodName);
				if (isIgnored(name, methodName)) {
					logger.logIgnored(name + "..." + methodName);

				} else {
					name = toNestedClassFormat(name);
					name = mapNestedClassToOuter(name);
				}

				if (CouplingMonitor.getInstance().getRegisteredClasses().contains(name)) {
					super.addSourceCodeCoupling(classFrom, name, Tag.RESOLVED);
				} else {
					super.addSourceCodeCoupling(classFrom, name, Tag.NOT_PROJECT_PART);
				}
			} catch (final UnsolvedSymbolException e) {
				logger.logByTag(getErrorMsg(classFrom, mce, e), Tag.UNSUPPORTED,
						sourceCodeInfo, this);
			} catch (final UnsupportedOperationException e) {
				logger.logByTag(getErrorMsg(classFrom, mce, e), Tag.UNSUPPORTED,
						sourceCodeInfo, this);
			} catch (final java.lang.RuntimeException e) {
				logger.logByTag(getErrorMsg(classFrom, mce, e), Tag.UNSUPPORTED, sourceCodeInfo,
						this);
			} catch (final StackOverflowError e) {
				logger.logByTag(getErrorMsg(classFrom, mce, e), Tag.UNSUPPORTED,
						sourceCodeInfo,
						this);
			}
		}
	}

	/**
	 * ignore StringBuilder calls; ignore Iterator calls (like foreach loops);
	 * ignore internal casts (like 1L); ignore ordinal calls; ignore
	 * SupressionWarings; ignore switch table calls
	 * 
	 * @param name
	 * @param methodName
	 * @return {@code true} if name or/and method name should be ignored, otherwise
	 *         {@code false}
	 */
	public static boolean isIgnored(final String name, final String methodName) {
		return name.equals("java.lang.StringBuilder")
				|| name.equals("java.util.Iterator")
				|| methodName.equals("valueOf") && name.startsWith("java.lang")
				|| methodName.equals("ordinal") && !CouplingMonitor.getInstance().getRegisteredClasses().contains(name)
				|| methodName.equals("addSuppressed") && name.equals("java.lang.Throwable")
				|| methodName.startsWith("$SWITCH_TABLE");
	}

	// // // // // // // // // // // BYTECODE // // // // // // // // // // //

	private final MethodVisitorRegisterStrategy strategy = (classFrom, classTo, methodTo) -> {

		logger.log(methodTo);
		// Is class of the project we want to count.
		if (CouplingMonitor.getInstance().getRegisteredClasses().contains(classTo)) {
			addByteCodeCoupling(classFrom, classTo, Tag.RESOLVED);
			return;
		}
		addByteCodeCoupling(classFrom, classTo, Tag.NOT_PROJECT_PART);
	};

	@Override
	public void calculateCoupling(final JavaClass javaClass) {

		final ConstantPoolGen constantPoolGen = new ConstantPoolGen(javaClass.getConstantPool());
		final Method[] methodDeclarations = javaClass.getMethods();

		for (final Method md : methodDeclarations) {

			final MethodVisitor mv = new MethodVisitor(md, javaClass.getClassName(), constantPoolGen, strategy);
			mv.visitMethod();
		}
	}
}
