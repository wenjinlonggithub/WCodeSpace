package com.architecture.datastructure.linear.queue;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Queue Tests")
public class QueueTest {
    private QueueImplementation<Integer> queue;

    @BeforeEach
    void setUp() {
        queue = new QueueImplementation<>();
    }

    @Test
    @DisplayName("测试enqueue和dequeue")
    void testEnqueueDequeue() {
        queue.enqueue(1);
        queue.enqueue(2);
        assertEquals(1, queue.dequeue());
        assertEquals(2, queue.dequeue());
    }

    @Test
    @DisplayName("测试peek")
    void testPeek() {
        queue.enqueue(1);
        assertEquals(1, queue.peek());
        assertEquals(1, queue.size());
    }

    @Test
    @DisplayName("测试空队列异常")
    void testEmptyQueueException() {
        assertThrows(IllegalStateException.class, () -> queue.dequeue());
    }

    @Test
    @DisplayName("测试循环队列")
    void testCircularQueue() {
        CircularQueueImplementation<Integer> cq = new CircularQueueImplementation<>(3);
        cq.enqueue(1);
        cq.enqueue(2);
        cq.enqueue(3);
        assertTrue(cq.isFull());
        assertEquals(1, cq.dequeue());
        cq.enqueue(4);
        assertEquals(2, cq.dequeue());
    }
}
