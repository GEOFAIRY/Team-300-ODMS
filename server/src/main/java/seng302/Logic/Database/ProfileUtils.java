package seng302.Logic.Database;

import javafx.util.Pair;
import org.apache.commons.dbutils.DbUtils;
import seng302.Config.DatabaseConfiguration;
import seng302.Logic.Database.DatabaseMethods;
import seng302.Logic.SaltHash;
import seng302.Model.Attribute.ProfileType;
import seng302.Server;
import spark.Request;
import spark.Response;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static spark.Spark.halt;

/**
 * Utility class for profiles
 */
public class ProfileUtils extends DatabaseMethods {

    /**
     * change a account to a team300 account
     * @param request the request received
     * @param response the response to send
     * @return String output for success
     */
    public String changeToTeam300(Request request, Response response) {
        int userId = Integer.parseInt(request.queryParams("id"));
        String username = request.queryParams("username");
        String password = request.queryParams("password");
        System.out.println(userId + username + password);

        try {
            changeToTeam300Account(userId, username, SaltHash.createHash(password));
        } catch (SQLException e) {
            response.status(500);
            return "Internal Server Error";
        }

        response.status(200);
        return "USER WITH ID: " + userId +" CHANGED TO FACEBOOK LOGIN SUCCESSFULLY";
    }


    /**
     * change requested account to a team 300 account type
     * @param userId the user id of the user to change
     * @throws SQLException catch sql errors
     */
    public String getAccountType(int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try (Connection connection = DatabaseConfiguration.getInstance().getConnection()) {
            String query = "SELECT acc_type FROM USER WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getString("acc_type");
        } finally {
            close(statement);
        }
    }



    /**
     * change a account to a team300 account
     * @param request the request received
     * @param response the response to send
     * @return String output for success
     */
    public String getAccountTypeReq(Request request, Response response) {
        int userId = Integer.parseInt(request.queryParams("id"));

        try {
            String accType = getAccountType(userId);

            response.status(200);
            response.body(accType);
            return accType;
        } catch (SQLException e) {
            response.status(500);
            return "Internal Server Error";
        }
    }


    /**
     * change requested account to a team 300 account type
     * @param userId the user id of the user to change
     * @param username the new username of the user
     * @param password the new password of the user
     * @throws SQLException catch sql errors
     */
    public void changeToTeam300Account(int userId, String username, String password) throws SQLException {
        PreparedStatement statement = null;
        try (Connection connection = DatabaseConfiguration.getInstance().getConnection()) {
            String query = "UPDATE USER " +
                    "SET acc_type = \"team300\" WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            statement.execute();
            statement.close();
            query = "UPDATE ACCOUNT " +
                    "SET username = ?, password = ? WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2,password);
            statement.setInt(3, userId);
            statement.execute();

        } finally {
            close(statement);
        }
    }


    /**
     * change a account to a facebook account
     * @param request the request received
     * @param response the response to send
     * @return String output for success
     */
    public String changeToFacebook(Request request, Response response) {
        int userId = Integer.parseInt(request.queryParams("id"));
        String apiID = request.queryParams("api_id");
        try {
            changeToFacebookAccount(userId, apiID);
        } catch (SQLException e) {
            response.status(500);
            return "Internal Server Error";
        }

        response.status(201);
        System.out.println(userId);
        return "USER WITH ID: " + userId +" CHANGED TO FACEBOOK LOGIN SUCCESSFULLY";
    }


    /**
     * change account tot facebook type
     * @param userId the id of the user to change
     * @param apiId the api id of the account to now use
     * @throws SQLException catch sql errors
     */
    public void changeToFacebookAccount(int userId, String apiId) throws SQLException {
        PreparedStatement statement = null;
        try (Connection connection = DatabaseConfiguration.getInstance().getConnection()) {
            String query = "UPDATE USER " +
                    "SET acc_type = ?, api_id = ?" +
                    "WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, "facebook");
            statement.setString(2, apiId);
            statement.setInt(3, userId);
            statement.execute();
        } finally {
            close(statement);
        }
    }


