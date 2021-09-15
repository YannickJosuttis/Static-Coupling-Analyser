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

package de.cau.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

import org.apache.tools.ant.DirectoryScanner;

/**
 * This class handles various operations related to directories.
 *
 */
public class DirectoryTool {

	private static String sourceCodeinputRoot;
	private static String byteCodeInputRoot;
	private static String outputRoot;
	private static String configFilePath;
	private static Optional<String> externalLibPath = Optional.empty();
	public static boolean isSourceCodeRootDefined = false;
	public static boolean isByteCodeRootDefined = false;

	public static String backslash = "/";

	/**
	 * Scanning directory and collect all paths matching the wild card pattern.
	 * 
	 * @param baseDir         the directory to search
	 * @param wildcardPattern
	 * @return all paths matching wild card.
	 */
	public static String[] filesScannedInDirectory(String baseDir, final String[] wildcardPattern) {
		baseDir = new File(baseDir).getAbsolutePath();
		final DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(wildcardPattern);
		scanner.setBasedir(baseDir);
		scanner.setExcludes(new String[] {});
		scanner.scan();
		final String[] returnValues = scanner.getIncludedFiles();
		for (int i = 0; i < returnValues.length; i++) {
			returnValues[i] = baseDir + File.separator + returnValues[i];
		}
		return returnValues;
	}

	/**
	 * Setting the output root based on this project.
	 */
	public static void setOutputRootBasedOnProject() {
		String directory = new File("./").getAbsolutePath();
		directory = directory.replace('\\', '/');
		directory = directory.substring(0, directory.lastIndexOf(backslash));
		outputRoot = directory.substring(0, directory.lastIndexOf(backslash) + 1) + "outputs" + backslash;
		new File(outputRoot).mkdir();
	}

	/**
	 * Create a directory on given path.
	 * 
	 * @param path
	 */
	public static void createDirectory(final String path) {
		new File(path).mkdir();
	}

	/**
	 * Setting the sourceode input root.
	 * 
	 * @param inputRoot
	 */
	public static void setSourceCodeInputRoot(final String inputRoot) {
		DirectoryTool.sourceCodeinputRoot = inputRoot;
		isSourceCodeRootDefined = true;
	}

	/**
	 * Setting the output root. There are all outputs are stored.
	 * 
	 * @param oRoot
	 */
	public static void setOutputRoot(String oRoot) {
		oRoot = Character.toString(oRoot.charAt(oRoot.length() - 1)) == backslash ? oRoot : oRoot + backslash;
		outputRoot = oRoot + backslash + "outputs" + backslash;
		createDirectory(outputRoot);
	}

	/**
	 * Setting the path to a config file to read from.
	 * 
	 * @param configFilePath
	 */
	public static void setConfigFilePath(final String configFilePath) {
		DirectoryTool.configFilePath = configFilePath;
	}

	/**
	 * Setting backslash/slash, this is may useful for differtent OS like Windows
	 * and Linux distributions.
	 * 
	 * @param backslash
	 */
	public static void setBackslash(final String backslash) {
		DirectoryTool.backslash = backslash;
	}

	/**
	 * Sets the path to external libraries, e.g. this could be set to the Gradle
	 * caches.
	 * 
	 * @param externalLibPath
	 */
	public static void setExternalLibPath(final Optional<String> externalLibPath) {
		DirectoryTool.externalLibPath = externalLibPath;
	}

	/**
	 * Sets the path to external libraries, e.g. this could be set to the Gradle
	 * caches.
	 * 
	 * @param externalLibPath
	 */
	public static void setExternalLibPath(final String extLibPath) {
		externalLibPath = Optional.of(extLibPath);
	}

	/**
	 * Sets the input root for bytecode.
	 * 
	 * @param byteCodeInputRoot
	 */
	public static void setByteCodeInputRoot(String byteCodeInputRoot) {
		byteCodeInputRoot = byteCodeInputRoot.replace('\\', '/');
		DirectoryTool.byteCodeInputRoot = byteCodeInputRoot;
		isByteCodeRootDefined = true;
	}

	/**
	 * Checks if given file is empty, by trying to read.
	 * 
	 * @param file to check
	 * @return true, if file is empty, otherwise false.
	 */
	@SuppressWarnings("resource")
	public static boolean isFileEmpty(final File file) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			return br.readLine() == null;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		throw new IllegalStateException();
	}

	/**
	 * Checks if given file of the path is empty, by trying to read.
	 * 
	 * @param path to file
	 * @return true, if file is empty, otherwise false.
	 */
	public static boolean isFileEmpty(final String path) {
		return isFileEmpty(new File(path));
	}

	public static String getSourceCodeInputRoot() {

		if (sourceCodeinputRoot == null)
			throw new IllegalStateException("Input root needs to be specified first!");

		return sourceCodeinputRoot;
	}

	public static String getByteCodeInputRoot() {
		if (sourceCodeinputRoot == null)
			throw new IllegalStateException("Input root needs to be specified first!");
		return byteCodeInputRoot;
	}

	public static Optional<String> getExternalLibPath() {
		return externalLibPath;
	}

	public static String getConfigFilePath() {
		if (configFilePath == null)
			throw new IllegalStateException("Path of config file needs to be specified first!");
		return configFilePath;
	}

	public static String getBackslash() {
		if (backslash == null)
			throw new IllegalStateException("Type of backslash needs to be specified first!");
		return backslash;
	}

	public static String getOutputRoot() {
		if (outputRoot == null)
			throw new IllegalStateException("Output root needs to be specified first!");
		return outputRoot;
	}
}
