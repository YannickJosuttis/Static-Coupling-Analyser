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

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import de.cau.bytecode.Main;
import de.cau.config.Configuration;
import de.cau.monitor.CouplingMonitor;
import de.cau.monitor.metrics.CouplingTag;
import de.cau.tools.DataTransformer;
import de.cau.tools.DataTransformer.MetricResult;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * This Controller takes care of representing the results of bytecode analysis.
 */
public class ResultBController implements Initializable {

	@FXML
	private TableView<MetricResult> tableviewB;

	@FXML
	TableColumn<MetricResult, String> name;
	@FXML
	TableColumn<MetricResult, Integer> m2m;
	@FXML
	TableColumn<MetricResult, Integer> imprt;
	@FXML
	TableColumn<MetricResult, Integer> field;
	@FXML
	TableColumn<MetricResult, Integer> inher;

	@FXML
	private TableView<MetricResult> tableViewPack;

	@FXML
	private TableColumn<MetricResult, String> namePackage;

	@FXML
	private TableColumn<MetricResult, Integer> pckage;

	@FXML
	private StackPane stackPane;

	@FXML
	private TabPane tabPane;

	@FXML
	private Text sdi;

	@FXML
	private Button btnRefresh;

	CouplingMonitor cm = CouplingMonitor.getInstance();

	/**
	 * Updates the contents, if data is found that can be displayed, it will be
	 * filled in the table.
	 * 
	 * @param event
	 */
	@FXML
	void refresh(final ActionEvent event) {

		// Defining which value represents the corresponding content in the table.
		name.setCellValueFactory(new PropertyValueFactory<MetricResult, String>("name"));
		m2m.setCellValueFactory(new PropertyValueFactory<MetricResult, Integer>("m2m"));
		imprt.setCellValueFactory(new PropertyValueFactory<MetricResult, Integer>("imprt"));
		field.setCellValueFactory(new PropertyValueFactory<MetricResult, Integer>("field"));
		inher.setCellValueFactory(new PropertyValueFactory<MetricResult, Integer>("inheritance"));
		namePackage.setCellValueFactory(new PropertyValueFactory<MetricResult, String>("name"));
		pckage.setCellValueFactory(new PropertyValueFactory<MetricResult, Integer>("pckage"));

		final Set<String> registerClasses = cm.getRegisteredClasses(x -> !x.contains("$"));

		final TableDataTask tableDataTask = new TableDataTask(() -> DataTransformer
				.mapToObservableList(Configuration.getBytecodeCodeMetrics(), registerClasses, false),
				() -> Main.isFinished);

		final TableDataTask tableDataTaskPackage = new TableDataTask(() -> DataTransformer
				.mapToObservableList(Configuration.getBytecodeCodeMetrics(), registerClasses, true),
				() -> Main.isFinished);

		final TableDataService service = new TableDataService(tableDataTask);
		final TableDataService servicePackage = new TableDataService(tableDataTaskPackage);

		fillTableIfReceive(service, servicePackage);

		Configuration.findCouplingbyTagB(CouplingTag.STRUCTUAL_DEBT_INDEX)
				.ifPresent(x -> {
					final Map<String, Integer> map = x.getRegisteredCouplings().get("STRUCTUAL_DEBT_INDEX");

					sdi.setText("Structural Debt Index: "
							+ (map != null ? map.get("SCORE").toString() : "-"));
				});
	}

	/**
	 * Filling the table with content if there are some calculated. Until this
	 * happens a grey box is placed and blocking the vision.
	 * 
	 * @param service
	 * @param servicePackage
	 */
	private void fillTableIfReceive(final TableDataService service, final TableDataService servicePackage) {
		final Region fog = new Region();
		fog.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7)");
		fog.setPrefSize(500, 500);
		fog.visibleProperty().bind(service.runningProperty());
		final Text text = new Text();
		text.setText("Loading results ...");
		text.setStyle("-fx-fill: white");
		text.visibleProperty().bind(service.runningProperty());

		tableviewB.itemsProperty().bind(service.valueProperty());
		tableViewPack.itemsProperty().bind(servicePackage.valueProperty());

		stackPane.getChildren().addAll(fog, text);
		service.start();
		servicePackage.start();
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {

		// sets the size of table an column in dependence of the window
		tableviewB.prefWidthProperty().bind(tabPane.widthProperty());
		tableviewB.prefHeightProperty()
				.bind(tabPane.heightProperty().subtract(tabPane.tabMaxHeightProperty()).subtract(10));
		name.prefWidthProperty().bind(tableviewB.widthProperty().divide(2));
		m2m.prefWidthProperty().bind(tableviewB.widthProperty().divide(5));
		imprt.prefWidthProperty().bind(tableviewB.widthProperty().divide(10));
		field.prefWidthProperty().bind(tableviewB.widthProperty().divide(12));
		inher.prefWidthProperty().bind(tableviewB.widthProperty().divide(8));
		tableViewPack.prefWidthProperty().bind(tabPane.widthProperty());
		namePackage.prefWidthProperty().bind(tableViewPack.widthProperty().divide(2));
		pckage.prefWidthProperty().bind(tableViewPack.widthProperty().divide(2));

		refresh(null);
	}

	/**
	 * Opens a window with the detailed coupling from a selected class or package
	 * @param event
	 */
	@FXML
	public void btnShowDetails(final ActionEvent event) {
		final ObservableList<MetricResult> selectedClass = tableviewB.getSelectionModel().getSelectedItems();
		final ObservableList<MetricResult> selectedPackage = tableViewPack.getSelectionModel().getSelectedItems();
		String fxmlSource = null;
		boolean isPackage = false;
		if(!selectedPackage.isEmpty()) {
			fxmlSource = "/fxml/PackageInfo.fxml";
			isPackage = true;
		}else if(!selectedClass.isEmpty()){
			fxmlSource = "/fxml/ClassInfo.fxml";
		}
		
		if(fxmlSource == null)
			return;
		
		final FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlSource));
		try {
			final Parent root = loader.load();
			if(isPackage) {
				final PackageController controller = loader.<PackageController>getController();
				controller.setPackageName(selectedPackage.get(0).getName());
				controller.setMode(1);
			}else {
				final ClassInfoController controller = loader.<ClassInfoController>getController();
				controller.setClassName(selectedClass.get(0).getName());
				controller.setMode(1);
			}
			
			refresh(null);
			final Scene scene = new Scene(root);
			final Stage stage = new Stage();
			stage.setTitle("Detailed Information");
			stage.getIcons().add(new Image("/img/icon_logo.png"));
			stage.setScene(scene);
			stage.setResizable(true);
			stage.show();
			
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
