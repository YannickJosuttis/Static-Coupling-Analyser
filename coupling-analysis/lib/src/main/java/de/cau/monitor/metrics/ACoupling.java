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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.Type;

import de.cau.config.Configuration;
import de.cau.monitor.CouplingMonitor;
import de.cau.tools.Logger;
import de.cau.tools.MetaInfo;
import de.cau.tools.Tag;

/**
 * An abstract representation of a coupling metric and helper functions.
 */
public abstract class ACoupling implements ICoupling, Comparable<ACoupling> {

	protected Logger logger;
	private final CouplingTag nameTag;
	protected final MetaInfo sourceCodeInfo;
	protected final MetaInfo byteCodeInfo;
	public Set<String> storeVisitor;

	private final Map<String, Map<String, Integer>> couplings;

	public ACoupling(final CouplingTag nameTag) {
		this.couplings = new HashMap<>();
		this.logger = Logger.getInstance();
		this.nameTag = nameTag;
		this.sourceCodeInfo = Configuration.getSourceCodeInfo();
		this.byteCodeInfo = Configuration.getByteCodeInfo();
	}

	/**
	 * Add sourcecode coupling.
	 * 
	 * @param from class from
	 * @param to   class to
	 * @param tag  {@link CouplingTag}
	 */
	protected void addSourceCodeCoupling(final String from, final String to, final Tag tag) {
		addCoupling(from, to, sourceCodeInfo, tag, concatWithArrow(from, to));
	}

	/**
	 * Add bytecode coupling.
	 * 
	 * @param from class from
	 * @param to   class to
	 * @param tag  {@link CouplingTag}
	 */
	protected void addByteCodeCoupling(final String from, final String to, final Tag tag) {
		addCoupling(from, to, byteCodeInfo, tag, concatWithArrow(from, to));
	}

	/**
	 * Add Coupling if the filter is past. Additional informations are logged for
	 * specific CouplingTag.
	 * 
	 * @param from
	 * @param to
	 * @param metaInfo
	 * @param tag
	 * @param msg
	 */
	private void addCoupling(String from, String to, final MetaInfo metaInfo, final Tag tag, final String msg) {

		from = mapNestedClassToOuter(from);
		to = mapNestedClassToOuter(to);

		// We only want to store necessary connections.
		if (isFiltered(from, to, tag)) {

			if (tag == Tag.RESOLVED) {
				if (from.equals(to)) {
					metaInfo.countAsSelfConnection(this);
				} else {
					logger.logByTag(msg, tag, metaInfo, this);
					addCoupling(from, to);
				}
			} else {
				logger.logByTag(msg, tag, metaInfo, this);
			}
		} else {
			logger.logFilteredOut(concatWithArrow(from, to));
			metaInfo.countAsFiltered(this);
		}
	}

	/**
	 * Adds coupling and counts its appearance.
	 * 
	 * @param from
	 * @param to
	 */
	private void addCoupling(final String from, final String to) {

		if (!couplings.containsKey(from)) {
			couplings.put(from, new HashMap<String, Integer>());
		}

		final Map<String, Integer> connectionCounts = couplings.get(from);

		if (!connectionCounts.containsKey(to)) {
			connectionCounts.put(to, 1);
		} else {
			int count = connectionCounts.get(to);
			connectionCounts.put(to, ++count);
		}

		if (storeVisitor != null) {
			storeVisitor.add(from);
			storeVisitor.add(to);
		}
	}

	/**
	 * All couplings counted will be removed.
	 */
	public void clearCouplings() {
		couplings.clear();
	}

	/**
	 * Gets the package name from a given String by using the Java naming
	 * convention.
	 * 
	 * @param className qualified name of a class.
	 * @return the package name
	 */
	public static String getPackageNameFromString(final String className) {
		final int index = findFirstUpperCase(className);
		return index > 0 ? className.substring(0, index - 1) : "";
	}

	/**
	 * Concatenate the string representation of the given objects with an arrow.
	 * 
	 * @param fst object
	 * @param snd object
	 * @return a String with an Arrow concatenation.
	 */
	protected String concatWithArrow(final Object fst, final Object snd) {
		return fst + " --> " + snd;
	}

	protected String mapNestedClassToOuter(final String classname) {
		// Map nested class to outer.
		final int pos = classname.indexOf('$');
		if (pos != -1)
			return classname.substring(0, pos);
		return classname;
	}

	/**
	 * Check if one of these classes or packages is in filter.
	 * 
	 * @param from class or package name
	 * @param to   class or package name
	 * @return {code true} if passed the filter, otherwise {@code false}
	 */
	public static boolean isFiltered(final String from, final String to, final Tag tag) {

		if (tag == Tag.CACHE)
			return true;

		return isFiltered(from, tag) && isFiltered(to, tag);
	}

