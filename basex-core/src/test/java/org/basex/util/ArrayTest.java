package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.*;

import org.junit.jupiter.api.*;

/**
 * Array tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayTest {
  /** Tests the stable sort of offsets. */
  @Test public void createOrder() {
    final Random random = new Random(0);
    for(final int size : new int[] { 0, 1, 2, 31, 32, 33, 100, 1000, 12345 }) {
      final int[] values = new int[size];
      for(int i = 0; i < size; i++) values[i] = random.nextInt(10);
      check(size, (x, y) -> Integer.compare(values[x], values[y]));
      check(size, (x, y) -> Integer.compare(x / 3, y / 3));
      check(size, (x, y) -> Integer.compare(y / 3, x / 3));
      check(size, (x, y) -> Integer.compare(y, x));
    }
  }

  /**
   * Compares the result of the stable sort with the one of {@link Arrays#sort}.
   * @param size number of values
   * @param cmp comparator
   */
  private static void check(final int size, final IntBinaryOperator cmp) {
    final Integer[] expected = new Integer[size];
    for(int i = 0; i < size; i++) expected[i] = i;
    Arrays.sort(expected, cmp::applyAsInt);
    assertArrayEquals(Arrays.stream(expected).mapToInt(i -> i).toArray(),
        Array.createOrder(size, cmp));
  }
}
