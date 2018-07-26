package seng302.GUI.Controllers.User;

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.http.client.HttpResponseException;
import seng302.Generic.Debugger;
import seng302.Generic.WindowManager;
import seng302.User.Attribute.*;
import seng302.User.User;
import seng302.User.WaitingListItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

public class UserAttributesController extends UserTabController implements Initializable {
    @FXML
    private Label settingAttributesLabel, ageLabel, bmiLabel;
    @FXML
    private TextField firstNameField, middleNameField, lastNameField, addressField, regionField, heightField, weightField, bloodPressureTextField, preferredFirstNameField, preferredMiddleNamesField, preferredLastNameField;
    @FXML
    private DatePicker dateOfBirthPicker, dateOfDeathPicker;
    @FXML
    private ComboBox<Gender> genderComboBox, genderIdentityComboBox;
    @FXML
    private ComboBox<BloodType> bloodTypeComboBox;
    @FXML
    private ComboBox<SmokerStatus> smokerStatusComboBox;
    @FXML
    private ComboBox<AlcoholConsumption> alcoholConsumptionComboBox;
    @FXML
    private CheckBox liverCheckBox, kidneyCheckBox, pancreasCheckBox, heartCheckBox, lungCheckBox, intestineCheckBox, corneaCheckBox,
        middleEarCheckBox, skinCheckBox, boneMarrowCheckBox, connectiveTissueCheckBox;
    @FXML
    private ImageView profileImage;
    @FXML
    private Button changePhotoButton;

    private Map<Organ, CheckBox> organTickBoxes;

    public void setCurrentUser(User user) {
        currentUser = user;
        populateUserFields();
        updateBMI();
        updateAge();
    }

    /**
     * Highlights the checkboxes in red if the user is also waiting to receive an organ of that type.
     * A tooltip is also added to highlighted checkboxes which tells the user what the problem is
     */
    public void highlightOrganCheckBoxes(){
        for(Organ organ: Organ.values()){
            if(organTickBoxes.get(organ).getStyleClass().contains("highlighted-checkbox")){
                organTickBoxes.get(organ).getStyleClass().remove("highlighted-checkbox");
                organTickBoxes.get(organ).setTooltip(null);
            }
            for(WaitingListItem item: currentUser.getWaitingListItems()){
                if(!organTickBoxes.get(organ).getStyleClass().contains("highlighted-checkbox") && item.getOrganType() == organ && organTickBoxes.get(organ).isSelected() && item.getStillWaitingOn()){
                    organTickBoxes.get(organ).getStyleClass().add("highlighted-checkbox");
                    organTickBoxes.get(organ).setTooltip(new Tooltip("User is waiting to receive this organ"));
                }
            }
        }
    }


    /**
     * Updates the age label for the user window based on the date of birth and death of the user.
     */
    public void updateAge() {
        LocalDate dobirthPick = dateOfBirthPicker.getValue();
        LocalDate dodeathPick = dateOfDeathPicker.getValue();

        if (dodeathPick == null) {
            LocalDate today = LocalDate.now();
            double years = Duration.between(dobirthPick.atStartOfDay(), today.atStartOfDay()).toDays() / 365.00;
            if (years <= 0) {
                ageLabel.setText("Age: Invalid Input.");
            } else {
                ageLabel.setText("Age: " + String.format("%.1f", years) + " years");
            }
        } else {
            double years = Duration.between(dobirthPick.atStartOfDay(), dodeathPick.atStartOfDay()).toDays() / 365.00;
            if (years <= 0) {
                ageLabel.setText("Age: Invalid Input.");
            } else {
                ageLabel.setText("Age: " + String.format("%.1f", years) + " years (At Death)");
            }
        }
    }

    /**
     * Updates the BMI label for the user based on the height and weight fields inputted.
     */
    private void updateBMI() {
        bmiLabel.setText("");
        if (!heightField.getText().isEmpty() && !weightField.getText().isEmpty()) {
            try {
                double height = Double.parseDouble(heightField.getText());
                double weight = Double.parseDouble(weightField.getText());
                double BMI = (weight / Math.pow(height, 2)) * 10000;
                if (!Double.isNaN(BMI)) {
                    bmiLabel.setText("BMI: " + String.format("%.2f", BMI));
                }
            } catch (NumberFormatException e) {
            }
        }
    }

