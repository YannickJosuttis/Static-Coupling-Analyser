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

package examples.example6;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.cau.config.Config;
import de.cau.monitor.metrics.CouplingTag;
import examples.TestSetup;

@Config(sourceCodeInputRoot = TestSetup.SOURCECODE_ROOT_TO_TEST_EXAMPLES, byteCodeInputRoot = TestSetup.BYTECODE_ROOT_TO_TEST_EXAMPLES
		+ "/example6", sourceCodeMetrics = {
				CouplingTag.FIELD }, byteCodeMetrics = { CouplingTag.FIELD }, isConsolePrinting = false)
class Example6Test extends TestSetup {

	@BeforeAll
	static void init() {
		init(Example6Test.class, "examples.example6");
	}

	@BeforeAll
	static void nullCheck() {
		assertNotEquals(0, resultSourcecode.size());
		assertNotEquals(0, resultBytecode.size());
		assertNotNull(resultSourcecode.get(CouplingTag.FIELD));
		assertNotNull(resultBytecode.get(CouplingTag.FIELD));
	}

	/**
	 * -- Field-Sourcecode -- Checks if simple Field Coupling is counted.
	 */
	@Test
	void countFieldCouplingSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.FIELD,
				"examples.example6.ClassA");

		assertEquals(1, innerMap.get("examples.example6.ClassB"),
				"simple Field Coupling is not counted in Sourcecode!");
	}

	/**
	 * -- Field-Bytecode -- Checks if simple Field Coupling is counted.
	 */
	@Test
	void countFieldCouplingBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.FIELD,
				"examples.example6.ClassA");

		assertEquals(1, innerMap.get("examples.example6.ClassB"),
				"simple Field Coupling is not counted in Sourcecode!");
	}

	/**
	 * -- Field-Sourcecode -- Checks if an Field which is casted to Interface is
	 * counted.
	 */
	@Test
	void fieldFromInterfaceFielCouplingSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.FIELD,
				"examples.example6.ClassA");
		assertEquals(1, innerMap.get("examples.example6.Interface"),
				"Field which is casted to Interface is not counted as Field Coupling in Sourcecode!");
	}

	/**
	 * -- Field-Bytecode -- Checks if an Field which is casted to Interface is
	 * counted.
	 */
	@Test
	void fieldFromInterfaceFielCouplingBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.FIELD,
				"examples.example6.ClassA");
		assertEquals(1, innerMap.get("examples.example6.Interface"),
				"Field which is casted to Interface is not counted as Field Coupling in Bytecode!");
	}

}
