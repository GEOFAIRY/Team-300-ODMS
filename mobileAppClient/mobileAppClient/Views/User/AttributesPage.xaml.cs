using mobileAppClient.odmsAPI;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using Xamarin.Forms;


namespace mobileAppClient
{
    /*
     * Class to handle all functionality regarding the attributes page for 
     * user details being shown.
     */
    public partial class AttributesPage : ContentPage
    {
        // Whether the date of death input is visible
        private bool dateOfDeathShowing;

        private bool isClinicianEditing;

        public AttributesPage()
        {
            InitializeComponent();
            if (ClinicianController.Instance.isLoggedIn())
            {
                hasDiedSwitch.IsEnabled = true;
                isClinicianEditing = true;
            } else
            {
                isClinicianEditing = false;
            }

            NHIInput.IsEnabled = isClinicianEditing;

            dateOfDeathShowing = false;
            dobInput.MaximumDate = DateTime.Today;
            dodInput.MaximumDate = DateTime.Today;

            FillFields();
        }

        /*
         * Called on page initialisation, populates fields with the current data from the logged in user
         */
        private void FillFields()
        {
            User loggedInUser = UserController.Instance.LoggedInUser;
            // Name
            FirstNameInput.Text = loggedInUser.name[0];
            MiddleNameInput.Text = "";
            LastNameInput.Text = "";

            // If the user has at least a last name
            if (loggedInUser.name.Count > 1)
            {
                // Set the last name to the last element in the name array
                LastNameInput.Text = loggedInUser.name.Last();
                // Set the middle name to everything in between the first and last element
                MiddleNameInput.Text = String.Join(" ", loggedInUser.name.GetRange(1, loggedInUser.name.Count - 2).ToArray());
            }

            NHIInput.Text = loggedInUser.nhi;

            // Preferred Name
            PrefFirstNameInput.Text = loggedInUser.preferredName[0];
            PrefMiddleNameInput.Text = "";
            PrefLastNameInput.Text = "";

            // If the user has at least a last name
            if (loggedInUser.preferredName.Count > 1)
            {
                // Set the last name to the last element in the name array
                PrefLastNameInput.Text = loggedInUser.preferredName.Last();
                // Set the middle name to everything in between the first and last element
                PrefMiddleNameInput.Text = String.Join(" ", loggedInUser.preferredName.GetRange(1, loggedInUser.preferredName.Count - 2));
            }


            BirthGenderInput.SelectedItem = FirstCharToUpper(loggedInUser.gender);
            GenderIdentityInput.SelectedItem = FirstCharToUpper(loggedInUser.genderIdentity);

            AddressInput.Text = loggedInUser.currentAddress;
            RegionInput.Text = loggedInUser.region;

            dobInput.Date = loggedInUser.dateOfBirth.ToDateTime();
            // Check if the user is dead
            if (loggedInUser.dateOfDeath != null)
            {
                hasDiedSwitch.On = true;
                dodInput.Date = loggedInUser.dateOfDeath.date.ToDateTime();
            }

            HeightInput.Text = loggedInUser.height.ToString();
            WeightInput.Text = loggedInUser.weight.ToString();

            BloodPressureInput.Text = loggedInUser.bloodPressure;
            BloodTypeInput.SelectedItem = BloodTypeExtensions.ToString(BloodTypeExtensions.ToBloodTypeJSON(loggedInUser.bloodType));
            SmokerStatusInput.SelectedItem = FirstCharToUpper(loggedInUser.smokerStatus);
            AlcoholConsumptionInput.SelectedItem = FirstCharToUpper(loggedInUser.alcoholConsumption);
        }

        /*
         * Converts any given string into having an uppercase first char with lowercase for the rest
         * - Used for Pickers
         */
        private string FirstCharToUpper(string input)
        {
            if (String.IsNullOrEmpty(input))
                return "";
            input = input.ToLower();
            return input.First().ToString().ToUpper() + input.Substring(1);
        }

