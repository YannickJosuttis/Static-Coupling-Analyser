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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.bcel.classfile.JavaClass;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 * This class is used for calculating the SDI.
 *
 */
public class StructalDebtIndex extends ACoupling {

	private PackageCoupling packACoupling;

	public StructalDebtIndex() {
		super(CouplingTag.STRUCTUAL_DEBT_INDEX);
	}

	public void setCoupling(final PackageCoupling packACoupling) {
		this.packACoupling = packACoupling;
	}

	@Override
	public void calculateCoupling(final ClassOrInterfaceDeclaration __, final String ___) {

		final Map<String, Map<String, Integer>> mapmap = packACoupling.getRegisteredCouplings();
		final List<Circle> cycles = findAllCircles(mapmap, packACoupling.storeVisitor);

		sourceCodeInfo.override(packACoupling, this);

		final int sdi = getstructualDebtIndexFromCycles(cycles, mapmap);
		final Map<String, Integer> res = new HashMap<>();
		res.put("SCORE", sdi);
		res.put("CYCLES", cycles.size());
		res.put(cycles.toString(), 1);

		getRegisteredCouplings().put(getNameTag().toString(), res);
	}

	/**
	 * Calculate the structural debt index score from given cycles.
	 * 
	 * @param cycles
	 * @param graph
	 * @return
	 */
	private int getstructualDebtIndexFromCycles(final List<Circle> cycles,
			final Map<String, Map<String, Integer>> graph) {

		int res = 0;

		for (final Circle circle : cycles) {
			res += 10;

			for (int i = 0; i < circle.vertices.size() - 1; i++) {
				final String v = circle.vertices.get(i);

				final Map<String, Integer> neighbors = graph.get(v);

				if (neighbors != null) {
					for (final String x : neighbors.keySet()) {
						final int y = neighbors.get(x);

						// connection to next vertex
						if (x == circle.vertices.get(i + 1)) {
							res += y;
						}
					}
				}
			}
		}
		return res;
	}

	@Override
	public void calculateCoupling(final JavaClass __) {

		final Map<String, Map<String, Integer>> mapmap = packACoupling.getRegisteredCouplings();
		final List<Circle> cycles = findAllCircles(mapmap, packACoupling.storeVisitor);

		final int sdi = getstructualDebtIndexFromCycles(cycles, mapmap);
		final Map<String, Integer> res = new HashMap<>();
		byteCodeInfo.override(packACoupling, this);
		res.put("SCORE", sdi);
		res.put("CYCLES", cycles.size());
		res.put(cycles.toString(), 1);

		getRegisteredCouplings().put(getNameTag().toString(), res);

	}

	/**
	 * Detect all circles in given graph. Therefore DFS is used. While traverse the
	 * graph: If already visited vertex found in circle, search in stack for first
	 * occurrence of this vertex (start of the circle). If a vertex does not have
	 * any neighbors anymore and not already count as part of a circle, this vertex
	 * can not be part of a circle.
	 * 
	 * @param graph
	 * @param vs
	 * @return all circles of the graph.
	 */
	private List<Circle> findAllCircles(final Map<String, Map<String, Integer>> graph, final Set<String> vs) {

		final Stack<String> stack = new Stack<>();
		final List<Circle> circles = new LinkedList<>();

		final Set<String> verticies = vs;
		final String[] vArray = new String[verticies.size()];
		final Map<String, Integer> toIdx = new HashMap<>(vArray.length);

		int idx = 0;
		for (final String v : vs) {
			vArray[idx] = v;
			toIdx.put(v, idx);
			idx++;

		}
		verticies.toArray(vArray);
		final Status[] states = new Status[vArray.length];

		// init
		for (int i = 0; i < states.length; i++) {
			states[i] = Status.UNVISITED;
		}

		// fresh vertex, if the graph is not connected this is helpful
		for (String vertex : vArray) {

			stack.push(vertex);

			// new entry point to find circles
			if (states[toIdx.get(vertex)] == Status.UNVISITED) {

				while (!stack.isEmpty()) {

					// find next neighbor
					String neighbor = null;
					final Map<String, Integer> neighborMap = graph.get(vertex);

					if (neighborMap != null) {
						final Set<String> neighborSet = neighborMap.keySet();
						for (final String n : neighborSet) {

							// cirlce found
							if (states[toIdx.get(n)] == Status.VISITED) {
								final Circle circle = getCirlce(stack, n);
								circles.add(circle);

								// check for next neighbor
							} else if (states[toIdx.get(n)] == Status.UNVISITED) {
								neighbor = n;
							}
						}
					}

					// neighbor
					if (neighbor != null) {

						states[toIdx.get(neighbor)] = Status.VISITED;
						stack.push(neighbor);
						vertex = neighbor;

						// no neighbor
					} else {

						states[toIdx.get(vertex)] = Status.FINNISHED;

						// find new entry point
						while (!stack.isEmpty()) {
							vertex = stack.pop();
							if (states[toIdx.get(vertex)] != Status.FINNISHED) {
								stack.push(vertex);
								break;
							}
						}
					}
				}
			}
		}
		return circles;
	}

	/**
	 * Building the circle by popping the stack till the starting point of the
	 * circle is found.
	 * 
	 * @param stack
	 * @param n
	 * @return
	 */
	private Circle getCirlce(final Stack<String> stack, final String n) {

		final List<String> list = new LinkedList<>();
		String next;

		do {
			next = stack.pop();
			list.add(0, next);

		} while (!next.equals(n));

		for (final String string : list) {
			stack.push(string);
		}

		return new Circle(list);
	}

	private enum Status {
		UNVISITED, VISITED, FINNISHED;
	}

	/**
	 * A Circle is a path of vertices.
	 *
	 */
	private class Circle {

		List<String> vertices;

		public Circle(final List<String> path) {
			vertices = new ArrayList<>(path.size());

			vertices.addAll(path);

		}

		@Override
		public String toString() {
			String str = "";

			for (final String v : vertices) {
				str += v + " -> ";
			}
			return str + vertices.get(0);
		}

	}
}
