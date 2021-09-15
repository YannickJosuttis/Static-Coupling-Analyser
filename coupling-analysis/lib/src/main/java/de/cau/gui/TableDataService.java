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

import de.cau.tools.DataTransformer.MetricResult;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Service for filling table contents.
 *
 */
public class TableDataService extends Service<ObservableList<MetricResult>> {

	private final TableDataTask tableDataTask;

	public TableDataService(final TableDataTask tableDataTask) {
		this.tableDataTask = tableDataTask;
	}

	@Override
	protected Task<ObservableList<MetricResult>> createTask() {
		return tableDataTask;
	}

}
