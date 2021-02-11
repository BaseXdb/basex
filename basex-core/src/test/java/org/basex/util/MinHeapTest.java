package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the {@link MinHeap} implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class MinHeapTest {
  /** Checks if the heap can be used for sorting. */
  @Test public void heapSort() {
    final MinHeap<Integer, Integer> heap = new MinHeap<>(null);
    final int vl = 1000;
    final Integer[] vals = new Integer[vl];
    for(int v = 0; v < vl; v++) vals[v] = v;
    Collections.shuffle(Arrays.asList(vals));

    for(final int v : vals) {
      heap.insert(v, v);
      heap.verify();
    }

    int i = 0;
    while(!heap.isEmpty()) {
      assertEquals(Integer.valueOf(i++), heap.removeMin());
      heap.verify();
    }
    assertEquals(1000, i);
  }

  /** Checks if the heap can be used for sorting a pre-sorted sequence. */
  @Test public void heapPreSort() {
    final MinHeap<Integer, Integer> heap = new MinHeap<>(null);
    for(int i = 0; i < 1000; i++) {
      heap.insert(i, i);
      heap.verify();
    }

    int i = 0;
    while(!heap.isEmpty()) {
      assertEquals(i++, heap.removeMin().intValue());
      heap.verify();
    }
    assertEquals(1000, i);
  }
}
