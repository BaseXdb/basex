package org.basex.query.util;

import java.util.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Bounded max-heap that keeps the smallest of all added entries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <E> entry type
 */
public final class TopK<E> {
  /** Maximum limit for which the heap is faster than sorting all entries. */
  public static final int MAX = 1 << 16;

  /** Maximum number of entries. */
  private final int limit;
  /** Comparator. */
  private final QueryComparator<E> comparator;
  /** Entries. */
  private Object[] entries = new Object[1];
  /** Input positions of the entries. */
  private long[] positions = new long[1];
  /** Number of entries. */
  private int size;
  /** Number of added entries. */
  private long added;

  /**
   * Constructor.
   * @param limit maximum number of entries
   * @param comparator comparator
   */
  public TopK(final int limit, final QueryComparator<E> comparator) {
    this.limit = limit;
    this.comparator = comparator;
  }

  /**
   * Adds an entry if it is one of the smallest entries.
   * @param entry entry
   * @throws QueryException query exception
   */
  public void add(final E entry) throws QueryException {
    final long pos = added++;
    if(size < limit) {
      if(size == entries.length) {
        final int s = Math.min(Array.newCapacity(size), limit);
        entries = Arrays.copyOf(entries, s);
        positions = Arrays.copyOf(positions, s);
      }
      int i = size++;
      for(int p; i > 0 && compare(p = i - 1 >> 1, entry, pos) < 0; i = p) {
        entries[i] = entries[p];
        positions[i] = positions[p];
      }
      entries[i] = entry;
      positions[i] = pos;
    } else if(size > 0 && compare(0, entry, pos) > 0) {
      siftDown(entry, pos, size);
    }
  }

  /**
   * Returns the entries in ascending order, and equal entries in the order of their addition.
   * @return entries
   * @throws QueryException query exception
   */
  @SuppressWarnings("unchecked")
  public List<E> finish() throws QueryException {
    final Object[] sorted = new Object[size];
    for(int s = size; s > 0;) {
      sorted[--s] = entries[0];
      siftDown((E) entries[s], positions[s], s);
    }
    return (List<E>) Arrays.asList(sorted);
  }

  /**
   * Inserts an entry at the root and moves it down until the heap property is restored.
   * @param entry entry
   * @param pos input position
   * @param sz heap size
   * @throws QueryException query exception
   */
  @SuppressWarnings("unchecked")
  private void siftDown(final E entry, final long pos, final int sz) throws QueryException {
    int i = 0;
    for(int c; (c = (i << 1) + 1) < sz; i = c) {
      if(c + 1 < sz && compare(c + 1, (E) entries[c], positions[c]) > 0) c++;
      if(compare(c, entry, pos) <= 0) break;
      entries[i] = entries[c];
      positions[i] = positions[c];
    }
    entries[i] = entry;
    positions[i] = pos;
  }

  /**
   * Compares a heap entry with another entry.
   * @param index index of the heap entry
   * @param entry other entry
   * @param pos input position of the other entry
   * @return result of comparison
   * @throws QueryException query exception
   */
  @SuppressWarnings("unchecked")
  private int compare(final int index, final E entry, final long pos) throws QueryException {
    final int diff = comparator.compare((E) entries[index], entry);
    // equal entries: the entry that was added first is smaller
    return diff != 0 ? diff : Long.compare(positions[index], pos);
  }

  /**
   * Comparator that raises query exceptions.
   * @param <E> entry type
   */
  @FunctionalInterface
  public interface QueryComparator<E> {
    /**
     * Compares two entries.
     * @param entry1 first entry
     * @param entry2 second entry
     * @return result of comparison
     * @throws QueryException query exception
     */
    int compare(E entry1, E entry2) throws QueryException;
  }
}