	/**
	 * Check whether the package or the class name should be filtered therefore
	 * using the predefined white list.
	 * 
	 * @param fullClassNameOrPackage
	 * @param tag
	 * @return {code true} if passed the filter, otherwise {@code false}
	 */
	public static boolean isFiltered(final String fullClassNameOrPackage, final Tag tag) {

		if (tag == Tag.NOT_PROJECT_PART)
			return false;
		// Is package we want to count.
		if (Configuration.isInWhiteList(fullClassNameOrPackage))
			return true;

		final String packageName = getPackageNameFromString(fullClassNameOrPackage);

		final String className = fullClassNameOrPackage.substring(packageName.length(),
				fullClassNameOrPackage.length());

		return Configuration.isInWhiteList(packageName)
				|| CouplingMonitor.getInstance().getRegisteredClasses().contains(className);
	}

	@Deprecated
	public void addFromCache(String from, String to, final Tag tag) {

		from = mapNestedClassToOuter(from);
		to = mapNestedClassToOuter(to);

		// We only want to store necessary connections.
		if (isFiltered(from, to, tag)) {
			addCoupling(from, to);
		} else {
			logger.logFilteredOut(concatWithArrow(from, to));
		}
	}

	/**
	 * Check if class is a nested one, by checking if the class name contains '$'.
	 * (This is based on Java's bytecode representation)
	 * 
	 * @param className qualified class name
	 * @return {@code true} if the class is nested, otherwise {@code false}
	 */
	public boolean isNested(final String className) {
		return className.contains("$");
	}

	/**
	 * Parse the name in 'path notation' into java package notation (e.g.
	 * a/b/CalsssName to e.g. a.b.CalsssName)
	 * 
	 * @param nameInPathNotation
	 * @return the package notation
	 */
	public String toPackageNotation(final String nameInPathNotation) {
		return nameInPathNotation.replace('/', '.');
	}

	/**
	 * Get the counted coupling.
	 * 
	 * @return couplings
	 */
	public Map<String, Map<String, Integer>> getRegisteredCouplings() {
		return couplings;
	}

	/**
	 * Get the name tag of the coupling
	 * 
	 * @return {@link CouplingTag}
	 */
	public CouplingTag getNameTag() {
		return this.nameTag;
	}

	@Override
	public String toString() {
		return nameTag.toString();
	}

	/**
	 * Compares with coupling tag.
	 */
	@Override
	public int compareTo(final ACoupling o) {
		return Integer.compare(this.getNameTag().getPriority(), o.getNameTag().getPriority());
	}

	/**
	 * Finds the first upperCase in given String.
	 * 
	 * @param str
	 * @return index of first upper case character, otherwise {@literal -1}
	 */
	protected static int findFirstUpperCase(final String str) {
		for (int i = 0; i < str.length(); i++) {
			if (Character.isUpperCase(str.charAt(i)))
				return i;
		}
		return -1;
	}

	/**
	 * Translates a qualified class name with nested class names in java Notation
	 * into nested class format. (a.b.ClassA.ClassB to a.b.ClassA$ClassB). This is
	 * inspired from java byte code.
	 * 
	 * @param fullname
	 * @return a String representing the nested class separated wit '$'
	 */
	protected String toNestedClassFormat(String fullname) {

		final int index = findFirstUpperCase(fullname);

		// Check for nested classes
		if (index >= 0) {
			for (int i = index; i < fullname.length(); i++) {

				// There is at least one nested class
				if (fullname.charAt(i) == '.') {
					fullname = fullname.substring(0, index)
							+ fullname.substring(index, fullname.length()).replace(".", "$");
				}
			}
		}
		return fullname;
	}

	/**
	 * If hook is set it stores all coupling sources and destinations.
	 */
	public void setCouplingHook() {
		storeVisitor = new HashSet<>();
	}

	/**
	 * 
	 * Creating an error message based on given parameter.
	 * 
	 * @param classFrom
	 * @param classTo
	 * @param e
	 * @return an error meassage
	 */
	protected String getErrorMsg(final String classFrom, final String classTo, final Throwable e) {
		return concatWithArrow("[class: " + classFrom + ", name: " + classTo,
				"unknown" + ", error type: " + e.getClass().getName() + "]");
	}

	/**
	 * Creating an error message based on given parameter.
	 * 
	 * @param classFrom
	 * @param node
	 * @param e
	 * @return an error meassage
	 */
	protected String getErrorMsg(final String classFrom, final Node node, final Throwable e) {

		String name = null;

		if (node instanceof MethodCallExpr) {
			name = ((MethodCallExpr) node).getNameAsString();
		} else if (node instanceof Type) {
			name = ((Type) node).asString();
		} else
			throw new NotImplementedException();

		final String position = node.getBegin().get().toString();
		return concatWithArrow("[class: " + classFrom + ", name: " + name,
				position + ", error type: " + e.getClass().getName() + "]");
	}
}
