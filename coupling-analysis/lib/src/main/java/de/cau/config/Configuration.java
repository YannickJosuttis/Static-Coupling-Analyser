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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import de.cau.monitor.metrics.ACoupling;
import de.cau.monitor.metrics.CouplingTag;
import de.cau.monitor.metrics.FieldCoupling;
import de.cau.monitor.metrics.ImportCoupling;
import de.cau.monitor.metrics.InheritanceCoupling;
import de.cau.monitor.metrics.MethodToMethodCoupling;
import de.cau.monitor.metrics.PackageCoupling;
import de.cau.monitor.metrics.StructalDebtIndex;
import de.cau.tools.Logger;
import de.cau.tools.MetaInfo;

/**
 * This class is used for setting general settings that are needed for
 * analyzing.
 *
 */
public class Configuration {

	private static List<ACoupling> sourceCodeMetrics;
	private static List<ACoupling> byteCodeMetrics;
	public static boolean isConsolePrinting;
	private static Set<String> whiteList;
	private static Class<?> clazz;
	private static Logger logger;
	private static MetaInfo sourceCodeInfo;
	private static MetaInfo byteCodeInfo;
	public static boolean countSelfConnection;
	public static final String UNDIFIND = "UNDIFINED";
	public static boolean isInit = false;

	/**
	 * Simple tag to define the type of reading.
	 */
	public enum ReadFrom {

		/**
		 * Read from a configuration file with (*.yaml or *.yml)
		 */
		@Deprecated
		FROM_FILE,

		/**
		 * Read from Annotation {@link Config}, this is for development and Testing
		 * purpose.
		 */
		FROM_ANNOTATION,

		/**
		 * Read from project directly.
		 */
		FROM_PROJECT,

		/**
		 * Read from storage directly.
		 */
		FROM_STORAGE
	};

	/**
	 * Creating Configuration. Note: This object is never used directly. The purpose
	 * of calling this is to overwrite the internal structure.
	 */
	private Configuration() {
		sourceCodeMetrics = new ArrayList<>();
		byteCodeMetrics = new ArrayList<>();
		whiteList = new HashSet<>();
		countSelfConnection = false;
//		readFrom = ReadFrom.FROM_ANNOTATION;
	}

	/**
	 * Important configurations are set here, like paths and metrics to be
	 * calculated.
	 * 
	 * @param args   console arguments
	 * @param aClass The class from which the configuration is called.(Only
	 *               necessary if read from Annotation {@code @Config} )
	 * @param rf     {@code ReadFrom} tag, to read from a config file, annotation or
	 *               the project itself (which could be useful for development)
	 */
	public static void configure(final String args[], final Class<?> aClass, final ReadFrom rf) {

		switch (rf) {
		case FROM_FILE:
			new Configuration();
			isConsolePrinting = false;
			clazz = aClass;
			readYamlFile();
			break;
		case FROM_ANNOTATION:
			new Configuration();
			isConsolePrinting = false;
			clazz = aClass;
			readFromAnnotation(clazz);
			break;
		case FROM_PROJECT:
			new Configuration();
			isConsolePrinting = false;

			clazz = aClass;
			readFromProject();
			break;
		case FROM_STORAGE:
			readFromStorage();
			break;

		default:
			throw new IllegalStateException("No valid configuaration source!");
		}

		// Sourcecode analysis needs external libraries.
		if (!sourceCodeMetrics.isEmpty() && DirectoryTool.getExternalLibPath().isEmpty()) {
			logger.logWarning(
					"\tYou try to calculate sourcecode coupling without specifiying the root of external libraries!\n"
							+ "\t\tThis can cause wrong results, if the project is using external libraries.\n"
							+ "\t\tIf this is not the case, this warning can be ignored.\n");
		}
	}

	/**
	 * Read from storage directly and clear data for next run.
	 */
	private static void readFromStorage() {

		sourceCodeInfo.clearData();
		byteCodeInfo.clearData();

		for (final ACoupling aCoupling : byteCodeMetrics) {
			aCoupling.clearCouplings();
		}
		for (final ACoupling aCoupling : sourceCodeMetrics) {
			aCoupling.clearCouplings();
		}

	}

	/**
	 * Reading configuration settings from predefined ones.
	 */
	private static void readFromProject() {

		sourceCodeInfo = new MetaInfo("SOURCECODE");
		byteCodeInfo = new MetaInfo("BYTECODE");

		countSelfConnection = false;
		isConsolePrinting = true;

		DirectoryTool.setConfigFilePath("./testConf.yaml");
	}

