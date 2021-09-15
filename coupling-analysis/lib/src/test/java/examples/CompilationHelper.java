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

import java.io.IOException;

import de.cau.config.DirectoryTool;

/**
 * Just to compile the test resources.
 *
 */
public class CompilationHelper {

	private static boolean isEclipseMode = true;

	/**
	 * This Method needs to be called if the test resource have changed.
	 * 
	 * @param relativeSrcPath
	 * @param relativeDestPath
	 */
	public static void createClassFiles(final String relativeSrcPath, final String relativeDestPath) {

		final Runtime r = Runtime.getRuntime();
		String root = TestSetup.class.getProtectionDomain().getCodeSource().getLocation().getPath();

		final String subPath = isEclipseMode ? "/bin/test" : "/build/classes/java/test";
		root = root.substring(0, root.length() - subPath.length());

		final String src = root + relativeSrcPath;
		final String[] fileNames = DirectoryTool.filesScannedInDirectory(src, new String[] { "**/*.java" });
		String f = "";

		for (int i = 0; i < fileNames.length; i++) {
			f += fileNames[i] + " ";
		}

		final String dest = root.substring(root.indexOf(':') + 1) + relativeDestPath;

		final String command = String.format("javac -d %s %s", dest, f);
		try {
			final Process p = r.exec(String.format(command));
			// Wait until the command is finished
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void main(final String[] args) {
		createClassFiles(TestSetup.SOURCECODE_ROOT_TO_TEST_EXAMPLES,
				TestSetup.SOURCECODE_ROOT_TO_TEST_EXAMPLES + "/compiled");
	}
}
