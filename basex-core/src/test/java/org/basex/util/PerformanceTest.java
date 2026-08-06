package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

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
   * Tests that allocating memory reduces the available memory. The former implementation
   * subtracted the free memory of the committed heap from the maximum heap size, which grew
   * whenever memory was allocated.
   */
  @Test public void available() {
    final long before = Performance.available();
    final byte[] array = new byte[SIZE];
    array[SIZE - 1] = 1;
    final long after = Performance.available();
    assertTrue(after < before, "available memory did not shrink: " + before + " -> " + after);
    assertEquals(1, array[SIZE - 1]);
  }
}
