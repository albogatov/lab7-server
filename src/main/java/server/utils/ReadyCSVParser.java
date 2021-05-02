package server.utils;

import java.io.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.opencsv.CSVParserBuilder;
import commons.elements.*;
import server.interaction.Storage;
import com.opencsv.CSVParser;

import javax.naming.LimitExceededException;

/**
 * Класс, отвечающий за парсинг данных из изначального файла.
 */
public class ReadyCSVParser {
    /**
     * Объект CSVParser.
     */
    protected static CSVParser parser;
    /**
     * Значения ключевой и их порядковый номер.
     */
    protected static HashMap<String, Integer> keySet = new HashMap<>();

    public static void initParser(char separator) {
        parser = new CSVParserBuilder().withSeparator(separator).build();
    }

    /**
     * Метод, считывающий ключевую строку для определения порядка значений.
     *
     * @param line строка.
     * @throws IOException в случае ошибки ввода/вывода.
     */
    public static void readKeyLine(String line) throws IOException {
        List<String> keyLineValues = Arrays.asList(parser.parseLine(line));
        for (String s : keyLineValues) {
            keySet.put(s.toLowerCase(), keyLineValues.indexOf(s));
        }
    }

    /**
     * Метод, считывающий строку.
     *
     * @param line строка.
     * @return список слов строки после парсинга.
     * @throws IOException в случае ошибки ввода/вывода.
     */
    public static List<String> readLine(String line) throws IOException {
        return Arrays.asList(parser.parseLine(line));
    }

    /**
     * Метод для создания объекта по значениям строки.
     *
     * @param line строки.
     * @return объект коллекции.
     * @throws IOException в случае ошибки ввода/вывода.
     */
    public static Worker readWorker(String line) throws IOException {
        List<String> values = readLine(line);
        long id;
        if (values.get(keySet.get("id")).equals(""))
            id = -1;
        else id = Long.parseLong(values.get(keySet.get("id")));
        String name;
        if (!values.get(keySet.get("name")).chars().allMatch(Character::isLetter) || values.get(keySet.get("name")).equals(""))
            name = null;
        else name = values.get(keySet.get("name"));
        Coordinates coordinates;
        if (values.get(keySet.get("x")).equals("") || values.get(keySet.get("y")).equals(""))
            coordinates = null;
        else
            coordinates = new Coordinates(Integer.parseInt(values.get(keySet.get("x"))), Integer.parseInt(values.get(keySet.get("y"))));
        ZonedDateTime creationDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss z");
        if (values.get(keySet.get("creationdate")).equals("")) {
            creationDate = ZonedDateTime.now();
//            StorageInteraction.implyChange();
        } else creationDate = ZonedDateTime.parse(values.get(keySet.get("creationdate")), formatter);
        Integer salary;
        if (values.get(keySet.get("salary")).equals(""))
            salary = null;
        else
            salary = Integer.parseInt(values.get(keySet.get("salary")));
        LocalDate endDate;
        if (values.get(keySet.get("enddate")).equals(""))
            endDate = null;
        else endDate = LocalDate.parse(values.get(keySet.get("enddate")));
        Position position;
        if (values.get(keySet.get("position")).equals(""))
            position = null;
        else position = Position.valueOf(values.get(keySet.get("position")).toUpperCase());
        Status status;
        if (values.get(keySet.get("status")).equals(""))
            status = null;
        else status = Status.valueOf(values.get(keySet.get("status")).toUpperCase());
        String orgName;
        if (values.get(keySet.get("organization")).equals(""))
            orgName = null;
        else orgName = values.get(keySet.get("organization")).toUpperCase();
        Long annualTurnover;
        if (values.get(keySet.get("annualturnover")).equals(""))
            annualTurnover = null;
        else annualTurnover = Long.parseLong(values.get(keySet.get("annualturnover")));
        OrganizationType orgType;
        if (values.get(keySet.get("orgtype")).equals(""))
            orgType = null;
        else orgType = OrganizationType.valueOf(values.get(keySet.get("orgtype")).toUpperCase());
        Address address;
        if (values.get(keySet.get("street")).equals("") || values.get(keySet.get("postalcode")).equals(""))
            address = null;
        else address = new Address(values.get(keySet.get("street")), values.get(keySet.get("postalcode")));
        Organization organization;
        if (annualTurnover != null || orgType != null || address != null || orgName != null)
            organization = new Organization(annualTurnover, orgType, address, orgName);
        else organization = null;
        if (organization != null && organization.getPostalAddress() == null)
            throw new NullPointerException("Данные неверны");
        if (name == null || name.equals("") || coordinates == null || salary == null || salary <= 0 || coordinates.getX() > 627 || coordinates.getY() > 990) {
            throw new NullPointerException("Данные неверны");
        }
        return new Worker(id, name, coordinates, creationDate, salary, endDate, position, status, organization);
    }

    /**
     * Метод, считывающий все объекты коллекции в файле.
     *
     * @param file    файл.
     * @param workers коллекция, в которую помещаются объекты.
     * @param storage объект класса для хранения коллекции.
     * @throws IOException в случае ошибки ввода/вывода.
     */
    public static void readWorkers(File file, HashSet<Worker> workers, Storage storage) throws IOException, LimitExceededException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(bis));
        String line;
        readKeyLine(br.readLine());
        while ((line = br.readLine()) != null) {
            Worker worker = readWorker(line);
            int initSize = workers.size();
            workers.add(worker);
            if (initSize < workers.size()) {
                if (worker.getId() == -1)
                    storage.generateId(worker);
                else storage.checkId(worker);
            }
        }
    }
}
