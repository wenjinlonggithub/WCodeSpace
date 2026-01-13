package com.architecture.datastructure.hash.hashset;

import com.architecture.datastructure.hash.hashmap.HashMapImplementation;

/**
 * HashSet实现 - 基于HashMap
 */
public class HashSetImplementation<T> {
    private static final Object PRESENT = new Object();
    private HashMapImplementation<T, Object> map;

    public HashSetImplementation() {
        map = new HashMapImplementation<>();
    }

    public boolean add(T element) {
        return map.put(element, PRESENT) == null;
    }

    public boolean contains(T element) {
        return map.containsKey(element);
    }

    public boolean remove(T element) {
        return map.remove(element) != null;
    }

    public int size() { return map.size(); }
    public boolean isEmpty() { return map.isEmpty(); }
}
