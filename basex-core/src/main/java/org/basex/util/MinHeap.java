package org.basex.util;

import java.util.*;

/**
 * A min-heap.
 *
 * @param <K> key type
 * @param <V> value type
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class MinHeap<K, V> {
  /** value array. */
  private Object[] vals;
  /** Comparator. */
  private final Comparator<K> comp;
  /** Size of the heap. */
  private int size;

  /**
   * Constructs a heap with the given initial capacity and order.
   * @param cap initial capacity
   * @param cmp comparator
   */
  public MinHeap(final int cap, final Comparator<K> cmp) {
    vals = new Object[2 * cap];
    comp = cmp;
  }

  /**
   * Inserts the given key/value pair into the heap.
   * @param key key
   * @param value value
   */
  public void insert(final K key, final V value) {
    final int s = size << 1;
    if(s == vals.length) vals = Array.copy(vals, new Object[s << 1]);
    vals[s] = key;
    vals[s + 1] = value;

    // let the inserted value bubble up to its position
    int curr = size++, par = (curr - 1) / 2;
    while(curr > 0 && compare(curr, par) < 0) {
      swap(curr, par);
      curr = par;
      par = (curr - 1) / 2;
    }
  }

  /**
   * Removes the minimum from this heap.
   * @return the removed entry's value
   */
  public V removeMin() {
    final V val = minValue();
    swap(0, --size);
    int pos = 0, sm;
    while(pos < size / 2) {
      sm = 2 * pos + 1;
      if(sm < size - 1 && compare(sm + 1, sm) < 0) sm++;
      if(compare(pos, sm) <= 0) break;
      swap(pos, sm);
      pos = sm;
    }
    return val;
  }

  /**
   * returns the value of the smallest key from this heap.
   * @return value of the smallest key
   */
  @SuppressWarnings("unchecked")
  public V minValue() {
    return (V) vals[1];
  }

  /**
   * Size of this heap.
   * @return number of entries
   */
  public int size() {
    return size;
  }

  /**
   * Checks if this heap is empty.
   * @return {@code true} if heap is empty, {@code false} otherwise
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Compares the keys at position {@code i} and {@code j}.
   * @param i position of first key
   * @param j position of second key
   * @return result of check
   */
  @SuppressWarnings("unchecked")
  private int compare(final int i, final int j) {
    final K a = (K) vals[2 * i], b = (K) vals[2 * j];
    return comp == null ? ((Comparable<K>) a).compareTo(b) : comp.compare(a, b);
  }

  /**
   * Swaps the entries at position {@code a} and {@code b}.
   * @param a first index
   * @param b second index
   */
  private void swap(final int a, final int b) {
    if(a == b) return;
    final int k1 = 2 * a, v1 = k1 + 1, k2 = 2 * b, v2 = k2 + 1;
    final Object k = vals[k1], v = vals[v1];
    vals[k1] = vals[k2];
    vals[v1] = vals[v2];
    vals[k2] = k;
    vals[v2] = v;
  }

  /**
   * Verifies the inner structure of the heap.
   * @throws IllegalStateException if the invariants don't hold
   */
  public void verify() {
    verify(0);
  }

  /**
   * Checks if the heap invariant holds for the node at position {@code i}.
   * @param i position of the node
   * @throws IllegalStateException if the invariants don't hold
   */
  private void verify(final int i) {
    if(2 * i + 1 < size) {
      final int left = 2 * i + 1, right = 2 * (i + 1);
      if(compare(i, left) > 0 || right < size && compare(i, right) > 0)
        throw new IllegalStateException("Heap invariant doesn'T hold at node " + i + '.');
      verify(left);
      verify(right);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Heap[");
    for(int i = 0; i < size; i++) {
      sb.append('(').append(vals[2 * i]).append(", ").append(
          vals[2 * i + 1]).append(')');
      if(i < size - 1) sb.append(", ");
    }
    return sb.append(']').toString();
  }
}
