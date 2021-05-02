package server.interaction;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Интерфейс для классов для хранения коллекции.
 *
 * @param <T> тип объектов коллекции.
 */
public interface StorageInterface<T> {
    void clear();

    Date getInitializationDate();

    int size();

    void put(T type);

    HashSet<T> getCollection();

    T generateId(T type) throws Exception;

    List<Long> getIdList();
}
