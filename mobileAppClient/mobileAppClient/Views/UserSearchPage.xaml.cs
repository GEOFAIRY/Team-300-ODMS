﻿using mobileAppClient.Models.CustomObjects;
using mobileAppClient.odmsAPI;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using System.Windows.Input;
using Xamarin.Forms;
using Xamarin.Forms.Xaml;

namespace mobileAppClient.Views
{

    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class UserSearchPage : ContentPage
    {
        // Loading represents fetching more users at the bottom of the list
        private bool _IsLoading;
        public bool IsLoading
        {
            get { return _IsLoading; }
            set
            {
                _IsLoading = value;
                if (_IsLoading == true)
                {
                    LoadingIndicator.IsVisible = true;
                    LoadingIndicator.IsRunning = true;
                } else
                {
                    LoadingIndicator.IsVisible = false;
                    LoadingIndicator.IsRunning = false;
                }
            }
        }
        

        // Refreshing represents checking for new users and returning to the top of the list
        private bool _isRefreshing = false;
        public bool IsRefreshing
        {
            get { return _isRefreshing; }
            set
            {
                _isRefreshing = value;
                OnPropertyChanged(nameof(IsRefreshing));
            }
        }
        private int currentIndex;
        private bool endOfUsers;

        
        public CustomObservableCollection<User> UserList { get; set; }

        public UserSearchPage()
        {
            InitializeComponent();
            IsLoading = false;
            endOfUsers = false;
            currentIndex = 0;
            UserList = new CustomObservableCollection<User>();
            UserListView.ItemsSource = UserList;
            UserListView.ItemAppearing += (sender, e) =>
            {
                if (IsLoading || UserList.Count == 0 || endOfUsers)
                    return;

                // Hit the bottom
                if (e.Item == UserList[UserList.Count - 1])
                {
                    LoadItems();
                }
            };

            LoadItems();
        }



        private async Task LoadItems()
        {
            IsLoading = true;
            // This is where users will be populated from
            UserList.AddRange(await getUsers(currentIndex, 20));

            currentIndex += 20;
            IsLoading = false; 
        }

        private async Task<List<User>> getUsers(int startIndex, int count)
        {
            UserAPI userAPI = new UserAPI();
            Tuple<HttpStatusCode, List<User>> users = await userAPI.GetUsers(startIndex, count);
            if (users.Item1 != HttpStatusCode.OK)
            {
                return new List<User>();
            } 

            if (users.Item2.Count < 20)
            {
                endOfUsers = true;
            }
            return users.Item2;
        }

        public ICommand RefreshCommand
        {
            get
            {
                return new Command(async () =>
                {
                    IsRefreshing = true;
                    await refreshUserList();
                    IsRefreshing = false;
                });
            }
        }

        private async Task refreshUserList()
        {
            UserList.Clear();
            currentIndex = 0;
            LoadItems();
        }

        async void Handle_UserTapped(object sender, ItemTappedEventArgs e)
        {
            if (e.Item == null)
                return;

            //Deselect Item
            ((ListView)sender).SelectedItem = null;

            User tappedUser = (User)e.Item;
            string message = String.Format("{0} {1}", tappedUser.name[0], tappedUser.name[2]);
            await DisplayAlert("User Selected", message, "OK");

            
        }
    }

        //private async void ListItemAppearing(object sender, ItemVisibilityEventArgs e)
        //{
        //    var itemTypeObject = e.Item as ItemType;
        //    if (ItemsListView.ItemsSource.ToList().Last() == itemTypeObject)
        //    {
        //        //Now you are at bottom of list. Add more items to the ObservableCollection.
        //    }
        //}
}