	/**
	 * Reading configuration settings from @Config Annotation with reflection.
	 * 
	 * @param clazz to look for the annotation.
	 */
	private static void readFromAnnotation(final Class<?> clazz) {

		sourceCodeInfo = new MetaInfo("SOURCECODE");
		byteCodeInfo = new MetaInfo("BYTECODE");

		final Annotation[] annotations = clazz.getAnnotations();

		for (final Annotation annotation : annotations)
			if (annotation instanceof Config) {
				final Config conf = (Config) annotation;

				isConsolePrinting = conf.isConsolePrinting();
				DirectoryTool.setOutputRoot(conf.outputRoot());
				logger = Logger.getInstance();
				DirectoryTool.setExternalLibPath(conf.externalLibPath().equals(UNDIFIND) ? Optional.empty()
						: Optional.of(conf.externalLibPath()));
				DirectoryTool.setSourceCodeInputRoot(conf.sourceCodeInputRoot());
				DirectoryTool.setByteCodeInputRoot(conf.byteCodeInputRoot());
				countSelfConnection = conf.countSelfConnection();

				for (final CouplingTag sMetric : conf.sourceCodeMetrics()) {

					final ACoupling aCoupl = nameTagToCouplingMetric(sMetric);

					if (aCoupl.getNameTag() == CouplingTag.PACKAGE) {
						aCoupl.setCouplingHook();
					}
					registerSourceCodeMetrics(aCoupl);
				}

				for (final CouplingTag bMetric : conf.byteCodeMetrics()) {

					final ACoupling aCoupl = nameTagToCouplingMetric(bMetric);
					if (aCoupl.getNameTag() == CouplingTag.PACKAGE) {
						aCoupl.setCouplingHook();
					}
					registerByteCodeMetrics(aCoupl);
				}

				// Just look out for first occurrence.
				return;
			}
	}

	/**
	 * Get the whitelist of package names.
	 * 
	 * @return whitelist of package names
	 */
	public static Set<String> getWhiteList() {
		return whiteList;
	}

	/**
	 * Add a package name that should be spectated.
	 * 
	 * @param candidate package name
	 */
	public static void addToWhiteList(final String candidate) {
		whiteList.add(candidate);
	}

	/**
	 * Add a collection of package name that should be spectated.
	 * 
	 * @param candidates collection of package name
	 */
	public static void addAllToWhiteList(final Collection<String> candidates) {
		whiteList.addAll(candidates);
	}

	/**
	 * Check if the package name is in whitelist.
	 * 
	 * @param candidate package name
	 * @return {@code true}, if package name is in white list otherwise
	 *         {@code false}
	 */
	public static boolean isInWhiteList(final String candidate) {
		return whiteList.contains(candidate);
	}

	/**
	 * Get all of the sourcecode metrics that were specified.
	 * 
	 * @return all sourcecode metrics.
	 */
	public static List<ACoupling> getSourceCodeMetrics() {
		return sourceCodeMetrics;
	}

	/**
	 * Get all of the bytecode metrics that were specified.
	 * 
	 * @return all bytecode metrics.
	 */
	public static List<ACoupling> getBytecodeCodeMetrics() {
		return byteCodeMetrics;
	}

	/**
	 * Register sourcecode metric that should be used.
	 * 
	 * @param couples a metric
	 */
	public static void registerSourceCodeMetrics(final ACoupling... couples) {
		sourceCodeInfo.addAll(couples);
		Collections.addAll(sourceCodeMetrics, couples);
	}

	/**
	 * Register bytecode metric that should be used.
	 * 
	 * @param couples a metric
	 */
	public static void registerByteCodeMetrics(final ACoupling... couples) {
		byteCodeInfo.addAll(couples);
		Collections.addAll(byteCodeMetrics, couples);
	}

	/**
	 * Remove metrics from bytecode.
	 * 
	 * @param couples
	 */
	public static void removeByteCodeMetrics(final ACoupling... couples) {
		byteCodeInfo.removeAll(couples);
		byteCodeMetrics.removeAll(Arrays.asList(couples));
	}

	/**
	 * Remove metrics from sourcecode.
	 * 
	 * @param couples
	 */
	public static void removeSourceCodeMetrics(final ACoupling... couples) {
		sourceCodeInfo.removeAll(couples);
		sourceCodeMetrics.removeAll(Arrays.asList(couples));
	}

