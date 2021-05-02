package server.interaction;

import commons.elements.Status;
import commons.elements.Worker;
import commons.utils.InteractionInterface;

import javax.naming.LimitExceededException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс-реализация взаимодействия с коллекцией.
 */
public final class StorageInteraction implements InteractionInterface {
    /**
     * Статическое поле-хранилище коллекции.
     */
    private static Storage storage;
    /**
     * Статическое поле, содержащее путь к файлу с хранимой коллекцией.
     */
    private static String originPath;
    /**
     * Статическое поле, содержит разделитель значений в оригинальном файле с коллекцией.
     */
    private static char separator;

    /**
     * Стандартный конструктор, задает хранилище, с которым будет работа.
     *
     * @param storage    хранилище.
     * @param originPath путь к данным.
     */
    public StorageInteraction(Storage storage, String originPath, char separator) {
        StorageInteraction.storage = storage;
        StorageInteraction.originPath = originPath;
        StorageInteraction.separator = separator;
    }

    /**
     * Метод, реализующий команду info.
     *
     * @return информация о коллекции.
     */
    public String info() {
        return "Дата доступа к коллекции: " + storage.getInitializationDate() + "\n" +
                "Тип коллекции: " + storage.getCollection().getClass() + "\n" +
                "Размер коллекции: " + storage.getCollection().size();
    }

    /**
     * Метод, реализующий команду show.
     *
     * @return Строковое представление объектов коллекции.
     */
    public String show() {
        ArrayList<Worker> sortedDisplay = new ArrayList(storage.getCollection());
        sortedDisplay.sort(Comparator.comparing(Worker::getCoordinatesValue));
        StringBuilder display = new StringBuilder();
        sortedDisplay.forEach((worker -> display.append(worker.displayWorker())));
        return display.toString();
    }

