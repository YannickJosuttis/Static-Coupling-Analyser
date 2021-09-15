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

package de.cau.gui;

import java.util.concurrent.Callable;

import com.google.common.base.Supplier;

import de.cau.tools.DataTransformer.MetricResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 * Task for filling table, if dates are accessible.
 *
 */
public class TableDataTask extends Task<ObservableList<MetricResult>> {

	private final Supplier<ObservableList<MetricResult>> supplier;
	private static final long MAX_LIVE_DURATION = 10_000L;
	private final Callable<Boolean> callable;

	public TableDataTask(final Supplier<ObservableList<MetricResult>> supplier, final Callable<Boolean> callable) {
		this.supplier = supplier;
		this.callable = callable;
	}

	@Override
	protected ObservableList<MetricResult> call() throws Exception {

		final long start = System.currentTimeMillis();
		long now = start;

		// Wait until we got a result.
		while (!isFinished()) {

			now = System.currentTimeMillis();
			Thread.sleep(100);

			// Takes care of too much Thread creations, if the result is not calculated yet.
			if (now - start >= MAX_LIVE_DURATION)
				return FXCollections.observableArrayList();
		}

		return supplier.get();
	}

	private boolean isFinished() {
		try {
			return callable.call();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		throw new IllegalStateException();
	}
}
