package seng302.data.database;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.http.client.HttpResponseException;
import seng302.data.interfaces.UsersDAO;
import seng302.generic.APIResponse;
import seng302.generic.APIServer;
import seng302.generic.Debugger;
import seng302.generic.IO;
import seng302.User.User;
import seng302.User.UserCSVStorer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.*;

public class UsersDB implements UsersDAO {
    private final APIServer server;
    private String users = "users";
    private String unableTo = "Could not access server";
    private String photo = "photo";

    public UsersDB(APIServer server) {
        this.server = server;
    }

    /**
     * inserts a new user into the server
     * @param user the user to insert
     */
    @Override
    public void insertUser(User user) {
        JsonParser jp = new JsonParser();
        JsonObject userJson = jp.parse(new Gson().toJson(user)).getAsJsonObject();
        System.out.println(userJson.getAsJsonPrimitive("passphrase"));
        userJson.add("password", userJson.getAsJsonPrimitive("passphrase"));
        Debugger.log("INSERTING USER");
        Debugger.log(userJson);
        userJson.remove("id");
        server.postRequest(userJson, new HashMap<>(), null, users);
    }

    @Override
    public void exportUsers(List<User> usersToSend) {
        // Serialise read user list to JSON string
        JsonParser jp = new JsonParser();
        UserCSVStorer csvUsers = new UserCSVStorer(usersToSend);
        JsonObject usersJson = jp.parse(new Gson().toJson(csvUsers)).getAsJsonObject();
        server.postRequest(usersJson, new HashMap<>(), "masterToken", "users/import");
    }

    /**
     * updates a user on the database
     * @param user the user to update
     * @param token the users token
     * @throws HttpResponseException throws if cannot connect to the server
     */
    @Override
    public void updateUser(User user, String token) throws HttpResponseException {
        JsonParser jp = new JsonParser();
        JsonObject userJson = jp.parse(new Gson().toJson(user)).getAsJsonObject();
        APIResponse response = server.patchRequest(userJson, new HashMap<>(), token, users, String.valueOf(user.getId()));
        if(response == null) throw new HttpResponseException(0, unableTo);
        if (response.getStatusCode() != 201)
            throw new HttpResponseException(response.getStatusCode(), response.getAsString());
    }

