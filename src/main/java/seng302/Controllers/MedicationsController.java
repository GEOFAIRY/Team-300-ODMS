package seng302.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import seng302.Core.Donor;
import seng302.Core.Main;
import seng302.Core.Medication;

import java.net.URL;
import java.util.ResourceBundle;

public class MedicationsController implements Initializable {

    private Donor currentDonor;
    public Donor getCurrentDonor() {
        return currentDonor;
    }

    public void setCurrentDonor(Donor currentDonor) {
        this.currentDonor = currentDonor;
        System.out.println("HELLO");
        donorNameLabel.setText("Donor: " + currentDonor.getName());
//        donorUndoStack.clear();
//        donorRedoStack.clear();
//        undoButton.setDisable(true);
//        undoWelcomeButton.setDisable(true);
//        redoButton.setDisable(true);
//        redoWelcomeButton.setDisable(true);
//        bloodPressureLabel.setText("");
    }

    @FXML
    private Label donorNameLabel;
    @FXML
    private TextField newMedicationField;
    @FXML
    private Label interactionsLabel;
    @FXML
    private Label drugALabel;
    @FXML
    private Label drugBLabel;
    @FXML
    private ListView historyListView;
    @FXML
    private ListView currentListView;

    public void addNewMedication() {
        //TODO Add in check for autocomplete
        String medicationChoice = newMedicationField.getText();
        currentDonor.getCurrentMedications().add(1, new Medication(medicationChoice));
        populateMedications();
    }

    public void populateMedications() {
        ObservableList<String> items = FXCollections.observableArrayList (
                "Single", "Double", "Suite", "Family App");
        historyListView.setItems(items);
        currentListView.setItems(currentDonor.getCurrentMedications());

    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Main.setMedicationsController(this);

    }


}
