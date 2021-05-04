package server.utils;

import sun.tools.jconsole.Worker;

import java.sql.*;
import java.util.HashSet;

public class DataBaseCenter {
    private final String URL = "jdbc:postgresql://pg:7855/workers";
    private String user = "default";
    private String password = "";
    {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public DataBaseCenter() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void addUser(String user, String password) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = null;
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            Statement statement = connection.createStatement();
            ResultSet resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {

        }

    }
    public void addWorker(Worker worker) {

    }
    public void updateWorker(Worker worker) {

    }
    public void removeWorker(long id) {

    }
    public HashSet<Worker> retrieveCollectionFromDB() {
        return null;
    }
    public void setUser() {

    }
    public void setPassword() {

    }
}
