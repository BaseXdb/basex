package org.basex.util;

import java.util.*;
import java.util.Map.Entry;

/**
 * Organizes free slots for heap files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FreeSlots {
  /** Free slots: byte sizes referencing file offsets. */
  private final TreeMap<Integer, LinkedList<Long>> free = new TreeMap<>();

  /**
   * Adds a value for the specified slot size.
   * @param size byte size
   * @param offset file offset
   */
  public void add(final int size, final long offset) {
    LinkedList<Long> ll = free.get(size);
    if(ll == null) {
      ll = new LinkedList<>();
      free.put(size, ll);
    }
    ll.add(offset);
  }

  /**
   * Returns the offset of a slot that is greater than or equal to the specified size.
   * @param size ideal byte size
   * @param offset offset used as fallback if no free slot is available
   * @return insertion offset
   */
  public long get(final int size, final long offset) {
    Long value = null;
    final Entry<Integer, LinkedList<Long>> ps = free.ceilingEntry(size);
    if(ps != null) {
      final int sz = ps.getKey();
      final LinkedList<Long> ll = ps.getValue();
      value = ll.pop();
      if(ll.isEmpty()) free.remove(sz);
      // add new slow entry if chosen entry is smaller than supplied size
      if(sz < size) throw Util.notExpected("Free slot is too small: % < %", sz, size);
      if(sz > size) add(sz - size, value + size);
    }
    return value == null ? offset : value;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(free.size() + " entries:\n");
    for(final Map.Entry<Integer, LinkedList<Long>> entry : free.entrySet()) {
      sb.append("- " + entry.getKey() + ": " + entry.getValue() + '\n');
    }
    return sb.toString();
  }
}
