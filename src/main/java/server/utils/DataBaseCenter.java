package server.utils;

import commons.elements.Worker;

import java.sql.*;
import java.util.HashSet;

public class DataBaseCenter {
    private final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private String user = "postgres";
    private String password = "zaqsedxcft";

//    {
//        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public DataBaseCenter() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean addUser(String login, String pwd) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "CREATE ROLE " + login + " WITH\n " +
                    "LOGIN\n" +
                    "NOSUPERUSER\n" +
                    "NOCREATEDB\n" +
                    "NOCREATEROLE\n" +
                    "NOREPLICATION\n " +
                    "ENCRYPTED PASSWORD '" + pwd + "';\n" +
                    "GRANT ALL ON worker TO " + login + ";\n" +
                    "ALTER ROLE " + login + " SET password_encryption TO 'scram-sha-256';";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean loginUser(String user, String password) {
        this.user = user;
        this.password = password;
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            Statement statement = connection.createStatement();
            statement.executeQuery("SELECT * FROM worker");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addWorker(Worker worker) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "INSERT INTO worker VALUES (" + worker.getId() + "," + worker.getName() + "," + worker.getCoordinateX() +
                    "," + worker.getCoordinateY() + "," + worker.getSalary() + "," + worker.getEndDateString() + "," +
                    worker.getCreationDateString() + "," + worker.getPositionString() + "," + worker.getStatusString() + "," +
                    worker.getOrganizationName() + "," + worker.getOrganizationTypeString() + "," + worker.getAnnualTurnover() +
                    "," + worker.getAddressStreet() + "," + worker.getAddressZipCode() + ");";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateWorker(Worker worker) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "UPDATE worker SET id = " + worker.getId() + " name = '" + worker.getName() + "' x = " + worker.getCoordinateX() + " y ="
                    + worker.getCoordinateY() + " salary = " + worker.getSalary() + " enddate = '" + worker.getEndDateString() + "' creationdate ="
                    + worker.getCreationDateString() + "' position = '" + worker.getPositionString() + "' status = '" + worker.getStatusString() +
                    "' organizationname = '" + worker.getOrganizationName() + "' orgtype = '" + worker.getOrganizationTypeString() + "' annualturnover = " +
                    worker.getAnnualTurnover() + " street = '" + worker.getAddressStreet() + "' postalcode = '" + worker.getAddressZipCode() + "\''" + "WHERE id = "
                    + worker.getId() + "AND user = '" + user + "';";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(long id) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "DELETE FROM worker WHERE id = " + id + " AND user = '" + user + "';";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HashSet<Worker> retrieveCollectionFromDB() {
        return null;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