	/**
	 * Reading from a config file in yaml format.
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static void readYamlFile() {

		sourceCodeInfo = new MetaInfo("SOURCECODE");
		byteCodeInfo = new MetaInfo("BYTECODE");

		final Yaml yaml = new Yaml(new SafeConstructor());

		try (final Reader yamlFile = new FileReader("./testConf.yaml")) {

			final Map<String, Object> yamlMaps = (Map<String, Object>) yaml.load(yamlFile);
			final boolean isMeasureSoureCode = (boolean) yamlMaps.get("measureSourceCode");
			final boolean isMeasureByteCode = (boolean) yamlMaps.get("measureByteCode");

			isConsolePrinting = (boolean) yamlMaps.get("isConsolePrinting");
			DirectoryTool.setOutputRoot((String) yamlMaps.get("outputRoot"));
			DirectoryTool.setExternalLibPath((String) yamlMaps.get("externalLibary"));
			DirectoryTool.setSourceCodeInputRoot((String) yamlMaps.get("sourceCodeInputRoot"));
			DirectoryTool.setByteCodeInputRoot((String) yamlMaps.get("byteCodeInputRoot"));

			logger = Logger.getInstance();

			if (isMeasureSoureCode) {

				final Map<String, Boolean> configs = (Map<String, Boolean>) yamlMaps.get("sourceCodeMetrics");

				for (final Entry<String, Boolean> entry : configs.entrySet())
					if (entry.getValue()) {
						final ACoupling aCoupl = nameTagToCouplingMetric(entry.getKey());
						sourceCodeMetrics.add(aCoupl);
					}
			}

			if (isMeasureByteCode) {

				final Map<String, Boolean> configs = (Map<String, Boolean>) yamlMaps.get("byteCodeMetrics");
				for (final Entry<String, Boolean> entry : configs.entrySet())
					if (entry.getValue()) {
						byteCodeMetrics.add(nameTagToCouplingMetric(entry.getKey()));
					}
			}
		} catch (final FileNotFoundException e) {
			throw new IllegalStateException("Config file was not found!\n" + e.getMessage());
		} catch (final Exception e) {
			throw new IllegalStateException("Config file has wrong format!");
		}
	}

	/**
	 * Maps the given String coupling Tag to the specific metric.
	 * 
	 * @param couplingTag
	 * @throws IllegalStateException if there is no metric for the given tag.
	 * @return The specific metric for the given name tag.
	 */
	private static ACoupling nameTagToCouplingMetric(final String couplingTag) {

		final CouplingTag tag = CouplingTag.valueOf(couplingTag);
		return nameTagToCouplingMetric(tag);
	}

	/**
	 * Maps the given String {@code couplingTag} to the specific metric.
	 * 
	 * @param couplingTag
	 * @return The specific metric for the given name tag.
	 */
	private static ACoupling nameTagToCouplingMetric(final CouplingTag couplingTag) {

		switch (couplingTag) {
		case METHOD_TO_METHOD:
			return new MethodToMethodCoupling();
		case INHERITANCE:
			return new InheritanceCoupling();
		case IMPORT:
			return new ImportCoupling();
		case PACKAGE:
			return new PackageCoupling();
		case FIELD:
			return new FieldCoupling();
		case STRUCTUAL_DEBT_INDEX:
			return new StructalDebtIndex();
		default:
			throw new IllegalStateException("No metrics found for: " + couplingTag);
		}
	}

	/**
	 * Gets {@link MetaInfo} of sorcecode.
	 * 
	 * @return sourceCodeInfo
	 */
	public static MetaInfo getSourceCodeInfo() {
		return sourceCodeInfo;
	}

	/**
	 * Gets {@link MetaInfo} of bytecode.
	 * 
	 * @return byteCodeInfo
	 */
	public static MetaInfo getByteCodeInfo() {
		return byteCodeInfo;
	}

	public static void clearWhiteList() {
		whiteList.clear();
	}

	/**
	 * Finds the metric used for sourcecode by given tag.
	 * 
	 * @param tag
	 * @return Optional.empty, if there is no coupling metric found.
	 */
	public static Optional<ACoupling> findCouplingbyTagS(final CouplingTag tag) {

		for (final ACoupling aCoupling : sourceCodeMetrics) {
			if (aCoupling.getNameTag() == tag)
				return Optional.of(aCoupling);
		}
		return Optional.empty();
	}

	/**
	 * Finds the metric used for bytecode by given tag.
	 * 
	 * @param tag
	 * @return Optional.empty, if there is no coupling metric found.
	 */
	public static Optional<ACoupling> findCouplingbyTagB(final CouplingTag tag) {

		for (final ACoupling aCoupling : byteCodeMetrics) {
			if (aCoupling.getNameTag() == tag)
				return Optional.of(aCoupling);
		}
		return Optional.empty();
	}
}
