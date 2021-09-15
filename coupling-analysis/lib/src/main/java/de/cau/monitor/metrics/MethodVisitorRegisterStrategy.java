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
 * A functional interface for bytecode analysis for method-to-method coupling
 * and package-coupling.
 */
@FunctionalInterface
public interface MethodVisitorRegisterStrategy {

	/**
	 * Register the coupling.
	 * 
	 * @param classFrom from which the coupling is counted.
	 * @param classTo   to which class there is a coupling.
	 * @param methodTo  to which method there is a coupling.
	 */
	void registerCoupling(final String classFrom, final String classTo, final String methodTo);

}
