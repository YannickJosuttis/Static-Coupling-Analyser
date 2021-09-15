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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.cau.monitor.metrics.ACoupling;

/**
 * This class is a container for meta data.
 *
 */
public class MetaInfo {

	private final Map<ACoupling, Info> couplingTagToInfo;
	private final String name;
	List<String> errorMsgs;

	public MetaInfo(final String name) {
		this.name = name;
		couplingTagToInfo = new HashMap<>();
		errorMsgs = new LinkedList<>();
	}

	/**
	 * Clears the containing data.
	 */
	public void clearData() {
		for (final ACoupling aCoupl : couplingTagToInfo.keySet()) {
			couplingTagToInfo.get(aCoupl).clear();
			errorMsgs.clear();
		}
	}

	/**
	 * Adding new metrics.
	 * 
	 * @param aCouplings
	 */
	public void addAll(final ACoupling... aCouplings) {
		for (final ACoupling aCoupl : aCouplings) {
			couplingTagToInfo.put(aCoupl, new Info());
		}
	}

	/**
	 * Remove metrics.
	 * 
	 * @param aCouplings
	 */
	public void removeAll(final ACoupling... aCouplings) {
		for (final ACoupling aCoupl : aCouplings) {
			couplingTagToInfo.remove(aCoupl);
		}
	}

	/**
	 * Resmove single metric.
	 * 
	 * @param aCoupl
	 */
	public void remove(final ACoupling aCoupl) {
		couplingTagToInfo.remove(aCoupl);
	}

	public void countAsResolved(final ACoupling aCoupl) {
		final Info info = getInfoByCoupling(aCoupl);
		info.all++;
		info.resolved++;
	}

	public void countAsError(final ACoupling aCoupl, final String msg) {
		final Info info = getInfoByCoupling(aCoupl);
		errorMsgs.add(aCoupl.getNameTag() + " => " + msg);
		info.all++;
		info.error++;
		// if we do not know the connection we assume it is not part of project
		info.notPartOfProject++;
	}

	public void countAsNotPart(final ACoupling aCoupl) {
		final Info info = getInfoByCoupling(aCoupl);
		info.all++;
		info.resolved++;
		info.notPartOfProject++;
	}

	public void countAsSelfConnection(final ACoupling aCoupl) {
		final Info info = getInfoByCoupling(aCoupl);
		info.all++;
		info.resolved++;
		info.selfConnections++;
	}

	public void countAsFiltered(final ACoupling aCoupl) {
		final Info info = getInfoByCoupling(aCoupl);
		info.all++;
		info.resolved++;
		info.notPartOfProject++;
	}

	public Info getInfoByCoupling(final ACoupling aCoupl) {
		final Info info = couplingTagToInfo.get(aCoupl);

		if (info == null)
			throw new IllegalStateException("No Info for given Tag! Insert Tag first.");
		return info;
	}

	/**
	 * Get an Array of meta data stored for specific metric.
	 * 
	 * @param aCoupl
	 * @return an array of meta data.
	 */
	public int[] getInfoResults(final ACoupling aCoupl) {
		final int[] result = new int[5];
		final Info info = couplingTagToInfo.get(aCoupl);

		result[0] = info.all;
		result[1] = info.resolved;
		result[2] = info.error;
		result[3] = info.all - info.notPartOfProject;
		result[4] = info.selfConnections;

		return result;
	}

	@Override
	public String toString() {
		String str = name + "-INFO:\n\n";

		final List<ACoupling> couplings = couplingTagToInfo.keySet().stream().collect(Collectors.toList());
		Collections.sort(couplings);

		for (final ACoupling aCoupl : couplings) {
			str += "\t" + aCoupl.getNameTag() + ":\n \t" + couplingTagToInfo.get(aCoupl) + "\n\n";
		}
		str += "\nError messages (" + errorMsgs.size() + ") : \n";
		for (final String msg : errorMsgs) {
			str += msg + "\n";
		}
		return str;
	}

	/**
	 * Overrides the meta data from given metric to the other ones.
	 * 
	 * @param from
	 * @param to
	 */
	public void override(final ACoupling from, final ACoupling to) {

		final Info infoFrom = getInfoByCoupling(from);
		final Info infoTo = getInfoByCoupling(to);

		final int all = infoFrom.all;
		final int resolved = infoFrom.resolved;
		final int error = infoFrom.error;
		final int notPartOfProject = infoFrom.notPartOfProject;
		final int selfConnections = infoFrom.selfConnections;

		infoTo.override(all, resolved, error, notPartOfProject, selfConnections);
	}

	/**
	 * Little Container for some information.
	 *
	 */
	private class Info {

		int all = 0;
		int resolved = 0;
		int error = 0;
		int notPartOfProject = 0;
		int selfConnections = 0;

		private double getPercent(final int all, final int part) {

			final double partAsDouble = part;
			final double allAsDouble = all;

			return (double) (int) (partAsDouble / allAsDouble * 10_000) / 100;
		}

		public void override(final int all, final int resolved, final int error, final int notPartOfProject,
				final int selfConnections) {
			this.all = all;
			this.resolved = resolved;
			this.error = error;
			this.notPartOfProject = notPartOfProject;
			this.selfConnections = selfConnections;
		}

		private void clear() {
			all = 0;
			resolved = 0;
			error = 0;
			notPartOfProject = 0;
			selfConnections = 0;
		}

		@Override
		public String toString() {
			return "[all=" + all + ", resolved=" + resolved + " (" + getPercent(all, resolved) + "%), error="
					+ error + " (" + getPercent(all, error) + "%), project_part="
					+ (all - notPartOfProject) + " (" + getPercent(all, all - notPartOfProject) + "%), selfConnections="
					+ selfConnections + " (" + getPercent(all, selfConnections) + "%)]";
		}
	}

	public void add(final ACoupling coupl) {
		couplingTagToInfo.put(coupl, new Info());

	}
}