    /**
     * /**
     * Function which takes all the inputs of the user attributes window.
     * Checks if all these inputs are valid and then sets the user's attributes to those inputted.
     *
     * @return returns boolean of it working or not
     */
    public boolean updateUser() {
        //Extract names from user
        String firstName = firstNameField.getText();
        String[] middleNames = middleNameField.getText().isEmpty() ? new String[]{""} : middleNameField.getText().split(",");
        String lastName = lastNameField.getText();

        int isLastName = lastNameField.getText() == null || lastNameField.getText().isEmpty() ? 0 : 1;
        String[] name = new String[1 + middleNames.length + isLastName];
        name[0] = firstName;
        System.arraycopy(middleNames, 0, name, 1, middleNames.length);
        if (isLastName == 1) {
            name[name.length - 1] = lastName;
        }

        String preferredFirstName = preferredFirstNameField.getText();
        String[] preferredMiddleNames = preferredMiddleNamesField.getText().isEmpty() ? new String[]{""} : preferredMiddleNamesField.getText().split(",");
        String preferredLastName = preferredLastNameField.getText();

        int isPreferredLastName = preferredLastNameField.getText() == null || preferredLastNameField.getText().isEmpty() ? 0 : 1;
        String[] preferredName = new String[1 + preferredMiddleNames.length + isPreferredLastName];
        preferredName[0] = preferredFirstName;
        System.arraycopy(preferredMiddleNames, 0, preferredName, 1, preferredMiddleNames.length);
        if (isLastName == 1) {
            preferredName[preferredName.length - 1] = preferredLastName;
        }


        double userHeight = -1;
        if (!heightField.getText().equals("")) {
            try {
                userHeight = Double.parseDouble(heightField.getText());
                currentUser.setHeight(userHeight);
            } catch (NumberFormatException e) {
                WindowManager.createAlert(AlertType.ERROR, "Error", "Error with the Height Input ", "Please input a valid height input.").show();
                userController.requestFocus();
                return false;
            }
        }

        double userWeight = -1;
        if (!weightField.getText().equals("")) {
            try {
                userWeight = Double.parseDouble(weightField.getText());
                currentUser.setWeight(userWeight);
            } catch (NumberFormatException e) {
                WindowManager.createAlert(AlertType.ERROR, "Error", "Error with the Weight Input ", "Please input a valid weight input.").show();
                userController.requestFocus();
                return false;
            }
        }

        String userBloodPressure = "";
        String bloodPressure = bloodPressureTextField.getText();
        if (bloodPressure != null && !bloodPressure.equals("")) {
            String[] bloodPressureList = bloodPressureTextField.getText().split("/");
            if (bloodPressureList.length != 2) {
                WindowManager.createAlert(AlertType.ERROR, "Error", "Error with the Blood Pressure Input ", "Please input a valid blood pressure " +
                    "input.").show();
                userController.requestFocus();
                return false;
            } else {
                for (String pressureComponent : bloodPressureList) {
                    try {
                        Integer.parseInt(pressureComponent);
                    } catch (NumberFormatException e) {
                        WindowManager.createAlert(AlertType.ERROR, "Error", "Error with the Blood Pressure Input ", "Please input a valid blood " +
                            "pressure input.").show();
                        userController.requestFocus();
                        return false;
                    }
                }
                userBloodPressure = bloodPressureTextField.getText();
            }
        }

        LocalDate currentDate = LocalDate.now();
        if (dateOfBirthPicker.getValue().isAfter(currentDate)) {
            WindowManager.createAlert(AlertType.ERROR, "Error", "Error with the Date Input ", "The date of birth cannot be after today.").show();
            userController.requestFocus();
            return false;
        } else if (dateOfDeathPicker.getValue() != null && dateOfDeathPicker.getValue().isAfter(currentDate)) {
            WindowManager.createAlert(AlertType.ERROR, "Error", "Error with the Date Input ", "The date of death cannot be after today.").show();
            userController.requestFocus();
            return false;
        } else if (dateOfDeathPicker.getValue() != null && dateOfBirthPicker.getValue().isAfter(dateOfDeathPicker.getValue())) {
            WindowManager.createAlert(AlertType.ERROR, "Error", "Error with the Date Input ", "The date of birth cannot be after the date of death" +
                ".").show();
            userController.requestFocus();
            return false;
        }

        userController.addHistoryEntry("Updated attribute", "A user attribute was updated.");
        //Commit changes
        currentUser.setNameArray(name);
        currentUser.setPreferredNameArray(preferredName);
        currentUser.setHeight(userHeight);
        currentUser.setWeight(userWeight);
        currentUser.setBloodPressure(userBloodPressure);
        currentUser.setDateOfBirth(dateOfBirthPicker.getValue());
        currentUser.setDateOfDeath(dateOfDeathPicker.getValue());
        currentUser.setGender(genderComboBox.getValue());
        currentUser.setGenderIdentity(genderIdentityComboBox.getValue());
        currentUser.setBloodType(bloodTypeComboBox.getValue());
        currentUser.setAlcoholConsumption(alcoholConsumptionComboBox.getValue());
        currentUser.setSmokerStatus(smokerStatusComboBox.getValue());
        currentUser.setRegion(regionField.getText());
        currentUser.setCurrentAddress(addressField.getText());
        for (Organ key : organTickBoxes.keySet()) {
            if (currentUser.getOrgans().contains(key)) {
                if (!organTickBoxes.get(key).isSelected()) {
                    currentUser.getOrgans().remove(key);
                }
            } else {
                if (organTickBoxes.get(key).isSelected()) {
                    currentUser.getOrgans().add(key);
                }
            }
        }
        userController.setWelcomeText("Welcome, " + currentUser.getPreferredName());
        settingAttributesLabel.setText("Attributes for " + currentUser.getPreferredName());
        return true;
    }

