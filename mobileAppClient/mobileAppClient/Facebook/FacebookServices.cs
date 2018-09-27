﻿using System;
using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace mobileAppClient
{
    public class FacebookServices
    {
        public string apiRequest =
            "https://www.facebook.com/dialog/oauth?client_id="
            + "971327199740898"
            + "&display=popup&response_type=token&redirect_uri=https://www.facebook.com/connect/login_success.html"
            + "&scope=email,user_birthday,user_gender,user_location";

        public FacebookServices()
        {
        }

        /*
         * Returns the Facebook access token form the given Facebook
         * authentication redirect url
         */
        public string ExtractAccessTokenFromUrl(string url)
        {
            if (url.Contains("access_token") && url.Contains("&expires_in="))
            {
                var at = url.Replace("https://www.facebook.com/connect/login_success.html#access_token=", "");

                var accessToken = at.Remove(at.IndexOf("&expires_in="));

                return accessToken;
            }

            return string.Empty;
        }

        /*
         * Makes an api call to Facebook withan access token
         * and retrieves the relevant FacebookProfile
         */
        public async Task<FacebookProfile> GetFacebookProfileAsync(string accessToken)
        {
            var requestUrl =
                "https://graph.facebook.com/v2.7/me/?fields=name,picture.width(1000).height(1000)," +
                "location,birthday,email,first_name,last_name," +
                "gender&access_token="
                + accessToken;

            var httpClient = new HttpClient();

            var userJson = await httpClient.GetStringAsync(requestUrl);

            var facebookProfile = JsonConvert.DeserializeObject<FacebookProfile>(userJson);

            return facebookProfile;
        }
    }
}
