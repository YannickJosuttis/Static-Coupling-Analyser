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

package examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import de.cau.bytecode.visitor.ClassVisitor;
import de.cau.config.Configuration;
import de.cau.config.DirectoryTool;
import de.cau.monitor.CouplingMonitor;
import de.cau.monitor.metrics.ACoupling;
import de.cau.monitor.metrics.CouplingTag;
import de.cau.sourcecode.ASTParser;

public class TestSetup {

	public static final String BYTECODE_ROOT_TO_TEST_EXAMPLES = "./src/test/resources/compiled/examples";
	public static final String SOURCECODE_ROOT_TO_TEST_EXAMPLES = "./src/test/resources";
	protected static Map<CouplingTag, Map<String, Map<String, Integer>>> resultBytecode;
	protected static Map<CouplingTag, Map<String, Map<String, Integer>>> resultSourcecode;

	protected static void init(final Class<?> testClass, final String... whiteListPackages) {

		Configuration.configure(null, testClass, Configuration.ReadFrom.FROM_ANNOTATION);

		final String[] patterns = { "**/*.jar", "**/*.class" };
		final String[] jarAndClassFiles = DirectoryTool.filesScannedInDirectory(DirectoryTool.getByteCodeInputRoot(),
				patterns);
		final CouplingMonitor cm = CouplingMonitor.getInstance();
		final ClassVisitor visitor = new ClassVisitor(cm);
		visitor.visitAllJavaClasses(jarAndClassFiles);

		resultBytecode = new HashMap<>();

		for (final ACoupling bCoupl : Configuration.getBytecodeCodeMetrics()) {
			resultBytecode.put(bCoupl.getNameTag(), bCoupl.getRegisteredCouplings());
		}

		// -----------------------------------------------------------------------------

		final ASTParser classCollector = new ASTParser();
		final List<Optional<CompilationUnit>> ocus = classCollector.getAllCompilationUnits();
		Configuration.addAllToWhiteList(List.of(whiteListPackages));

		// Register classes (is needed only for Inheritance-Coupling)
		ocus.forEach(ocu -> ocu.ifPresent(cu -> cu.findFirst(ClassOrInterfaceDeclaration.class).ifPresent(coi -> {
			final String pack = cu.getPackageDeclaration().get().getNameAsString();
			cm.registerClass(pack + "." + coi.getNameAsString());

		})));

		cm.registerSourceCodeCoupling(ocus);
		resultSourcecode = new HashMap<>();

		for (final ACoupling sCoupl : Configuration.getSourceCodeMetrics()) {
			resultSourcecode.put(sCoupl.getNameTag(), sCoupl.getRegisteredCouplings());
		}
	}

	/**
	 * Sets the Metric-Map and the outer key
	 * 
	 * @param indexOfMetric The index of Metric you want to check from the
	 *                      Annotations sourceCodeMetrics
	 * @param checkKey      The outer key you want to check
	 * @return The value from checkKey
	 */
	protected Map<String, Integer> getInnerMapByCouplingTagForSourcecode(final CouplingTag tag,
			final String checkKey) {

		if (resultSourcecode == null)
			throw new IllegalStateException("No Sourcecode-Metrics defined in @Config Annotation!");
		// which Metric is asked for from result
		final Map<String, Map<String, Integer>> mapS = resultSourcecode.get(tag);

		if (mapS == null)
			throw new IllegalArgumentException("CouplingTag not specified for Sourcecode in Annotation!");

		final Map<String, Integer> innerMapS = mapS.get(checkKey);
		return innerMapS;
	}

	/**
	 * Sets the Metric-Map and the outer key
	 * 
	 * @param indexOfMetric The index of Metric you want to check from the
	 *                      Annotations byteCodeMetrics
	 * @param checkKey      The outer key you want to check
	 * @return The value from checkKey
	 */
	protected Map<String, Integer> getInnerMapByCouplingTagForBytecode(final CouplingTag tag, final String checkKey) {
		// which Metric is asked for from result
		final Map<String, Map<String, Integer>> mapB = resultBytecode.get(tag);

		if (mapB == null)
			throw new IllegalArgumentException("CouplingTag not specified for Bytecode in Annotation!");

		final Map<String, Integer> innerMapB = mapB.get(checkKey);
		return innerMapB;
	}

}
