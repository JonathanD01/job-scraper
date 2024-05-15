package no.jobbscraper.database;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    private static final Logger logger = Logger.getLogger(Database.class.getName());

    private Database() {
        throw new AssertionError();
    }

    public static void setUp() {
        createTable();
    }

    public static boolean exists(String url, String fullIp) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT EXISTS (SELECT 1 FROM VISITED_URLS WHERE URL = ? AND IP = ?)")) {
            preparedStatement.setString(1, url);
            preparedStatement.setString(2, fullIp);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error occurred when checking if " + url + " & " + fullIp + " exists", e.getMessage());
            return false;
        }
    }

    public static void insertUrl(String url, String fullIp) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement("INSERT INTO VISITED_URLS (URL, IP) VALUES (?, ?)")) {
            preparedStatement.setString(1, url);
            preparedStatement.setString(2, fullIp);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error occurred when inserting " + url + " & " + fullIp, e.getMessage());
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    private static void createTable() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS VISITED_URLS " +
                    "(URL CHAR(255) NOT NULL, " +
                    "IP CHAR(255) NOT NULL)");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error occurred when creating table", e.getMessage());
        }
    }
}