        /*
         * Called when the Save button is pressed, reads + validates input fields and pushes changes to the API
         */
        private async void SaveClicked(object sender, EventArgs e)
        {
            User loggedInUser = UserController.Instance.LoggedInUser;

            string givenFirstName = InputValidation.Trim(FirstNameInput.Text);
            string givenMiddleName = InputValidation.Trim(MiddleNameInput.Text);
            string givenLastName = InputValidation.Trim(LastNameInput.Text);

            string givenPrefFirstName = InputValidation.Trim(PrefFirstNameInput.Text);
            string givenPrefMiddleName = InputValidation.Trim(PrefMiddleNameInput.Text);
            string givenPrefLastName = InputValidation.Trim(PrefLastNameInput.Text);

            string givenNHINumber = InputValidation.Trim(NHIInput.Text);

            string givenAddress = InputValidation.Trim(AddressInput.Text);
            string givenRegion = InputValidation.Trim(RegionInput.Text);

            string givenHeight = InputValidation.Trim(HeightInput.Text);
            string givenWeight = InputValidation.Trim(WeightInput.Text);
            string givenBloodPressure = InputValidation.Trim(BloodPressureInput.Text);

            // Birth names
            if (!InputValidation.IsValidTextInput(givenFirstName, false, false)) {
                await DisplayAlert("", "Please enter a valid first name", "OK");
                return;
            } 

            if (!InputValidation.IsValidTextInput(givenMiddleName, false, true)) {
                await DisplayAlert("", "Please enter a valid middle name", "OK");
                return;
            }

            if (!InputValidation.IsValidTextInput(givenLastName, false, false))
            {
                await DisplayAlert("", "Please enter a valid last name", "OK");
                return;
            }

            // Preferred names
            if (!InputValidation.IsValidTextInput(givenPrefFirstName, false, true))
            {
                await DisplayAlert("", "Please enter a valid preferred first name", "OK");
                return;
            }

            if (!InputValidation.IsValidTextInput(givenPrefMiddleName, false, true))
            {
                await DisplayAlert("", "Please enter a valid preferred middle name", "OK");
                return;
            }

            if (!InputValidation.IsValidTextInput(givenPrefLastName, false, true))
            {
                await DisplayAlert("", "Please enter a valid preferred last name", "OK");
                return;
            }


            if (!InputValidation.IsValidNhiInput(givenNHINumber))
            {
                await DisplayAlert("", "Please enter a valid NHI number", "OK");
                return;
            }



            // Address
            if (!InputValidation.IsValidTextInput(givenAddress, true, true))
            {
                await DisplayAlert("", "Please enter a valid address", "OK");
                return;
            }

            // Physical attributes
            if (!InputValidation.IsValidNumericInput(givenWeight, 0, 500, true))
            {
                await DisplayAlert("", "Please enter a valid weight in kg", "OK");
                return;
            }

            if (!InputValidation.IsValidNumericInput(givenHeight, 0, 300, true))
            {
                await DisplayAlert("", "Please enter a valid height in cm", "OK");
                return;
            }

            if (!InputValidation.IsValidBloodPressure(givenBloodPressure))
            {
                await DisplayAlert("", "Please enter a valid blood pressure eg. 120/80", "OK");
                return;
            }

            //if (loggedInUser.dateOfDeath.ToDateTime() < loggedInUser.dateOfBirth.ToDateTime())
            //{
            //    await DisplayAlert("",
            //    "Please enter a valid date of death",
            //    "OK");
            //    return;
            //}

            // Set user attributes to the new fields
            List<string> name = new List<string>();
            name.Add(givenFirstName);
            name.AddRange(givenMiddleName.Split(' '));
            name.Add(givenLastName);
            loggedInUser.name = name;

            List<string> prefName = new List<string>();
            prefName.Add(givenPrefFirstName);
            prefName.AddRange(givenPrefMiddleName.Split(' '));
            prefName.Add(givenPrefLastName);
            loggedInUser.preferredName = prefName;

            loggedInUser.gender = BirthGenderInput.SelectedItem.ToString().ToUpper();
            loggedInUser.genderIdentity = GenderIdentityInput.SelectedItem.ToString().ToUpper();

            loggedInUser.currentAddress = givenAddress;
            loggedInUser.region = givenRegion;

            loggedInUser.dateOfBirth = new CustomDate(dobInput.Date);

            if (hasDiedSwitch.On)
            {
                loggedInUser.dateOfDeath = new CustomDateTime(dodInput.Date);
                //loggedInUser.dateOfDeath.date = new CustomDate(dodInput.Date);
            } else
            {
                loggedInUser.dateOfDeath = null;
            }

            // Don't worry about conversion exceptions -> this was checked with InputValidation
            loggedInUser.height = Convert.ToDouble(givenHeight);
            loggedInUser.weight = Convert.ToDouble(givenWeight);
            loggedInUser.bloodPressure = givenBloodPressure;
            loggedInUser.bloodType = BloodTypeExtensions.ToBloodType(BloodTypeInput.SelectedItem.ToString().ToUpper()).ToString();
            loggedInUser.smokerStatus = SmokerStatusInput.SelectedItem.ToString().ToUpper();
            loggedInUser.alcoholConsumption = AlcoholConsumptionInput.SelectedItem.ToString().ToUpper();

            UserAPI userAPI = new UserAPI();
            HttpStatusCode userUpdated = await userAPI.UpdateUser(isClinicianEditing);

            switch (userUpdated)
            {
                case HttpStatusCode.Created:
                    await DisplayAlert("",
                    "User details successfully updated",
                    "OK");
                    break;
                case HttpStatusCode.BadRequest:
                    await DisplayAlert("",
                    "User details update failed (400)",
                    "OK");
                    break;
                case HttpStatusCode.ServiceUnavailable:
                    await DisplayAlert("",
                    "Server unavailable, check connection",
                    "OK");
                    break;
                case HttpStatusCode.Unauthorized:
                    await DisplayAlert("",
                    "Unauthorised to modify profile",
                    "OK");
                    break;
                case HttpStatusCode.InternalServerError:
                    await DisplayAlert("",
                    "Server error, please try again (500)",
                    "OK");
                    break;
            }
        }

        private void dateOfDeathSwitchChanged(object sender, ToggledEventArgs e)
        {
            dateOfDeathShowing = !dateOfDeathShowing;
            dateOfDeathCombo.IsVisible = dateOfDeathShowing;
            dateOfDeathCombo.ForceLayout();
        }
    }
}
