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

import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.UnsolvedSymbolException;

import de.cau.monitor.CouplingMonitor;
import de.cau.tools.Tag;

/**
 * Can calculate coupling of fields for source and bytecode.
 * 
 */
public class FieldCoupling extends ACoupling {

	public FieldCoupling() {
		super(CouplingTag.FIELD);
	}

	@Override
	public void calculateCoupling(final ClassOrInterfaceDeclaration coi, final String classFrom) {

		final CouplingMonitor cm = CouplingMonitor.getInstance();
		final Set<String> classes = cm.getRegisteredClasses();

		final List<FieldDeclaration> fields = coi.getFields();

		for (final FieldDeclaration field : fields) {
			final Type type = field.findFirst(VariableDeclarator.class).get().getType().getElementType();
			if (type.isReferenceType()) {

				try {

					String classTo = type.asReferenceType().resolve().describe();

					// generic to simple name
					final int idx = classTo.indexOf('<');
					classTo = classTo.substring(0, idx < 0 ? classTo.length() : idx);
					classTo = toNestedClassFormat(classTo);
					classTo = mapNestedClassToOuter(classTo);

					if (classes.contains(classTo)) {
						super.addSourceCodeCoupling(classFrom, classTo, Tag.RESOLVED);
					} else {
						super.addSourceCodeCoupling(classFrom, classTo, Tag.NOT_PROJECT_PART);
					}
				} catch (final UnsolvedSymbolException e) {
					logger.logByTag(getErrorMsg(classFrom, type, e), Tag.UNSUPPORTED, sourceCodeInfo, this);
				} catch (final UnsupportedOperationException e) {
					logger.logByTag(getErrorMsg(classFrom, type, e), Tag.UNSUPPORTED, sourceCodeInfo, this);
				} catch (final RuntimeException e) {
					logger.logByTag(getErrorMsg(classFrom, type, e), Tag.UNSUPPORTED, sourceCodeInfo, this);
				}
			} else {
				super.addSourceCodeCoupling(classFrom, Tag.NOT_PROJECT_PART.toString(), Tag.NOT_PROJECT_PART);
			}
		}
	}

	// // // // // // // // // // // BYTECODE // // // // // // // // // // //

	@Override
	public void calculateCoupling(final JavaClass javaClass) {

		final CouplingMonitor cm = CouplingMonitor.getInstance();
		final Set<String> classes = cm.getRegisteredClasses();

		for (final Field field : javaClass.getFields()) {

			String classTo = field.getSignature();

			// String manipulations
			final int index = classTo.indexOf("L");

			classTo = index < 0 ? classTo : classTo.substring(index + 1, classTo.length() - 1);
			classTo = toPackageNotation(classTo);

			if (classes.contains(classTo)) {
				super.addByteCodeCoupling(javaClass.getClassName(), classTo, Tag.RESOLVED);
			} else {
				super.addByteCodeCoupling(javaClass.getClassName(), Tag.NOT_PROJECT_PART.toString(),
						Tag.NOT_PROJECT_PART);
			}
		}
	}
}
