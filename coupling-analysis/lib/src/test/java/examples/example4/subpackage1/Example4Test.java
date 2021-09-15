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

package examples.example4.subpackage1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.cau.config.Config;
import de.cau.monitor.metrics.CouplingTag;
import examples.TestSetup;

@Config(sourceCodeInputRoot = TestSetup.SOURCECODE_ROOT_TO_TEST_EXAMPLES, byteCodeInputRoot = TestSetup.BYTECODE_ROOT_TO_TEST_EXAMPLES
		+ "/example4", sourceCodeMetrics = { CouplingTag.IMPORT, CouplingTag.PACKAGE }, byteCodeMetrics = {
				CouplingTag.IMPORT, CouplingTag.PACKAGE }, isConsolePrinting = false)
class Example4Test extends TestSetup {

	@BeforeAll
	static void init() {
		init(Example4Test.class, "examples.example4.subpackage1", "examples.example4.subpackage2");
	}

	@BeforeAll
	static void nullCheck() {
		assertNotEquals(0, resultSourcecode.size());
		assertNotEquals(0, resultBytecode.size());
		assertNotNull(resultSourcecode.get(CouplingTag.IMPORT));
		assertNotNull(resultSourcecode.get(CouplingTag.PACKAGE));
		assertNotNull(resultBytecode.get(CouplingTag.IMPORT));
		assertNotNull(resultBytecode.get(CouplingTag.PACKAGE));
	}

	/**
	 * -- Sourcecode vs. Bytecode -- ClassA imports OuterClass and calls from an
	 * InnerClass-Object a Method from InnerClass. Sourcecode counts one Import,
	 * Bytecode counts two Imports (Dependencies). Checks if Bytecode counts more
	 * Imports than Sourcecode.
	 */
	@Test
	void importCouplingWithInnerClassShouldBeCountDifferentInSourceAndBytecode() {

		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.IMPORT,
				"examples.example4.subpackage1.ClassA");
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.IMPORT,
				"examples.example4.subpackage1.ClassA");

		// Bytecode counts "Import-Coupling" twice in case of InnerClass dependencies.
		assertFalse(innerMapS.get("examples.example4.subpackage2.OuterClass")
				.equals(innerMapB.get("examples.example4.subpackage2.OuterClass")));
	}

	/**
	 * -- Package-Sourcecode -- Checks if Package-Coupling is counted also for calls
	 * to InnerClasses from different Packages.
	 */
	@Test
	void packageCouplingWithInnerClassesShouldBeCountedSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.PACKAGE,
				"examples.example4.subpackage1");

		assertEquals(2, innerMap.get("examples.example4.subpackage2"),
				"Wrong Package-Cpoupling was count for Outer and InnerClass in Sourcecode!");
	}

	/**
	 * -- Package-Bytecode -- Checks if Package-Coupling is counted also for calls
	 * to InnerClasses from different Packages.
	 */
	@Test
	void packageCouplingWithInnerClassesShouldBeCountedBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.PACKAGE,
				"examples.example4.subpackage1");

		assertEquals(2, innerMap.get("examples.example4.subpackage2"),
				"Wrong Package-Cpoupling was count for Outer and InnerClass in Bytecode!");
	}

}
