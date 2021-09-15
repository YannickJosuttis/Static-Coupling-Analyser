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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.cau.config.Configuration;
import de.cau.config.Configuration.ReadFrom;
import de.cau.config.DirectoryTool;
import de.cau.main.Main;
import de.cau.monitor.metrics.ACoupling;
import de.cau.monitor.metrics.CouplingTag;
import de.cau.monitor.metrics.FieldCoupling;
import de.cau.monitor.metrics.ImportCoupling;
import de.cau.monitor.metrics.InheritanceCoupling;
import de.cau.monitor.metrics.MethodToMethodCoupling;
import de.cau.monitor.metrics.PackageCoupling;
import de.cau.monitor.metrics.StructalDebtIndex;
import de.cau.tools.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * This class controlling, the settings made by the user. The settings are
 * stored beyond context switches and restarts.
 *
 */
public class FXMLController implements Initializable {

	private final DirectoryChooser directoryChooser = new DirectoryChooser();
	private SettingStorage storage = new SettingStorage();
	private final String storagePath = "./src/main/resources/settings.yml";

	@FXML
	private StackPane stackPane;

	@FXML
	private VBox vBoxDir;

	@FXML
	private VBox vBoxBnt;

	@FXML
	private Button btnSourceRoot;

	@FXML
	private TextField txtSoureRoot;

	@FXML
	private Button btnBytecodeRoot;

	@FXML
	private TextField txtBytecodeRoot;

	@FXML
	private Button btnExternalLib;

	@FXML
	private TextField txtExternalLib;

	@FXML
	private TextField txtOutputRoot;

	@FXML
	private Button btnRun;

	@FXML
	private VBox vBoxLeftB;

	@FXML
	private VBox vBoxRightB;

	@FXML
	private VBox vBoxLeftS;

	@FXML
	private VBox vBoxRightS;

	@FXML
	private CheckBox checkM2mB;

	@FXML
	private CheckBox checkPackB;

	@FXML
	private CheckBox checkInheritanceB;

	@FXML
	private CheckBox checkFieldB;

	@FXML
	private CheckBox checkImportB;

	@FXML
	private CheckBox checkM2mS;

	@FXML
	private CheckBox checkPackS;

	@FXML
	private CheckBox checkInheritanceS;

	@FXML
	private CheckBox checkFieldS;

	@FXML
	private CheckBox checkImportS;

	@FXML
	private Button btnOutputRoot;

	/**
	 * This is the entry point to the core program.
	 * 
	 * @param e
	 */
	@FXML
	public void run(final ActionEvent e) {
		Configuration.configure(null, getClass(), Configuration.ReadFrom.FROM_STORAGE);

		final Region fog = new Region();
		fog.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7)");
		fog.setPrefSize(500, 500);
		final ProgressIndicator pi = new ProgressIndicator(-1f);
		pi.setStyle(String.format("-fx-progress-color: %s;", Color.DEEP_SAFFRON));

		pi.setMaxSize(100, 100);
		stackPane.getChildren().addAll(fog, pi);

