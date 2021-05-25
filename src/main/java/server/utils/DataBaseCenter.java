package server.utils;

import commons.app.User;
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
    private String password = "";

    public DataBaseCenter() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean addUser(User newUser) {
//        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
//            String query = "CREATE ROLE " + login + " WITH\n " +
//                    "LOGIN\n" +
//                    "NOSUPERUSER\n" +
//                    "NOCREATEDB\n" +
//                    "NOCREATEROLE\n" +
//                    "NOREPLICATION\n " +
//                    "ENCRYPTED PASSWORD '" + pwd + "';\n" +
//                    "GRANT ALL ON worker TO " + login + ";\n" +
//                    "ALTER ROLE " + login + " SET password_encryption TO 'scram-sha-256';";
//            PreparedStatement preparedStatement = connection.prepareStatement(query);
//            preparedStatement.execute();
//            return true;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "INSERT INTO users VALUES ('" + newUser.getLogin() + "','" + newUser.getPassword() + "')";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginUser(User loggingUser) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
            while (resultSet.next()) {
                if (resultSet.getString("username").equals(loggingUser.getLogin()) && resultSet.getString("password").equals(loggingUser.getPassword())) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addWorker(Worker worker, User loggedUser) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "INSERT INTO worker (name, x, y, salary, enddate, creationdate, position, status, organizationname, " +
                    "orgtype, annualturnover, street, postalcode, \"username\") VALUES ('" + worker.getName() + "'," + worker.getCoordinateX() +
                    "," + worker.getCoordinateY() + "," + worker.getSalary() + "," + worker.getEndDateString() + ",'" +
                    worker.getCreationDate() + "'," + worker.getPositionString() + "," + worker.getStatusString() + "," +
                    worker.getOrganizationNameString() + "," + worker.getOrganizationTypeString() + "," + worker.getAnnualTurnover() +
                    "," + worker.getAddressStreet() + "," + worker.getAddressZipCode() + ",'" + loggedUser.getLogin() + "');";
            PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.execute();
            ResultSet generated = preparedStatement.getGeneratedKeys();
            if (generated.next())
                worker.setId(generated.getLong(1));
            else throw new SQLException();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

//    public boolean addWorkerNoAG(Worker worker) {
//        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
//            String query = "INSERT INTO worker VALUES ('" + worker.getName() + "'," + worker.getCoordinateX() +
//                    "," + worker.getCoordinateY() + "," + worker.getSalary() + "," + worker.getEndDateString() +
//                    "," + worker.getPositionString() + "," + worker.getStatusString() + "," +
//                    worker.getOrganizationNameString() + "," + worker.getOrganizationTypeString() + "," + worker.getAnnualTurnover() +
//                    "," + worker.getAddressStreet() + "," + worker.getAddressZipCode() + ",'" + loggedUser + "');";
//            PreparedStatement preparedStatement = connection.prepareStatement(query);
//            preparedStatement.execute();
//            return true;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    public boolean updateWorker(Worker worker, long id, User loggedUser) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "SELECT creationdate FROM worker WHERE id = " + id;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            ZonedDateTime creationDate = null;
            while (resultSet.next()) {
                creationDate = ZonedDateTime.parse(String.valueOf(resultSet.getTimestamp(1)), DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.S", Locale.ENGLISH)
                        .withZone(ZoneOffset.UTC));
            }
            String queryUpdate = "UPDATE worker SET name = '" + worker.getName() + "', x = " + worker.getCoordinateX() + ", y ="
                    + worker.getCoordinateY() + ", salary = " + worker.getSalary() + ", enddate = " + worker.getEndDateString() + ", position = " + worker.getPositionString() + ", status = " + worker.getStatusString() +
                    ", organizationname = " + worker.getOrganizationNameString() + ", orgtype = " + worker.getOrganizationTypeString() + ", annualturnover = " +
                    worker.getAnnualTurnover() + ", street = " + worker.getAddressStreet() + ", postalcode = " + worker.getAddressZipCode() + " WHERE id = "
                    + id + " AND username = '" + loggedUser.getLogin() + "';";
            System.out.println(queryUpdate);
            worker.setCreationDate(creationDate);
//            removeWorker(id, loggedUser);
//            worker.setId(id);
//            addWorker(worker, loggedUser);
            PreparedStatement preparedStatement = connection.prepareStatement(queryUpdate);
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeWorker(long id, User loggedUser) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "SELECT * FROM worker WHERE id = " + id + ";";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                if (!resultSet.getString("username").equals(loggedUser.getLogin()))
                    return false;
            }
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM worker WHERE id = " + id +
                    " AND \"username\" = '" + loggedUser.getLogin() + "';");
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("FALSE");
            e.printStackTrace();
            return false;
        }
    }

    public void retrieveCollectionFromDB(InteractionInterface interaction) {
        HashSet<Worker> collection = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM worker");
            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                if (!interaction.findById(id)) {
                    interaction.getStorage().getIdList().add(id);
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
                if (!(resultSet.getString(12) == null))
                    annualTurnover = resultSet.getLong(12);
                Address address = new Address(resultSet.getString(13), resultSet.getString(14));
                Organization organization = new Organization(annualTurnover, orgType, address, organizationName);
                Worker worker = new Worker(id, name, coordinates, creationDate, salary, endDate, position, status, organization);
                collection.add(worker);
            }
            interaction.clear();
            interaction.addAll(collection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean clearCollection(User loggedUser) {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query = "SELECT * FROM worker";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                String deletionQuery = "DELETE FROM worker WHERE id = " + id + " AND \"username\" = '" + loggedUser.toString() + "';";
                PreparedStatement deletion = connection.prepareStatement(deletionQuery);
                deletion.execute();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createTable() {
        try (Connection connection = DriverManager.getConnection(URL, user, password)) {
            String query1 = "CREATE SEQUENCE IF NOT EXISTS workerid START 1;";
            String query2 = "CREATE TABLE IF NOT EXISTS worker " +
                    "(id BIGINT NOT NULL UNIQUE, name VARCHAR (50) NOT NULL DEFAULT nextval('workerid'), x INT NOT NULL CHECK (x <= 627), y BIGINT NOT NULL CHECK (y <= 990), " +
                    "salary INT NOT NULL CHECK (salary >= 0), enddate DATE, creationdate TIMESTAMP NOT NULL, position VARCHAR(50)," +
                    "status VARCHAR(50), organizationname VARCHAR(50), orgtype VARCHAR(50), annualturnover INT CHECK (annualturnover >= 0)," +
                    "street VARCHAR (50), postalcode VARCHAR(50));";
            String query3 = "CREATE TABLE IF NOT EXISTS users" + "(\"username\" VARCHAR(50), password VARCHAR(100))";
            PreparedStatement statement = connection.prepareStatement(query1);
            statement.execute();
            statement = connection.prepareStatement(query2);
            statement.execute();
            statement = connection.prepareStatement(query3);
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //    public void setUser(String login) {
//        loggedUser = login;
//    }
//
    public void setPassword(String pwd) {
        password = pwd;
    }

}
