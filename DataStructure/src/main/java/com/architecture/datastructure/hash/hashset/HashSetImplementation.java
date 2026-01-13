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

    public void add(T element) {
        map.put(element, PRESENT);
    }

    public boolean contains(T element) {
        return map.containsKey(element);
    }

    public boolean remove(T element) {
        return map.remove(element) != null;
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
