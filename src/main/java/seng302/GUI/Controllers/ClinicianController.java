package seng302.GUI.Controllers;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import seng302.GUI.TitleBar;
import seng302.Generic.*;
import org.controlsfx.control.StatusBar;
import seng302.GUI.StatusIndicator;
import seng302.GUI.Controllers.UserWindowController;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import seng302.GUI.TFScene;
import seng302.User.Clinician;
import seng302.User.User;

import static seng302.Generic.Main.streamOut;

/**
 * Class to control all the logic for the clinician interactions with the application.
 */
public class ClinicianController implements Initializable {


    private Clinician clinician;

    @FXML
    private TableColumn profileName;

    @FXML
    private TableColumn profileAge;

    @FXML
    private TableColumn profileGender;

    @FXML
    private TableColumn profileRegion;

    @FXML
    private TableView profileTable;
    @FXML
    private TextField profileSearchTextField;
    @FXML
    private Pane background;
    @FXML
    private TextField nameInput;
    @FXML
    private Label staffIDLabel;
    @FXML
    private TextField addressInput;
    @FXML
    private TextField regionInput;
    @FXML
    private MenuItem accountSettingsMenuItem;

    @FXML
    private Label userDisplayText;

    @FXML
    private Button nextPageButton;

    @FXML
    private Button previousPageButton;

    @FXML
    private Label resultsDisplayLabel;

    @FXML
    private Button undoWelcomeButton;

    @FXML
    private Button redoWelcomeButton;

    @FXML
    private GridPane mainPane;

    @FXML
    private StatusBar statusBar;

    private StatusIndicator statusIndicator = new StatusIndicator();
    private TitleBar titleBar = new TitleBar();

    private int resultsPerPage;
    private int page = 1;
    private ArrayList<User> usersFound;

    private ArrayList<UserWindowController> userWindows = new ArrayList<UserWindowController>();

    private ArrayList<Clinician> clinicianUndoStack = new ArrayList<>();
    private ArrayList<Clinician> clinicianRedoStack = new ArrayList<>();


    private ObservableList<User> currentPage = FXCollections.observableArrayList();

    ObservableList<Object> users;

    public void setTitle(){
        titleBar.setTitle(clinician.getName(), "Clinician", null);
    }

    /**
     * Sets the current clinician
     * @param clinician The clinician to se as the current
     */
    public void setClinician(Clinician clinician) {
        this.clinician = clinician;
        updateDisplay();
    }

    /**
     * Updates all the displayed TextFields to the values
     * from the current clinician
     */
    public void updateDisplay() {
        titleBar.setTitle(clinician.getName(), "Clinician", null);
        System.out.print(clinician);
        userDisplayText.setText("Welcome " + clinician.getName());
        nameInput.setText(clinician.getName());
        staffIDLabel.setText(Long.toString(clinician.getStaffID()));
        addressInput.setText(clinician.getWorkAddress());
        regionInput.setText(clinician.getRegion());
    }

    /**
     * Update the window title when there are unsaved changes
     */
    @FXML
    private void edited(){
        titleBar.saved(false);
    }

    /**
     * Refreshes the results in the user profile table to match the values
     * in the user ArrayList in Main
     */
    public void updateUserTable(){
        updatePageButtons();
        displayCurrentPage();
        updateResultsSummary();
    }

