package seng302.gui.controllers.user;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import seng302.generic.Debugger;
import seng302.generic.WindowManager;
import seng302.User.Attribute.Organ;
import seng302.User.Procedure;
import seng302.User.User;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * Class which handles all the logic for the Medical History (Procedures) Window.
 * Handles all functions including:
 * Adding, deleting, updating and marking procedures in the Table Views.
 */
public class UserProceduresController extends UserTabController implements Initializable {

    @FXML
    private DatePicker dateOfProcedureInput;
    @FXML
    private TextField summaryInput, descriptionInput;
    @FXML
    private TableView<Procedure> previousProcedureTableView, pendingProcedureTableView;
    @FXML
    private TableColumn<Procedure, String> previousSummaryColumn, previousDescriptionColumn, previousDateColumn, pendingSummaryColumn,
            pendingDescriptionColumn, pendingDateColumn;
    @FXML
    private Label donorNameLabel;

    @FXML
    private Button addNewProcedureButton, deleteProcedureButton;
    @FXML
    private Label newProcedureDateLabel, newProcedureLabel, pendingProceduresLabel, previousProceduresLabel;
    @FXML
    private MenuButton organAffectChoiceBox;
    @FXML
    private CheckBox pancreasCheckBox, lungCheckBox, heartCheckBox, kidneyCheckBox, intestineCheckBox, corneaCheckBox,
            middleEarCheckBox, skinCheckBox, boneMarrowCheckBox, connectiveTissueCheckBox;

    @FXML
    private Label donatingLabel;

