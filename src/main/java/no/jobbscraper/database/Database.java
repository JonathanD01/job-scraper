package no.jobbscraper.database;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    private static final Logger logger = Logger.getLogger(Database.class.getName());
    private static final String TABLE_NAME = "VISITED_URLS";
    private static final String ROW_NAME = "URL";

    private Database() {
        throw new AssertionError();
    }

    public static void setUp() {
        createTable();
    }

    public static boolean exists(String url) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT EXISTS (SELECT 1 FROM " + TABLE_NAME + " WHERE " + ROW_NAME + " = ?)")) {
            preparedStatement.setString(1, url);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error occurred when checking if " + url + " exists", e.getMessage());
            return false;
        }
    }

    public static void insertUrl(String url) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO " + TABLE_NAME + " (" + ROW_NAME + ") VALUES (?)")) {
            preparedStatement.setString(1, url);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error occurred when inserting " + url, e.getMessage());
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    private static void createTable() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    " (" + ROW_NAME + " CHAR(255) PRIMARY KEY NOT NULL)");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error occurred when creating table", e.getMessage());
        }
    }
}
