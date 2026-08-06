package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the memory statistics of the performance class.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PerformanceTest {
  /** Size of the test allocations. */
  private static final int SIZE = 64 << 20;

  /**
   * Tests that available and consumed memory add up to the maximum heap size. The former
   * implementation subtracted the free memory of the committed heap from the maximum heap size,
   * which yields a value that is too large by the size of the committed heap.
   */
  @Test public void available() {
    // allocate memory, so that only a small part of the committed heap is free
    final byte[] array = new byte[SIZE];
    array[SIZE - 1] = 1;
    final long max = Runtime.getRuntime().maxMemory();
    final long sum = Performance.available() + Performance.memory();
    assertTrue(Math.abs(sum - max) < (1 << 20),
        "available and consumed memory add up to " + sum + ", expected " + max);
    assertEquals(1, array[SIZE - 1]);
  }

  /**
   * Tests that thread allocations are measured, and that they are attributed to the allocating
   * thread. Skipped if the JVM supplies no allocation statistics.
   * @throws Exception exception
   */
  @Test public void allocated() throws Exception {
    final Thread thread = Thread.currentThread();
    Assumptions.assumeTrue(Performance.allocated(thread) != -1, "no allocation statistics");

    // allocations of the current thread are measured
    final long before = Performance.allocated(thread);
    final byte[] array = new byte[SIZE];
    array[SIZE - 1] = 1;
    final long allocated = Performance.allocated(thread) - before;
    assertTrue(allocated >= SIZE, "allocation was not measured: " + allocated);

    // allocations of another thread are not attributed to the current one
    final long other = Performance.allocated(thread);
    final CountDownLatch latch = new CountDownLatch(1);
    final Thread worker = new Thread(() -> {
      final byte[] tmp = new byte[SIZE];
      tmp[SIZE - 1] = 1;
      latch.countDown();
    });
    worker.start();
    latch.await();
    worker.join();
    assertTrue(Performance.allocated(thread) - other < SIZE,
        "allocation of another thread was attributed to the current one");
    assertEquals(1, array[SIZE - 1]);
  }
}