    private ArrayList<CheckBox> affectedOrganCheckBoxes = new ArrayList<>();
    private ObservableList<Procedure> pendingProcedureItems, previousProcedureItems;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        affectedOrganCheckBoxes.addAll(Arrays.asList(pancreasCheckBox, lungCheckBox, heartCheckBox, kidneyCheckBox,
                intestineCheckBox, corneaCheckBox, middleEarCheckBox, skinCheckBox, boneMarrowCheckBox, connectiveTissueCheckBox));
        setupListeners();
    }

    /**
     * Update the displayed user procedures to what is currently stored in the user object.
     */
    public void updateProcedures() {
        pendingProcedureItems.clear();
        pendingProcedureItems.addAll(currentUser.getPendingProcedures());
        previousProcedureItems.clear();
        previousProcedureItems.addAll(currentUser.getPreviousProcedures());
    }

    /**
     * Function to saving the most current versions of the previous and pending procedures to an undo stack
     */
    private void saveToUndoStack() {
        userController.addCurrentUserToUndoStack();
        currentUser.getPendingProcedures().clear();
        currentUser.getPendingProcedures().addAll(pendingProcedureItems);
        currentUser.getPreviousProcedures().clear();
        currentUser.getPreviousProcedures().addAll(previousProcedureItems);
    }

    /**
     * Adds a new procedure to the unsaved Donor Procedures array list.
     * Also checks for invalid input in the procedure summary, description and date fields.
     */
    public void addNewProcedure() {
        Debugger.log("UserProceduresController: Adding new procedure");
        // Check for empty disease name TODO could be a listener to disable the add button
        if (summaryInput.getText().equals("")) {
            Alert alert = WindowManager.createAlert(Alert.AlertType.WARNING, "Invalid Procedure", "",
                    "Invalid procedure summary provided.");
            alert.showAndWait();
            summaryInput.clear();
            // Check for an empty date
        } else if (descriptionInput.getText().equals("")) {
            Alert alert = WindowManager.createAlert(Alert.AlertType.WARNING, "Invalid Procedure", "",
                    "Invalid procedure description provided.");
            alert.showAndWait();
            descriptionInput.clear();
        } else if (dateOfProcedureInput.getValue() == null) {
            Alert alert = WindowManager.createAlert(Alert.AlertType.WARNING, "Invalid Procedure", "",
                    "No date provided.");
            alert.showAndWait();
        } else if (dateOfProcedureInput.getValue().isBefore(currentUser.getDateOfBirth())) {
            Alert alert = WindowManager.createAlert(Alert.AlertType.WARNING, "Invalid Procedure", "",
                    "Due date occurs before the user's date of birth.");
            alert.showAndWait();
        } else {
            //Go through the organs affected and see which is affected.
            ArrayList<Organ> organsAffected = new ArrayList<>();

            for (CheckBox organCheckBox: affectedOrganCheckBoxes) {
                if(organCheckBox.isSelected()) {
                    organsAffected.add(Organ.parse(organCheckBox.getText()));
                    organCheckBox.setSelected(false);
                }
            }

            Procedure procedureToAdd = new Procedure(summaryInput.getText(), descriptionInput.getText(),
                    dateOfProcedureInput.getValue(), organsAffected);
            if (dateOfProcedureInput.getValue().isBefore(LocalDate.now())) {
                previousProcedureItems.add(procedureToAdd);
                previousProcedureItems.sort((a,b)-> a.getDate().isBefore(b.getDate()) ?  1 : a.getDate().isAfter(b.getDate()) ? -1 : 0);
                userController.addHistoryEntry("Previous procedure added", "A previous procedure (" + procedureToAdd.getSummary() + ") was added.");
            } else {
                pendingProcedureItems.add(procedureToAdd);
                pendingProcedureItems.sort((a,b)-> a.getDate().isBefore(b.getDate()) ?  -1 : a.getDate().isAfter(b.getDate()) ? 1 : 0);
                userController.addHistoryEntry("Pending procedure added", "A pending procedure (" + procedureToAdd.getSummary() + ") was added.");
            }
            saveToUndoStack();
            summaryInput.clear();
            descriptionInput.clear();
            dateOfProcedureInput.getEditor().clear();
            Debugger.log("UserProceduresController: Finished adding new procedure");
            statusIndicator.setStatus("Added " + procedureToAdd, false);
            titleBar.saved(false);
        }
    }


    /**
     * Deletes a procedure from either the pending or previous table views for the procedures.
     */
    public void deleteProcedure() {
        Debugger.log("UserProceduresController: Deleting disease");

        if (pendingProcedureTableView.getSelectionModel().getSelectedItem() != null) {

            Alert alert = WindowManager.createAlert(Alert.AlertType.CONFIRMATION,
                "Are you sure?", "Are you sure would like to delete the selected pending procedure? ", "By doing so, the procedure will be erased from the database.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Procedure chosenProcedure = pendingProcedureTableView.getSelectionModel().getSelectedItem();
                pendingProcedureItems.remove(chosenProcedure);
                userController.addHistoryEntry("Pending procedure removed", "A pending procedure (" + chosenProcedure.getSummary() + ") was removed.");
                statusIndicator.setStatus("Deleted " + chosenProcedure, false);
                titleBar.saved(false);
            }
            alert.close();
        } else if (previousProcedureTableView.getSelectionModel().getSelectedItem() != null) {

            Alert alert = WindowManager.createAlert(Alert.AlertType.CONFIRMATION,
                "Are you sure?", "Are you sure would like to delete the selected previous procedure? ", "By doing so, the procedure will be erased from the database.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Procedure chosenProcedure = previousProcedureTableView.getSelectionModel().getSelectedItem();
                previousProcedureItems.remove(chosenProcedure);
                userController.addHistoryEntry("Previous procedure removed", "A previous procedure (" + chosenProcedure.getSummary() + ") was removed.");
                statusIndicator.setStatus("Deleted " + chosenProcedure, false);
                titleBar.saved(false);
                saveToUndoStack();
            }
            alert.close();
        }


    }

    /**
     * Updates the current state of the user's procedures for both their previous and pending procedures.
     */
    public void updateUser() {
        currentUser.getPendingProcedures().clear();
        currentUser.getPendingProcedures().addAll(pendingProcedureItems);

        currentUser.getPreviousProcedures().clear();
        currentUser.getPreviousProcedures().addAll(previousProcedureItems);
    }

    /**
     * Function which produces a pop up window to update all the given fields for a procedure.
     *
     * @param selectedProcedure The selected procedure that the user wishes to update.
     * @param pending           If the procedure is a pending procedure or not.
     */
    private void updateProcedurePopUp(Procedure selectedProcedure, boolean pending) {

        // Create the custom dialog.
        Dialog<ArrayList<String>> dialog = new Dialog<>();
        dialog.setTitle("Update Procedure");
        dialog.setHeaderText("Update Procedure Details");
        WindowManager.setIconAndStyle(dialog.getDialogPane());

        // Set the button types.
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField procedureSummary = new TextField();
        procedureSummary.setPromptText(selectedProcedure.getSummary());
        procedureSummary.setId("procedureSummary");
        TextField procedureDescription = new TextField();
        procedureDescription.setId("procedureDescription");
        procedureDescription.setPromptText(selectedProcedure.getDescription());
        DatePicker dateDue = new DatePicker();
        dateDue.setId("dateDue");
        dateDue.setPromptText(selectedProcedure.getDate().toString());

        CheckBox pancreasUpdateCheckBox = new CheckBox();
        pancreasUpdateCheckBox.setText("pancreas");
        pancreasUpdateCheckBox.setId("pancreasCheckBox");

        CheckBox lungUpdateCheckBox = new CheckBox();
        lungUpdateCheckBox.setText("lung");
        lungUpdateCheckBox.setId("lungCheckBox");

        CheckBox heartUpdateCheckBox = new CheckBox();
        heartUpdateCheckBox.setText("heart");
        heartUpdateCheckBox.setId("heartCheckBox");

        CheckBox kidneyUpdateCheckBox = new CheckBox();
        kidneyUpdateCheckBox.setText("kidney");
        kidneyUpdateCheckBox.setId("kidneyCheckBox");

        CheckBox intestineUpdateCheckBox = new CheckBox();
        intestineUpdateCheckBox.setText("intestine");
        intestineUpdateCheckBox.setId("intestineCheckBox");

        CheckBox corneaUpdateCheckBox = new CheckBox();
        corneaUpdateCheckBox.setText("cornea");
        corneaUpdateCheckBox.setId("corneaCheckBox");

        CheckBox middleEarUpdateCheckBox = new CheckBox();
        middleEarUpdateCheckBox.setText("middle-ear");
        middleEarUpdateCheckBox.setId("middleEarCheckBox");

        CheckBox skinUpdateCheckBox = new CheckBox();
        skinUpdateCheckBox.setText("skin");
        skinUpdateCheckBox.setId("skinCheckBox");

        CheckBox boneMarrowUpdateCheckBox = new CheckBox();
        boneMarrowUpdateCheckBox.setText("bone-marrow");
        boneMarrowUpdateCheckBox.setId("boneMarrowCheckBox");

        CheckBox connectiveTissueUpdateCheckBox = new CheckBox();
        connectiveTissueUpdateCheckBox.setText("connective-tissue");
        connectiveTissueUpdateCheckBox.setId("connectiveTissueCheckBox");

        CheckBox liverUpdateCheckBox = new CheckBox();
        liverUpdateCheckBox.setText("liver");
        liverUpdateCheckBox.setId("liverCheckBox");


        ArrayList<CustomMenuItem> menuItems = new ArrayList<>();

        CustomMenuItem pancreasMenuItem = new CustomMenuItem();
        pancreasMenuItem.setContent(pancreasUpdateCheckBox);
        pancreasMenuItem.setHideOnClick(false);
        menuItems.add(pancreasMenuItem);

        CustomMenuItem liverMenuItem = new CustomMenuItem();
        liverMenuItem.setContent(liverUpdateCheckBox);
        liverMenuItem.setHideOnClick(false);
        menuItems.add(liverMenuItem);

        CustomMenuItem lungMenuItem = new CustomMenuItem();
        lungMenuItem.setContent(lungUpdateCheckBox);
        lungMenuItem.setHideOnClick(false);
        menuItems.add(lungMenuItem);

        CustomMenuItem heartMenuItem = new CustomMenuItem();
        heartMenuItem.setContent(heartUpdateCheckBox);
        heartMenuItem.setHideOnClick(false);
        menuItems.add(heartMenuItem);

        CustomMenuItem kidneyMenuItem = new CustomMenuItem();
        kidneyMenuItem.setContent(kidneyUpdateCheckBox);
        kidneyMenuItem.setHideOnClick(false);
        menuItems.add(kidneyMenuItem);

        CustomMenuItem intestineMenuItem = new CustomMenuItem();
        intestineMenuItem.setContent(intestineUpdateCheckBox);
        intestineMenuItem.setHideOnClick(false);
        menuItems.add(intestineMenuItem);

        CustomMenuItem corneaMenuItem = new CustomMenuItem();
        corneaMenuItem.setContent(corneaUpdateCheckBox);
        corneaMenuItem.setHideOnClick(false);
        menuItems.add(corneaMenuItem);

        CustomMenuItem middleEarMenuItem = new CustomMenuItem();
        middleEarMenuItem.setContent(middleEarUpdateCheckBox);
        middleEarMenuItem.setHideOnClick(false);
        menuItems.add(middleEarMenuItem);

        CustomMenuItem skinMenuItem = new CustomMenuItem();
        skinMenuItem.setContent(skinUpdateCheckBox);
        skinMenuItem.setHideOnClick(false);
        menuItems.add(skinMenuItem);

        CustomMenuItem boneMarrowMenuItem = new CustomMenuItem();
        boneMarrowMenuItem.setContent(boneMarrowUpdateCheckBox);
        boneMarrowMenuItem.setHideOnClick(false);
        menuItems.add(boneMarrowMenuItem);

        CustomMenuItem connectiveTissueMenuItem = new CustomMenuItem();
        connectiveTissueMenuItem.setContent(connectiveTissueUpdateCheckBox);
        connectiveTissueMenuItem.setHideOnClick(false);
        menuItems.add(connectiveTissueMenuItem);

        MenuButton organsMenu = new MenuButton();
        organsMenu.getItems().addAll(menuItems);

        HashMap<Organ, CheckBox> organTickBoxes;
        organTickBoxes = new HashMap<>();

        organTickBoxes.put(Organ.KIDNEY, kidneyUpdateCheckBox);
        organTickBoxes.put(Organ.CORNEA, corneaUpdateCheckBox);
        organTickBoxes.put(Organ.BONE, boneMarrowUpdateCheckBox);
        organTickBoxes.put(Organ.LIVER, liverUpdateCheckBox);
        organTickBoxes.put(Organ.EAR, middleEarUpdateCheckBox);
        organTickBoxes.put(Organ.HEART, heartUpdateCheckBox);
        organTickBoxes.put(Organ.INTESTINE, intestineUpdateCheckBox);
        organTickBoxes.put(Organ.PANCREAS, pancreasUpdateCheckBox);
        organTickBoxes.put(Organ.SKIN, skinUpdateCheckBox);
        organTickBoxes.put(Organ.TISSUE, connectiveTissueUpdateCheckBox);
        organTickBoxes.put(Organ.LUNG, lungUpdateCheckBox);


        for (Organ key : organTickBoxes.keySet()) {
            organTickBoxes.get(key).setSelected(selectedProcedure.getOrgansAffected().contains(key));
        }

        ArrayList<Organ> organsUpdatedAffected = new ArrayList<>();

        organsMenu.setText("Affecting which organs?");
        organsMenu.setId("updateOrganChoiceBox");

        grid.add(new Label("Summary:"), 0, 0);
        grid.add(procedureSummary, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(procedureDescription, 1, 1);
        grid.add(new Label("Date Due:"), 0, 2);
        grid.add(dateDue, 1, 2);
        grid.add(new Label("Organs:"), 0, 3);
        grid.add(organsMenu, 1, 3);


        // Enable/Disable login button depending on whether a username was entered.
        Node updateButton = dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        procedureSummary.textProperty().addListener((observable, oldValue, newValue) -> updateButton.setDisable(newValue.trim().isEmpty()));

        // Do some validation (using the Java 8 lambda syntax).
        procedureDescription.textProperty().addListener((observable, oldValue, newValue) -> updateButton.setDisable(newValue.trim().isEmpty()));

        dateDue.valueProperty().addListener((observable, oldValue, newValue) -> updateButton.setDisable(newValue.toString().trim().isEmpty()));

        for (CheckBox checkBox: organTickBoxes.values()) {
            checkBox.selectedProperty().addListener(((observable, oldValue, newValue) -> updateButton.setDisable(newValue == oldValue)));
        }

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(procedureSummary::requestFocus);

        // Convert the result to a diseaseName-dateOfDiagnosis-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                if (dateDue.getValue() == null){
                    Debugger.log(selectedProcedure.getDate().toString());
                } else {
                    Debugger.log(dateDue.getValue());
                }
                String newSummary;
                String newDescription;
                String newDate = "";

                for (CheckBox organCheckBox: organTickBoxes.values()) {
                    if(organCheckBox.isSelected()) {
                        organsUpdatedAffected.add(Organ.parse(organCheckBox.getText()));
                        organCheckBox.setSelected(false);
                    }
                }

                if (procedureSummary.getText().equals("")) {
                    newSummary = selectedProcedure.getSummary();
                } else {
                    newSummary = procedureSummary.getText();
                }

                if (procedureDescription.getText().equals("")) {
                    newDescription = selectedProcedure.getDescription();
                } else {
                    newDescription = procedureDescription.getText();
                }

                if (dateDue.getValue() == null) {
                    newDate = selectedProcedure.getDate().toString();
                } else {
                    if (dateDue.getValue().isBefore(currentUser.getDateOfBirth())) {
                        Alert alert = WindowManager.createAlert(Alert.AlertType.WARNING, "Invalid Procedure", "",
                                "Due date occurs before the user's date of birth.");
                        alert.showAndWait();
                        dateDue.getEditor().clear();
                        if (pending) {
                            updateProcedurePopUp(selectedProcedure, true);
                        } else {
                            updateProcedurePopUp(selectedProcedure, false);
                        }
                    } else {
                        newDate = dateDue.getValue().toString();
                    }
                }

                if (organsUpdatedAffected.size() == 0) {
                    organsUpdatedAffected.addAll(selectedProcedure.getOrgansAffected());
                }

                return new ArrayList<>(Arrays.asList(newSummary, newDescription, newDate));
            }
            return null;
        });

        Optional<ArrayList<String>> result = dialog.showAndWait();

        result.ifPresent(newProcedureDetails -> {
            Debugger.log("Summary=" + newProcedureDetails.get(0) + ", Description=" + newProcedureDetails.get(1) + ", DateDue=" +
                    newProcedureDetails.get(2));
            selectedProcedure.setSummary(newProcedureDetails.get(0));
            selectedProcedure.setDescription(newProcedureDetails.get(1));
            LocalDate newDateFormat = LocalDate.parse(newProcedureDetails.get(2));
            selectedProcedure.setDate(newDateFormat);
            selectedProcedure.setOrgansAffected(organsUpdatedAffected);
            userController.addHistoryEntry("Procedure updated", "A procedure (" + selectedProcedure.getSummary() + ") was updated.");
            if (pending) {
                if (newDateFormat.isAfter(LocalDate.now())) {
                    pendingProcedureItems.remove(selectedProcedure);
                    pendingProcedureItems.add(selectedProcedure);
                } else {
                    pendingProcedureItems.remove(selectedProcedure);
                    previousProcedureItems.add(selectedProcedure);
                }

            } else {
                if (newDateFormat.isAfter(LocalDate.now())) {
                    previousProcedureItems.remove(selectedProcedure);
                    pendingProcedureItems.add(selectedProcedure);
                } else {
                    previousProcedureItems.remove(selectedProcedure);
                    previousProcedureItems.add(selectedProcedure);
                }
            }
            saveToUndoStack();

        });
    }

    /**
     * Function which sets up all the listeners for the text fields and also populates
     * the table views based on the values in the observable array lists.
     * Also creates the menu items for right clicking on a procedure.
     */
    private void setupListeners() {

        pendingProcedureTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {

            if (newItem != null) {
                previousProcedureTableView.getSelectionModel().clearSelection();
                Procedure currentProcedure = pendingProcedureTableView.getSelectionModel().getSelectedItem();
                String organsString = "";
                if (currentProcedure.getOrgansAffected().size() == 0) {
                    organsString = "This procedure currently affects no organs.";
                    donatingLabel.setText(organsString);
                } else {
                    for (Organ organ : currentProcedure.getOrgansAffected()) {
                        organsString += organ.toString() + ", ";
                    }
                    donatingLabel.setText("* Organs affected: " + organsString.substring(0, organsString.length() - 2));
                }

            }


        });

        previousProcedureTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {

            if (newItem != null) {
                pendingProcedureTableView.getSelectionModel().clearSelection();
                Procedure currentProcedure = previousProcedureTableView.getSelectionModel().getSelectedItem();
                String organsString = "";
                if (currentProcedure.getOrgansAffected().size() == 0) {
                    organsString = "This procedure currently affects no organs.";
                    donatingLabel.setText(organsString);
                } else {
                    for (Organ organ : currentProcedure.getOrgansAffected()) {
                        organsString += organ.toString() + ", ";
                    }
                    donatingLabel.setText("* Organs affected: " + organsString.substring(0, organsString.length() - 2));
                }
            }

        });

        final ContextMenu pendingProcedureListContextMenu = new ContextMenu();

        // Update selected procedure on the pending procedures table
        MenuItem updatePendingProcedureMenuItem = new MenuItem();
        updatePendingProcedureMenuItem.setOnAction(event -> {
            Procedure selectedProcedure = pendingProcedureTableView.getSelectionModel().getSelectedItem();
            updateProcedurePopUp(selectedProcedure, true);
            statusIndicator.setStatus("Edited " + selectedProcedure, false);
            titleBar.saved(false);
        });
        pendingProcedureListContextMenu.getItems().add(updatePendingProcedureMenuItem);

        final ContextMenu previousProcedureListContextMenu = new ContextMenu();

        // Update selected procedure on the previous procedures table
        MenuItem updatePreviousProcedureMenuItem = new MenuItem();
        updatePreviousProcedureMenuItem.setOnAction(event -> {
            Procedure selectedProcedure = previousProcedureTableView.getSelectionModel().getSelectedItem();
            updateProcedurePopUp(selectedProcedure, false);
            statusIndicator.setStatus("Edited " + selectedProcedure, false);
            titleBar.saved(false);

        });
        previousProcedureListContextMenu.getItems().add(updatePreviousProcedureMenuItem);

        /*Handles the right click action of showing a ContextMenu on the pendingProcedureTableView and sets the MenuItem
        text depending on the procedure chosen*/
        pendingProcedureTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                Procedure selectedProcedure = pendingProcedureTableView.getSelectionModel().getSelectedItem();
                updatePendingProcedureMenuItem.setText("Update pending procedure");
                pendingProcedureListContextMenu.show(pendingProcedureTableView, event.getScreenX(), event.getScreenY());
            }
        });

        /*Handles the right click action of showing a ContextMenu on the previousProcedureTableView and sets the MenuItem
        text depending on the procedure chosen*/
        previousProcedureTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                Procedure selectedProcedure = previousProcedureTableView.getSelectionModel().getSelectedItem();
                updatePreviousProcedureMenuItem.setText("Update previous procedure");
                previousProcedureListContextMenu.show(previousProcedureTableView, event.getScreenX(), event.getScreenY());
            }
        });

        // Sets the cell factory to style each pending procedure item depending on its details
        pendingSummaryColumn.setCellFactory(column -> new TableCell<Procedure, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {

                    setText(item);
                    Procedure currentProcedure = getTableView().getItems().get(getIndex());

                    // If the disease is chronic, update label + colour
                    if (currentProcedure.getOrgansAffected().size() > 0) {
                        setText("* " + item);
                        this.setStyle("-fx-background-color: GREY;");
                    }
                }
            }
        });

        // Sets the cell factory to style each previous Procedure item depending on its details
        previousSummaryColumn.setCellFactory(column -> new TableCell<Procedure, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {

                    setText(item);
                    Procedure currentProcedure = getTableView().getItems().get(getIndex());

                    // If the disease is chronic, update label + colour
                    if (currentProcedure.getOrgansAffected().size() > 0) {
                        setText("* " + item);
                        this.setStyle("-fx-background-color: GREY;");

                    }
                }
            }
        });

        // Set up columns to extract correct information from a Procedure object
        pendingDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        pendingSummaryColumn.setCellValueFactory(new PropertyValueFactory<>("summary"));
        pendingDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        previousDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        previousSummaryColumn.setCellValueFactory(new PropertyValueFactory<>("summary"));
        previousDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        previousProcedureTableView.setSortPolicy(t -> {
            Comparator<Procedure> comparator1 = (r1,r2)
            -> r1.getDate().isBefore(r2.getDate()) ? 1 : r1.getDate().isAfter(r2.getDate()) ? -1 : 0;
            FXCollections.sort(previousProcedureTableView.getItems(), comparator1);
            return true;
        });


    }

    /**
     * Sets whether the control buttons are shown or not on the medications pane
     *
     * @param shown Boolean that sets if the fxml items are visible or not
     */
    public void setControlsShown(boolean shown) {
        dateOfProcedureInput.setVisible(shown);
        summaryInput.setVisible(shown);
        descriptionInput.setVisible(shown);
        newProcedureDateLabel.setVisible(shown);
        newProcedureLabel.setVisible(shown);
        pendingProceduresLabel.setVisible(true);
        previousProceduresLabel.setVisible(true);
        addNewProcedureButton.setVisible(shown);
        pendingProcedureTableView.setDisable(!shown);
        previousProcedureTableView.setDisable(!shown);
        deleteProcedureButton.setVisible(shown);
        organAffectChoiceBox.setVisible(shown);
    }


    /**
     * updates the users procedures when a procedure date is passed
     * UNUSED
     */
    private void updatePendingProcedures() {
        //Check if pending procedure due date is now past the current date
        for (Procedure procedure : currentUser.getPendingProcedures()) {
            if (procedure.getDate().isBefore(LocalDate.now())) {
                currentUser.getPendingProcedures().remove(procedure);
                currentUser.getPreviousProcedures().add(procedure);
            }
        }
    }

    /**
     * Function to set the current user of this class to that of the instance of the application.
     *
     * @param currentUser The donor to set the current donor.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        donorNameLabel.setText("user: " + currentUser.getName());

        //Check if pending procedure due date is now past the current date
        for (Procedure procedure : currentUser.getPendingProcedures()) {
            if (procedure.getDate().isBefore(LocalDate.now())) {
                currentUser.getPendingProcedures().remove(procedure);
                currentUser.getPreviousProcedures().add(procedure);
            }
        }

        pendingProcedureItems = FXCollections.observableArrayList();
        pendingProcedureItems.addAll(currentUser.getPendingProcedures());
        pendingProcedureItems.sort((a,b)-> a.getDate().isBefore(b.getDate()) ?  -1 : a.getDate().isAfter(b.getDate()) ? 1 : 0);
        pendingProcedureTableView.setItems(pendingProcedureItems);

        previousProcedureItems = FXCollections.observableArrayList();
        previousProcedureItems.addAll(currentUser.getPreviousProcedures());
        previousProcedureItems.sort((a,b)-> a.getDate().isBefore(b.getDate()) ? 1 : a.getDate().isAfter(b.getDate()) ? -1 : 0);
        previousProcedureTableView.setItems(previousProcedureItems);
    }

    /**
     * undos the last change
     */
    @Override
    public void undo() {
        updateUser();
        //Add the current procedures lists to the redo stack
        redoStack.add(new User(currentUser));
        //Copy the procedures lists from the top element of the undo stack
        currentUser.copyProceduresListsFrom(undoStack.getLast());
        //Remove the top element of the undo stack
        undoStack.removeLast();
        updateProcedures();
    }

    /**
     * redos the last undo
     */
    @Override
    public void redo() {
        updateUser();
        //Add the current procedures lists to the redo stack
        undoStack.add(new User(currentUser));
        //Copy the procedures lists from the top element of the undo stack
        currentUser.copyProceduresListsFrom(redoStack.getLast());
        //Remove the top element of the undo stack
        redoStack.removeLast();
        updateProcedures();
    }
}
