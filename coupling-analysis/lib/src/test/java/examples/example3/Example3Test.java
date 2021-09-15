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

package examples.example3;

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

@Config(sourceCodeInputRoot = TestSetup.SOURCECODE_ROOT_TO_TEST_EXAMPLES, byteCodeInputRoot = TestSetup.BYTECODE_ROOT_TO_TEST_EXAMPLES
		+ "/example3", sourceCodeMetrics = { CouplingTag.INHERITANCE,
				CouplingTag.METHOD_TO_METHOD }, byteCodeMetrics = { CouplingTag.INHERITANCE,
						CouplingTag.METHOD_TO_METHOD }, isConsolePrinting = false)
class Example3Test extends TestSetup {

	@BeforeAll
	static void init() {
		init(Example3Test.class, "examples.example3");
	}

	@BeforeAll
	static void nullCheck() {
		assertNotEquals(0, resultSourcecode.size());
		assertNotEquals(0, resultBytecode.size());
		assertNotNull(resultSourcecode.get(CouplingTag.INHERITANCE));
		assertNotNull(resultSourcecode.get(CouplingTag.METHOD_TO_METHOD));
		assertNotNull(resultBytecode.get(CouplingTag.INHERITANCE));
		assertNotNull(resultBytecode.get(CouplingTag.METHOD_TO_METHOD));
	}

