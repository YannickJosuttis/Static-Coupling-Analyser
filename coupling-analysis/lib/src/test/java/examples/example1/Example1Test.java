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

package examples.example1;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * Testclass for simple Method-to-Method Coupling
 *
 */
@Config(sourceCodeInputRoot = TestSetup.SOURCECODE_ROOT_TO_TEST_EXAMPLES, byteCodeInputRoot = TestSetup.BYTECODE_ROOT_TO_TEST_EXAMPLES
		+ "/example1", sourceCodeMetrics = { CouplingTag.METHOD_TO_METHOD }, byteCodeMetrics = {
				CouplingTag.METHOD_TO_METHOD }, isConsolePrinting = false)
class Example1Test extends TestSetup {

	@BeforeAll
	static void init() {
		init(Example1Test.class, "examples.example1");
	}

	@BeforeAll
	static void nullCheck() {
		assertNotEquals(0, resultSourcecode.size());
		assertNotEquals(0, resultBytecode.size());
		assertNotNull(resultSourcecode.get(CouplingTag.METHOD_TO_METHOD));
		assertNotNull(resultBytecode.get(CouplingTag.METHOD_TO_METHOD));

	}

	/**
	 * -- M2M-Sourcecode -- Checks if a Methodcall from an Object counted. Should be
	 * one.
	 */
	@Test
	void methodcallFromObjectM2MSourcode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example1.ClassA");

		assertNotNull(innerMap);
		assertEquals(1, innerMap.get("examples.example1.ClassB"),
				"M2M-Sourcecode: Methodcall from an Object was not count!");
	}

	/**
	 * -- M2M-Sourcecode -- Checks if a static Methodcall counted. Should be one.
	 */
	@Test
	void methodcallStaticM2MSourcode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example1.ClassA");

		assertNotNull(innerMap);
		assertEquals(1, innerMap.get("examples.example1.ClassC"), "M2M-Sourcecode: Static Methodcall was not count!");
	}

	/**
	 * -- M2M-Sourcecode -- Checks if a Methodcall from Field counted. Should be
	 * one.
	 */
	@Test
	void methodcallFromFieldM2MSourcode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example1.ClassA");

		assertNotNull(innerMap);
		assertEquals(1, innerMap.get("examples.example1.ClassD"),
				"M2M-Sourcecode: Methodcall from a Field was not count!");
	}

	/**
	 * -- M2M-Bytecode -- Checks if a Methodcall from an Object counted. Should be
	 * one.
	 */
	@Test
	void methodcallFromObjectM2MBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example1.ClassA");

		assertNotNull(innerMap);
		assertEquals(1, innerMap.get("examples.example1.ClassB"),
				"M2M-Bytecode: Methodcall from an Object was not count!");
	}

	/**
	 * -- M2M-Bytecode -- Checks if a static Methodcall counted. Should be one.
	 */
	@Test
	void methodcallStaticM2MBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example1.ClassA");

		assertNotNull(innerMap);
		assertEquals(1, innerMap.get("examples.example1.ClassC"), "M2M-Bytecode: Static Methodcall was not count!");
	}

	/**
	 * -- M2M-Bytecode -- Checks if a Methodcall from Field counted. Should be one.
	 */
	@Test
	void methodcallFromFieldM2MBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example1.ClassA");

		assertNotNull(innerMap);
		assertEquals(1, innerMap.get("examples.example1.ClassD"),
				"M2M-Bytecode: Methodcall from a Field was not count!");
	}

	/**
	 * -- Sourcecode vs. Bytecode -- Checks if a Methodcall from an Object, from a
	 * Field and as static is counted equally by Source- and Bytecode. Should be
	 * three
	 */
	@Test
	void byteAndSourcecodeShouldCountSameM2MCoupling() {
		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example1.ClassA");
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example1.ClassA");

		assertNotNull(innerMapS);
		assertNotNull(innerMapB);

		assertTrue(innerMapS.equals(innerMapB));
	}

}
