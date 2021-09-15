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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import de.cau.monitor.CouplingMonitor;
import de.cau.tools.Tag;

/**
 * Can calculate coupling of inheritance for source and bytecode.
 * 
 */
public class InheritanceCoupling extends ACoupling {

	Map<String, String> classNameToFullClassName = new HashMap<>();

	public InheritanceCoupling() {
		super(CouplingTag.INHERITANCE);
	}

	// // // // // // // // // // // SOURCECODE // // // // // // // // // // //

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void calculateCoupling(final ClassOrInterfaceDeclaration coi, final String classFrom) {
		// Check for super classes.
		final NodeList<ClassOrInterfaceType> superClass = coi.getExtendedTypes();
		// All classes we want to check.
		final Set<String> fullNames = CouplingMonitor.getInstance().getRegisteredClasses();
		final CompilationUnit cu = coi.findCompilationUnit().get();

		// If class has super class.
		if (!superClass.isEmpty()) {

			// Find fullName of given class.
			final String classTo = findMatchingFullClassName(cu,
					superClass.getFirst().get().getNameAsString(), fullNames);

			if (classTo.equals(Tag.NOT_PROJECT_PART.toString())) {
				super.addSourceCodeCoupling(classFrom, classTo, Tag.NOT_PROJECT_PART);
			} else {
				super.addSourceCodeCoupling(classFrom, classTo, Tag.RESOLVED);
			}
		}

		// Check for interfaces.
		final NodeList<ClassOrInterfaceType> interfaces = coi.getImplementedTypes();
		if (!interfaces.isEmpty()) {

			for (final ClassOrInterfaceType iface : interfaces) {

				// Find fullName of given interface.
				final String classTo = findMatchingFullClassName(cu,
						iface.getNameAsString(), fullNames);

				if (!classTo.equals(Tag.NOT_PROJECT_PART.toString())) {
					super.addSourceCodeCoupling(classFrom, classTo, Tag.RESOLVED);
				} else {
					super.addSourceCodeCoupling(classFrom, classTo, Tag.NOT_PROJECT_PART);
				}
			}
		}
	}

	/**
	 * Finds the fully-qualified name (i.e. 'packageName.className') of the given
	 * className {@code className}
	 * 
	 * @param cu        the associated CompliationUnit
	 * @param className for which the full name is to be found.
	 * @param fullNames list of all fully-qualified names in which to look for.
	 * @return fully-qualified class name.
	 */
	private String findMatchingFullClassName(final CompilationUnit cu, final String className,
			final Set<String> fullNames) {

		final NodeList<ImportDeclaration> imports = cu.getImports();

		// No class (that we want to count) with this name contained in the whole
		// Project .
		if (fullNames.isEmpty())
			return Tag.NOT_PROJECT_PART.toString();

		// Search for class name in imports.
		if (!imports.isEmpty()) {
			for (final ImportDeclaration importDeclaration : imports) {

				final String impStr = importDeclaration.getNameAsString();

				// Found class in imports.
				if (impStr.contains(className) && fullNames.contains(impStr))
					return impStr;
			}
		}

		// ...Not 'active' imported...

		final String fullClassName = buildFullClassName(cu.getPackageDeclaration(), className);

		// Check if class is in same package.
		if (fullNames.contains(fullClassName))
			return fullClassName;
		return Tag.NOT_PROJECT_PART.toString();
	}

	/**
	 * Build the fully-qualified name.
	 * 
	 * @param opd       an optional package declaration.
	 * @param className
	 * @return fully-qualified name
	 */
	private String buildFullClassName(final Optional<PackageDeclaration> opd, final String className) {
		return opd.isPresent() ? opd.get().getNameAsString() + "." + className : className;
	}

	// // // // // // // // // // // BYTECODE // // // // // // // // // // //

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void calculateCoupling(final JavaClass javaClass) {

		final String classFrom = javaClass.getClassName();
		try {

			final JavaClass superClass = javaClass.getSuperClass();
			final String superClassName = superClass.getClassName();

			// We are interested in simple inheritance only, if it is Object inheritance we
			// do not care either
			if (!superClassName.equals("java.lang.Object")) {
				super.addByteCodeCoupling(classFrom, superClassName, Tag.RESOLVED);
			}

			final String[] ifaces = javaClass.getInterfaceNames();

			for (final String classTo : ifaces) {
				super.addByteCodeCoupling(classFrom, classTo, Tag.RESOLVED);
			}

		} catch (final ClassNotFoundException e) {
			logger.logByTag(getErrorMsg(classFrom, "unknown", e), Tag.UNSUPPORTED,
					byteCodeInfo, this);
		}
	}
}
