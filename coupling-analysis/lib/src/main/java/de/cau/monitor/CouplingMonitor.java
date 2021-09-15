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

package de.cau.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.bcel.classfile.JavaClass;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;

import de.cau.config.Configuration;
import de.cau.monitor.metrics.ACoupling;
import de.cau.monitor.metrics.CouplingTag;
import de.cau.monitor.metrics.PackageCoupling;
import de.cau.monitor.metrics.StructalDebtIndex;
import de.cau.tools.FunctionHelper;
import de.cau.tools.Logger;
import de.cau.tools.MetaInfo;
import de.cau.tools.Tag;

/**
 * This class monitors, calculates and coordinates the various coupling metrics
 * to be counted.
 * 
 * @param <StructalDebtInex>
 *
 */
public class CouplingMonitor {

	private final Set<String> classes;
	private final Logger logger;
	private static CouplingMonitor couplingMonitorInstance;

	/**
	 * Constructor
	 */
	private CouplingMonitor() {
		this.classes = new HashSet<>();
		this.logger = Logger.getInstance();
	}

	/**
	 * Get Singleton instance.
	 * 
	 * @return coupling monitor
	 */
	public static CouplingMonitor getInstance() {
		if (couplingMonitorInstance == null) {
			couplingMonitorInstance = new CouplingMonitor();
		}
		return couplingMonitorInstance;
	}

	public void clearRegisteredClasses() {
		classes.clear();
	}

	/**
	 * Register a class, which should be counted.
	 * 
	 * @param fullClassName
	 * @param predicate
	 */
	public void registerClass(final String fullClassName, final Predicate<? super String> predicate) {

		if (!classes.contains(fullClassName)) {
			classes.add(fullClassName);
		}
	}

	/**
	 * Register a class, which should be counted.
	 * 
	 * @param fullClassName
	 */
	public void registerClass(final String fullClassName) {

		if (!classes.contains(fullClassName)) {
			classes.add(fullClassName);
		}
	}

	/**
	 * Register all given classes, which should be counted.
	 * 
	 * @param fullClassName
	 * @param predicate
	 */
	public void registerAllClasses(final Collection<? extends String> fullClassName,
			final Predicate<? super String> predicate) {
		classes.addAll(fullClassName);
	}

	/**
	 * Register all classes, which should be counted.
	 * 
	 * @param classNames
	 */
	public void registerAllClasses(final Collection<? extends String> classNames) {
		classes.addAll(classNames);
	}

	/**
	 * Get all registers classes.
	 * 
	 * @return all registered classes.
	 */
	public Set<String> getRegisteredClasses() {
		return classes;
	}

	/**
	 * Get all registers classes.
	 * 
	 * @param predicate for filter registered classes.
	 * @return all registered classes, that passed the filter.
	 */
	public Set<String> getRegisteredClasses(final Predicate<String> predicate) {
		return classes
				.stream()
				.filter(x -> predicate.test(x))
				.collect(Collectors.toSet());
	}

	/**
	 * For the given Collection of javaClasses register for all configured bytecode
	 * metrics the coupling.
	 * 
	 * @param javaClassFiles
	 */
	public void registerByteCodeCoupling(final Collection<JavaClass> javaClassFiles) {

		final Optional<ACoupling> oPackage = Configuration.findCouplingbyTagB(CouplingTag.PACKAGE);
		final Optional<ACoupling> oSDI = Configuration.findCouplingbyTagB(CouplingTag.STRUCTUAL_DEBT_INDEX);

		if (oPackage.isPresent() && oSDI.isPresent()) {
			oPackage.get().setCouplingHook();
			((StructalDebtIndex) oSDI.get()).setCoupling((PackageCoupling) oPackage.get());
		} else if (oSDI.isPresent()) {
			final PackageCoupling packACoupling = new PackageCoupling();
			packACoupling.setCouplingHook();
			((StructalDebtIndex) oSDI.get()).setCoupling(packACoupling);
			Configuration.registerByteCodeMetrics(packACoupling);
		}

		final List<ACoupling> metrics = Configuration.getBytecodeCodeMetrics();
		Collections.sort(metrics);

		for (final ACoupling coupl : metrics) {
			logger.log("Calculate " + coupl.getNameTag() + ":");
			for (final JavaClass javaClass : javaClassFiles) {

				if (!javaClass.isInterface()) {
					logger.log("...visiting..." + javaClass.getClassName());
					coupl.calculateCoupling(javaClass);

					if (coupl.getNameTag() == CouplingTag.STRUCTUAL_DEBT_INDEX) {
						break;
					}
				}

			}
		}
	}

	/**
	 * For the given List of {@code CompilationUnits} register for all configured
	 * sourcecode metrics the coupling.
	 * 
	 * @param ocus
	 */
	public void registerSourceCodeCoupling(final List<Optional<CompilationUnit>> ocus) {

		// Compilation units that could not be parsed correctly are filtered.
		final List<CompilationUnit> cus = ocus.stream()
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());

		logger.logSeparation();

		final Optional<ACoupling> oPackage = Configuration.findCouplingbyTagS(CouplingTag.PACKAGE);
		final Optional<ACoupling> oSDI = Configuration.findCouplingbyTagS(CouplingTag.STRUCTUAL_DEBT_INDEX);

