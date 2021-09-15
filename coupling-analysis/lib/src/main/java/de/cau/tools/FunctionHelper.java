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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A Class containing useful functions to operate with.
 */
public class FunctionHelper {

	public static <A, B> List<Entry<A, B>> zip(final List<A> as, final List<B> bs) {
		return IntStream.range(0, Math.min(as.size(), bs.size())).mapToObj(i -> Map.entry(as.get(i), bs.get(i)))
				.collect(Collectors.toList());
	}

	public static <From, To> Function<From, Optional<To>> handleExceptionFunctionWithWrapper(
			final exceptionHandlerFunction<From, To, Exception> handlerFunction) {
		return obj -> {
			try {
				return Optional.of(handlerFunction.apply(obj));
			} catch (final Exception ex) {
				return Optional.empty();
			}
		};
	}

	public static <From, To> Function<From, To> handleExceptionFunction(
			final exceptionHandlerFunction<From, To, Exception> handlerFunction) {
		return obj -> {
			try {
				return handlerFunction.apply(obj);
			} catch (final Exception ex) {
				throw new RuntimeException(ex);
			}
		};
	}

	/**
	 * Wrap the exceptionHandlerConsumer into a try and catch. And the returned
	 * Consumer 'takes over' the functionality of the given handlerConsumer.
	 * 
	 * @param <To>
	 * @param handlerConsumer
	 * @return
	 */
	public static <To> Consumer<To> handleExceptionConsumer(
			final exceptionHandlerConsumer<To, Exception> handlerConsumer) {
		return obj -> {
			try {
				handlerConsumer.accept(obj);
			} catch (final Exception ex) {
				throw new RuntimeException(ex);
			}
		};
	}

	@FunctionalInterface
	public interface exceptionHandlerConsumer<To, Ex extends Exception> {
		public void accept(To target) throws Ex;
	}

	/**
	 * Mimic the functional interface of java.util.function.Function with a throw.
	 *
	 * @param <From>
	 * @param <To>
	 * @param <Ex>
	 */
	@FunctionalInterface
	public interface exceptionHandlerFunction<From, To, Ex extends Exception> {
		public To apply(From from) throws Ex;
	}

}
