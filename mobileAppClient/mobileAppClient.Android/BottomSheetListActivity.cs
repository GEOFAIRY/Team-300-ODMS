﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Android.App;
using Android.Content;
using Android.OS;
using Android.Runtime;
using Android.Views;
using Android.Widget;
using Android.Support.V7.App;
using Android.Graphics.Drawables;
using Android.Graphics;
using Android.Support.V4.Graphics.Drawable;
using ServiceStack;
using Xamarin.Forms;
using System.Threading;

namespace mobileAppClient.Droid
{
    [Activity]
    public class BottomSheetListActivity : AppCompatActivity, Android.Views.View.IOnClickListener
    {
        private Android.Views.View lastClicked = null;
        private Android.Views.View lastAdded = null;
        private TableLayout organTable;
        private List<TableRow> allRows;

        List<DonatableOrgan> organs;
        protected override void OnCreate(Bundle bundle)
        {
            base.OnCreate(bundle);
            SetContentView(Resource.Layout.UserOrganOverview);

            PrepareSheet();

        }

        public void OnClick(Android.Views.View view)
        {
            int rowIndex = view.Id;
            String organName = null;
            Android.Views.View nextChild = ((ViewGroup)view).GetChildAt(1);
            
            organName = ((TextView)nextChild).Text;

           
            
            foreach (DonatableOrgan organ in organs)
            {
                if (organName.ToLower().Equals(organ.organType.ToLower()))
                {
                    String OrganString = organ.ToJson();

                    Intent intent = new Intent(this, typeof(OrganTransferActivity));
                    intent.PutExtra("organ", OrganString);
                    this.StartActivity(intent);
                    break;
                }
            }


        }
  
        

        public void PrepareSheet()
        {
            /* TODO:
                - Countdowns
                - Hospital support
                - Recipient support
            */
            var name = FindViewById<TextView>(Resource.Id.User_Name);
            var address = FindViewById<TextView>(Resource.Id.Address);
            var profilePicture = FindViewById<ImageView>(Resource.Id.ProfilePictureFrame);
            organTable = FindViewById<TableLayout>(Resource.Id.organTableLayout);


            if (name != null)
            {
                name.Text = Intent.GetStringExtra("name");
            }
            if (address != null)
            {
                address.Text = Intent.GetStringExtra("address");
            }
            if (profilePicture != null)
            {
                var pictureString = Intent.GetStringExtra("profilePicture");
                if (pictureString.Length == 0)
                {
                    profilePicture.SetImageResource(Resource.Drawable.donationIcon);
                }
                else
                {
                    var imageBytes = Convert.FromBase64String(pictureString);
                    var imageData = BitmapFactory.DecodeByteArray(imageBytes, 0, imageBytes.Length);
                    profilePicture.SetImageBitmap(imageData);
                }
            }

            var organString = Intent.GetStringExtra("organs");
            organs = organString.FromJson<List<DonatableOrgan>>();

            //This is used onClick as an index to insert the receiver table.
            int i = 1;
            allRows = new List<TableRow>();
            foreach (DonatableOrgan organ in organs)
            {
                TableRow organRow = new TableRow(this);
                TextView organText = new TextView(this);
                TextView organTimer = new TextView(this);
                ImageView organImage = new ImageView(this);
                String organName = organ.organType;
                //ViewGroup.LayoutParams textParameters = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WrapContent, ViewGroup.LayoutParams.WrapContent);

                organText.Text = char.ToUpper(organ.organType[0]) + organ.organType.Substring(1).ToLower();
                switch (organ.organType.ToLower())
                {
                    case "bone":
                        organImage.SetImageResource(Resource.Drawable.bone_icon);
                        break;
                    case "ear":
                        organImage.SetImageResource(Resource.Drawable.ear_icon);
                        break;
                    case "cornea":
                        organImage.SetImageResource(Resource.Drawable.cornea_icon);
                        break;
                    case "heart":
                        organImage.SetImageResource(Resource.Drawable.heart_icon);
                        break;
                    case "intestine":
                        organImage.SetImageResource(Resource.Drawable.intestine_icon);
                        break;
                    case "kidney":
                        organImage.SetImageResource(Resource.Drawable.kidney_icon);
                        break;
                    case "liver":
                        organImage.SetImageResource(Resource.Drawable.liver_icon);
                        break;
                    case "lung":
                        organImage.SetImageResource(Resource.Drawable.lung_icon);
                        break;
                    case "pancreas":
                        organImage.SetImageResource(Resource.Drawable.pancreas_icon);
                        break;
                    case "skin":
                        organImage.SetImageResource(Resource.Drawable.skin_icon);
                        break;
                    case "tissue":
                        organImage.SetImageResource(Resource.Drawable.tissue_icon);
                        break;
                }
                organText.SetTextAppearance(this, Android.Resource.Style.TextAppearanceMedium);

                organImage.SetAdjustViewBounds(true);


                organImage.SetMaxHeight(80);
                organImage.SetMaxWidth(10);
                organImage.SetPadding(0, 0, 0, 0);
                //organImage.LayoutParameters = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WrapContent, ViewGroup.LayoutParams.MatchParent, 0.2f);
                //organTable.ShrinkAllColumns = true;
                organTable.SetColumnShrinkable(0, true);


                string countdownDetail = "";
                if (organ.expired)
                {
                    countdownDetail = "EXPIRED";
                    organTimer.SetTextColor(Android.Graphics.Color.Red);
                } else if (organ.inTransfer == 1)
                {
                    countdownDetail = "IN TRANSFER";
                    organTimer.SetTextColor(Android.Graphics.Color.Orange);
                }
                else if (organ.inTransfer == 2)
                {
                    countdownDetail = "SUCCESSFULLY TRANSFERRED";
                    organTimer.SetTextColor(Android.Graphics.Color.Green);
                }
                else
                {
                    Tuple<string, long> timeRemainingTuple = organ.getTimeRemaining();
                    organTimer.Text = timeRemainingTuple.Item1;
                    //Change colour based on severity
                    long timeRemaining = timeRemainingTuple.Item2;
                    if (timeRemaining <= 3600)
                    {
                        organTimer.SetTextColor(Android.Graphics.Color.Rgb(244, 65, 65));
                    }
                    else if (timeRemaining <= 10800)
                    {
                        organTimer.SetTextColor(Android.Graphics.Color.Rgb(244, 130, 65));
                    }
                    else if (timeRemaining <= 21600)
                    {
                        organTimer.SetTextColor(Android.Graphics.Color.Rgb(244, 190, 65));
                    }
                    else if (timeRemaining <= 43200)
                    {
                        organTimer.SetTextColor(Android.Graphics.Color.Rgb(244, 241, 65));
                    }
                    else if (timeRemaining <= 86400)
                    {
                        organTimer.SetTextColor(Android.Graphics.Color.Rgb(208, 244, 65));
                    }
                    else if (timeRemaining <= 172800)
                    {
                        organTimer.SetTextColor(Android.Graphics.Color.Rgb(160, 244, 65));
                    }
                    else
                    {
                        organTimer.SetTextColor(Android.Graphics.Color.Rgb(76, 244, 65));
                    }

           
                }

                //organText.LayoutParameters = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WrapContent, ViewGroup.LayoutParams.MatchParent, 0.4f);
                //organTimer.LayoutParameters = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WrapContent, ViewGroup.LayoutParams.MatchParent, 0.4f);

                organText.SetPadding(0,0,2,0);
                organRow.AddView(organImage);
                organRow.AddView(organText);
                organRow.AddView(organTimer);
                organRow.Id = i;
                organRow.SetOnClickListener(this);
                organTable.AddView(organRow);
                allRows.Add(organRow);
                i++;

                
            }
            StartTickingTimer(1000);

        }

