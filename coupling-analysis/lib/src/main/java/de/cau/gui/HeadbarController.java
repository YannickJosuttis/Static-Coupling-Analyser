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
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

/**
 * This class controlling the general behavior of the GUI. The controller is
 * responsible for context switches and button highlighting.
 *
 */
public class HeadbarController implements Initializable {

	private Button selectedBtn;
	private List<Button> allButtons;

	@FXML
	private Button btn1;

	@FXML
	private Button btn2;

	@FXML
	private Button btn3;

	@FXML
	private BorderPane borderPane;

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		selectedBtn = btn1;
		allButtons = List.of(btn1, btn2, btn3);
		loadUI("Main");
	}

	@FXML
	void btn1Enter(final MouseEvent event) {
		hoverStart(btn1);
	}

	@FXML
	void btn2Enter(final MouseEvent event) {
		hoverStart(btn2);

	}

	@FXML
	void btn3Enter(final MouseEvent event) {
		hoverStart(btn3);
	}

	@FXML
	void btn1Exit(final MouseEvent event) {
		hoverEnd();
	}

	/**
	 * Hover the button with a Color.
	 * 
	 * @param button
	 */
	private void hoverStart(final Button button) {
		changeButtonColor(button, Color.STAR_COMMAND_BLUE);
	}

	/**
	 * Stops hover the button, but checks if the the button is selected. If the
	 * button is selected the button should stay in this selected color.
	 */
	private void hoverEnd() {

		for (final Button button : allButtons) {

			if (button == selectedBtn) {
				changeButtonColor(button, Color.DEEP_SAFFRON);
			} else {
				changeButtonColor(button, Color.RAISING_BLACK);
			}
		}
	}

	@FXML
	void btn2Exit(final MouseEvent event) {
		hoverEnd();
	}

	@FXML
	void btn3Exit(final MouseEvent event) {
		hoverEnd();
	}

	@FXML
	void clickSettings(final MouseEvent event) {
		selectButton(btn1);
		loadUI("Main");
	}

	@FXML
	void clickResultsB(final MouseEvent event) {
		selectButton(btn2);
		loadUI("ui3");
	}

	@FXML
	void clickResultsS(final MouseEvent event) {
		selectButton(btn3);
		loadUI("ui2");
	}

	private void selectButton(final Button button) {
		changeButtonColor(button, Color.DEEP_SAFFRON);
		selectedBtn = button;
	}

	/**
	 * Loading UI from *.fxml format.
	 * 
	 * @param ui
	 */
	private void loadUI(final String ui) {
		Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("/fxml/" + ui + ".fxml"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		borderPane.setCenter(root);

	}

	/**
	 * Changes the color of a button using css style.
	 * 
	 * @param button, which should be colored.
	 * @param color
	 * 
	 */
	private void changeButtonColor(final Button button, final String color) {
		final String bstyle = String.format("-fx-background-color: %s; -fx-text-fill: white;", color);
		button.setStyle(bstyle);
	}

}
