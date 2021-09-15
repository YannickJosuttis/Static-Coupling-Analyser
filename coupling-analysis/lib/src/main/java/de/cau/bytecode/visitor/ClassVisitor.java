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

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import de.cau.config.Configuration;
import de.cau.monitor.CouplingMonitor;

/**
 * This class can visit all class files used for the bytecode analysis.
 *
 */
public class ClassVisitor {

	private final CouplingMonitor cm;
	private final List<JavaClass> allProjectClassFiles;

	public ClassVisitor(final CouplingMonitor cm) {
		this.cm = cm;
		allProjectClassFiles = new LinkedList<>();
	}

	/**
	 * Collecting all *.jar files.
	 * 
	 * @param pathToJarFile
	 */
	private void collectJarFile(final String pathToJarFile) {

		try {
			final JarFile jar = new JarFile(pathToJarFile);
			final Enumeration<JarEntry> entries = jar.entries();

			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();

				if (entry.getName().endsWith(".class")) {
					final JavaClass javaClass = new ClassParser(pathToJarFile, entry.getName()).parse();
					addClass(javaClass);
				}
			}
			jar.close();

		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Visiting all jar and class files and register them to the coupling monitor.
	 * 
	 * @param jarAndClassFiles
	 */
	public void visitAllJavaClasses(final String[] jarAndClassFiles) {

		for (final String jarOrClassFile : jarAndClassFiles) {

			if (jarOrClassFile.endsWith(".jar")) {
				this.collectJarFile(jarOrClassFile);

			} else if (jarOrClassFile.endsWith(".class")) {
				this.collectJavaClass(jarOrClassFile);
			}
		}

		cm.registerAllClasses(
				allProjectClassFiles
						.stream()
						.map(JavaClass::getClassName)
						.collect(Collectors.toSet()));

		final Set<String> packageNames = allProjectClassFiles
				.stream()
				.map(JavaClass::getPackageName)
				.collect(Collectors.toSet());

		Configuration.addAllToWhiteList(packageNames);
		cm.registerByteCodeCoupling(allProjectClassFiles);
	}

	/**
	 * Collect all {@link JavaClass} and add them to the BCEL Repository.
	 * 
	 * @param pathToClassFile
	 */
	private void collectJavaClass(final String pathToClassFile) {

		final ClassParser parser = new ClassParser(pathToClassFile);

		try {
			final JavaClass javaClass = parser.parse();
			// Caching java classes
			// this is necessary to resolve dependencies between classes later.
			Repository.addClass(javaClass);
			addClass(javaClass);
		} catch (ClassFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simply adding {@link JavaClass} with conditions.
	 * 
	 * @param javaClass
	 */
	private void addClass(final JavaClass javaClass) {
		if (!javaClass.isAnnotation() && !javaClass.isEnum()) {
			allProjectClassFiles.add(javaClass);
		}
	}
}
