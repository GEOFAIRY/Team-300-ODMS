package seng302.NotificationManager;


import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gson.Gson;
import seng302.Logic.Database.Conversations;
import seng302.Logic.Database.Notifications;
import seng302.Model.Message;
import seng302.Model.WaitingListItem;
import seng302.Server;


import javax.swing.text.StringContent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class PushAPI {
    /**
     * URL to send API requests to
     */
    private final String URL_BASE = "https://appcenter.ms/api/v0.1/apps/%s/%s/push/notifications/";
    private final String user = "jma326";
    private final String[] apps = {"transcend-Android", "transcend-iOS"};
    private final String token = (String) Server.getInstance().getConfig().get("vs_token");
    private String[] urls = new String[apps.length];

    private HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();

    private Notifications notificationsDatabase = new Notifications();

    private static PushAPI instance = new PushAPI();

    private PushAPI() {
        // Create a url for each app (iOS, Android, UWP etc.)
        for (int i = 0; i < apps.length; i++) {
            urls[i] = String.format(URL_BASE, user, apps[i]);
        }
    }

    public static PushAPI getInstance() {
        return instance;
    }

    /**
     * Sends a notification to each device on which the user with the given ID is logged in
     *
     * @param notification The notification object containing title, message etc.
     * @param user_id      The ID of the user to which the notification is being sent
     */
    public void sendNotification(Notification notification, long user_id) {
        // Get the devices on which the users are logged on
        List<String> devices = getDevices(user_id);
        if(devices.size() > 0) {
            for (String url : urls) {
                // Get the notification JSON
                String json = notification.toJSON(devices);
                // Create the HttpContent
                HttpContent content = new JsonHttpContent(new JacksonFactory(), new Gson().fromJson(json, Map.class));
                // Create a request
                HttpRequest request = createRequest(content, token, url);
                // Execute the request
                if (request != null) {
                    executeRequest(request).start();
                }
            }
        }
    }

    /**
     * Sends a message to all memmbers of a conversation
     * @param message A message object simply holding the message text
     * @param userAccountId The id of the user to receive the notification
     */
    public void sendMessage(Message message, int userAccountId) {
        // Get the ids of users in the given conversation and assign the message to their devices
        Notification n = new Notification("New Message", "You have unread messages");
        for (Map.Entry<String, String> entry : message.messageMap().entrySet()) {
            n.addCustomData(entry.getKey(), entry.getValue());
        }
        sendNotification(n, userAccountId);
    }


    /**
     * Sends a waiting list item update to the user it concerns.
     *
     * @param userId The id of the affected user
     * @param title The type of change that triggered the notification
     * @param message A short description of the change
     */
    public void sendTextNotification(int userId, String title, String message) {
        Notification n = new Notification(title, message);
        n.addCustomData("waitingListItem", null);
        sendNotification(n, userId);
    }

    /**
     * Gets a list of device ids on which the given user is logged in
     * @param user_id The IDs of the user
     * @return A list of Strings representing the IDs of each device the user with the given ID is logged in on
     */
    private List<String> getDevices(long user_id) {
        try {
            Server.getInstance().log.info("Getting devices logged in by users with id " + user_id);
            return notificationsDatabase.getDevices(user_id);
        } catch (SQLException e) {
            Server.getInstance().log.info("Failed to get devices");
            Server.getInstance().log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Create an HTTP POST request to the push API with the given content. Adds the API token.
     * @param content The JSON content of the notification
     * @param token The Push API authorisation token
     * @param url The url of the Push API (with app and user name)
     * @return An HTTP request to the API to send the notification
     */
    private HttpRequest createRequest(HttpContent content, String token, String url) {
        try {
            HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(url), content);
            request.getHeaders().put("X-API-Token", token);
            return request;
        } catch (IOException e) {
            Server.getInstance().log.error("Could not create API request");
            Server.getInstance().log.error(e.toString());
            return null;
        }
    }


    /**
     * Creates a thread on which to execute an HTTP request
     * @param request The POST request to be sent
     * @return The thread to be started
     */
    private Thread executeRequest(HttpRequest request) {
        return new Thread(() -> {
            try {
                Server.getInstance().log.info("Notification sent: " + request.execute().parseAsString());
            } catch (IOException e) {
                Server.getInstance().log.error("Could not send push notification");
                Server.getInstance().log.error(e.toString());

            }
        });
    }
}
