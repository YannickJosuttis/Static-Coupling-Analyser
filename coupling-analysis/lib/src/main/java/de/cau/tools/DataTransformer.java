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

package de.cau.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.cau.config.DirectoryTool;
import de.cau.monitor.metrics.ACoupling;
import de.cau.monitor.metrics.CouplingTag;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class creates csv-files for each metric and prepares the data for the
 * gui.
 *
 */
public class DataTransformer {

	Logger logger;

	String path;

	public DataTransformer() {
		path = DirectoryTool.getOutputRoot() + "csv-files" + DirectoryTool.backslash;
		DirectoryTool.createDirectory(path);
		this.logger = Logger.getInstance();
	}

	/**
	 * Creates a csv-file for a metric with the coupling numbers from all classes to
	 * the coupled one.
	 * 
	 * @param fileName      Name of the metric
	 * @param mapmap
	 * @param allClassNames
	 */
	public void mapToCSV(final String fileName, final Map<String, Map<String, Integer>> mapmap,
			final Set<String> allClassNames) {

		final String pathAndFilename = path + fileName + ".csv";

		if (logger != null) {
			logger.log("Writing CSV file: " + pathAndFilename);
		}

		try (final FileWriter writer = new FileWriter(pathAndFilename)) {

			// first cell empty
			writer.write(",");

			final List<String> sortedKeys = sortKeys(allClassNames);

			// labeling of columns in the first row
			fillFirstRow(writer, sortedKeys);
			writer.write("sum_out\n");

			final int[] sumIn = new int[sortedKeys.size()];

			// row labeling
			for (final String key : sortedKeys) {
				int sumOut = 0;
				int index = 0;
				writer.write(key + ",");
				final StringBuilder sb = new StringBuilder();
				// entry
				for (final String innerKey : sortedKeys) {
					if (mapmap.containsKey(key)) {
						// if there is no Coupling in a class than val=0 for this class
						final int val = mapmap.get(key).containsKey(innerKey) ? mapmap.get(key).get(innerKey) : 0;
						sb.append(val + ",");
						// sums the coupling from a class
						sumOut += val;
						sumIn[index++] += val;
					} else {
						sb.append(0 + ",");
					}
				}
				writer.write(sb.toString() + sumOut + "\n");
			}
			// sums how many times a class is coupled.
			writer.write("sum_in,");
			for (int i = 0; i < sumIn.length; i++) {
				writer.write(String.valueOf(sumIn[i]) + ",");
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * creates a csv-file for a metric with the coupling of one class.
	 * 
	 * @param fileName      Name of metric
	 * @param mapmap
	 * @param className
	 * @param allClassNames
	 */
	public void classToCSV(final String fileName, final Map<String, Map<String, Integer>> mapmap,
			final String className, final Set<String> allClassNames) {

		try (final FileWriter writer = new FileWriter(fileName + ".csv")) {

			// first cell empty
			writer.write(",");

			final List<String> sortedKeys = sortKeys(allClassNames);

			// labeling of columns in the first row
			fillFirstRow(writer, sortedKeys);
			writer.write("sum_out\n" + className + ",");

			final StringBuilder sb = new StringBuilder();
			int sumOut = 0;
			for (final String key : sortedKeys) {
				if (mapmap.containsKey(className)) {
					final int val = mapmap.get(className).containsKey(key) ? mapmap.get(className).get(key) : 0;
					sb.append(val + ",");
					sumOut += val;
				} else {
					sb.append(0 + ",");
				}
			}
			writer.write(sb.toString() + sumOut);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sums the coupling number for each class.
	 * 
	 * @param mapmap
	 * @param allClassNames
	 * @return
	 */
	private static Map<String, Integer> sumMetric(final Map<String, Map<String, Integer>> mapmap,
			final Set<String> allClassNames) {
		final List<String> sortedKeys = sortKeys(allClassNames);

		final Map<String, Integer> classSums = new HashMap<>();

		for (final String key : sortedKeys) {
			int sum = 0;

			for (final String innerKey : sortedKeys) {
				if (mapmap.containsKey(key)) {
					// if there is no Coupling in a class val=0 for this class
					final int val = mapmap.get(key).containsKey(innerKey) ? mapmap.get(key).get(innerKey) : 0;
					sum += val;
				}
			}
			classSums.put(key, sum);
		}

		return classSums;
	}

	/**
	 * Creates a csv-file with the coupling number for each metric of all classes.
	 * 
	 * @param fileName
	 * @param allMetrics
	 * @param allClassNames
	 */
	public void createClassMetricTable(final String fileName, final List<ACoupling> allMetrics,
			final Set<String> allClassNames) {

		Map<String, Integer> metricSum = new HashMap<>();
		final List<Map<String, Integer>> metricSums = new ArrayList<>();
		final String pathAndFilename = path + fileName + "allMetrics" + ".csv";

		// remove metric PACKAGE
		final List<ACoupling> metrics = removeMetricPackage(allMetrics);

		try (final FileWriter writer = new FileWriter(pathAndFilename)) {

			// first line
			writer.write(",");
			for (final ACoupling key : metrics) {
				writer.write(key + ",");
			}
			writer.write("\n");

			for (int i = 0; i < metrics.size(); i++) {
				metricSum = sumMetric(metrics.get(i).getRegisteredCouplings(), allClassNames);
				// List with one Map for each Metric
				// each Map contains key= class and value= sum of Couplings
				metricSums.add(metricSum);
			}

			for (final String clazz : allClassNames) {
				writer.write(clazz + ",");
				for (final Map<String, Integer> map : metricSums) {
					writer.write(map.get(clazz) + ",");
				}
				writer.write("\n");
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sorts the classes alphabetically.
	 * 
	 * @param allClassNames
	 * @return
	 */
	private static List<String> sortKeys(final Set<String> allClassNames) {
		final List<String> keys = new ArrayList<>();
		allClassNames.stream().forEach(keys::add);
		Collections.sort(keys);
		return keys;
	}

	/**
	 * Fills the first row in a file.
	 * 
	 * @param <T>
	 * @param writer
	 * @param keys
	 */
	private static <T> void fillFirstRow(final FileWriter writer, final List<T> keys) {
		for (final T key : keys) {
			try {
				writer.write(key + ",");
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Removes the metric PACKAGE from a list.
	 * 
	 * @param allMetrics
	 * @return
	 */
	private static List<ACoupling> removeMetricPackage(final List<ACoupling> allMetrics) {
		final List<ACoupling> metrics = new ArrayList<>();
		for (int i = 0; i < allMetrics.size(); i++) {
			if (allMetrics.get(i).getNameTag() != CouplingTag.PACKAGE) {
				metrics.add(allMetrics.get(i));
			}
		}
		return metrics;
	}

	/**
	 * Creates an observalbe list with the coupling of all classes in all metrics.
	 * This is for the gui.
	 * 
	 * @param allMetrics
	 * @param allClassNames
	 * @param isPackage
	 * @return
	 */
	public static ObservableList<MetricResult> mapToObservableList(final List<ACoupling> allMetrics,
			final Set<String> allClassNames, final boolean isPackage) {

		if (!isPackage) {

			// remove Metric PACKAGE
			final List<ACoupling> metrics = removeMetricPackage(allMetrics);

			// new List for sorting the maps
			List<Map<String, Map<String, Integer>>> sortedMaps = new ArrayList<>();

			// sort maps by metrics, so that it is in same order as MetricResult
			sortedMaps = sortMetrics(sortedMaps, metrics);

			Map<String, Integer> metricSum = new HashMap<>();
			final List<Map<String, Integer>> metricSums = new ArrayList<>();

			for (int i = 0; i < sortedMaps.size(); i++) {
				metricSum = sumMetric(sortedMaps.get(i), allClassNames);
				// List with one Map for each Metric
				// each Map conatins key= class and value= sum of Couplings
				metricSums.add(metricSum);
			}

			final ObservableList<MetricResult> results = FXCollections.observableArrayList();
			for (final String clazz : allClassNames) {
				results.add(new MetricResult(clazz, metricSums.get(0).get(clazz), metricSums.get(1).get(clazz),
						metricSums.get(2).get(clazz), metricSums.get(3).get(clazz), null));
			}

			return results;
		}

		// ---------- only Package Coupling ----------
		ACoupling packageCoupl = null;
		for (int i = 0; i < allMetrics.size(); i++) {
			if (allMetrics.get(i).getNameTag() == CouplingTag.PACKAGE) {
				packageCoupl = allMetrics.get(i);
				break;
			}
		}

		final ObservableList<MetricResult> results = FXCollections.observableArrayList();
		if (packageCoupl == null)
			return results;

		final Set<String> allPackageNames = packageCoupl.storeVisitor;
		Map<String, Integer> packageSum = new HashMap<>();
		packageSum = sumMetric(packageCoupl.getRegisteredCouplings(), allPackageNames);

		for (final String pck : allPackageNames) {
			results.add(new MetricResult(pck, null, null, null, null, packageSum.get(pck)));
		}

		return results;

	}

	/**
	 * Creates an observalbe List with the coupling of one class or package.
	 * @param allMetrics
	 * @param allClassNames
	 * @param className (or packageName in case of a package)
	 * @param isPackage (in case of package coupling it is a different table)
	 * @return
	 */
	public static ObservableList<MetricResult> classToObservableList(final List<ACoupling> allMetrics,
			final Set<String> allClassNames, final String className, final boolean isPackage) {

		if (!isPackage) {

			// remove Metric PACKAGE
			final List<ACoupling> metrics = removeMetricPackage(allMetrics);

			// new List for sorting the maps
			List<Map<String, Map<String, Integer>>> sortedMaps = new ArrayList<>();

			// sort maps by metrics, so that it is in same order as MetricResult
			sortedMaps = sortMetrics(sortedMaps, metrics);
			Map<String, Integer> classResult = new HashMap<>();
			final List<Map<String, Integer>> metricResults = new ArrayList<>();

			for (int i = 0; i < sortedMaps.size(); i++) {
				classResult = sortedMaps.get(i).get(className);
				metricResults.add(classResult);
			}

			final ObservableList<MetricResult> results = FXCollections.observableArrayList();
			for (final String clazz : allClassNames) {
				final int m2m = isNull(metricResults, 0) ? 0 : metricResults.get(0).getOrDefault(clazz, 0);
				final int imprt = isNull(metricResults, 1) ? 0 : metricResults.get(1).getOrDefault(clazz, 0);
				final int field = isNull(metricResults, 2) ? 0 : metricResults.get(2).getOrDefault(clazz, 0);
				final int inher = isNull(metricResults, 3) ? 0 : metricResults.get(3).getOrDefault(clazz, 0);

				if (!(m2m == 0 && imprt == 0 && field == 0 && inher == 0)) {
					results.add(new MetricResult(clazz, m2m, imprt, field, inher, null));
				}
			}

			return results;
		}

		// ---------- only Package Coupling ----------
		ACoupling packageCoupl = null;
		for (int i = 0; i < allMetrics.size(); i++) {
			if (allMetrics.get(i).getNameTag() == CouplingTag.PACKAGE) {
				packageCoupl = allMetrics.get(i);
				break;
			}
		}

		final ObservableList<MetricResult> results = FXCollections.observableArrayList();
		if (packageCoupl == null)
			return results;

		Map<String, Integer> packageResult = new HashMap<>();

		packageResult = packageCoupl.getRegisteredCouplings().get(className);
		if (!isNull(packageResult))
			for (String pckName : packageResult.keySet()) {
				final int pckage = packageResult.get(pckName);
				System.out.println(pckage);
				results.add(new MetricResult(pckName, null, null, null, null, pckage));
			}

		return results;
	}

	private static boolean isNull(final List<Map<String, Integer>> metricResults, final int i) {
		if (metricResults.get(i) == null)
			return true;
		return false;

	}

	private static boolean isNull(final Map<String, Integer> packageResult) {
		if (packageResult == null)
			return true;
		return false;
	}

	/**
	 * Sorts the list of metrics compatible with the maps with coupling.
	 * @param sortedMaps
	 * @param metrics
	 * @return
	 */
	private static List<Map<String, Map<String, Integer>>> sortMetrics(
			final List<Map<String, Map<String, Integer>>> sortedMaps, final List<ACoupling> metrics) {

		for (int i = 0; i < 4; i++) {
			sortedMaps.add(new HashMap<>());
		}

		for (int i = 0; i < metrics.size(); i++) {

			switch (metrics.get(i).getNameTag()) {
			case METHOD_TO_METHOD:
				sortedMaps.set(0, metrics.get(i).getRegisteredCouplings());
				break;
			case IMPORT:
				sortedMaps.set(1, metrics.get(i).getRegisteredCouplings());
				break;
			case FIELD:
				sortedMaps.set(2, metrics.get(i).getRegisteredCouplings());
				break;
			case INHERITANCE:
				sortedMaps.set(3, metrics.get(i).getRegisteredCouplings());
				break;

			default:
				break;
			}
		}
		return sortedMaps;
	}

	public static class MetricResult {

		public String name;
		public Integer m2m;
		public Integer imprt;
		public Integer field;
		public Integer inheritance;
		public Integer pckage;

		public MetricResult(final String name, final Integer m2m, final Integer imprt, final Integer field,
				final Integer inheritance, final Integer pckage) {
			this.name = name;
			this.m2m = m2m;
			this.imprt = imprt;
			this.field = field;
			this.inheritance = inheritance;
			this.pckage = pckage;
		}

		@Override
		public String toString() {
			return "MetricResult [name=" + name + ", m2m=" + m2m + ", imprt=" + imprt + ", field=" + field
					+ ", inheritance=" + inheritance + "]";
		}

		public String getName() {
			return name;
		}

		public Integer getM2m() {
			return m2m;
		}

		public Integer getImprt() {
			return imprt;
		}

		public Integer getField() {
			return field;
		}

		public Integer getInheritance() {
			return inheritance;
		}

		public Integer getPckage() {
			return pckage;
		}
	}

}
