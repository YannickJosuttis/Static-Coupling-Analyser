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

package de.cau.main;

import de.cau.config.Config;
import de.cau.config.Configuration;
import de.cau.config.Configuration.ReadFrom;
import de.cau.monitor.CouplingMonitor;
import de.cau.monitor.metrics.CouplingTag;
import de.cau.tools.Logger;
import de.cau.tools.MetaInfo;

/**
 * This main is starting the bytecode main and the sourcecode main. Annotation
 * is useful for development.
 *
 */
@Config(byteCodeMetrics = {
		CouplingTag.METHOD_TO_METHOD,
		CouplingTag.IMPORT,
		CouplingTag.PACKAGE,
		CouplingTag.INHERITANCE,
		CouplingTag.FIELD,

}, sourceCodeMetrics = {
		CouplingTag.METHOD_TO_METHOD,
		CouplingTag.IMPORT,
		CouplingTag.PACKAGE,
		CouplingTag.INHERITANCE,
		CouplingTag.FIELD,

}, externalLibPath = "/home/yannick/.gradle/caches/modules-2/files-2.1", countSelfConnection = true, isConsolePrinting = false)
public class Main {

	public static void main(final String[] args) {

		if (args.length <= 0) {
			Configuration.configure(args, Main.class, ReadFrom.FROM_ANNOTATION);
		}

		final String[] args2 = { "work around" };
		if (!Configuration.getBytecodeCodeMetrics().isEmpty()) {
			de.cau.bytecode.Main.main(args2);
		}

		final CouplingMonitor cm = CouplingMonitor.getInstance();
		Logger.getInstance().log(cm.getRegisteredClasses());

		if (!Configuration.getSourceCodeMetrics().isEmpty()) {
			cm.clearRegisteredClasses();
			Configuration.clearWhiteList();
			de.cau.sourcecode.Main.main(args2);
		}
		Logger.getInstance().log(cm.getRegisteredClasses());

		final Logger logger = Logger.getInstance();

		final MetaInfo byteCodeInfo = Configuration.getByteCodeInfo();
		final MetaInfo sourceCodeInfo = Configuration.getSourceCodeInfo();
		logger.log(byteCodeInfo);
		logger.log(sourceCodeInfo);
	}
}
