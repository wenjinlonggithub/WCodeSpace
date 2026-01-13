package com.architecture.datastructure.hash.hashmap;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HashMap Tests")
public class HashMapTest {
    private HashMapImplementation<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new HashMapImplementation<>();
    }

    @Test
    @DisplayName("测试put和get")
    void testPutAndGet() {
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertEquals(3, map.get("three"));
        assertNull(map.get("four"));
    }

    @Test
    @DisplayName("测试更新值")
    void testUpdate() {
        map.put("key", 1);
        map.put("key", 2);

        assertEquals(2, map.get("key"));
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("测试remove")
    void testRemove() {
        map.put("key", 100);
        assertEquals(100, map.remove("key"));
        assertNull(map.get("key"));
    }
}
