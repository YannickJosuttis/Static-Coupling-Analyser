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

import java.net.URL;
import java.util.ResourceBundle;

import de.cau.config.Configuration;
import de.cau.monitor.CouplingMonitor;
import de.cau.tools.DataTransformer;
import de.cau.tools.DataTransformer.MetricResult;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

/**
 * This controller takes care of representing the coupling details from a selected class.
 */
public class ClassInfoController implements Initializable {

	@FXML
	private AnchorPane window;

	@FXML
	private Text titleClassName;

	@FXML
	private TableView<MetricResult> tableViewClassInfo;

	@FXML
	private TableColumn<MetricResult, String> name;

	@FXML
	private TableColumn<MetricResult, Integer> m2m;

	@FXML
	private TableColumn<MetricResult, Integer> imprt;

	@FXML
	private TableColumn<MetricResult, Integer> field;

	@FXML
	private TableColumn<MetricResult, Integer> inheritance;

	CouplingMonitor cm = CouplingMonitor.getInstance();

	private String className;

	private int mode;

	public ClassInfoController() {
	}

	public ClassInfoController(final String className, final int mode) {
		this.className = className;
		this.mode = mode;
	}

	/**
	 * Fills a table with detailed coupling from a selected class.
	 * @param className
	 * @param mode
	 */
	private void showTable(final String className, final int mode) {
		// Defining which value represents the corresponding content in the table.
		name.setCellValueFactory(new PropertyValueFactory<MetricResult, String>("name"));
		m2m.setCellValueFactory(new PropertyValueFactory<MetricResult, Integer>("m2m"));
		imprt.setCellValueFactory(new PropertyValueFactory<MetricResult, Integer>("imprt"));
		field.setCellValueFactory(new PropertyValueFactory<MetricResult, Integer>("field"));
		inheritance.setCellValueFactory(new PropertyValueFactory<MetricResult, Integer>("inheritance"));

		final ObservableList<MetricResult> result = DataTransformer.classToObservableList(
				mode == 0 ? Configuration.getSourceCodeMetrics() : Configuration.getBytecodeCodeMetrics(),
				cm.getRegisteredClasses(x -> !x.contains("$")), className, false);

		titleClassName.setText(className);
		tableViewClassInfo.setItems(result);
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {

		// sets the size of columns in dependence of the table.
		name.prefWidthProperty().bind(tableViewClassInfo.widthProperty().divide(2));
		m2m.prefWidthProperty().bind(tableViewClassInfo.widthProperty().divide(5));
		imprt.prefWidthProperty().bind(tableViewClassInfo.widthProperty().divide(10));
		field.prefWidthProperty().bind(tableViewClassInfo.widthProperty().divide(12));
		inheritance.prefWidthProperty().bind(tableViewClassInfo.widthProperty().divide(8));

		Platform.runLater(() -> showTable(className, mode));
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	/**
	 * Sets the mode. 0 for Sourcecode, 1 for Bytecode
	 * @param mode
	 */
	public void setMode(final int mode) {
		this.mode = mode;
	}

}
