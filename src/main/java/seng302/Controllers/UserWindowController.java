package seng302.Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import seng302.Core.*;
import seng302.TUI.CommandLineInterface;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserWindowController implements Initializable {

    private Donor currentDonor;

    public void setCurrentDonor(Donor currentDonor) {
        this.currentDonor = currentDonor;
        userDisplayText.setText("Currently logged in as: " + currentDonor.getName());
    }

    @FXML
    private Label userDisplayText;

    @FXML
    private GridPane attributesGridPane;
    @FXML
    private Pane historyPane;
    @FXML
    private Pane medicationsPane;
    @FXML
    private Pane welcomePane;

    @FXML
    private TextField firstNameField;
    @FXML
    private Label settingAttributesLabel;
    @FXML
    private TextField middleNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField regionField;
    @FXML
    private DatePicker dateOfBirthPicker;
    @FXML
    private DatePicker dateOfDeathPicker;
    @FXML
    private TextField heightField;
    @FXML
    private TextField weightField;
    @FXML
    private ComboBox genderComboBox;
    @FXML
    private ComboBox bloodTypeComboBox;
    @FXML
    private CheckBox liverCheckBox;
    @FXML
    private CheckBox kidneyCheckBox;
    @FXML
    private CheckBox pancreasCheckBox;
    @FXML
    private CheckBox heartCheckBox;
    @FXML
    private CheckBox lungCheckBox;
    @FXML
    private CheckBox intestineCheckBox;
    @FXML
    private CheckBox corneaCheckBox;
    @FXML
    private CheckBox middleEarCheckBox;
    @FXML
    private CheckBox skinCheckBox;
    @FXML
    private CheckBox boneMarrowCheckBox;
    @FXML
    private CheckBox connectiveTissueCheckBox;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button saveButton;
    @FXML
    private Label ageLabel;
    @FXML
    private Label bmiLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Main.setUserWindowController(this);
        welcomePane.setVisible(true);
        attributesGridPane.setVisible(false);
        historyPane.setVisible(false);
        medicationsPane.setVisible(false);
    }

    public void showHistoryPane() {
        welcomePane.setVisible(false);
        attributesGridPane.setVisible(false);
        historyPane.setVisible(true);
        medicationsPane.setVisible(false);

    }

    public void showMedicationsPane() {
        welcomePane.setVisible(false);
        attributesGridPane.setVisible(false);
        historyPane.setVisible(false);
        medicationsPane.setVisible(true);
    }

    public void showAttributesPane() {
        welcomePane.setVisible(false);
        attributesGridPane.setVisible(true);
        historyPane.setVisible(false);
        medicationsPane.setVisible(false);
    }

    public void populateDonorFields() {

        settingAttributesLabel.setText("Attributes for " + currentDonor.getName());
        String[] splitNames = currentDonor.getNameArray();
        firstNameField.setText(splitNames[0]);
        if (splitNames.length > 2) {
            String[] middleName = new String[splitNames.length-2];
            System.arraycopy(splitNames, 1, middleName, 0, splitNames.length-2);
            middleNameField.setText(String.join(",", middleName));
            lastNameField.setText(splitNames[splitNames.length-1]);
        } else if (splitNames.length == 2) {
            middleNameField.setText("");
            lastNameField.setText(splitNames[1]);
        } else {
            middleNameField.setText("");
            lastNameField.setText("");
        }
        addressField.setText(currentDonor.getCurrentAddress());
        regionField.setText(currentDonor.getRegion());

        dateOfBirthPicker.setValue(currentDonor.getDateOfBirth());
        dateOfDeathPicker.setValue(currentDonor.getDateOfDeath());
        updateAge();

        ObservableList<String> genders =
                FXCollections.observableArrayList(
                        "Male",
                        "Female",
                        "Other"
                );
        genderComboBox.setItems(genders);
        ObservableList<String> bloodTypes =
                FXCollections.observableArrayList(
                        "A-",
                        "A+",
                        "B-",
                        "B+",
                        "AB-",
                        "AB+",
                        "O-",
                        "O+"
                );
        bloodTypeComboBox.setItems(bloodTypes);
        EnumSet<Organ> donorOrgans = currentDonor.getOrgans();
        if(donorOrgans.contains(Organ.LIVER)) {
            liverCheckBox.setSelected(true);
        }
        if(donorOrgans.contains(Organ.KIDNEY)) {
            kidneyCheckBox.setSelected(true);
        }
        if(donorOrgans.contains(Organ.PANCREAS)) {
            pancreasCheckBox.setSelected(true);
        }
        if(donorOrgans.contains(Organ.HEART)) {
            heartCheckBox.setSelected(true);
        }
        if(donorOrgans.contains(Organ.LUNG)) {
            lungCheckBox.setSelected(true);
        }
        if(donorOrgans.contains(Organ.INTESTINE)) {
            intestineCheckBox.setSelected(true);
        }
        if(donorOrgans.contains(Organ.CORNEA)) {
            corneaCheckBox.setSelected(true);
        }
        if(donorOrgans.contains(Organ.EAR)) {
            middleEarCheckBox.setSelected(true);
        }
        if(donorOrgans.contains(Organ.SKIN)) {
            skinCheckBox.setSelected(true);
        }
        if(donorOrgans.contains(Organ.BONE)) {
            boneMarrowCheckBox.setSelected(true);
        }
        if(donorOrgans.contains(Organ.TISSUE)) {
            connectiveTissueCheckBox.setSelected(true);
        }

        usernameField.setText(currentDonor.getUsername());
        emailField.setText(currentDonor.getEmail());
        passwordField.setText(currentDonor.getPassword());


    }

    public void updateDonor() {

        //TODO Middle Names
        String[] middleNames = middleNameField.getText().split(" ");

        //TODO Gender ComboBox
        Gender donorGender = null;
        try {
            String genderPick = (String) genderComboBox.getValue();
            if (genderPick.equals("Male")) {
                donorGender = Gender.MALE;
            } else if (genderPick.equals("Female")) {
                donorGender = Gender.FEMALE;
            } else if (genderPick.equals("Other")) {
                donorGender = Gender.OTHER;
            } else {
                donorGender = null;
            }
        } catch(Exception e) {
            System.out.println("Input a gender");
            donorGender = null;
            //TODO Alert box for Gender
        }

        //TODO Blood Type ComboBox
        BloodType donorBloodType = null;
        try {
            String bloodTypePick = (String) bloodTypeComboBox.getValue();

            switch (bloodTypePick) {
                case "A-":
                    donorBloodType = BloodType.A_NEG;
                    break;
                case "A+":
                    donorBloodType = BloodType.A_POS;
                    break;
                case "B-":
                    donorBloodType = BloodType.B_NEG;
                    break;
                case "B+":
                    donorBloodType = BloodType.B_POS;
                    break;
                case "AB-":
                    donorBloodType = BloodType.AB_NEG;
                    break;
                case "AB+":
                    donorBloodType = BloodType.AB_POS;
                    break;
                case "O-":
                    donorBloodType = BloodType.O_NEG;
                    break;
                case "O+":
                    donorBloodType = BloodType.O_POS;
                    break;
                case "Blood Type":
                    donorBloodType = null;
                    break;
            }
        } catch (Exception e) {
            System.out.println("Input a blood type.");
            donorBloodType = null;
        }


        double donorHeight = -1;
        try{
            donorHeight = Double.parseDouble(heightField.getText());
        } catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error with the Height Input ");
            alert.setContentText("Please input a valid height input.");
        }

        double donorWeight = -1;
        try{
            donorWeight = Double.parseDouble(weightField.getText());
        } catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error with the Weight Input ");
            alert.setContentText("Please input a valid weight input.");
        }


        try {
            Donor saveDonor = new Donor(
                    firstNameField.getText(),
                    middleNames,
                    lastNameField.getText(),
                    dateOfBirthPicker.getValue(),
                    dateOfDeathPicker.getValue(),
                    donorGender,
                    donorHeight,
                    donorWeight,
                    donorBloodType,
                    regionField.getText(),
                    addressField.getText(),
                    usernameField.getText(),
                    emailField.getText(),
                    passwordField.getText()
                    );

            if(liverCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.LIVER);
            }
            if(kidneyCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.KIDNEY);
            }
            if(pancreasCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.PANCREAS);
            }
            if(heartCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.HEART);
            }
            if(lungCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.LUNG);
            }
            if(intestineCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.INTESTINE);
            }
            if(corneaCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.CORNEA);
            }
            if(middleEarCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.EAR);
            }
            if(skinCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.SKIN);
            }
            if(boneMarrowCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.BONE);
            }
            if(connectiveTissueCheckBox.isSelected()) {
                saveDonor.setOrgan(Organ.TISSUE);
            }

            Main.donors.remove(currentDonor);
            Main.saveUsers(Main.getDonorPath(), true);

            currentDonor = saveDonor;
            Main.donors.add(currentDonor);


            settingAttributesLabel.setText("Attributes for " + currentDonor.getName());
            userDisplayText.setText("Currently logged in as: " + currentDonor.getName());
            System.out.println(currentDonor.toString());
            //TODO Implement save function, saving to database and updating the old donor.
            Main.saveUsers(Main.getDonorPath(), true);

        } catch(Exception e) {
            System.out.println(e);
        }

    }

    public void save() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Are you sure?");
        alert.setHeaderText("Are you sure would like to update the current donor? ");
        alert.setContentText("By doing so, the donor will be updated with all filled in fields.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            updateDonor();
            alert.close();
        } else {
            alert.close();
        }

    }

    public void updateAge() {
        LocalDate dobirthPick = dateOfBirthPicker.getValue();
        LocalDate dodeathPick = dateOfDeathPicker.getValue();

        if(dodeathPick == null) {
            LocalDate today = LocalDate.now();
            long days = Duration.between(dobirthPick.atStartOfDay(), today.atStartOfDay()).toDays();
            double years = days/365.00;
            System.out.println(years);
            String age = String.format("%.1f", years);
            ageLabel.setText("Age: " + age + " years");
        } else {
            long days = Duration.between(dobirthPick.atStartOfDay(), dodeathPick.atStartOfDay()).toDays();
            double years = days/365.00;
            System.out.println(years);
            String age = String.format("%.1f", years);
            ageLabel.setText("Age: " + age + " years (At Death)");
        }
    }

    public void updateBMI() {
        try {


            if ((heightField.getText().equals("")) || (weightField.getText().equals(""))) {

                System.out.print("Input a character in both fields.");
            } else {
                double height = Double.parseDouble(heightField.getText());
                double weight = Double.parseDouble(weightField.getText());
                double BMI = ((weight) / (Math.pow(height, 2))) * 10000;
                String bmiString = String.format("%.2f", BMI);
                bmiLabel.setText("BMI: " + bmiString);
            }
        } catch(Exception e) {
            System.out.println("Enter a valid character.");

        }

    }

    public void stop() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Are you sure?");
        alert.setHeaderText("Are you sure would like to exit the application? ");
        alert.setContentText("Exiting without saving loses your non-saved data.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            System.out.println("Exiting GUI");
            Platform.exit();
        } else {
            alert.close();
        }

    }

    public void tester() {
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        String[] helpString = new String[]{"Andy"};
        commandLineInterface.showHelp(helpString);
    }
}