    /**
     * Logs out the clinician. The user is asked if they're sure they want to log out, if yes,
     * all open user windows spawned by the clinician are closed and the main scene is returned to the logout screen.
     */
    public void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Are you sure?");
        alert.setHeaderText("Are you sure would like to log out? ");
        alert.setContentText("Logging out without saving loses your non-saved data.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            for(Stage userWindow: Main.getCliniciansUserWindows()){
                userWindow.close();
            }
            Main.setScene(TFScene.login);
            Main.clearUserScreen();
        } else {
            alert.close();
        }
    }



    /**
     * Function which is called when the user wants to update their account settings in the user Window,
     * and creates a new account settings window to do so. Then does a prompt for the password as well.
     */
    public void updateAccountSettings() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("View Account Settings");
        dialog.setHeaderText("In order to view your account settings, \nplease enter your login details.");
        dialog.setContentText("Please enter your password:");

        Optional<String> password = dialog.showAndWait();
        if(password.isPresent()){ //Ok was pressed, Else cancel
            if(password.get().equals(clinician.getPassword())){
                try {
                    Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/clinicianAccountSettings.fxml"));
                    Stage stage = new Stage();
                    stage.setTitle("Account Settings");
                    stage.setScene(new Scene(root, 290, 350));
                    stage.initModality(Modality.APPLICATION_MODAL);

                    Main.setCurrentClinicianForAccountSettings(clinician);

                    stage.showAndWait();
                } catch (Exception e) {
                    System.out.println("here");
                    e.printStackTrace();
                }
            }else{ // Password incorrect
                Main.createAlert(Alert.AlertType.INFORMATION, "Incorrect",
                        "Incorrect password. ", "Please enter the correct password to view account settings").show();
            }
        }
    }



    /**
     * Updates the current clinicians attributes to
     * reflect those of the values in the displayed TextFields
     */
    public void updateClinician() {
        addClinicianToUndoStack(clinician);
        clinician.setName(nameInput.getText());
        clinician.setWorkAddress(addressInput.getText());
        clinician.setRegion(regionInput.getText());
        titleBar.setTitle(clinician.getName(), "Clinician", null);
        statusIndicator.setStatus("Updated clinician details", false);

        System.out.println("Updated to: " + clinician);
    }

    /**
     * Saves the clinician ArrayList to a JSON file
     */
    public void save(){
        Main.saveUsers(Main.getClinicianPath(), false);
    }

    /**
     * Closes the application
     */
    public void close(){
        Platform.exit();
    }

    /**
     * Changes the focus to the pane when pressed
     */
    public void requestFocus() { background.requestFocus(); }

    /**
     * The main clincian undo function. Called from the button press, reads from the undo stack and then updates the GUI accordingly.
     */
    public void undo(){
        clinician = clinicianUndo(clinician);
        updateDisplay();
        redoWelcomeButton.setDisable(false);

        if (clinicianUndoStack.isEmpty()){
            undoWelcomeButton.setDisable(true);
        }
        titleBar.saved(false);
        statusIndicator.setStatus("Undid last action", false);
    }

    /**
     * The main clincian redo function. Called from the button press, reads from the redo stack and then updates the GUI accordingly.
     */
    public void redo(){
        clinician = clinicianRedo(clinician);
        updateDisplay();
        undoWelcomeButton.setDisable(false);
        if(clinicianRedoStack.isEmpty()){
            redoWelcomeButton.setDisable(true);
        }
        titleBar.saved(false);
        statusIndicator.setStatus("Redid last action", false);
    }

    /**
     * Reads the top element of the undo stack and removes it, while placing the current clinician in the redo stack.
     * Then returns the clinician from the undo stack.
     * @param oldClinician the clincian being placed in the redo stack.
     * @return the previous iteration of the clinician object.
     */
    public Clinician clinicianUndo(Clinician oldClinician) {
        if (clinicianUndoStack != null) {
            Clinician newClinician = clinicianUndoStack.get(clinicianUndoStack.size() - 1);
            clinicianUndoStack.remove(clinicianUndoStack.size() - 1);
            clinicianRedoStack.add(oldClinician);
            return newClinician;
        }
        return null;
    }

    /**
     * Creates a deep copy of the current clinician and adds that copy to the undo stack. Then updates the GUI button to be usable.
     * @param clinician the clinician object being copied.
     */
    public void addClinicianToUndoStack(Clinician clinician) {
        Clinician prevClinician = new Clinician(clinician);
        clinicianUndoStack.add(prevClinician);
        if (undoWelcomeButton.isDisable()) {
            undoWelcomeButton.setDisable(false);
        }
    }

    /**
     * Pops the topmost clinician object from the redo stack and returns it, while adding the provided clinician object to the undo stack.
     * @param newClinician the clinican being placed on the undo stack.
     * @return the topmost clinician object on the redo stack.
     */
    public Clinician clinicianRedo(Clinician newClinician){
        if (clinicianRedoStack != null) {
            Clinician oldClinician = clinicianRedoStack.get(clinicianRedoStack.size() - 1);
            addClinicianToUndoStack(newClinician);
            clinicianRedoStack.remove(clinicianRedoStack.size() - 1);
            return oldClinician;
        } else{
            return null;
        }
    }


    /**
     * Updates the ObservableList for the profile table
     */
    public void displayCurrentPage() {
        currentPage.clear();
        currentPage.addAll(getCurrentPage());
    }

    /**
     * Updates the list of users found from the search
     * @param searchTerm the search term
     */
    public void updateFoundUsers(String searchTerm){
        usersFound = Main.getUsersByNameAlternative(searchTerm);
        users = FXCollections.observableArrayList(usersFound);

    }


    /**
     * Splits the sorted list of found users and returns a page worth
     * @return The sorted page of results
     */
    public ObservableList<User> getCurrentPage(){
        int firstIndex = Math.max((page-1),0)*resultsPerPage;
        int lastIndex = Math.min(users.size(), page*resultsPerPage);
        if(lastIndex<firstIndex){
            System.out.println(firstIndex+" to "+lastIndex+ " is an illegal page");
            return FXCollections.observableArrayList(new ArrayList<User>());
        }
        return FXCollections.observableArrayList(new ArrayList(users.subList(firstIndex, lastIndex)));
    }

    /**
     * Displays the next page of results.
     */
    public void nextPage(){
        page++;
        updateUserTable();
    }

    /**
     * Updates the resultsDisplayLabel to show how many results were found,
     * and how many are displayed.
     */
    public void updateResultsSummary(){
        String text;
        if(usersFound.size()==0){
            text = "No results found";
        }else{
            int from = ((page-1)*resultsPerPage)+1;
            int to = Math.min((page*resultsPerPage), usersFound.size());
            int of = usersFound.size();
            text = "Displaying " + from + "-" + to + " of " + of;
        }
        resultsDisplayLabel.setText(text);
    }

    /**
     * Displays the next page of user search results
     */
    public void previousPage(){
        page--;
        updateUserTable();
    }

    /**
     * Enables and disables the next and previous page buttons as necessary.
     */
    public void updatePageButtons(){
        if((page)*resultsPerPage>=usersFound.size()){
            nextPageButton.setDisable(true);
        }else{
            nextPageButton.setDisable(false);
        }
        if(page==1){
            previousPageButton.setDisable(true);
        }else{
            previousPageButton.setDisable(false);
        }
    }

    /**
     * Sets the User Attribute pane as the visible pane
     */
    public void showMainPane() {
        mainPane.setVisible(true);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resultsPerPage = 15;
        profileSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            page = 1;
            updateFoundUsers(newValue);
            updateUserTable();
        });

        profileName.setCellValueFactory(new PropertyValueFactory<>("name"));
        profileAge.setCellValueFactory(new PropertyValueFactory<>("ageString"));
        profileGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        profileRegion.setCellValueFactory(new PropertyValueFactory<>("region"));

        profileTable.setItems(currentPage);

        Main.setClinicianController(this);

        updateFoundUsers("");
        updateUserTable();

        profileTable.setItems(currentPage);

        /**
         * RowFactory for the profileTable.
         * Displays a tooltip when the mouse is over a table entry.
         * Adds a mouse click listener to each row in the table so that a user window
         * is opened when the event is triggered
         */
        profileTable.setRowFactory(new Callback<TableView<User>, TableRow<User>>() {
            @Override
            public TableRow<User> call(TableView<User> tableView) {
                final TableRow<User> row = new TableRow<User>() {
                    private Tooltip tooltip = new Tooltip();
                    @Override
                    public void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        if (user == null || empty) {
                            setTooltip(null);
                        } else {
                            if (user.getOrgans().isEmpty()) {
                                tooltip.setText(user.getName() + ".");
                            } else {
                                String organs = user.getOrgans().toString();
                                tooltip.setText(user.getName() + ". User: " + organs.substring(1, organs.length() - 1));
                            }
                            setTooltip(tooltip);
                        }
                    }
                };
                row.setOnMouseClicked(event -> {
                    if (!row.isEmpty() && event.getClickCount()==2) {
                        System.out.println(row.getItem());
                        Stage stage = new Stage();
                        stage.setMinHeight(550);
                        stage.setMinWidth(650);

                        Main.addCliniciansUserWindow(stage);
                        stage.initModality(Modality.NONE);

                        try{
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/userWindow.fxml"));
                            Parent root = (Parent) loader.load();
                            UserWindowController userWindowController = loader.getController();
                            userWindowController.setTitleBar(stage);
                            Main.setCurrentUser(row.getItem());

                            String text = History.prepareFileStringGUI(row.getItem().getId(), "view");
                            History.printToFile(streamOut, text);

                            userWindowController.populateUserFields();
                            userWindowController.populateHistoryTable();
                            userWindowController.showWaitingListButton();
                            Main.controlViewForClinician();
                            Main.medicalHistoryDiseasesViewForClinician();
                            Main.medicalHistoryProceduresViewForClinician();

                            Scene newScene = new Scene(root, 900, 575);
                            stage.setScene(newScene);
                            stage.show();
                            userWindowController.setAsChildWindow();
                        } catch (IOException | NullPointerException e) {
                            System.err.println("Unable to load fxml or save file.");
                            e.printStackTrace();
                            Platform.exit();
                        }
                    }
                });
                return row;
            }
        });



        profileTable.refresh();
        /**
         * Sorts of the profileTable across all pages.
         * As items are removed and re-added, multiple sort calls can trigger an
         * IndexOutOfBoundsException exception.
         */
        profileTable.setSortPolicy(new Callback<TableView, Boolean>() {
            @Override public Boolean call(TableView table) {
                try{
                    Comparator comparator = table.getComparator();
                    if (comparator == null) {
                        return true;
                    }
                    FXCollections.sort(users, comparator);
                    displayCurrentPage();
                    profileTable.getSelectionModel().select(0);
                    return true;
                }catch(IndexOutOfBoundsException e){
                    System.out.println("Error");
                    return false;
                }catch(UnsupportedOperationException e){
                    return false;
                }catch(NullPointerException e){
                    return false;
                }
            }
        });
        statusIndicator.setStatusBar(statusBar);
        titleBar.setStage(Main.getStage());
    }

    /**
     * calls the transplantWaitingList controller and displays it.
     * also refreshes the waitinglist table data
     */
    public void transplantWaitingList() {
        Main.getTransplantWaitingListController().updateTransplantList();
        //background.setVisible(false);
        Main.setScene(TFScene.transplantList);
        titleBar.setTitle(clinician.getName(), "Clinician", "Transplant Waiting List");
    }
}