    /**
     * Function which takes the current user object that has logged in and
     * takes all their attributes and populates the user attributes on the attributes pane accordingly.
     */
    public void populateUserFields() {
        settingAttributesLabel.setText("Attributes for " + currentUser.getPreferredName());
        String[] splitNames = currentUser.getNameArray();
        firstNameField.setText(splitNames[0]);
        if (splitNames.length > 2) {
            String[] middleName = new String[splitNames.length - 2];
            System.arraycopy(splitNames, 1, middleName, 0, splitNames.length - 2);
            middleNameField.setText(String.join(",", middleName));
            lastNameField.setText(splitNames[splitNames.length - 1]);
        } else if (splitNames.length == 2) {
            middleNameField.setText("");
            lastNameField.setText(splitNames[1]);
        } else {
            middleNameField.setText("");
            lastNameField.setText("");
        }
        String[] splitPreferredNames = currentUser.getPreferredNameArray();
        preferredFirstNameField.setText(splitPreferredNames[0]);
        if (splitPreferredNames.length > 2) {
            String[] preferredMiddleName = new String[splitPreferredNames.length - 2];
            System.arraycopy(splitPreferredNames, 1, preferredMiddleName, 0, splitPreferredNames.length - 2);
            preferredMiddleNamesField.setText(String.join(",", preferredMiddleName));
            preferredLastNameField.setText(splitPreferredNames[splitPreferredNames.length - 1]);
        } else if (splitPreferredNames.length == 2) {
            preferredMiddleNamesField.setText("");
            preferredLastNameField.setText(splitPreferredNames[1]);
        } else {
            preferredMiddleNamesField.setText("");
            preferredLastNameField.setText("");
        }
        addressField.setText(currentUser.getCurrentAddress());
        regionField.setText(currentUser.getRegion());

        dateOfBirthPicker.setValue(currentUser.getDateOfBirth());
        dateOfDeathPicker.setValue(currentUser.getDateOfDeath());
        updateAge();

        bloodPressureTextField.setText(currentUser.getBloodPressure());

        genderComboBox.setValue(currentUser.getGender());
        genderIdentityComboBox.setValue(currentUser.getGenderIdentity());
        bloodTypeComboBox.setValue(currentUser.getBloodType());
        smokerStatusComboBox.setValue(currentUser.getSmokerStatus());
        alcoholConsumptionComboBox.setValue(currentUser.getAlcoholConsumption());

        for (Organ key : organTickBoxes.keySet()) {
            organTickBoxes.get(key).setSelected(currentUser.getOrgans().contains(key));
        }

        weightField.setText(currentUser.getWeight() == -1 ? "" : Double.toString(currentUser.getWeight()));
        heightField.setText(currentUser.getHeight() == -1 ? "" : Double.toString(currentUser.getHeight()));
        //set profile image

        Debugger.log("Attempting to update photo when populating attributes page");
        profileImage.setImage(WindowManager.getDataManager().getUsers().getUserPhoto((int) currentUser.getId()));

        updateBMI();
        highlightOrganCheckBoxes();
    }


