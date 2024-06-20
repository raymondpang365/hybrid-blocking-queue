package com.raymondpang365.CustomBlockingQueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdvancedHybridQueueWithCasAndConditionTest {

    private final AdvancedHybridQueueWithCasAndCondition<String> queue = new AdvancedHybridQueueWithCasAndCondition<>(5);

    @Test
    @DisplayName("Test HybridQueue")
    void testHybridQueue() throws InterruptedException{

        final CountDownLatch latch = new CountDownLatch(2);

        queue.enqueue("A");
        queue.enqueue("B");
        queue.enqueue("C");
        queue.enqueue("D");
        queue.enqueue("E");

        final Thread thread1 = new Thread(() -> {
            try {
                queue.enqueue("F");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            latch.countDown();
        });

        final Thread thread2 = new Thread(() -> {
            try {
                queue.dequeue();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            latch.countDown();
        });

        thread1.start();
        thread2.start();
        latch.await();

        assertEquals( 5, queue.getSize());
        assertEquals( "B", queue.dequeue());
        assertEquals( "C", queue.dequeue());
        assertEquals( "D", queue.dequeue());
        assertEquals( "E", queue.dequeue());
        assertEquals( "F", queue.dequeue());

    }


}
