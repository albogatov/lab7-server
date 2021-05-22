package server;

import commons.app.Command;
import commons.app.CommandCenter;
import commons.commands.Login;
import commons.commands.Register;
import commons.commands.Save;
import commons.elements.Worker;
import commons.utils.InteractionInterface;
import server.interaction.Storage;
import server.interaction.StorageInteraction;
import commons.utils.UserInterface;
import server.utils.DataBaseCenter;
import server.utils.PasswordEncoder;
import server.utils.ReadyCSVParser;
import commons.utils.SerializationTool;

import javax.naming.LimitExceededException;
import java.io.*;
import java.net.*;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {
    public static final Logger logger = Logger.getLogger(
            Server.class.getName());
    private DataBaseCenter dataBaseCenter;
    private String[] arguments;
    private DatagramSocket datagramSocket;
    private File dataFile;
    private Character separator = null;
    private final UserInterface userInterface = new UserInterface(new InputStreamReader(System.in),
            new OutputStreamWriter(System.out), true);
    private final int port = 7855;
    private Storage storage = new Storage();
    private InteractionInterface interactiveStorage = null;
    String login;
    String password;

    public static void main(String[] args) {
        logger.log(Level.INFO, "commons.app.server operation initiated");
        Server server = new Server(new DataBaseCenter());
        server.setArguments(args);
        server.run();
    }

    public Server(DataBaseCenter dbc) {
        this.dataBaseCenter = dbc;
    }

    public void setArguments(String[] arguments) {
        logger.log(Level.INFO, "Setting server's arguments");
        this.arguments = arguments;
    }

    private void receive() throws SocketTimeoutException {
        logger.log(Level.INFO, "Receiving initiated");
        byte[] receiver = new byte[1000000];
        DatagramPacket inCommand = new DatagramPacket(receiver, receiver.length);
        Command cmd;
        String argument;
        Worker worker;
        try {
            logger.log(Level.INFO, "Receiving command from client");
            datagramSocket.receive(inCommand);
            cmd = (Command) new SerializationTool().deserializeObject(receiver);
            InetAddress clientAddress = inCommand.getAddress();
            CommandCenter.setClientAddress(clientAddress);
            int clientPort = inCommand.getPort();
            CommandCenter.setClientPort(clientPort);
            if (cmd.getCommand().equals("exit")) {
                logger.log(Level.INFO, "Collection saving initiated");
                CommandCenter.getInstance().executeCommand(userInterface, "save", interactiveStorage);
            } else {
                if (cmd.getClass().getName().contains(".Register") && authoriseUser(cmd.getArgument(), cmd.getAdditionalArgument(), "new") ||
                        cmd.getClass().getName().contains(".Login") && authoriseUser(cmd.getArgument(), cmd.getAdditionalArgument(), "old")) {
                    if (cmd.getArgumentAmount() == 0) {
                        logger.log(Level.INFO, "Executing command without arguments");
                        CommandCenter.getInstance().executeCommand(userInterface, cmd, interactiveStorage, dataBaseCenter);
                    }
                    if (cmd.getArgumentAmount() == 1 && !cmd.getNeedsObject()) {
                        logger.log(Level.INFO, "Executing command with a String argument");
                        argument = cmd.getArgument();
                        CommandCenter.getInstance().executeCommand(userInterface, cmd, argument, interactiveStorage, dataBaseCenter);
                    }
                    if (cmd.getArgumentAmount() == 1 && cmd.getNeedsObject()) {
                        logger.log(Level.INFO, "Executing command with an object as an argument");
                        worker = cmd.getObject();
                        CommandCenter.getInstance().executeCommand(userInterface, cmd, interactiveStorage, worker, dataBaseCenter);
                    }
                    if (cmd.getArgumentAmount() == 2 && cmd.getNeedsObject()) {
                        logger.log(Level.INFO, "Executing command with arguments of various types");
                        argument = cmd.getArgument();
                        worker = cmd.getObject();
                        CommandCenter.getInstance().executeCommand(userInterface, cmd, argument, interactiveStorage, worker, dataBaseCenter);
                    }
                }
//                if (cmd.getClass().getName().contains(".Register") && authoriseUser(cmd.getArgument(), cmd.getAdditionalArgument(), "new")) {
//                    login = cmd.getArgument();
//                    password = cmd.getAdditionalArgument();
//                }
//                if (cmd.getClass().getName().contains(".Login") && authoriseUser(cmd.getArgument(), cmd.getAdditionalArgument(), "old")) {
//                    login = cmd.getArgument();
//                    password = cmd.getAdditionalArgument();
//                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An I/O Exception has occurred", e);
            if (e instanceof SocketTimeoutException)
                throw new SocketTimeoutException("Timeout!!!");
        }
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "The server is now operational");
        interactiveStorage = new StorageInteraction(storage);
        try {
            try {
                logger.log(Level.INFO, "Reading the collection from database");
                dataBaseCenter.createTable();
                dataBaseCenter.retrieveCollectionFromDB(interactiveStorage);
            } catch (NullPointerException e) {
                logger.log(Level.SEVERE, "File data is invalid or incorrect CSV separator was chosen", e);
                System.exit(-1);
            } catch (DateTimeParseException e) {
                logger.log(Level.SEVERE, "Date formatting in the original file is invalid", e);
                System.exit(-1);
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.log(Level.SEVERE, "The file is invalid, empty lines possible", e);
                System.exit(-1);
            } catch (IllegalArgumentException e) {
                logger.log(Level.SEVERE, "File is invalid", e);
                System.exit(-1);
            }
            datagramSocket = new DatagramSocket(port);
            userInterface.connectToServer(datagramSocket);
            logger.log(Level.INFO, "Collection successfully uploaded");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.log(Level.INFO, "Collection saving...");
                CommandCenter.getInstance().executeServerCommand(new Save(), interactiveStorage);
            }));
            while (true) {
                try {
                    datagramSocket.setSoTimeout(60 * 1000);
                    receive();
                } catch (IOException e) {
                    if (e instanceof SocketTimeoutException) {
                        logger.log(Level.SEVERE, "Timeout is reached", e);
                        break;
                    } else throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "An I/O Exception has occurred", e);
        } finally {
            try {
                logger.log(Level.INFO, "Collection saving...");
                CommandCenter.getInstance().executeServerCommand(new Save(), interactiveStorage);
                logger.log(Level.INFO, "server shutting down");
                System.exit(0);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "An unknown Exception has occurred", e);
                System.exit(-1);
            }
        }
    }

    public boolean authoriseUser(String user, String password, String existence) {
        System.out.println(PasswordEncoder.getHexString(password));
//        String checkPwd = PasswordEncoder.hashOldPassword(password);
        String saltedPwd = PasswordEncoder.getHexString(password);
        System.out.println(saltedPwd);
        dataBaseCenter.setUser(login);
        dataBaseCenter.setPassword(saltedPwd);
        if (existence.equals("new")) {
            if (dataBaseCenter.addUser(user, saltedPwd)) {
                login = user;
                this.password = saltedPwd;
                CommandCenter.getInstance().executeCommand(userInterface, new Register(), true);
                return true;
            } else {
                CommandCenter.getInstance().executeCommand(userInterface, new Register(), false);
                return false;
            }
        } else {
            if (dataBaseCenter.loginUser(user, saltedPwd)) {
                System.out.println("login true");
                login = user;
                this.password = saltedPwd;
                CommandCenter.getInstance().executeCommand(userInterface, new Login(), true);
                return true;
            } else {
                System.out.println("login fuck");
                CommandCenter.getInstance().executeCommand(userInterface, new Login(), false);
                return false;
            }
        }
    }
}
