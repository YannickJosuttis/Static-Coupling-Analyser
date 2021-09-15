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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import de.cau.config.Configuration;
import de.cau.tools.Tag;

/**
 * Can calculate coupling of imports for source and bytecode.
 * 
 */
public class ImportCoupling extends ACoupling {

	public ImportCoupling() {
		super(CouplingTag.IMPORT);
	}

	@Override
	public void calculateCoupling(final ClassOrInterfaceDeclaration coi, final String from) {

		if (isNested(from))
			return;

		final CompilationUnit cu = coi.findCompilationUnit().get();
		final NodeList<ImportDeclaration> imports = cu.getImports();

		for (final ImportDeclaration to : imports) {

			final String packageName = getPackageNameFromString(to.getNameAsString());

			if (Configuration.isInWhiteList(packageName)) {
				super.addSourceCodeCoupling(from, to.getNameAsString(), Tag.RESOLVED);
			} else {
				super.addSourceCodeCoupling(from, Tag.NOT_PROJECT_PART.toString(), Tag.NOT_PROJECT_PART);
			}
		}
	}

	// // // // // // // // // // // BYTECODE // // // // // // // // // // //

	@Override
	public void calculateCoupling(final JavaClass javaClass) {

		final Set<String> dependentClasses = getClassDependencies(javaClass);
		final String className = javaClass.getClassName();

		for (final String dc : dependentClasses) {
			super.addByteCodeCoupling(className, dc, Tag.RESOLVED);
		}
	}

	private final Pattern classArrayPattern = Pattern.compile("\\[+L(.*);");

	/**
	 * Finding all class dependencies without self connections.
	 * 
	 * @param javaClass to look for all dependencies
	 * @return a Set of all class dependencies
	 */
	public Set<String> getClassDependencies(final JavaClass javaClass) {

		final ConstantPool constanatPool = javaClass.getConstantPool();
		final Set<String> classes = new HashSet<>();

		for (final Constant constant : constanatPool.getConstantPool()) {

			if (constant instanceof ConstantClass) {

				// Name of dependent java Class.
				String constantName = (String) ((ConstantClass) constant).getConstantValue(constanatPool);
				constantName = toPackageNotation(constantName);

				final String classFrom = javaClass.getClassName();

				// Ignore self-dependencies
				if (!classFrom.equals(constantName)) {

					// Handle arrays
					final Matcher matcher = classArrayPattern.matcher(constantName);
					String classTo;
					if (matcher.matches()) {
						classTo = matcher.group(1);
					} else {
						classTo = constantName;
					}
					classes.add(classTo);
				}
			}
		}
		return classes;
	}
}