		new Thread(() -> {
			Main.main(new String[] { "workarround" });
			Platform.runLater(
					() -> {
						pi.setVisible(false);
						fog.setVisible(false);
					});
		}).start();
	}

	/**
	 * Setting the sourcecode root in core part and GUI, based on user's choice.
	 * 
	 * @param e
	 */
	@FXML
	public void chooseSourceRootDir(final ActionEvent e) {

		setRootProperties(path -> {
			DirectoryTool.setSourceCodeInputRoot(path);
			txtSoureRoot.setText(path);
			DirectoryTool.isSourceCodeRootDefined = true;
			storage.sourceCodeInputRoot = path;
		});
	}

	/**
	 * Setting the bytecode root in core part and GUI, based on user's choice.
	 * 
	 * @param e
	 */
	@FXML
	public void chooseBytecodeRootDir(final ActionEvent e) {

		setRootProperties(path -> {
			DirectoryTool.setByteCodeInputRoot(path);
			txtBytecodeRoot.setText(path);
			DirectoryTool.isByteCodeRootDefined = true;
			storage.byteCodeInputRoot = path;
		});
	}

	/**
	 * Setting the path to external libraries in core part and GUI, based on user's
	 * choice.
	 * 
	 * @param e
	 */
	@FXML
	public void chooseExternalLibDir(final ActionEvent e) {

		setRootProperties(path -> {
			DirectoryTool.setExternalLibPath(path);
			txtExternalLib.setText(path);
			storage.externalLibPath = path;
		});
	}

	@FXML
	void chooseOutputRootDir(final ActionEvent event) {
		setRootProperties(path -> {
			DirectoryTool.setOutputRoot(path);
			txtOutputRoot.setText(path);
			storage.outputRoot = path;
			Logger.getInstance().updateChanges();
		});
	}

	/**
	 * Setting root properties based on user's choice via {@link DirectoryChooser}.
	 * 
	 * @param consumer consuming the path defined by the user.
	 */
	private void setRootProperties(final Consumer<String> consumer) {

		updatePropertiesAfter(() -> {
			final File selectedDirectory = getDir();
			final File dir = selectedDirectory;

			if (dir == null)
				return;

			final String path = dir.getAbsolutePath();

			consumer.accept(path);
			disableButtonifNeeded();
			storeObjToYaml(storage);
		});
	}

	/**
	 * Context wrapper, that takes care of postconditions. Similar to python's
	 * 'with' statement.
	 * 
	 * @param runnable
	 */
	private void updatePropertiesAfter(final Runnable runnable) {
		runnable.run();
		disableButtonifNeeded();
		storeObjToYaml(storage);
	}

	/**
	 * Disables the button, if the execution of the program does not make sense.
	 */
	private void disableButtonifNeeded() {

		final boolean isDisabled = !(DirectoryTool.isByteCodeRootDefined
				&& !Configuration.getBytecodeCodeMetrics().isEmpty()
				|| DirectoryTool.isSourceCodeRootDefined && !Configuration.getSourceCodeMetrics().isEmpty());
		btnRun.setDisable(isDisabled);
	}

	/**
	 * Translates the user input of checkboxes.
	 * 
	 * @param checkbox for bytecode
	 * @param coupling
	 */
	private void setCheckBoxPropertyB(final CheckBox checkbox, final ACoupling coupling) {

		updatePropertiesAfter(() -> {
			if (checkbox.isSelected()) {
				Configuration.registerByteCodeMetrics(coupling);
				storage.tagsB.add(Mapper.tagAndCheckB.inverse().get(checkbox));
			} else {
				Configuration.removeByteCodeMetrics(coupling);
				storage.tagsB.remove(Mapper.tagAndCheckB.inverse().get(checkbox));
			}
		});
	}

	/**
	 * Translates the user input of checkboxes.
	 * 
	 * @param checkbox for sourcecode
	 * @param coupling
	 */
	private void setCheckBoxPropertyS(final CheckBox checkbox, final ACoupling coupling) {

		updatePropertiesAfter(() -> {
			if (checkbox.isSelected()) {
				Configuration.registerSourceCodeMetrics(coupling);
				storage.tagsS.add(Mapper.tagAndCheckS.inverse().get(checkbox));
			} else {
				Configuration.removeSourceCodeMetrics(coupling);
				storage.tagsS.remove(Mapper.tagAndCheckS.inverse().get(checkbox));
			}
		});
	}

	/**
	 * Getting the directory from the {@link DirectoryChooser}
	 * 
	 * @return the directory represented by the file.
	 */
	private File getDir() {
		final Stage stage = (Stage) btnSourceRoot.getScene().getWindow();
		return directoryChooser.showDialog(stage);
	}

	/**
	 * Corresponding checkbox is clicked.
	 * 
	 * @param event
	 */
	@FXML
	void fieldB(final MouseEvent event) {
		setCheckBoxPropertyB(checkFieldB, Mapper.tagToCachedCouplB.get(CouplingTag.FIELD));
	}

	/**
	 * Corresponding checkbox is clicked.
	 * 
	 * @param event
	 */
	@FXML
	void importB(final MouseEvent event) {
		setCheckBoxPropertyB(checkImportB, Mapper.tagToCachedCouplB.get(CouplingTag.IMPORT));
	}

	/**
	 * Corresponding checkbox is clicked.
	 * 
	 * @param event
	 */
	@FXML
	void inheritanceB(final MouseEvent event) {
		setCheckBoxPropertyB(checkInheritanceB, Mapper.tagToCachedCouplB.get(CouplingTag.INHERITANCE));
	}

	@FXML
	void mtm2B(final MouseEvent event) {
		setCheckBoxPropertyB(checkM2mB, Mapper.tagToCachedCouplB.get(CouplingTag.METHOD_TO_METHOD));
	}

	/**
	 * Corresponding checkbox is clicked.
	 * 
	 * @param event
	 */
	@FXML
	void packB(final MouseEvent event) {
		setCheckBoxPropertyB(checkPackB, Mapper.tagToCachedCouplB.get(CouplingTag.PACKAGE));
	}

	/**
	 * Corresponding checkbox is clicked.
	 * 
	 * @param event
	 */
	@FXML
	void fieldS(final MouseEvent event) {
		setCheckBoxPropertyS(checkFieldS, Mapper.tagToCachedCouplS.get(CouplingTag.FIELD));
	}

	/**
	 * Corresponding checkbox is clicked.
	 * 
	 * @param event
	 */
	@FXML
	void importS(final MouseEvent event) {
		setCheckBoxPropertyS(checkImportS, Mapper.tagToCachedCouplS.get(CouplingTag.IMPORT));
	}

	/**
	 * Corresponding checkbox is clicked.
	 * 
	 * @param event
	 */
	@FXML
	void inheritanceS(final MouseEvent event) {
		setCheckBoxPropertyS(checkInheritanceS, Mapper.tagToCachedCouplS.get(CouplingTag.INHERITANCE));
	}

	/**
	 * Corresponding checkbox is clicked.
	 * 
	 * @param event
	 */
	@FXML
	void mtm2S(final MouseEvent event) {
		setCheckBoxPropertyS(checkM2mS, Mapper.tagToCachedCouplS.get(CouplingTag.METHOD_TO_METHOD));
	}

	/**
	 * Corresponding checkbox is clicked.
	 * 
	 * @param event
	 */
	@FXML
	void packS(final MouseEvent event) {
		setCheckBoxPropertyS(checkPackS, Mapper.tagToCachedCouplS.get(CouplingTag.PACKAGE));
	}

	private void positionOfDirTextfield() {
		vBoxDir.setSpacing(20);
		vBoxDir.setPrefWidth(480);
		vBoxDir.setPadding(new Insets(35, 0, 0, 30));
	}

	private void positionOfDirBnt() {
		vBoxBnt.setSpacing(20);
		vBoxBnt.setPrefWidth(180);
		vBoxBnt.setPadding(new Insets(35, 0, 0, 0));
	}

	private void positionOfCheckboxesLeftB() {
		vBoxLeftB.setPrefWidth(165);
		vBoxLeftB.setSpacing(10);
		vBoxLeftB.setPadding(new Insets(30, 0, 0, 15));
	}

	private void positionOfCheckboxesRightB() {
		vBoxRightB.setPrefWidth(150);
		vBoxRightB.setSpacing(10);
		vBoxRightB.setPadding(new Insets(30, 0, 0, 15));
	}

	private void positionOfCheckboxesLeftS() {
		vBoxLeftS.setPrefWidth(165);
		vBoxLeftS.setSpacing(10);
		vBoxLeftS.setPadding(new Insets(30, 0, 0, 15));
	}

	private void positionOfCheckboxesRightS() {
		vBoxRightS.setPrefWidth(150);
		vBoxRightS.setSpacing(10);
		vBoxRightS.setPadding(new Insets(30, 0, 0, 15));
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {

		// First run after start
		if (!Configuration.isInit) {
			Configuration.isInit = true;

			Configuration.configure(null, null, ReadFrom.FROM_PROJECT);

			final File file = new File(storagePath);
			// Reading data based on older ones.
			if (file.exists() && !DirectoryTool.isFileEmpty(file)) {
				storage = loadYamlAsObj(storagePath);
				readFromStorage();
			} else {
				// No data to read from.
				DirectoryTool.setOutputRootBasedOnProject();
				storeObjToYaml(storage);
				setup();
			}
			Configuration.registerByteCodeMetrics(new StructalDebtIndex());
//			Configuration.registerSourceCodeMetrics(new StructalDebtIndex());
			disableButtonifNeeded();
		} else {
			// Reading data based on older ones.
			storage = loadYamlAsObj(storagePath);
			readFromStorage();
		}

		positionOfDirTextfield();
		positionOfDirBnt();
		positionOfCheckboxesLeftS();
		positionOfCheckboxesRightS();
		positionOfCheckboxesLeftB();
		positionOfCheckboxesRightB();
	}

	/**
	 * Reads contents from storage object and sets the defined properties.
	 */
	private void readFromStorage() {

		final String externalLibPath = storage.externalLibPath;
		final String byteCodeInputRoot = storage.byteCodeInputRoot;
		final String sourceCodeInputRoot = storage.sourceCodeInputRoot;
		final String outputRoot = storage.outputRoot;

		if (externalLibPath != null) {
			txtExternalLib.setText(externalLibPath);
			DirectoryTool.setExternalLibPath(externalLibPath);
		}
		if (byteCodeInputRoot != null) {
			txtBytecodeRoot.setText(byteCodeInputRoot);
			DirectoryTool.setByteCodeInputRoot(byteCodeInputRoot);
		}
		if (sourceCodeInputRoot != null) {
			txtSoureRoot.setText(sourceCodeInputRoot);
			DirectoryTool.setSourceCodeInputRoot(sourceCodeInputRoot);
		}

		if (outputRoot != null) {
			txtOutputRoot.setText(outputRoot);
			DirectoryTool.setOutputRoot(outputRoot);
		}

		setup();

		for (final CouplingTag tag : storage.tagsB) {
			Mapper.tagAndCheckB.get(tag).setSelected(true);
			final Optional<ACoupling> oACoupl = Configuration.findCouplingbyTagB(tag);

			final ACoupling aCoupl;
			if (oACoupl.isPresent()) {
				aCoupl = oACoupl.get();
				Mapper.tagToCachedCouplB.put(tag, aCoupl);
			} else {
				aCoupl = Mapper.tagToCachedCouplB.get(tag);
				Configuration.registerByteCodeMetrics(aCoupl);
			}

		}

		Configuration.findCouplingbyTagB(CouplingTag.STRUCTUAL_DEBT_INDEX).ifPresent(
				__ -> Configuration.findCouplingbyTagB(CouplingTag.PACKAGE).ifPresent(ACoupling::setCouplingHook));

		for (final CouplingTag tag : storage.tagsS) {

			Mapper.tagAndCheckS.get(tag).setSelected(true);
			final Optional<ACoupling> oACoupl = Configuration.findCouplingbyTagS(tag);

			final ACoupling aCoupl;
			if (oACoupl.isPresent()) {
				aCoupl = oACoupl.get();
				Mapper.tagToCachedCouplS.put(tag, aCoupl);
			} else {
				aCoupl = Mapper.tagToCachedCouplS.get(tag);
				Configuration.registerSourceCodeMetrics(aCoupl);
			}
		}
		Configuration.findCouplingbyTagS(CouplingTag.STRUCTUAL_DEBT_INDEX).ifPresent(
				__ -> Configuration.findCouplingbyTagS(CouplingTag.PACKAGE).ifPresent(ACoupling::setCouplingHook));
	}

	/**
	 * Loads a *.yaml file into a {@link SettingStorage} object.
	 * 
	 * @param path
	 * @return a {@link SettingStorage}
	 */
	private SettingStorage loadYamlAsObj(final String path) {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(new File(path));
			final Yaml yaml = new Yaml(new Constructor(SettingStorage.class));
			final SettingStorage data = yaml.load(inputStream);
			return data;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Stores a storage object into a *.yaml file.
	 * 
	 * @param storage object, which has to fullfill the bean characteristic.
	 */
	private void storeObjToYaml(final SettingStorage storage) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new File(storagePath));
			final Yaml yaml = new Yaml();
			yaml.dump(storage, writer);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simple class containing Maps for easier access.
	 *
	 */
	private static class Mapper {
		public static final BiMap<CouplingTag, CheckBox> tagAndCheckB = HashBiMap.create();
		public static final BiMap<CouplingTag, CheckBox> tagAndCheckS = HashBiMap.create();
		public static final Map<CouplingTag, ACoupling> tagToCachedCouplB = new HashMap<>();
		public static final Map<CouplingTag, ACoupling> tagToCachedCouplS = new HashMap<>();
	}

	/**
	 * Filling the Maps for easier handling.
	 */
	private void setup() {
		Mapper.tagToCachedCouplB.put(CouplingTag.METHOD_TO_METHOD, new MethodToMethodCoupling());
		Mapper.tagToCachedCouplB.put(CouplingTag.PACKAGE, new PackageCoupling());
		Mapper.tagToCachedCouplB.put(CouplingTag.FIELD, new FieldCoupling());
		Mapper.tagToCachedCouplB.put(CouplingTag.IMPORT, new ImportCoupling());
		Mapper.tagToCachedCouplB.put(CouplingTag.INHERITANCE, new InheritanceCoupling());

		Mapper.tagToCachedCouplS.put(CouplingTag.METHOD_TO_METHOD, new MethodToMethodCoupling());
		Mapper.tagToCachedCouplS.put(CouplingTag.PACKAGE, new PackageCoupling());
		Mapper.tagToCachedCouplS.put(CouplingTag.FIELD, new FieldCoupling());
		Mapper.tagToCachedCouplS.put(CouplingTag.IMPORT, new ImportCoupling());
		Mapper.tagToCachedCouplS.put(CouplingTag.INHERITANCE, new InheritanceCoupling());

		Mapper.tagAndCheckB.put(CouplingTag.METHOD_TO_METHOD, checkM2mB);
		Mapper.tagAndCheckB.put(CouplingTag.PACKAGE, checkPackB);
		Mapper.tagAndCheckB.put(CouplingTag.FIELD, checkFieldB);
		Mapper.tagAndCheckB.put(CouplingTag.IMPORT, checkImportB);
		Mapper.tagAndCheckB.put(CouplingTag.INHERITANCE, checkInheritanceB);

		Mapper.tagAndCheckS.put(CouplingTag.METHOD_TO_METHOD, checkM2mS);
		Mapper.tagAndCheckS.put(CouplingTag.PACKAGE, checkPackS);
		Mapper.tagAndCheckS.put(CouplingTag.FIELD, checkFieldS);
		Mapper.tagAndCheckS.put(CouplingTag.IMPORT, checkImportS);
		Mapper.tagAndCheckS.put(CouplingTag.INHERITANCE, checkInheritanceS);
	}

	/**
	 * This is class is used to store the settings information in Yaml-format,
	 * Therefore this class is constructed as bean.
	 *
	 */
	public static class SettingStorage {

		public Set<CouplingTag> tagsS = new HashSet<>();
		public Set<CouplingTag> tagsB = new HashSet<>();

		public String outputRoot;
		public String externalLibPath;
		public String byteCodeInputRoot;
		public String sourceCodeInputRoot;

		public SettingStorage() {
		}

		public Set<CouplingTag> getTagsS() {
			return tagsS;
		}

		public void setTagsS(final Set<CouplingTag> tagsS) {
			this.tagsS = tagsS;
		}

		public Set<CouplingTag> getTagsB() {
			return tagsB;
		}

		public void setTagsB(final Set<CouplingTag> tagsB) {
			this.tagsB = tagsB;
		}

		public String getExternalLibPath() {
			return externalLibPath;
		}

		public void setExternalLibPath(final String externalLibPath) {
			this.externalLibPath = externalLibPath;
		}

		public String getByteCodeInputRoot() {
			return byteCodeInputRoot;
		}

		public void setByteCodeInputRoot(final String byteCodeInputRoot) {
			this.byteCodeInputRoot = byteCodeInputRoot;
		}

		public String getSourceCodeInputRoot() {
			return sourceCodeInputRoot;
		}

		public void setSourceCodeInputRoot(final String sourceCodeInputRoot) {
			this.sourceCodeInputRoot = sourceCodeInputRoot;
		}

		public String getTxtOutputRoot() {
			return outputRoot;
		}

		public void setTxtOutputRoot(final String txtOutputRoot) {
			this.outputRoot = txtOutputRoot;
		}

		@Override
		public String toString() {
			return "SettingStorage [tagsS=" + tagsS + ", tagsB=" + tagsB + ", outputRoot=" + outputRoot
					+ ", externalLibPath=" + externalLibPath + ", byteCodeInputRoot=" + byteCodeInputRoot
					+ ", sourceCodeInputRoot=" + sourceCodeInputRoot + "]";
		}

	}
}
