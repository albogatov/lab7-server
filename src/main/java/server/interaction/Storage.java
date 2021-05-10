package server.interaction;

import commons.elements.Worker;

import javax.naming.LimitExceededException;
import java.util.*;

/**
 * Класс-хранилище.
 */
public class Storage implements StorageInterface<Worker> {

    /**
     * Хранимая коллекция.
     */
    private HashSet<Worker> workers = new HashSet<>();
    /**
     * Дата создания.
     */
    private final Date initDate;
    /**
     * Список идентификаторов элементов коллекции.
     */
    private final List<Long> idList = new ArrayList<>();
    /**
     * Поле, обозначающее такое максимальное занятое значение ID, которое входит в отрезок от 0 до maxConsecutiveId.
     */
    private long maxConsecutiveId = 0;

    /**
     * Стандартный конструктор, задает дату создания.
     */
    public Storage() {
        initDate = new Date();
    }

    /**
     * Метод очистки хранилища.
     */
    public void clear() {
        workers.clear();
    }

    /**
     * Метод, возвращающий дату создания коллекции.
     *
     * @return дата создания.
     */
    public Date getInitializationDate() {
        return initDate;
    }

    /**
     * Метод, возвращающий число элементов коллекции.
     *
     * @return размер.
     */
    public int size() {
        return workers.size();
    }

    /**
     * Метод, помещающий объект в коллекцию.
     *
     * @param worker помещаемый объект.
     */
    public void put(Worker worker) {
        workers.add(worker);
    }

    /**
     * Метод, возвращающий коллекцию.
     *
     * @return коллекция.
     */
    public HashSet<Worker> getCollection() {
        return workers;
    }

    public void setCollection(HashSet<Worker> collection) {
        this.workers = collection;
    }

    /**
     * Метод генерации идентификатора.
     *
     * @param worker объект, для которого генерируется ID.
     * @return объект с установленным ID.
     */
    public Worker generateId(Worker worker) throws LimitExceededException {
        long id;
        for (id = maxConsecutiveId + 1; id < Long.MAX_VALUE; id++) {
            if (!idList.contains(id)) {
                maxConsecutiveId = id;
                break;
            }
            if (maxConsecutiveId + 1L == Long.MAX_VALUE) {
                throw new LimitExceededException("В коллекции достигнуто максимальное количество элементов");
            }
        }
        worker.setId(id);
        idList.add(id);
        return worker;
    }

    /**
     * Метод, возвращающий список ID.
     *
     * @return список ID.
     */
    public List<Long> getIdList() {
        return idList;
    }

    public void checkId(Worker worker) {
        long id = worker.getId();
        if (idList.contains(id))
            throw new IllegalArgumentException("Повторяющийся ID");
        else {
            idList.add(id);
        }
    }
}
