package com.quizplatform.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Generic Collection Utility class demonstrating:
 * - Generic methods with type parameters (<T>, <K, V>, <T, R>)
 * - Bounded generics (Predicate<? super T>, Function<? super T, ? extends R>)
 * - Comprehensive Java Collections Framework usage (List, Set, Map, Queue)
 */
public final class GenericCollectionUtil {

    private GenericCollectionUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Generic method checking if a collection is null or empty.
     */
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Generic method checking if a collection is non-null and not empty.
     */
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    /**
     * Generic filter method with bounded wildcard.
     * Demonstrates: List<T>, Predicate<? super T>
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<? super T> predicate) {
        List<T> result = new ArrayList<>();
        if (collection == null || predicate == null) {
            return result;
        }
        for (T item : collection) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Generic map/transform method with dual bounded wildcards.
     * Demonstrates: Function<? super T, ? extends R>
     */
    public static <T, R> List<R> map(Collection<T> collection, Function<? super T, ? extends R> mapper) {
        List<R> result = new ArrayList<>();
        if (collection == null || mapper == null) {
            return result;
        }
        for (T item : collection) {
            result.add(mapper.apply(item));
        }
        return result;
    }

    /**
     * Converts a collection to a Map indexed by the extracted key.
     * Demonstrates: Map<K, V> and HashMap<K, V>
     */
    public static <T, K> Map<K, T> toMap(Collection<T> collection, Function<? super T, ? extends K> keyMapper) {
        Map<K, T> result = new HashMap<>();
        if (collection == null || keyMapper == null) {
            return result;
        }
        for (T item : collection) {
            K key = keyMapper.apply(item);
            if (key != null) {
                result.put(key, item);
            }
        }
        return result;
    }

    /**
     * Converts a collection to a Set to ensure unique elements.
     * Demonstrates: Set<T> and HashSet<T>
     */
    public static <T> Set<T> toSet(Collection<T> collection) {
        if (collection == null) {
            return new HashSet<>();
        }
        return new HashSet<>(collection);
    }

    /**
     * Converts a collection to a standard FIFO Queue.
     * Demonstrates: Queue<T> and ArrayDeque<T>
     */
    public static <T> Queue<T> toQueue(Collection<T> collection) {
        Queue<T> queue = new ArrayDeque<>();
        if (collection != null) {
            queue.addAll(collection);
        }
        return queue;
    }

    /**
     * Finds the first element in a collection matching a predicate.
     * Demonstrates: Optional<T>
     */
    public static <T> Optional<T> findFirst(Collection<T> collection, Predicate<? super T> predicate) {
        if (collection == null || predicate == null) {
            return Optional.empty();
        }
        for (T item : collection) {
            if (predicate.test(item)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }
}
