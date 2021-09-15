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

package de.cau.bytecode;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.cau.bytecode.visitor.ClassVisitor;
import de.cau.config.Config;
import de.cau.config.Configuration;
import de.cau.config.DirectoryTool;
import de.cau.monitor.CouplingMonitor;
import de.cau.monitor.metrics.ACoupling;
import de.cau.monitor.metrics.CouplingTag;
import de.cau.tools.DataTransformer;
import de.cau.tools.FunctionHelper;
import de.cau.tools.Logger;

/**
 * Main for starting bytecode Analysis. Annotation is useful for development.
 *
 */
@Config(byteCodeMetrics = { CouplingTag.METHOD_TO_METHOD,
		CouplingTag.IMPORT,
		CouplingTag.PACKAGE,
		CouplingTag.INHERITANCE,
		CouplingTag.FIELD,
		CouplingTag.STRUCTUAL_DEBT_INDEX

}, isConsolePrinting = true, countSelfConnection = true)
public class Main {

	public static boolean isFinished = false;

	public static void main(final String[] args) {

		isFinished = false;

		if (args.length <= 0) {
			Configuration.configure(args, Main.class, Configuration.ReadFrom.FROM_ANNOTATION);
		}

		final Logger logger = Logger.getInstance();

		logger.log("\n##################################################################");
		logger.log("#                      BYTECODE ANALYSIS                         #");
		logger.log("##################################################################\n");

		final String[] patterns = { "**/*.jar", "**/*.class" };
		final String[] jarAndClassFiles = DirectoryTool.filesScannedInDirectory(DirectoryTool.getByteCodeInputRoot(),
				patterns);

		final CouplingMonitor cm = CouplingMonitor.getInstance();
		final ClassVisitor visitor = new ClassVisitor(cm);

		// Here we start analyzing
		visitor.visitAllJavaClasses(jarAndClassFiles);

		if (args.length <= 0) {
			cm.printAllCouplings();
			logger.log(Configuration.getByteCodeInfo().toString());
		}

		// Store results in csv files.
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

		final List<Map<String, Map<String, Integer>>> coupl = cm.getRegisteredByteCodeCouplings();

		// Get registered classes without nested classes.
		final Set<String> classes = cm.getRegisteredClasses(x -> !x.contains("$"));
		final List<ACoupling> metrics = Configuration.getBytecodeCodeMetrics();

		for (final Entry<Map<String, Map<String, Integer>>, ACoupling> entry : FunctionHelper.zip(coupl, metrics)) {
			creator.mapToCSV("BYTECODE-" + entry.getValue().toString(), entry.getKey(),
					entry.getValue().toString().equals(CouplingTag.PACKAGE.toString()) ? packages : classes);
		}
		creator.createClassMetricTable("BYTECODE-", metrics, classes);
	}
}
