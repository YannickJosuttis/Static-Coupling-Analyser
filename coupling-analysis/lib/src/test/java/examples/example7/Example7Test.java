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

package examples.example7;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.cau.config.Config;
import de.cau.monitor.metrics.CouplingTag;
import examples.TestSetup;

@Config(sourceCodeInputRoot = TestSetup.SOURCECODE_ROOT_TO_TEST_EXAMPLES, byteCodeInputRoot = TestSetup.BYTECODE_ROOT_TO_TEST_EXAMPLES
+ "/example7", sourceCodeMetrics = { CouplingTag.METHOD_TO_METHOD }, byteCodeMetrics = {
		CouplingTag.METHOD_TO_METHOD }, isConsolePrinting = false)
class Example7Test extends TestSetup{

	@BeforeAll
	static void init() {
		init(Example7Test.class, "examples.example7");
	}

	/**
	 * -- M2M - Source- vs. Bytecode --
	 * Checks if Bytecode count a method call three times and Sourcecode one times in a try-catch-finally block.
	 */
	@Test
	void tryCatchFinallyDifferentCountForSourceAndBytecode() {
		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example7.ClassA");
		
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example7.ClassA");
		
		int[] expected = {1,3};
		int[] actual = {innerMapS.get("examples.example7.ClassA2"), innerMapB.get("examples.example7.ClassA2")};
		assertArrayEquals(expected, actual, "M2M-Source- vs. Bytecode: Couplung in try-catch-finally block was " + actual + " and not {1,3}!");
	}
	
	/**
	 * -- M2M - Source- vs. Bytecode --
	 * Checks if Source- and Bytecode count same Coupling for a method chain.
	 */
	@Test
	void rightCountOfMethodChain() {
		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example7.ClassA");
		
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example7.ClassA");
		
		int[] expected = {5,5};
		int[] actual = {innerMapS.get("examples.example7.ClassA3"), innerMapB.get("examples.example7.ClassA3")};
		assertArrayEquals(expected, actual, "M2M-Source- vs. Bytecode: Coupling in method chain was " + actual + " and not {5,5}!");
		
	}
	
	@Test
	void rightCountInCaseOfGenericType() {
		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example7.ClassC");
		
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example7.ClassC");
		
		assertEquals(1, innerMapS.get("examples.example7.ClassC2"), "M2M-Sourcecode: Coupling to method with generic type was not count!"
				+ "");
	}
	
	/**
	 * -- M2M - Source- and Bytecode --
	 * Checks the coupling count in case of a lambda expression with a functional Interface.
	 */
	@Test
	void couplingWithLambdaAndFunctionalInterface() {
		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example7.ClassB");
		
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example7.ClassB");
		
		assertEquals(1, innerMapS.get("examples.example7.ClassB3"));
		assertEquals(1, innerMapB.get("examples.example7.ClassB3"));
	}
	
	/**
	 * -- M2M - Source- and Bytecode --
	 * Checks the coupling count in case of a lambda expression with an anonymous class.
	 */
	@Test
	void couplingWithLambdaAndAnonymousClass() {
		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example7.ClassB2");
		
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example7.ClassB2");
		
		assertEquals(1, innerMapS.get("examples.example7.ClassB3"));
		assertEquals(1, innerMapB.get("examples.example7.ClassB3"));
	}

}