	/**
	 * -- Inhertiance-Sourcecode -- Checks if 'extends' and 'implements' are
	 * counted. Should be one each.
	 */
	@Test
	void countExtendsAndImplementsAsInheritensCouplingSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.INHERITANCE,
				"examples.example3.ClassA");
		assertEquals(1, innerMap.get("examples.example3.ClassB"),
				"Inheritance-Coupling for 'extends' was not count in Sourcecode!");
		assertEquals(1, innerMap.get("examples.example3.Interface"),
				"Inheritance-Coupling for 'implents' was not count in Sourcecode!");
	}

	/**
	 * -- Inhertiance-Bytecode -- Checks if 'extends' and 'implements' are counted.
	 * Should be one each.
	 */
	@Test
	void countExtendsAndImplementsAsInheritensCouplingBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.INHERITANCE,
				"examples.example3.ClassA");
		assertEquals(1, innerMap.get("examples.example3.ClassB"),
				"Inheritance-Coupling for 'extends' was not count in Bytecode!");
		assertEquals(1, innerMap.get("examples.example3.Interface"),
				"Inheritance-Coupling for 'implements' was not count in Bytecode!");
	}

	/**
	 * -- Inheritance-Sourcecode -- Checks if the 'implements' from Outer and
	 * InnerClass are counted both. Should be two.
	 */
	@Test
	void countImplementsInOuterAndInnerclassAsInheritanceSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.INHERITANCE,
				"examples.example3.ClassB");
		assertEquals(2, innerMap.get("examples.example3.AnotherInterface"),
				"Inheritance Coupling was not count for InnerClass!");
	}

	/**
	 * -- Inheritance-Bytecode -- Checks if the 'implements' from Outer and
	 * InnerClass are counted both. Should be two.
	 */
	@Test
	void countImplementsInOuterAndInnerclassAsInheritanceBytecode() {

		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.INHERITANCE,
				"examples.example3.ClassB");
		assertEquals(2, innerMap.get("examples.example3.AnotherInterface"),
				"Inheritance Coupling was not count for InnerClass!");
	}

	/**
	 * -- Inheritance-Sourcecode -- Checks that no transitive Inheritance is
	 * counted.
	 */
	@Test
	void transitiveInheritanceShouldNotBeCountedSourcecode() {

		assertNotNull(getInnerMapByCouplingTagForSourcecode(CouplingTag.INHERITANCE, "examples.example3.ClassA"));

		for (final String innerKey : getInnerMapByCouplingTagForSourcecode(CouplingTag.INHERITANCE,
				"examples.example3.ClassA").keySet()) {
			assertNotEquals("examples.example3.ClassC", innerKey,
					"Transitive Inheritance-Coupling was Count in Sourcecode");
		}
	}

	/**
	 * -- Inheritance-Bytecode -- Checks that no transitive Inheritance is counted.
	 */
	@Test
	void transitiveInheritanceShouldNotBeCountedBytecode() {

		assertNotNull(getInnerMapByCouplingTagForBytecode(CouplingTag.INHERITANCE, "examples.example3.ClassA"));

		for (final String innerKey : getInnerMapByCouplingTagForBytecode(CouplingTag.INHERITANCE,
				"examples.example3.ClassA").keySet()) {
			assertNotEquals("examples.example3.ClassC", innerKey,
					"Transitive Inheritance-Coupling was Count in Bytecode");
		}
	}

	/**
	 * --Inheritance-Sourcecode -- Checks if multiple implements are count.
	 */
	@Test
	void moreThanOneImplemtedInterfaceShouldBeCountedSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.INHERITANCE,
				"examples.example3.ClassA");

		assertTrue(
				innerMap.get("examples.example3.Interface").equals(1)
						&& innerMap.get("examples.example3.AnotherInterface").equals(1),
				"Multiple 'implements' were not counted in Sourcecode!");
	}

	/**
	 * -- Inheritance-Bytecode -- Checks if multiple implements are count.
	 */
	@Test
	void moreThanOneImplemtedInterfaceShouldBeCountedBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.INHERITANCE,
				"examples.example3.ClassA");

		assertTrue(
				innerMap.get("examples.example3.Interface").equals(1)
						&& innerMap.get("examples.example3.AnotherInterface").equals(1),
				"Multiple 'implements' were not counted in Bytecode!");
	}

	/**
	 * -- M2M-Sourcecode -- ClassA extends ClassB extends ClassC. ClassC has a Field
	 * of Type ClassA (pseudo Inheritance circle). Checks if the M2M-Coupling from
	 * ClassC to ClassA by a Methodcall from that Field is counted.
	 */
	@Test
	void circleInheritanceBecauseOfFieldShouldCountM2MCouplingFromFieldSourcecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example3.ClassC");

		assertEquals(1, innerMap.get("examples.example3.ClassA"),
				"M2M-Coupling with a Field in an Inheritance Circle (because of that Field) was not count in Sourcecode!");
	}

	/**
	 * -- M2M-Bytecode -- ClassA extends ClassB extends ClassC. ClassC has a Field
	 * of Type ClassA (pseudo Inheritance circle). Checks if the M2M-Coupling from
	 * ClassC to ClassA by a Methodcall from that Field is counted.
	 */
	@Test
	void circleInheritanceBecauseOfFieldShouldCountM2MCouplingFromFieldBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example3.ClassC");

		assertEquals(1, innerMap.get("examples.example3.ClassA"),
				"M2M-Coupling with a Field in an Inheritance Circle (because of that Field) was not count in Bytecode!");
	}

	/**
	 * -- Sourcecode vs. Bytecode -- Checks if both count same simple
	 * Inheritance-Coupling.
	 */
	@Test
	void byteAndSourceCodeShouldCountSameInheritance() {

		final Map<String, Map<String, Integer>> mapS = resultSourcecode.get(CouplingTag.INHERITANCE);
		final Map<String, Map<String, Integer>> mapB = resultBytecode.get(CouplingTag.INHERITANCE);

		// Make sure that results are different Lists
		assertFalse(mapS == mapB);
		assertTrue(mapS.equals(mapB));
	}

	/**
	 * -- M2M-Sourcecode -- Checks if M2M-Coupling is counted in case of a
	 * Methodcall to InterfaceMethod from an InterfaceObjet.
	 */
	@Test
	void interfaceCallByCastedObjectShouldBeCountedAsM2MSourcecode() {

		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example3.ClassA");

		assertEquals(1, innerMap.get("examples.example3.Interface"),
				"M2M-Coupling was not count for Methodcall with Interface Object!");
	}

	/**
	 * -- M2M-Bytecode -- Checks if M2M-Coupling is counted in case of a Methodcall
	 * to InterfaceMethod from an InterfaceObjet.
	 */
	@Test
	void interfaceCallByCastedObjectShouldBeCountedAsM2MBytecode() {
		final Map<String, Integer> innerMap = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example3.ClassA");

		assertEquals(1, innerMap.get("examples.example3.Interface"),
				"M2M-Coupling was not count for Methodcall with Interface Object!");
	}

	/**
	 * -- M2M - Sourcecode vs. Bytecode -- ClassA extends ClassB extends ClassC.
	 * ClassA calls a Method from ClassC, Bytecode does not count this. ClassA makes
	 * a superCall to ClassC, Bytecode count this as Coupling to ClassB. Checks if
	 * Bytecode counts Methodcalls in this case different than Sourcecode.
	 */
	@Test
	void bytecodeShouldCountRightSuperCallInInheritanceHirarchie() {

		final Map<String, Integer> innerMapS = getInnerMapByCouplingTagForSourcecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example3.ClassA");
		final Map<String, Integer> innerMapB = getInnerMapByCouplingTagForBytecode(CouplingTag.METHOD_TO_METHOD,
				"examples.example3.ClassA");

		int sumS = 0;
		for (final String key : innerMapS.keySet()) {
			sumS += innerMapS.get(key);
		}
		int sumB = 0;
		for (final String key : innerMapB.keySet()) {
			sumB += innerMapB.get(key);
		}

		final int difference = innerMapB.get("examples.example3.ClassB") - innerMapS.get("examples.example3.ClassB");
		assertNotEquals(0, difference, "Bytecode counts Supercalls to another class than expected!");
		assertFalse(sumS == sumB, "Bytecode counts Supercall without 'super.' to Superclass and not this class!");
	}
}
