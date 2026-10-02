package com.quizplatform.dao.impl;

import com.quizplatform.dao.GenericRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Generic in-memory repository implementation demonstrating:
 * - Generic class with parameterized types <T, ID>
 * - LinkedHashMap<K, V> preservation of insertion order
 * - List<T> / ArrayList<T> conversion
 * - Optional<T> return types
 *
 * @param <T>  The entity type
 * @param <ID> The entity identifier type
 */
public class InMemoryGenericRepository<T, ID> implements GenericRepository<T, ID> {

    // Demonstrates Map<K,V> with LinkedHashMap implementation and generic key/value types
    private final Map<ID, T> storage = new LinkedHashMap<>();
    private final Function<T, ID> idExtractor;

    public InMemoryGenericRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    public synchronized void save(T entity) {
        if (entity != null) {
            ID id = idExtractor.apply(entity);
            if (id != null) {
                storage.put(id, entity);
            }
        }
    }

    @Override
    public synchronized Optional<T> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public synchronized boolean deleteById(ID id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    public synchronized int size() {
        return storage.size();
    }

    public synchronized void clear() {
        storage.clear();
    }
}