    /**
     * change a account to a google account
     * @param request the request received
     * @param response the response to send
     * @return String output for success
     */
    public String changeToGoogle(Request request, Response response) {
        int userId = Integer.parseInt(request.queryParams("id"));
        String apiID = request.queryParams("api_id");

        try {
            changeToGoogleAccount(userId, apiID);
        } catch (SQLException e) {
            response.status(500);
            return "Internal Server Error";
        }

        response.status(200);
        return "USER WITH ID: " + userId +" CHANGED TO FACEBOOK LOGIN SUCCESSFULLY";
    }


    /**
     * change to google account
     * @param userId the user id to change
     * @param apiId the api id to set
     * @throws SQLException catch sql errors
     */
    public void changeToGoogleAccount(int userId, String apiId) throws SQLException {
        PreparedStatement statement = null;
        try (Connection connection = DatabaseConfiguration.getInstance().getConnection()) {
            String query = "UPDATE USER " +
                    "SET acc_type = ?, api_id = ?" +
                    "WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, "google");
            statement.setString(2, apiId);
            statement.setInt(3, userId);
            statement.execute();
        } finally {
            close(statement);
        }
    }

    /**
     * Checks the authorisation level of a token.
     *
     * @param token The token to check for
     * @return The authorisation level of the token, or -1 if the token is not found or the database could not be contacted
     */
    public int checkToken(String token) throws SQLException {
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        PreparedStatement statement1 = null;
        try (Connection connection = DatabaseConfiguration.getInstance().getConnection()) {
            // Check all tokens for time expiry
            statement = connection.prepareStatement(
                    "DELETE FROM TOKEN WHERE token != 'masterToken' AND date_time < DATE_SUB(NOW(), INTERVAL 1 DAY)");
            statement.execute();
            statement.close();

            statement = connection.prepareStatement(
                    "SELECT access_level FROM TOKEN WHERE token = ?");
            statement.setString(1, token);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                statement1 = connection.prepareStatement(
                        "UPDATE TOKEN SET date_time = NOW() WHERE token = ?");
                statement1.setString(1, token);
                statement1.execute();
                return resultSet.getInt("access_level");
            } else {
                return -1;
            }
        } finally {
            close(resultSet, statement, statement1);
        }
    }

    /**
     * Checks the id associated with a token.
     *
     * @param token       The token to check for
     * @param profileType The type of profile associated with that token
     * @param id          The id to check for
     * @return Whether the token is associated with that id or false if the database could not be contacted
     */
    public boolean checkTokenId(String token, ProfileType profileType, int id) throws SQLException {
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try (Connection connection = DatabaseConfiguration.getInstance().getConnection()) {
            statement = connection.prepareStatement(
                    "SELECT access_level FROM TOKEN WHERE token = ? AND access_level = ? AND id = ?");
            statement.setString(1, token);
            statement.setInt(2, profileType.getAccessLevel());
            statement.setInt(3, id);

            resultSet = statement.executeQuery();
            return resultSet.next();
        } finally {
            close(resultSet, statement);
        }
    }

    /**
     * Checks whether the accessor is the same user/clinician/admin they are trying to access.
     *
     * @param request Spark HTTP request obj
     * @param response Spark HTTP response obj
     * @return Whether they are authorised
     */
    public boolean isSpecificUser(Request request, Response response, ProfileType profileType) throws SQLException {
        String failure = "Unauthorised: access denied to specific user ";

        String token = request.headers("token");
        if (token.equals("masterToken")) {
            return true;
        }

        int accessLevel = checkToken(token);
        int id = getId(request.params(":id"));
        if (id == -1) {
            Server.getInstance().log.warn(failure + "(invalid id supplied)");
            halt(401, "Unauthorized");
            return false;
        }
        if (accessLevel == -1) {
            Server.getInstance().log.warn(failure + "(token not found)");
            halt(401, "Unauthorized");
            return false; //Token was not found
        }
        if (accessLevel > 2) {
            Server.getInstance().log.warn(failure + "(access attempt with malformed access level)");
            halt(401, "Unauthorized");
            return false;
        }
        return checkIdMatch(failure + "(token does not match id)", profileType, token, id);
    }

    /**
     * Checks whether the accessor is permitted to access the conversation
     *
     * @param request Spark HTTP request obj
     * @param response Spark HTTP response obj
     * @return Whether they are authorised
     */
    public boolean hasConversationAccess(Request request, Response response, ProfileType profileType) throws SQLException {
        if (!isSpecificUser(request, response, profileType)) {
            return false;
        }
        int id = getId(request.params(":id"));
        int conversationId;
        try {
            conversationId = Integer.parseInt(request.params(":conversationId"));
        } catch (NumberFormatException nfe) {
            Server.getInstance().log.warn("Invalid conversation id");
            halt(400, "Bad request");
            return false;
        }

        boolean authorized = false;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try (Connection connection = DatabaseConfiguration.getInstance().getConnection()) {
            statement = connection.prepareStatement(
                    "SELECT * FROM CONVERSATION_MEMBER WHERE user_id = ? AND conversation_id = ?");
            statement.setInt(1, id);
            statement.setInt(2, conversationId);

            resultSet = statement.executeQuery();
            authorized = resultSet.next();
        } catch (SQLException ignored) {
        } finally {
            close(resultSet, statement);
        }
        if (!authorized) {
            Server.getInstance().log.warn("Attempted access to unauthorized conversation.");
            halt(401, "Unauthorized");
            return false;
        }
        return true;
    }

    /**
     * Checks if a user/clinician/admin with a specified id and profile type matches a token. Halts and sets status to
     * 401 unauthorized if there is no match.
     *
     * @param failure The server log failure message to display if there is no match
     * @param profileType The profile type of the user to check
     * @param token The token to check against
     * @param id The id of the user/clinician/admin to check
     * @return Whether there was a match
     */
    private boolean checkIdMatch(String failure, ProfileType profileType, String token, int id) throws SQLException {
        if (checkTokenId(token, profileType, id)) {
            return true; //user is logged on and supplied their token
        } else {
            Server.getInstance().log.warn(failure);
            halt(401, "Unauthorized");
            return false;
        }
    }

    /**
     * Checks if the requester is authorized to access all users.
     *
     * @param request  Spark HTTP request obj
     * @param response Spark HTTP response obj
     * @return Whether they are authorised
     */
    public boolean hasAccessToAllUsers(Request request, Response response) throws SQLException {
        String failure = "Unauthorised: access denied to all user access request ";

        String token = request.headers("token");
        int accessLevel = checkToken(token);
        if (accessLevel == -1) {
            Server.getInstance().log.warn(failure + "(token not found)");
            halt(401, "Unauthorized");
            return false; //Token was not found
        } else if (accessLevel == 0) {
            Server.getInstance().log.warn(failure + "because they only have single user level access");
            halt(401, "Unauthorized");
            return false; //Token was not found
        } else {
            return true; //user has clinician or admin level access
        }
    }

    /**
     * Checks if the requester is authorized to access/modify a user.
     *
     * @param request  Spark HTTP request obj
     * @param response Spark HTTP response obj
     * @return Whether they are authorised
     */
    public boolean hasUserLevelAccess(Request request, Response response) throws SQLException {
        String failure = "Unauthorised: access denied to single user access ";

        String token = request.headers("token");
        int accessLevel = checkToken(token);
        if (accessLevel == -1) {
            Server.getInstance().log.warn(failure + "(token not found)");
            halt(401, "Unauthorized");
            return false; //Token was not found
        } else if (accessLevel == 0) {
            int id = getId(request.params(":id"));
            if (id == -1) {
                Server.getInstance().log.warn(failure + "(invalid id supplied)");
                halt(401, "Unauthorized");
                return false;
            }
            return checkIdMatch(failure, ProfileType.USER, token, id);
        } else {
            return true; //user has clinician or admin level access
        }
    }

    /**
     * Checks if the requester is authorized to access/modify a clinician.
     *
     * @param request  Spark HTTP request obj
     * @param response Spark HTTP response obj
     * @return Whether they are authorised
     */
    public boolean hasClinicianLevelAccess(Request request, Response response) throws SQLException {
        String failure = "Unauthorised: access denied to single clinician access ";
        String token = request.headers("token");

        int accessLevel = checkToken(token);
        if (accessLevel == -1) {
            Server.getInstance().log.warn(failure + "(token not found).");
            halt(401, "Unauthorized");
            return false; //Token was not found
        } else if (accessLevel == 0) {
            Server.getInstance().log.warn(failure + "(access level too low)");
            halt(401, "Unauthorized");
            return false;
        } else if (accessLevel == 1) {
            int id = getId(request.params(":id"));
            if (id == -1) {
                Server.getInstance().log.warn(failure + "(invalid id supplied).");
                halt(401, "Unauthorized");
                return false;
            }
            return checkIdMatch(failure + "(token does not match clinician id)", ProfileType.CLINICIAN, token, id);
        } else {
            return true; //user has clinician or admin level access
        }
    }

    /**
     * Checks if the requester is authorized to access all users.
     *
     * @param request  Spark HTTP request obj
     * @param response Spark HTTP response obj
     * @return Whether they are authorised
     */
    public boolean hasAdminAccess(Request request, Response response) throws SQLException {
        String failure = "Unauthorised: access denied to admin level request ";

        String token = request.headers("token");
        int accessLevel = checkToken(token);
        if (accessLevel == -1) {
            Server.getInstance().log.warn(failure + "(token not found)");
            halt(401, "Unauthorized");
            return false; //Token was not found
        } else if (accessLevel == 0) {
            Server.getInstance().log.warn(failure + "because they only have single user level access");
            halt(401, "Unauthorized");
            return false; //Token was not found
        } else if (accessLevel == 1) {
            Server.getInstance().log.warn(failure + "because they only have single clinician level access");
            halt(401, "Unauthorized");
            return false; //Token was not found
        } else {
            return true; //user has admin level access
        }
    }

    /**
     * Checks the validity of the ":id" HTTP request param
     *
     * @param request  Spark HTTP request obj
     * @param response Spark HTTP response obj
     * @return True if ID is valid, false otherwise
     */
    public boolean checkId(Request request, Response response) {
        return getId(request.params(":id")) != -1;
    }

    /**
     * Checks whether a raw (string) id is a valid id.
     *
     * @param rawId The raw id
     * @return The id if it is valid, or -1 otherwise
     */
    private int getId(String rawId) {
        int id;

        try {
            id = Integer.parseInt(rawId);
        } catch (NumberFormatException nfe) {
            id = -1;
        }
        if (id == -1) {
            Server.getInstance().log.warn(String.format("Invalid ID: %s", rawId));
            halt(400, "Bad Request");
            return -1;
        } else {
            return id;
        }
    }

    /**
     * Checks whether the query param identifier is unique against all users, clinicians, and admins.
     *
     * @param request  Spark HTTP request obj
     * @param response Spark HTTP response obj
     * @return Whether the identifier is unique
     */
    public boolean isUniqueIdentifier(Request request, Response response) throws SQLException {
        String usernameEmail = request.queryParams("usernameEmail");
        String api_id = request.queryParams("api_id");
        if ((usernameEmail == null || usernameEmail.isEmpty()) && (api_id == null || api_id.isEmpty())) {
            Server.getInstance().log.warn("Received unique identifier request that did not contain an identifier to check.");
            halt(400, "Bad Request");
            return false;
        }

        if (api_id == null || api_id.isEmpty()) {
            ResultSet resultSet = null;
            PreparedStatement statement = null;
            try (Connection connection = DatabaseConfiguration.getInstance().getConnection()) {
                statement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE username = ?");
                statement.setString(1, usernameEmail);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    response.status(200);
                    return false;
                }
                resultSet.close();
                statement.close();
                statement = connection.prepareStatement("SELECT * FROM USER WHERE email = ? OR nhi = ?");
                statement.setString(1, usernameEmail);
                statement.setString(2, usernameEmail);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    response.status(200);
                    return false;
                }
                response.status(200);
                return true;
            } finally {
                close(resultSet, statement);
            }
        } else if (usernameEmail == null || usernameEmail.isEmpty()) {
            ResultSet resultSet = null;
            PreparedStatement statement = null;
            try (Connection connection = DatabaseConfiguration.getInstance().getConnection()) {
                statement = connection.prepareStatement("SELECT * FROM USER WHERE api_id = ?");
                statement.setString(1, api_id);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    response.status(200);
                    return false;
                }
                response.status(200);
                return true;
            } finally {
                close(resultSet, statement);
            }
        }
        return false;
    }
}
