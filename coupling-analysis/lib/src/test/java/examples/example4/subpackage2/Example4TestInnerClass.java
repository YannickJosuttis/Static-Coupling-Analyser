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

package examples.example4.subpackage2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.cau.config.Config;
import de.cau.monitor.metrics.CouplingTag;
import examples.TestSetup;

@Config(sourceCodeInputRoot = TestSetup.SOURCECODE_ROOT_TO_TEST_EXAMPLES, byteCodeInputRoot = TestSetup.BYTECODE_ROOT_TO_TEST_EXAMPLES
		+ "/example4", sourceCodeMetrics = { CouplingTag.IMPORT, CouplingTag.METHOD_TO_METHOD,
				CouplingTag.FIELD }, byteCodeMetrics = { CouplingTag.IMPORT, CouplingTag.METHOD_TO_METHOD,
						CouplingTag.FIELD }, isConsolePrinting = false)
class Example4TestInnerClass extends TestSetup {

	@BeforeAll
	static void init() {
		init(Example4TestInnerClass.class, "examples.example4.subpackage1", "examples.example4.subpackage2",
				"examples.example4.subpackage2");
	}

	@BeforeAll
	static void nullCheck() {
		assertNotEquals(0, resultSourcecode.size());
		assertNotEquals(0, resultBytecode.size());
		assertNotNull(resultSourcecode.get(CouplingTag.IMPORT));
		assertNotNull(resultBytecode.get(CouplingTag.IMPORT));
		assertNotNull(resultSourcecode.get(CouplingTag.METHOD_TO_METHOD));
		assertNotNull(resultBytecode.get(CouplingTag.METHOD_TO_METHOD));
		assertNotNull(resultSourcecode.get(CouplingTag.FIELD));
		assertNotNull(resultBytecode.get(CouplingTag.FIELD));
	}

	/**
	 * -- Import - Sourcecode vs. Bytecode -- Checks if Bytecode counts an
	 * Import-Coupling of two, if the imported Class is called from OuterClass and
	 * Innerclass, whereas Sourcecode counts only one Import-Coupling.
	 * 
	 */
	@Test
	void bytecodeCountMoreImportCouplingThanSourcecode() {

		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.IMPORT,
				"examples.example4.subpackage2.OuterClass");
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.IMPORT,
				"examples.example4.subpackage2.OuterClass");

		assertEquals(1, innerMapS.get("examples.example4.subpackage1.ClassA"));
		// Import-Coupling is counted twice, but it is only once imported (Bytecode).
		assertEquals(2, innerMapB.get("examples.example4.subpackage1.ClassA"));
	}

	/**
	 * -- M2M-Sourcecode -- Checks if M2M Coupling between Outer and InnerClass is
	 * not counted.
	 */
	@Test
	void m2mShouldNotCountCouplingBetweenOuterAndInnerClassSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example4.subpackage2.OuterClass");

		assertNull(innerMap, "M2M-Sourcecode has count Coupling between Outer and InnerClass!");
	}

	/**
	 * -- M2M-Bytecode -- Checks if M2M Coupling between Outer and InnerClass is not
	 * counted.
	 */
	@Test
	void m2mShouldNotCountCouplingBetweenOuterAndInnerClassBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example4.subpackage2.OuterClass");

		assertNull(innerMap, "M2M-Bytecode has count Coupling between Outer and InnerClass!");
	}

	/**
	 * -- Field-Sourcecode -- Checks if Field Coupling is counted belonging to
	 * Outerclass, if its InnerClass has a Field.
	 */
	@Test
	void fieldInInnerClassSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.FIELD,
				"examples.example4.subpackage2.OuterClass");

		assertEquals(1, innerMap.get("examples.example4.subpackage1.ClassA"),
				"Field Coupling was not count for a Field in an InnerClass In Sourcecode!");
	}

	/**
	 * -- Field-Bytecode -- Checks if Field Coupling is counted belonging to
	 * Outerclass, if its InnerClass has a Field.
	 */
	@Test
	void fieldInInnerClassBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.FIELD,
				"examples.example4.subpackage2.OuterClass");

		assertEquals(1, innerMap.get("examples.example4.subpackage1.ClassA"),
				"Field Coupling was not count for a Field in an InnerClass In Bytecode!");
	}

}
