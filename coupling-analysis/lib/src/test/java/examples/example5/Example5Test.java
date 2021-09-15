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

package examples.example5;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.cau.config.Config;
import de.cau.monitor.metrics.CouplingTag;
import examples.TestSetup;

@Config(sourceCodeInputRoot = TestSetup.SOURCECODE_ROOT_TO_TEST_EXAMPLES, byteCodeInputRoot = TestSetup.BYTECODE_ROOT_TO_TEST_EXAMPLES
		+ "/example5", sourceCodeMetrics = { CouplingTag.METHOD_TO_METHOD }, byteCodeMetrics = {
				CouplingTag.METHOD_TO_METHOD }, isConsolePrinting = false)
class Example5Test extends TestSetup {

	@BeforeAll
	static void init() {
		init(Example5Test.class, "examples.example5");
	}

	@BeforeAll
	static void nullCheck() {
		assertNotEquals(0, resultSourcecode.size());
		assertNotEquals(0, resultBytecode.size());
		assertNotNull(resultSourcecode.get(CouplingTag.METHOD_TO_METHOD));
		assertNotNull(resultBytecode.get(CouplingTag.METHOD_TO_METHOD));
	}

	/**
	 * -- M2M-Sourcecode -- Checks if calls to an InnerClass are counted as
	 * M2M-Coupling to it's OuterClass.
	 */
	@Test
	void m2McouplingToInnerClassShouldBeCountedAsCouplingToOuterClassSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example5.ClassA");

		for (final String innerKey : innerMap.keySet()) {
			assertNotEquals("examples.example5.InnerClass", innerKey,
					"Coupling to an InnerClass was not count to its OuterClass in Sourcecode!");
		}
	}

	/**
	 * -- M2M-Bytecode -- Checks if calls to an InnerClass are counted as
	 * M2M-Coupling to it's OuterClass.
	 */
	@Test
	void m2McouplingToInnerClassShouldBeCountedAsCouplingToOuterClassBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example5.ClassA");

		for (final String innerKey : innerMap.keySet()) {
			assertNotEquals("examples.example5.InnerClass", innerKey,
					"Coupling to an InnerClass was not count to its OuterClass in Bytecode!");
		}
	}

}
