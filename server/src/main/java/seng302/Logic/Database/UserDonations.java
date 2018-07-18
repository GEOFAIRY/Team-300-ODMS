package seng302.Logic.Database;

import seng302.Config.DatabaseConfiguration;
import seng302.Model.Attribute.Organ;
import seng302.Model.WaitingListItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UserDonations {

    public Set<Organ> getAllUserDonations(int userId) throws SQLException {
        Set<Organ> organs = new HashSet<>();
        String query = "SELECT * FROM DONATION_LIST_ITEM WHERE user_id = ?";
        Connection connection = DatabaseConfiguration.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        ResultSet resultSet = statement.executeQuery();
        connection.close();

        while(resultSet.next()) {
            organs.add(getOrganFromResultSet(resultSet));
        }
        return organs;
    }

    public Set<Organ> getAllDonations() throws SQLException {
        Set<Organ> organs = new HashSet<>();
        String query = "SELECT * FROM DONATION_LIST_ITEM";
        Connection connection = DatabaseConfiguration.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        connection.close();

        while(resultSet.next()) {
            organs.add(getOrganFromResultSet(resultSet));
        }
        return organs;
    }

    public void insertDonation(Organ organ, int userId) throws SQLException {
        String insertDonationQuery = "INSERT INTO DONATION_LIST_ITEM (name, user_id) " +
                "VALUES (?,?)";
        Connection connection = DatabaseConfiguration.getInstance().getConnection();
        PreparedStatement insertDonationStatement = connection.prepareStatement(insertDonationQuery);

        insertDonationStatement.setString(1, organ.name());
        insertDonationStatement.setInt(2, userId);

        System.out.println("Inserting new donation -> Successful -> Rows Added: " + insertDonationStatement.executeUpdate());
        connection.close();
    }

    public Organ getOrganFromResultSet(ResultSet resultSet) throws SQLException {
        return Organ.valueOf(resultSet.getString("name").toUpperCase());
    }

    public Organ getDonationListItemFromName(String donationListItemName, int userId) throws SQLException {
        // SELECT * FROM DONATION_LIST_ITEM id = id;
        String query = "SELECT * FROM DONATION_LIST_ITEM WHERE name = ? AND user_id = ?";
        Connection connection = DatabaseConfiguration.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, donationListItemName);
        statement.setInt(2, userId);
        ResultSet resultSet = statement.executeQuery();
        connection.close();

        //If response is empty then return null
        if (!resultSet.next()) {
            return null;
        } else {
            //If response is not empty then return a indication that fields arent empty
            return getOrganFromResultSet(resultSet);
        }

    }

    public void removeDonationListItem(int userId, String donationListItemName) throws SQLException {
        String update = "DELETE FROM DONATION_LIST_ITEM WHERE name = ? AND user_id = ?";
        Connection connection = DatabaseConfiguration.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(update);
        statement.setString(1, donationListItemName);
        statement.setInt(2, userId);
        System.out.println("Deletion of Donation List Item - NAME: " + donationListItemName + " USERID: " + userId + " -> Successful -> Rows Removed: " + statement.executeUpdate());
        connection.close();
    }

    public void removeAllUserDonations(int userId) throws SQLException {
        String update = "DELETE FROM DONATION_LIST_ITEM WHERE user_id = ?";
        Connection connection = DatabaseConfiguration.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(update);
        statement.setInt(1, userId);
        System.out.println("Deletion of all Donation List Items for - " + " USERID: " + userId + " -> Successful -> Rows Removed: " + statement.executeUpdate());
        connection.close();
    }


}