    /**
     * Used for searching, takes a hashmap of keyvalue pairs and searches the DB for them.
     * eg. "age", "10" returns all users aged 10.
     *
     * @param searchMap The hashmap with associated key value pairs
     * @return a JSON array of users.
     */
    @Override
    public List<User> queryUsers(Map<String, String> searchMap, String token) {
        APIResponse response =  server.getRequest(searchMap, token, users);
        if(response == null){
            return new ArrayList<>();
        }
        if(response.isValidJson()){
            JsonArray searchResults = response.getAsJsonArray();
            Type type = new TypeToken<ArrayList<User>>() {
            }.getType();
            return new Gson().fromJson(searchResults, type);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * gets a specific user from the server
     * @param id the id of the user to get
     * @param token the users token
     * @return returns a user
     */
    @Override
    public User getUser(long id, String token) {
        APIResponse response = server.getRequest(new HashMap<>(), token, users, String.valueOf(id));
        if(response == null){
            return null;
        }
        else if (response.isValidJson()) {
            return new Gson().fromJson(response.getAsJsonObject(), User.class);
        }
        else return null;
    }

    @Override
    public Image getUserPhoto(long id, String token) {
        APIResponse response = server.getRequest(new HashMap<>(), token, users, String.valueOf(id), photo);

        if(response == null) return getDefaultProfilePhoto();

        if (response.getStatusCode() == 404) {
            // No image uploaded, return default image
            Debugger.log("No profile photo loaded: setting default");
            return getDefaultProfilePhoto();
        }
        try {
            Debugger.log(response.getStatusCode());
            String encodedImage = response.getAsString();
            //Decode the string to a byte array
            byte[] decodedImage = Base64.getDecoder().decode(encodedImage);

            //Turn it into a buffered image
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(decodedImage);
            BufferedImage bImage = ImageIO.read(byteInputStream);
            byteInputStream.close();
            return SwingFXUtils.toFXImage(bImage, null);
        } catch (Exception e) {
            Debugger.error(e);
            return getDefaultProfilePhoto();
        }
    }

    /**
     * Helper function that returns a default placeholder image for the profile photo
     * @return placeholder image
     */
    private Image getDefaultProfilePhoto() {
        File imageFile = new File(IO.getJarPath() + "/classes/icon.png");
        try {
            String imageURL = imageFile.toURI().toURL().toString();
            return new Image(imageURL);
        } catch (MalformedURLException e1) {
            return null;
        }
    }

    @Override
    public void updateUserPhoto(long id, String image, String token) throws HttpResponseException {
        JsonParser jp = new JsonParser();
        PhotoStruct photoStruct = new PhotoStruct(image);
        JsonObject imageJson = jp.parse(new Gson().toJson(photoStruct)).getAsJsonObject();
        APIResponse response = server.patchRequest(imageJson, new HashMap<>(), token, users, String.valueOf(id), photo);
        if(response == null) throw new HttpResponseException(0, unableTo);
    }

    @Override
    public void deleteUserPhoto(long id, String token) throws HttpResponseException {
        APIResponse response = server.deleteRequest(new HashMap<>(), token, users, String.valueOf(id), photo);
        if(response == null) throw new HttpResponseException(0, unableTo);
    }

    /**
     * get all the users from the server
     * @param token the users token
     * @return returns a list of all users
     */
    @Override
    public List<User> getAllUsers(String token) {
        APIResponse response = server.getRequest(new HashMap<>(), token, users);
        if(response == null){
            return new ArrayList<>();
        }
        if (response.isValidJson()) {
            return new Gson().fromJson(response.getAsJsonArray(), new TypeToken<List<User>>(){}.getType());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * removes a user from the server
     * @param id the id of the users to remove
     * @param token the users token
     * @throws HttpResponseException throws if cannot connect to the server
     */
    @Override
    public void removeUser(long id, String token) throws HttpResponseException {
        APIResponse response = server.deleteRequest(new HashMap<>(), token, users, String.valueOf(id));
        if(response == null) throw new HttpResponseException(0, unableTo);
        if (response.getStatusCode() != 201)
            throw new HttpResponseException(response.getStatusCode(), response.getAsString());
    }

    /**
     * gets the total number of users
     * @param token the users token
     * @return returns the number of users
     * @throws HttpResponseException throws if cannot connect to the server
     */
    @Override
    public int count(String token) throws HttpResponseException {
        APIResponse response = server.getRequest(new HashMap<>(), token, "usercount");
        if(response == null){
            throw new HttpResponseException(0, "Could not reach server");
        }
        if (response.getStatusCode() != 200)
            throw new HttpResponseException(response.getStatusCode(), response.getAsString());
        return Integer.parseInt(response.getAsString());
    }

    /**
     * Updates a user's username, email and password
     * @param id The id of the user
     * @param username The new username to update too
     * @param email The new email
     * @param password The new password
     * @param token The login token
     * @throws HttpResponseException If something goes wrong
     */
    @Override
    public void updateAccount(long id, String username, String email, String password, String token) throws HttpResponseException {
        JsonObject jo = new JsonObject();
        jo.addProperty("username", username);
        jo.addProperty("email", email);
        jo.addProperty("password", password);

        APIResponse response = server.patchRequest(jo, new HashMap<>(), token, users, String.valueOf(id), "account");
        if(response == null){
            throw new HttpResponseException(0, "Could not reach server");
        }
        if (response.getStatusCode() != 201) {
            throw new HttpResponseException(response.getStatusCode(), response.getAsString());
        }

    }
}