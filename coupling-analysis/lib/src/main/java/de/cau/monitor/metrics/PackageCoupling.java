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
import de.cau.config.Configuration;
import de.cau.tools.Tag;

/**
 * Can calculate coupling of packages for source and bytecode.(similar to
 * method-to-method coupling)
 * 
 */
public class PackageCoupling extends ACoupling {

	public PackageCoupling() {
		super(CouplingTag.PACKAGE);
		setCouplingHook();
	}

	@Override
	public void calculateCoupling(final ClassOrInterfaceDeclaration coi, final String classFrom) {

		if (isNested(classFrom))
			return;

		coi.findAll(MethodCallExpr.class).forEach(mce -> {

			final String packageNameFrom = getPackageNameFromString(classFrom);

			try {

				final ResolvedMethodDeclaration rmd = mce.resolve();

				final String methodName = mce.getNameAsString();
				String name = rmd.getQualifiedName();
				// cut method name
				name = name.substring(0, name.length() - methodName.length() - 1);

				if (MethodToMethodCoupling.isIgnored(name, methodName)) {
					logger.logIgnored(name + "..." + methodName);

				} else {

					// ...Continued if type could be resolved...
					final String packageNameTo = rmd.getPackageName();

					if (Configuration.isInWhiteList(packageNameTo)) {
						super.addSourceCodeCoupling(packageNameFrom, packageNameTo, Tag.RESOLVED);
					} else {
						super.addSourceCodeCoupling(packageNameFrom, Tag.NOT_PROJECT_PART.toString(),
								Tag.NOT_PROJECT_PART);
					}
				}
			} catch (final UnsolvedSymbolException e) {
				logger.logByTag(getErrorMsg(classFrom, mce, e), Tag.UNSUPPORTED, sourceCodeInfo, this);
			} catch (final UnsupportedOperationException e) {
				logger.logByTag(getErrorMsg(classFrom, mce, e), Tag.UNSUPPORTED, sourceCodeInfo, this);
			} catch (final RuntimeException e) {
				// Sometimes the symbol solver can not resolve the type and raises a runtime
				// exception
				logger.logByTag(getErrorMsg(classFrom, mce, e), Tag.UNSUPPORTED, sourceCodeInfo, this);
			} catch (final StackOverflowError e) {
				logger.logByTag(getErrorMsg(classFrom, mce, e), Tag.UNSUPPORTED, sourceCodeInfo, this);
			}
		});
	}

	// // // // // // // // // // // BYTECODE // // // // // // // // // // //

	private final MethodVisitorRegisterStrategy strategy = (classFrom, classTo, methodTo) -> {

		final String packageNameFrom = getPackageNameFromString(classFrom);
		final String packageNameTo = classTo.equals(Tag.LAMBDA.toString()) ? classTo
				: getPackageNameFromString(classTo);

		// Is package of the project we want to count.
		if (Configuration.isInWhiteList(packageNameTo)) {
			super.addByteCodeCoupling(packageNameFrom, packageNameTo, Tag.RESOLVED);
			return;
		}
		super.addByteCodeCoupling(packageNameFrom, packageNameTo, Tag.NOT_PROJECT_PART);
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
