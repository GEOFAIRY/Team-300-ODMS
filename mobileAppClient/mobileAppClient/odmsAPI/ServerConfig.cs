﻿using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Http;
using System.Net.NetworkInformation;
using System.Text;
using System.Threading.Tasks;

namespace mobileAppClient.odmsAPI
{
    /*
     * Provides a singleton container that tracks the current server address and holds a single HttpClient to be used across API calls
     */
    public sealed class ServerConfig
    {
        public String serverAddress { get; set; }

		public HttpClient client;

        private static readonly Lazy<ServerConfig> lazy =
        new Lazy<ServerConfig>(() => new ServerConfig());

        public static ServerConfig Instance { get { return lazy.Value; } }

        private ServerConfig()
        {
            client = new HttpClient();

            // Sets default address
            //serverAddress = "http://10.196.82.119:7015/api/v1";
            //serverAddress = "https://csse-s302g3.canterbury.ac.nz/api/v1";
            //serverAddress = "http://localhost:7015/api/v1";
            serverAddress = "http://10.0.2.2:7015/api/v1";
        }

        /*
         * Verifies the device is able to make queries to the ODMS API
         */ 
        public async Task<bool> IsConnectedToInternet()
        {
            HttpResponseMessage response;
            try {
                response = await client.GetAsync(serverAddress + "/hello");
            } catch (System.Net.Http.HttpRequestException e)
            {
                // Thrown by invalid URL or connection timeout
                return false;
            }
            
            if (response.StatusCode != HttpStatusCode.OK)
            {
                return false;
            } else
            {
                return true;
            }
        }
    }
}
