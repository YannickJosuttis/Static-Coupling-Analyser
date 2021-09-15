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
 * This controller takes care of representing the coupling details from a selected package.
 */
public class PackageController implements Initializable {

	@FXML
	private AnchorPane window;

    @FXML
    private TableView<MetricResult> tableViewPackageInfo;

    @FXML
    private TableColumn<MetricResult, String> name;

    @FXML
    private TableColumn<MetricResult, Integer> pckage;

    @FXML
    private Text titlePackageName;

	CouplingMonitor cm = CouplingMonitor.getInstance();

	private String packageName;

	private int mode;

	public PackageController() {
	}

	public PackageController(final String packageName, final int mode) {
		this.packageName = packageName;
		this.mode = mode;
	}

	/**
	 * Fills a table with detailed coupling from a selected package.
	 * @param className
	 * @param mode
	 */
	private void showTable(final String packageName, final int mode) {
		// Defining which value represents the corresponding content in the table.
		name.setCellValueFactory(new PropertyValueFactory<MetricResult, String>("name"));
		pckage.setCellValueFactory(new PropertyValueFactory<MetricResult, Integer>("pckage"));

		final ObservableList<MetricResult> result = DataTransformer.classToObservableList(
				mode == 0 ? Configuration.getSourceCodeMetrics() : Configuration.getBytecodeCodeMetrics(),
				cm.getRegisteredClasses(x -> !x.contains("$")), packageName, true);

		titlePackageName.setText(packageName);
		tableViewPackageInfo.setItems(result);
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {

		// sets the size of column in dependence of the table
		name.prefWidthProperty().bind(tableViewPackageInfo.widthProperty().divide(2));
		pckage.prefWidthProperty().bind(tableViewPackageInfo.widthProperty().divide(2));

		Platform.runLater(() -> showTable(packageName, mode));
	}

	public void setPackageName(final String packageName) {
		this.packageName = packageName;
	}

	/**
	 * Sets the mode. 0 for Sourcecode, 1 for Bytecode
	 * @param mode
	 */
	public void setMode(final int mode) {
		this.mode = mode;
	}

}
