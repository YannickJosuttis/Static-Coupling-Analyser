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

package de.cau.sourcecode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import de.cau.config.Config;
import de.cau.config.Configuration;
import de.cau.monitor.CouplingMonitor;
import de.cau.monitor.metrics.ACoupling;
import de.cau.monitor.metrics.CouplingTag;
import de.cau.tools.DataTransformer;
import de.cau.tools.FunctionHelper;
import de.cau.tools.Logger;

/**
 * Main for sourcecode analyzer. Annotation useful for development.
 *
 */
@Config(sourceCodeMetrics = {
		CouplingTag.METHOD_TO_METHOD,
		CouplingTag.IMPORT,
		CouplingTag.PACKAGE,
		CouplingTag.INHERITANCE,
		CouplingTag.FIELD,
		CouplingTag.STRUCTUAL_DEBT_INDEX

}, externalLibPath = "/home/yannick/.gradle/caches/modules-2/files-2.1", countSelfConnection = false, isConsolePrinting = true)

public class Main {

	public static boolean isFinished = false;

	public static void main(final String[] args) {

		isFinished = false;

		if (args.length <= 0) {
			Configuration.configure(args, Main.class, Configuration.ReadFrom.FROM_ANNOTATION);
		}

		final Logger logger = Logger.getInstance();

		logger.log("\n##################################################################");
		logger.log("#                     SOURCECODE ANALYSIS                        #");
		logger.log("##################################################################\n");

		final CouplingMonitor cm = CouplingMonitor.getInstance();
		final ASTParser classCollector = new ASTParser();

		final List<Optional<CompilationUnit>> cus = classCollector.getAllCompilationUnits();

		// Register all classes we want to count and collect all packages of the source
		// source root.
		final Set<String> packages = new HashSet<>();
		cus.forEach(ocu -> ocu.ifPresent(cu -> cu.findFirst(ClassOrInterfaceDeclaration.class).ifPresent(coi -> {
			final Optional<PackageDeclaration> packageDeclaration = cu.getPackageDeclaration();
			String pack = packageDeclaration.isPresent()
					? cu.getPackageDeclaration().get().getNameAsString()
					: "";
			Configuration.addToWhiteList(pack);
			pack = pack.equals("") ? "" : pack + ".";
			packages.add(pack);

			if (!coi.isEnumDeclaration() && !coi.isAnnotationDeclaration()) {
				cm.registerClass(pack + coi.getNameAsString());
			}

		})));

		logger.log("Calculate coupling with given metric(s): ");
		logger.logSeparation();
		logger.logAll(Configuration.getSourceCodeMetrics().stream().map(x -> "\t - " + x.toString())
				.collect(Collectors.toList()));

		// Here we start to calculate the coupling for the defined metrics.
		cm.registerSourceCodeCoupling(cus);

		if (args.length <= 0) {
			// Print to console
			cm.printAllCouplings();
			logger.log(Configuration.getSourceCodeInfo().toString());

		}

		// Create a CSV file for each metrics.
		writeDataToCSV(cm, new DataTransformer(), Configuration.getWhiteList());

		if (args.length <= 0) {
			logger.close();
		}
		isFinished = true;
	}

	/**
	 * Writing all counted couplings into CSV files.
	 * 
	 * @param cm       Coupling monitor
	 * @param creator  Datatransformer
	 * @param packages the package names
	 */
	private static void writeDataToCSV(final CouplingMonitor cm, final DataTransformer creator,
			final Set<String> packages) {

		final List<Map<String, Map<String, Integer>>> coupl = cm.getRegisteredSorceCodeCouplings();

		final Set<String> classes = cm.getRegisteredClasses();
		final List<ACoupling> metrics = Configuration.getSourceCodeMetrics();

		for (final Entry<Map<String, Map<String, Integer>>, ACoupling> entry : FunctionHelper.zip(coupl,
				metrics)) {
			creator.mapToCSV("SOURCECODE-" + entry.getValue().toString(), entry.getKey(),
					entry.getValue().toString().equals(CouplingTag.PACKAGE.toString()) ? packages : classes);
		}
		creator.createClassMetricTable("Sourcecode-", metrics, classes);
	}
}