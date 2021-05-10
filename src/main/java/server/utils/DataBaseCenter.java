package server.utils;

import commons.elements.*;
import commons.utils.InteractionInterface;
import server.interaction.Storage;
import server.interaction.StorageInteraction;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;

public class DataBaseCenter {
    private final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private String user = "postgres";
    private String password = "zaqsedxcft";

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

    public boolean addWorker(Worker worker) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "INSERT INTO worker VALUES (" + worker.getId() + ",'" + worker.getName() + "'," + worker.getCoordinateX() +
                    "," + worker.getCoordinateY() + "," + worker.getSalary() + "," + worker.getEndDateString() + ",'" +
                    worker.getCreationDate() + "'," + worker.getPositionString() + "," + worker.getStatusString() + "," +
                    worker.getOrganizationNameString() + "," + worker.getOrganizationTypeString() + "," + worker.getAnnualTurnover() +
                    "," + worker.getAddressStreet() + "," + worker.getAddressZipCode() + ");";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateWorker(Worker worker) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "UPDATE worker SET id = " + worker.getId() + " name = '" + worker.getName() + "' x = " + worker.getCoordinateX() + " y ="
                    + worker.getCoordinateY() + " salary = " + worker.getSalary() + " enddate = " + worker.getEndDateString() + " creationdate ='"
                    + worker.getCreationDate() + "' position = " + worker.getPositionString() + " status = " + worker.getStatusString() +
                    " organizationname = " + worker.getOrganizationNameString() + " orgtype = " + worker.getOrganizationTypeString() + " annualturnover = " +
                    worker.getAnnualTurnover() + " street = " + worker.getAddressStreet() + " postalcode = " + worker.getAddressZipCode() + "WHERE id = "
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

    public InteractionInterface retrieveCollectionFromDB(Storage storage) {
        HashSet<Worker> collection = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM worker");
            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                if(!storage.getIdList().contains(id)) {
                    storage.getIdList().add(id);
                }
                String name = resultSet.getString(2);
                Coordinates coordinates = new Coordinates(resultSet.getInt(3), resultSet.getLong(4));
                Integer salary = resultSet.getInt(5);
                LocalDate endDate;
                if (!(resultSet.getString(6) == null))
                    endDate = LocalDate.parse(String.valueOf(resultSet.getDate(6)));
                else endDate = null;
                ZonedDateTime creationDate = ZonedDateTime.parse(String.valueOf(resultSet.getTimestamp(7)), DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.S", Locale.ENGLISH)
                        .withZone(ZoneOffset.UTC));
                Position position = null;
                if (!(resultSet.getString(8) == null))
                    Position.valueOf(resultSet.getString(8));
                Status status = null;
                if (!(resultSet.getString(9) == null))
                    status = Status.valueOf(resultSet.getString(9));
                String organizationName = "";
                if (!(resultSet.getString(10) == null))
                    organizationName = resultSet.getString(10);
                OrganizationType orgType = null;
                if (!(resultSet.getString(11) == null))
                    orgType = OrganizationType.valueOf(resultSet.getString(11));
                Long annualTurnover = null;
                if (!(resultSet.getString(12) == null ))
                    annualTurnover = resultSet.getLong(12);
                Address address = new Address(resultSet.getString(13), resultSet.getString(14));
                Organization organization = new Organization(annualTurnover, orgType, address, organizationName);
                Worker worker = new Worker(id, name, coordinates, creationDate, salary, endDate, position, status, organization);
                collection.add(worker);
            }
            storage.clear();
            storage.setCollection(collection);
            return new StorageInteraction(storage);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
