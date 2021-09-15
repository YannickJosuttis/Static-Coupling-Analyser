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

/**
 * Tags of the different couplings used in this project.
 *
 */
public enum CouplingTag {

	/**
	 * Method to Method coupling: Simplified calls to another objects.
	 */
	METHOD_TO_METHOD(0),

	/**
	 * Field coupling: Fields of another objects.
	 */
	FIELD(0),

	/**
	 * Field coupling: Abstraction of method-to-method coupling.
	 */
	PACKAGE(1),

	/**
	 * Field coupling: Counting how much imports of other objects are used.
	 */
	IMPORT(0),

	/**
	 * Inheritance coupling: Simple inheritance to other objects.
	 */
	INHERITANCE(0),

	/**
	 * StructalDebtIndex, cost of cyclic removements
	 */
	STRUCTUAL_DEBT_INDEX(2);

	// The priority determines the order of the calculation.
	private int priority;

	CouplingTag(final int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

}