    /**
     * Checks for any new updates when an attribute field loses focus, and appends to the attribute undo stack if there is new changes.
     */
    public void attributeFieldUnfocused() {
        User oldFields = new User(currentUser);
        if (updateUser() && !currentUser.attributeFieldsEqual(oldFields)) {
            addToUndoStack(oldFields);
            userController.setUndoRedoButtonsDisabled(false, true);
            titleBar.saved(false);
            statusIndicator.setStatus("Edited user details", false);
        }
    }


    /**
     * Called when the upload photo button is pressed. Presents a dialog with a choice of deleting an uploaded photo, or uploading a new one.
     */
    public void changeProfilePhoto() {

            List<String> options = new ArrayList<>();
            options.add("Upload a new profile photo");
            if (currentUser.getProfileImageType() != null) {
                options.add("Delete the current profile photo");
            }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Photo Options");
        alert.setHeaderText("Maximum Image size is 5MB.");
        alert.setContentText("Accepted file formats are JPG, PNG, and BMP. Images MUST be square.");

        ButtonType upload_new_photo = new ButtonType("Upload new");
        ButtonType use_default_photo = new ButtonType("Use default");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(upload_new_photo, use_default_photo, buttonTypeCancel);
        WindowManager.setIconAndStyle(alert.getDialogPane());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == upload_new_photo){
            uploadProfileImage();
        } else if (result.get() == use_default_photo) {
            deleteProfileImage();
        } /*

            ChoiceDialog<String> dialog = new ChoiceDialog<>("Upload a new profile photo", options);
            WindowManager.setIconAndStyle(dialog.getDialogPane());
            dialog.setTitle("Change Profile Photo");
            dialog.setHeaderText("Max image size is 5MB. Accepted image formats are BMP, PNG, JPEG.");
            dialog.setContentText("Select an option:");

            //Get Input Code
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(this::processPhoto);
            */

    }

    private void processPhoto(String option) {
        if (Objects.equals(option, "Upload a new profile photo")){
            uploadProfileImage();
        } else {
            deleteProfileImage();
        }
    }

    /**
     * Removes a profile photo from the server, if it exists. Then fetches the default photo with a seperate request.
     * This maintains REST.
     */
    private void deleteProfileImage() {
        try {
            WindowManager.getDataManager().getUsers().deleteUserPhoto(currentUser.getId());
            //success catch, set to default photo
            Image profilePhoto = WindowManager.getDataManager().getUsers().getUserPhoto(currentUser.getId());
            profileImage.setImage(profilePhoto);
            currentUser.setProfileImageType(null);

        } catch (HttpResponseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uploads a new profile photo, and sends and stores it on the server by converting it to a base64 string.
     * Images MUST be square, and bmp, png or jpg.
     * The max file size is 5MB.
     */
    private void uploadProfileImage() {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter fileExtensions =
                new FileChooser.ExtensionFilter(
                        "JPEG, PNG, BITMAP", "*.png", "*.jpg", "*.bmp"
                );
        fileChooser.getExtensionFilters().add(fileExtensions);
        try {
            File file = fileChooser.showOpenDialog(stage);
            String fileType = Files.probeContentType(file.toPath()).split("/")[1];
            if (file.length() < 5000000){
                if(fileType.equals("png") || fileType.equals("jpg") || fileType.equals("bmp")){
                    currentUser.setProfileImageType(fileType);
                    BufferedImage bImage = ImageIO.read(file);
                    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(bImage, fileType, byteOutputStream);
                    byte[] byteArrayImage = byteOutputStream.toByteArray();

                    String image = Base64.getEncoder().encodeToString(byteArrayImage);
                    WindowManager.getDataManager().getUsers().updateUserPhoto(currentUser.getId(), image);
                    Image profileImg = SwingFXUtils.toFXImage(bImage, null);
                    if (profileImg.getWidth() == profileImg.getHeight()) {
                        profileImage.setImage(profileImg);
                    } else {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Image not square");
                        alert.setContentText("Image file must have the same width and height.");
                        WindowManager.setIconAndStyle(alert.getDialogPane());
                        alert.showAndWait();
                    }

                }

            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("File Size Too Large");
                alert.setContentText("File size must be below 5MB.");
                WindowManager.setIconAndStyle(alert.getDialogPane());
                alert.showAndWait();
            }
        } catch (NullPointerException e){
            System.out.println("Error: NULL");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        genderIdentityComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        bloodTypeComboBox.setItems(FXCollections.observableArrayList(BloodType.values()));
        alcoholConsumptionComboBox.setItems(FXCollections.observableArrayList(AlcoholConsumption.values()));
        smokerStatusComboBox.setItems(FXCollections.observableArrayList(SmokerStatus.values()));

        organTickBoxes = new HashMap<>();
        organTickBoxes.put(Organ.KIDNEY, kidneyCheckBox);
        organTickBoxes.put(Organ.CORNEA, corneaCheckBox);
        organTickBoxes.put(Organ.BONE, boneMarrowCheckBox);
        organTickBoxes.put(Organ.LIVER, liverCheckBox);
        organTickBoxes.put(Organ.EAR, middleEarCheckBox);
        organTickBoxes.put(Organ.HEART, heartCheckBox);
        organTickBoxes.put(Organ.INTESTINE, intestineCheckBox);
        organTickBoxes.put(Organ.PANCREAS, pancreasCheckBox);
        organTickBoxes.put(Organ.SKIN, skinCheckBox);
        organTickBoxes.put(Organ.TISSUE, connectiveTissueCheckBox);
        organTickBoxes.put(Organ.LUNG, lungCheckBox);
        for(CheckBox checkbox:organTickBoxes.values()){
            checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> highlightOrganCheckBoxes());
        }

        //Add listeners for attribute undo and redo
        preferredFirstNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        preferredMiddleNamesField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        preferredLastNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        firstNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        middleNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        lastNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        addressField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        regionField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        dateOfBirthPicker.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        dateOfDeathPicker.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        heightField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        weightField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });
        bloodPressureTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                attributeFieldUnfocused();
            }
        });

        //Add listeners to correctly update BMI and blood pressure based on user input
        heightField.textProperty().addListener((observable, oldValue, newValue) -> updateBMI());
        weightField.textProperty().addListener((observable, oldVaqlue, newValue) -> updateBMI());
    }

    public void undo(){
        attributeFieldUnfocused();
        //Add the current fields to the redo stack
        redoStack.add(new User(currentUser));
        //Copy the attribute information from the top element of the undo stack
        currentUser.copyFieldsFrom(undoStack.getLast());
        //Remove the top element of the undo stack
        undoStack.removeLast();
        populateUserFields();
    }

    @Override
    public void redo() {
        attributeFieldUnfocused();
        //Add the current fields to the undo stack
        undoStack.add(new User(currentUser));
        //Copy the attribute information from the top element of the redo stack
        currentUser.copyFieldsFrom(redoStack.getLast());
        //Remove the top element of the redo stack
        redoStack.removeLast();
        populateUserFields();
    }
}
