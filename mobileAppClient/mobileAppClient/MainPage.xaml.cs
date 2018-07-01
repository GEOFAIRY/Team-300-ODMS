﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Xamarin.Forms;
using Xamarin.Forms.Xaml;


namespace mobileAppClient
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class MainPage : MasterDetailPage
    {

        public List<MasterPageItem> menuList { get; set; }


        public MainPage()
        {
            OpenLogin();
            InitializeComponent();
            menuList = new List<MasterPageItem>();

            var page2 = new MasterPageItem() { Title = "Overview", Icon = "ic_home.png", TargetType = typeof(OverviewPage) };
            var page1 = new MasterPageItem() { Title = "Attributes", Icon = "ic_local_shipping.png", TargetType = typeof(AttributesPage) };
            var page6 = new MasterPageItem() { Title = "Organs", Icon = "ic_my_location.png", TargetType = typeof(OrgansPage) };
            // Adding menu items to menuList
            menuList.Add(page2);
            menuList.Add(page1);
            menuList.Add(page6);



            // Setting our list to be ItemSource for ListView in MainPage.xaml
            navigationDrawerList.ItemsSource = menuList;
            // Initial navigation, this can be used for our home page
            Detail = new NavigationPage((Page)Activator.CreateInstance(typeof(OverviewPage)));
            this.BindingContext = new
            {
                Header = "",
                Image = "http://www3.hilton.com/resources/media/hi/GSPSCHF/en_US/img/shared/full_page_image_gallery/main/HH_food_22_675x359_FitToBoxSmallDimension_Center.jpg",
                //Footer = "         -------- Welcome To HotelPlaza --------           "
                Footer = "      Welcome To SENG302 - Team 300     "
            };
        }

        private async void OpenLogin()
        {
            var loginPage = new LoginPage();
            await Navigation.PushModalAsync(loginPage);
        }


        private void OnMenuItemSelected(object sender, SelectedItemChangedEventArgs e)
        {

            var item = (MasterPageItem)e.SelectedItem;
            Type page = item.TargetType;
            Detail = new NavigationPage((Page)Activator.CreateInstance(page));
            IsPresented = false;
        }
    }
}
