package seng302.GUI.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import seng302.GUI.TFScene;
import seng302.Generic.History;
import seng302.Generic.IO;
import seng302.Generic.Main;
import seng302.User.Clinician;
import seng302.User.User;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * A controller class for the log in screen.
 */
public class LoginController implements Initializable {
    @FXML
    private TextField identificationInput;
    @FXML
    private PasswordField passwordInput;
    @FXML
    private Label errorMessage;
    @FXML
    private Button loginButton;
    @FXML
    private AnchorPane background;

    /**
     * Attempts to log in based on the information currently provided by the user. Provides appropriate feedback if log in fails.
     */
    public void login() {
        boolean identificationMatched = false;

        //Check for a user match
        User currentUser = null;
        for (User user: Main.users) {
            if (user.getUsername() != null && user.getUsername().equals(identificationInput.getText()) ||
                user.getEmail() != null && user.getEmail().equals(identificationInput.getText())) {
                identificationMatched = true;
                if (user.getPassword().equals(passwordInput.getText())) {
                    currentUser = user;
                    String text = History.prepareFileStringGUI(user.getId(), "login");
                    History.printToFile(IO.streamOut, text);
                }
            }
        }

        //Check for a clinician match
        Clinician currentClinician = null;
        for(Clinician clinician: Main.clinicians){
            if(clinician.getUsername() != null && clinician.getUsername().equals(identificationInput.getText())){
                identificationMatched = true;
                if(clinician.getPassword().equals(passwordInput.getText())){
                    currentClinician = clinician;
                }
            }
        }

        if (identificationMatched) {
            if (currentUser != null || currentClinician != null) {
                //Reset scene to original state
                identificationInput.setText("");
                passwordInput.setText("");
                loginButton.setDisable(true);
                errorMessage.setVisible(false);
                if (currentUser != null) {
                    Main.setCurrentUser(currentUser);
                    Main.setScene(TFScene.userWindow);

                } else {
                    Main.setClinician(currentClinician);
                    Main.setScene(TFScene.clinician);
                }
            } else {
                errorMessage.setText("Incorrect password.");
                errorMessage.setVisible(true);
            }
        } else {
            errorMessage.setText("Username or email not recognized.");
            errorMessage.setVisible(true);
        }
    }

    /**
     * Removes focus from all fields.
     */
    public void requestFocus() {
        background.requestFocus();
    }

    /**
     * Switches the displayed scene to the create account scene.
     */
    public void createAccount() {
        Main.setScene(TFScene.createAccount);
    }

    /**
     * Sets the enter key press to attempt log in if sufficient information is present.
     */
    public void setEnterEvent() {
        Main.getScene(TFScene.login).setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !loginButton.isDisable()) {
                login();
            }
        });
    }

    /**
     * Add listeners to enable/disable the login button based on information supplied
     * @param location Not used
     * @param resources Not used
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Main.setLoginController(this);
        requestFocus();
        identificationInput.textProperty().addListener((observable, oldValue, newValue) ->
                loginButton.setDisable(identificationInput.getText().isEmpty() || passwordInput.getText().isEmpty()));
        passwordInput.textProperty().addListener((observable, oldValue, newValue) ->
                loginButton.setDisable(identificationInput.getText().isEmpty() || passwordInput.getText().isEmpty()));
    }
}