        private void transferOrgan(List<DonatableOrgan> organs, String organName, int index)
        {
            var organTable = FindViewById<TableLayout>(Resource.Id.organTableLayout);
            TableLayout recipientTable = new TableLayout(this);
            //Get list of recipients
            //Iterate and create rows for each

            foreach(DonatableOrgan organ in organs)
            {
                if (organName.ToLower().Equals(organ.organType.ToLower()))
                {
                    if (organ.topReceivers.IsEmpty())
                    {
                        TableRow tableRow = new TableRow(this);
                        TextView noReceivers = new TextView(this);

                        noReceivers.Text = "No valid receivers found";

                        tableRow.AddView(noReceivers);
                        recipientTable.AddView(tableRow);

                        lastAdded = recipientTable;
                    }
                    else
                    {
                        foreach (User recipient in organ.topReceivers)
                        {
                            TableRow recipientRow = new TableRow(this);
                            TextView recipientName = new TextView(this);

                            recipientName.Text = recipient.FullName;

                            recipientRow.AddView(recipientName);
                            recipientTable.AddView(recipientRow);
                        }
                    }
                }
            }

            //Add null check to show "No Valid Receivers" or something
            organTable.AddView(recipientTable, index + 1);
        }

        public void StartTickingTimer(int interval)
        {
            Timer timer = new Timer(RefreshCountdownsInTableView, null, 0, interval);
        }

        public void RefreshCountdownsInTableView(object o)
        {
            Device.BeginInvokeOnMainThread(() =>
            {
                foreach(TableRow row in allRows)
                {
                    string detailString = ((TextView)(row.GetChildAt(2))).Text;
                    if (detailString.Equals("EXPIRED"))
                    {
                        continue;
                    }
                    else
                    {
                        string timeLeftString = detailString.Substring(16);
                        string timeString = timeLeftString.Remove(timeLeftString.Length - 5);

                        TimeSpan timeLeft = TimeSpan.Parse(timeString);
                        if (timeLeft.Equals(new TimeSpan(0, 0, 0)))
                        {
                            detailString = "EXPIRED";
                            ((TextView)(row.GetChildAt(2))).SetTextColor(Android.Graphics.Color.Red);
                            //Update the Organ object to be expired
                        }
                        else
                        {
                            timeLeft = timeLeft.Subtract(new TimeSpan(0, 0, 1));
                            detailString = detailString.Substring(0, 16) + timeLeft.ToString(@"dd\:hh\:mm\:ss") + " days";

                        }
                        ((TextView)(row.GetChildAt(2))).Text = detailString;
                    }


                }

            });
        }
    }
}
