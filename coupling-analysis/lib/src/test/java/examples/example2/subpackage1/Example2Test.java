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

package examples.example2.subpackage1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.cau.config.Config;
import de.cau.monitor.metrics.CouplingTag;
import examples.TestSetup;

/**
 * Testclass for checking the right coupling count in case of two classes with
 * same name in different packages. Also check for simple Package and Import
 * Coupling.
 * 
 */
@Config(sourceCodeInputRoot = TestSetup.SOURCECODE_ROOT_TO_TEST_EXAMPLES, byteCodeInputRoot = TestSetup.BYTECODE_ROOT_TO_TEST_EXAMPLES
		+ "/example2", sourceCodeMetrics = {
				CouplingTag.METHOD_TO_METHOD, CouplingTag.PACKAGE, CouplingTag.IMPORT }, byteCodeMetrics = {
						CouplingTag.METHOD_TO_METHOD, CouplingTag.PACKAGE,
						CouplingTag.IMPORT }, isConsolePrinting = false)
class Example2Test extends TestSetup {

	@BeforeAll
	static void init() {
		init(Example2Test.class, "examples.example2.subpackage1", "examples.example2.subpackage2");
	}

	@BeforeAll
	static void nullCheck() {
		assertNotEquals(0, resultSourcecode.size());
		assertNotEquals(0, resultBytecode.size());
		assertNotNull(resultSourcecode.get(CouplingTag.METHOD_TO_METHOD));
		assertNotNull(resultSourcecode.get(CouplingTag.PACKAGE));
		assertNotNull(resultSourcecode.get(CouplingTag.IMPORT));
		assertNotNull(resultBytecode.get(CouplingTag.METHOD_TO_METHOD));
		assertNotNull(resultBytecode.get(CouplingTag.PACKAGE));
		assertNotNull(resultBytecode.get(CouplingTag.IMPORT));

	}

	/**
	 * -- M2M-Sourcecode -- Checks whether the correct class from different packages
	 * is counted despite the same name. Should be one each.
	 */
	@Test
	void countRightClassDespiteSameNameM2MSourcecode() {

		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example2.subpackage1.ClassA");

		assertEquals(1, innerMap.get("examples.example2.subpackage1.ClassB"),
				"M2M Coupling with same Classname in same Package was not count in Sourcecode!");
		assertEquals(1, innerMap.get("examples.example2.subpackage2.ClassB"),
				"M2M Coupling with same Classname in different Package was not count in Sourcecode!");
	}

	/**
	 * -- M2M-Bytecode -- Checks whether the correct class from different packages
	 * is counted despite the same name. Should be one each.
	 */
	@Test
	void countRightClassDespiteSameNameM2MBytecode() {

		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example2.subpackage1.ClassA");

		assertEquals(1, innerMap.get("examples.example2.subpackage1.ClassB"),
				"M2M Coupling with same Classname in same Package was not count in Bytecode!");
		assertEquals(1, innerMap.get("examples.example2.subpackage2.ClassB"),
				"M2M Coupling with same Classname in different Package was not count in Bytecode!");
	}

	/**
	 * -- Package-Sourcecode -- Checks if simple Package Coupling is counted.
	 */
	@Test
	void countRightSimplePackageCouplingSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.PACKAGE,
				"examples.example2.subpackage1");

		assertEquals(2, innerMap.get("examples.example2.subpackage2"),
				"Simple Package-Coupling was not count in Sourcode!");
	}

	/**
	 * -- Package-Bytecode -- Checks if simple Package Coupling is counted.
	 */
	@Test
	void countRightSimplePackageCouplingBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.PACKAGE,
				"examples.example2.subpackage1");

		assertEquals(2, innerMap.get("examples.example2.subpackage2"),
				"Simple Package-Coupling was not count in Bytecode!");
	}

	/**
	 * -- Package - Sourcecode vs. Bytecode -- Checks if simple Package-Coupling is
	 * counted. Should be 2.
	 */
	@Test
	void packageCouplingShouldBeCount() {

		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.PACKAGE,
				"examples.example2.subpackage1");
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.PACKAGE,
				"examples.example2.subpackage1");

		assertTrue(innerMapS.equals(innerMapB), "Source- and Bytecodeanalysis have not count same Package Coupling!");
	}

	/**
	 * -- Import-Sourcecode -- Checks if the import is count as Import Coupling.
	 * Chould be one.
	 */
	@Test
	void countSimpleImportCouplingSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.IMPORT,
				"examples.example2.subpackage1.ClassA");

		assertEquals(1, innerMap.get("examples.example2.subpackage2.ClassC"),
				"Import Coupling was not count in Sourcode!");
	}

	/**
	 * -- Import-Bytecode -- Checks if the import is count as Import Coupling.
	 * Should be one.
	 */
	@Test
	void countSimpleImportCouplingBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.IMPORT,
				"examples.example2.subpackage1.ClassA");

		assertEquals(1, innerMap.get("examples.example2.subpackage2.ClassC"),
				"Import Coupling was not Count in Bytecode!");
	}

	/**
	 * -- Sourcecode vs. Bytecode -- Checks if Bytecode counts more Imports than
	 * Sourcecode. Should be true.
	 */
	@Test
	void bytecodeShouldCountMoreImportCouplingThanSourcecode() {

		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.IMPORT,
				"examples.example2.subpackage1.ClassA");
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.IMPORT,
				"examples.example2.subpackage1.ClassA");

		// Bytecode counts more dependencies (hidden 'Imports') than Sourcecode as
		// Import Coupling
		assertFalse(innerMapS.equals(innerMapB), "Sourcecode counts " + innerMapS.size()
				+ " ImportCouplings and Bytecode counts " + innerMapB.size() + " ImportCouplings.");
	}

}