		// if we want to calculate the SDI, we also need the package coupling for this
		// job.
		if (oPackage.isPresent() && oSDI.isPresent()) {
			oPackage.get().setCouplingHook();
			((StructalDebtIndex) oSDI.get()).setCoupling((PackageCoupling) oPackage.get());
		} else if (oSDI.isPresent()) {
			final PackageCoupling packACoupling = new PackageCoupling();
			packACoupling.setCouplingHook();
			((StructalDebtIndex) oSDI.get()).setCoupling(packACoupling);
			Configuration.registerSourceCodeMetrics(packACoupling);
		}

		// Order is important.
		final List<ACoupling> metrics = Configuration.getSourceCodeMetrics();
		Collections.sort(metrics);

		for (final ACoupling coupl : metrics) {
			logger.log("Calculate " + coupl.getNameTag() + ":");

			for (final CompilationUnit cu : cus) {

				final List<ClassOrInterfaceDeclaration> coi = cu.findAll(ClassOrInterfaceDeclaration.class);

				final List<String> fullClassNames = getFullClassNameByCompilationUnit(cu);
				for (final Entry<ClassOrInterfaceDeclaration, String> entry : FunctionHelper.zip(coi,
						fullClassNames)) {

					if (!entry.getKey().isInterface()) {
						logger.log("...visiting..." + entry.getValue());
						coupl.calculateCoupling(entry.getKey(), entry.getValue());

						if (coupl.getNameTag() == CouplingTag.STRUCTUAL_DEBT_INDEX) {
							break;
						}
					}

				}
			}
			logger.logSeparation();
		}
	}

	/**
	 * Get all full class names or qualified names (packagename + classname)
	 * 
	 * @param cu
	 * @return list of all full calss names
	 */
	private List<String> getFullClassNameByCompilationUnit(final CompilationUnit cu) {

		final Optional<PackageDeclaration> oPack = cu.getPackageDeclaration();
		final String pack = oPack.isPresent() ? oPack.get().getNameAsString() + "." : "";
		final List<ClassOrInterfaceDeclaration> coiList = cu.findAll(ClassOrInterfaceDeclaration.class);
		final List<String> fullClassNames = new ArrayList<>();

		for (final ClassOrInterfaceDeclaration coi : coiList) {

			// If it is a nested class separate with a $ like the byte code does.
			final String fullClassName = fullClassNames.isEmpty() ? pack + coi.getNameAsString()
					: fullClassNames.get(0) + "$" + coi.getNameAsString();
			fullClassNames.add(fullClassName);
		}

		// ...Not a class or interface declaration...

		final Optional<AnnotationDeclaration> oad = cu.findFirst(AnnotationDeclaration.class);

		if (oad.isPresent()) {
			final String fullClassName = pack + oad.get().getNameAsString();
			fullClassNames.add(fullClassName);
		}

		final Optional<EnumDeclaration> oed = cu.findFirst(EnumDeclaration.class);
		if (oed.isPresent()) {
			// If it is a nested class separate with a $ like the byte code does.
			final String fullClassName = fullClassNames.isEmpty() ? pack + oed.get().getNameAsString()
					: fullClassNames.get(0) + "$" + oed.get().getNameAsString();

			fullClassNames.add(fullClassName);
		}
		return fullClassNames;
	}

	/**
	 * Prints all found couplings for source and bytecode.
	 */
	public void printAllCouplings() {

		for (final ACoupling coupl : Configuration.getSourceCodeMetrics()) {
			logger.log("SOURCECODE-" + coupl.getNameTag() + ":");
			logger.log(coupl.getRegisteredCouplings());
			logger.log("");
		}
		for (final ACoupling coupl : Configuration.getBytecodeCodeMetrics()) {
			logger.log("BYTECODE-" + coupl.getNameTag() + ":");
			logger.log(coupl.getRegisteredCouplings());
			logger.log("");
		}
	}

	/**
	 * Get all the registered sourcecode couplings.
	 * 
	 * @return all registered sourcecode couplings
	 */
	public List<Map<String, Map<String, Integer>>> getRegisteredSorceCodeCouplings() {
		return Configuration.getSourceCodeMetrics()
				.stream()
				.map(ACoupling::getRegisteredCouplings)
				.collect(Collectors.toList());
	}

	/**
	 * Get all the registered bytecode couplings.
	 * 
	 * @return all registered bytecode couplings
	 */
	public List<Map<String, Map<String, Integer>>> getRegisteredByteCodeCouplings() {
		return Configuration.getBytecodeCodeMetrics()
				.stream()
				.map(ACoupling::getRegisteredCouplings)
				.collect(Collectors.toList());
	}

	/**
	 * Read information from cache instead of calculate them.
	 * 
	 * @param metrics
	 * @param packageCoupl
	 */
	@Deprecated
	private void readFromCache(final List<ACoupling> metrics, final ACoupling packageCoupl) {

		logger.log("...READ FROM " + CouplingTag.METHOD_TO_METHOD + "'s CACHE...");
		final MetaInfo sourceCodeInfo = Configuration.getSourceCodeInfo();

		for (final ACoupling cacheSource : metrics) {
			if (cacheSource.getNameTag().equals(CouplingTag.METHOD_TO_METHOD)) {

				final Map<String, Map<String, Integer>> cSMap = cacheSource.getRegisteredCouplings();

				for (final String key : cSMap.keySet()) {
					final String newKey = ACoupling.getPackageNameFromString(key);
					for (final String innerkey : cSMap.get(key).keySet()) {
						final String newInnerKey = ACoupling.getPackageNameFromString(innerkey);
						packageCoupl.addFromCache(newKey, newInnerKey, Tag.CACHE);

					}
				}
				Configuration.getSourceCodeInfo().override(cacheSource, packageCoupl);
				break;
			}
		}
	}
}