    /**
     * Метод, реализующий команду add.
     *
     * @param worker добавляемый объект.
     */
    public void add(Worker worker) {
        try {
            worker = storage.generateId(worker);
            worker.setCreationDate(ZonedDateTime.now());
            storage.put(worker);
//            changesMade = true;
        } catch (LimitExceededException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод, реализующий команду update.
     *
     * @param id     ID обновляемого объекта.
     * @param worker новый объект коллекции.
     */
    public void update(long id, Worker worker) {
        removeById(id);
        worker.setId(id);
        storage.put(worker);
    }

    /**
     * Метод, реализующий команду remove_by_id.
     *
     * @param id ID удаляемого объекта.
     */
    public void removeById(long id) {
        Worker worker = storage.getCollection().stream()
                .filter(w -> (id == w.getId()))
                .findAny()
                .orElse(null);
        if (!(worker == null)) {
            storage.getCollection().remove(worker);
            storage.getIdList().remove(id);
        }
    }

    /**
     * Метод, реализующий команду clear.
     */
    public void clear() {
        storage.clear();
    }

    /**
     * Метод, реализующий команду save.
     */
    public void save() {
        try {
            PrintWriter printWriter = new PrintWriter(new FileOutputStream(StorageInteraction.originPath));
            String keyLine = "id" + returnSeparator() + "name" + returnSeparator() + "x" + returnSeparator() +
                    "y" + returnSeparator() + "salary" + returnSeparator() + "endDate" + returnSeparator() +
                    "creationDate" + returnSeparator() + "position" + returnSeparator() + "status" + returnSeparator() +
                    "organization" + returnSeparator() + "orgType" + returnSeparator() + "annualTurnover" + returnSeparator() +
                    "street" + returnSeparator() + "postalCode" + "\n";
            printWriter.write(keyLine);
            HashSet<Worker> collection = storage.getCollection();
            for (Worker w : collection) {
                printWriter.write(w.getId() + returnSeparator());
                printWriter.write(w.getName() + returnSeparator());
                printWriter.write(w.getCoordinateX() + returnSeparator());
                printWriter.write(w.getCoordinateY() + returnSeparator());
                printWriter.write(w.getSalary() + returnSeparator());
                printWriter.write(w.getEndDateString() + returnSeparator());
                printWriter.write(w.getCreationDateString() + returnSeparator());
                printWriter.write(w.getPositionString() + returnSeparator());
                printWriter.write(w.getStatusString() + returnSeparator());
                printWriter.write(w.getOrganizationNameString() + returnSeparator());
                printWriter.write(w.getOrganizationTypeString() + returnSeparator());
                printWriter.write(w.getAnnualTurnoverString() + returnSeparator());
                printWriter.write(w.getAddressStreet() + returnSeparator());
                printWriter.write(w.getAddressZipCode() + "\n");
                printWriter.flush();
            }
//            changesMade = false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод, реализующий команду add_if_min.
     *
     * @param worker добавляемый объект.
     */
    public void addIfMin(Worker worker) {
        try {
            HashSet<Worker> workers = storage.getCollection();
            List<Worker> toBeSortedWorkers = new ArrayList<>(workers);
            toBeSortedWorkers.sort(Comparator.comparing(Worker::getSalary));
            if (worker.getSalary() < toBeSortedWorkers.get(0).getSalary()) {
                worker = storage.generateId(worker);
                worker.setCreationDate(ZonedDateTime.now());
                storage.put(worker);
//            changesMade = true;
            }
        } catch (LimitExceededException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод, реализующий команду remove_greater.
     *
     * @param worker объект для сравнения.
     */
    public void removeGreater(Worker worker) {
        HashSet<Worker> workers = storage.getCollection();
        List<Worker> toBeSortedWorkers = new ArrayList<>(workers);
//        List<Worker> toBeRemovedWorkers = new ArrayList<>();
        toBeSortedWorkers.sort(Comparator.comparing(Worker::getSalary));
        List<Worker> toBeRemovedWorkers = toBeSortedWorkers.stream().
                filter(worker1 -> worker1.compareTo(worker) > 0).
                collect(Collectors.toList());
        toBeRemovedWorkers.
                forEach(worker1 -> storage.getCollection().remove(worker1));
    }

    /**
     * Метод, реализующий команду remove_lower.
     *
     * @param worker объект для сравнения.
     */
    public void removeLower(Worker worker) {
        HashSet<Worker> workers = storage.getCollection();
        List<Worker> toBeSortedWorkers = new ArrayList<>(workers);
        toBeSortedWorkers.sort(Comparator.comparing(Worker::getSalary));
        List<Worker> toBeRemovedWorkers = toBeSortedWorkers.stream().
                filter(worker1 -> worker1.compareTo(worker) < 0).
                collect(Collectors.toList());
        toBeRemovedWorkers.
                forEach(worker1 -> storage.getCollection().remove(worker1));
    }

    /**
     * Метод, реализующий команду count_by_status.
     *
     * @param status статус
     * @return Число объектов с указанным статусом.
     */
    public long countByStatus(Status status) {
        return storage.getCollection().stream()
                .filter(worker -> status.equals(worker.getStatus())).count();
    }

    /**
     * Метод, реализующий команду print_ascending.
     *
     * @return Отсортированное строковое представление коллекции.
     */
    public String printAscending() {
        HashSet<Worker> workers = storage.getCollection();
        List<Worker> toBeSortedWorkers = new ArrayList<>(workers);
        toBeSortedWorkers.sort(Comparator.comparing(Worker::getSalary));
        StringBuilder display = new StringBuilder();
        toBeSortedWorkers.forEach((worker -> display.append(worker.displayWorker())));
        return display.toString();
    }

    /**
     * Метод, реализующий команду print_unique_organization.
     *
     * @return Список всех уникальных организаций.
     */
    public List<String> printUniqueOrganization() {
        List<String> organizations = new ArrayList<>();
        storage.getCollection().stream()
                .filter(worker -> !organizations.contains(worker.getOrganization().toString())).
                forEach(worker -> organizations.add(worker.getOrganization().toString()));
        return organizations;
    }

    /**
     * Метод, возвращающий размер коллекции.
     *
     * @return Размер коллекции.
     */
    public int getSize() {
        return storage.getCollection().size();
    }

    /**
     * Метод, проверяющий наличие объекта по ID.
     *
     * @param id ID для поиска.
     * @return True если объект существует, иначе false.
     */
    public boolean findById(long id) {
        return storage.getIdList().contains(id);
    }

    /**
     * Метод, возвращающий разделитель, используемый в оригинальном файле с коллекцией.
     *
     * @return Разделитель.
     */
    public String returnSeparator() {
        return Character.toString(separator);
    }
}
