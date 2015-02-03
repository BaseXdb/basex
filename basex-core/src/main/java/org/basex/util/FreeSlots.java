package org.basex.util;

import java.util.*;
import java.util.Map.Entry;

/**
 * Organizes free slots for heap files.
 *
 * @author BaseX Team 2005-15, BSD License
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
    Long off = null;
    final Entry<Integer, LinkedList<Long>> entry = free.ceilingEntry(size);
    if(entry != null) {
      final int slotSize = entry.getKey();
      final LinkedList<Long> offsets = entry.getValue();
      off = offsets.pop();
      if(offsets.isEmpty()) free.remove(slotSize);
      // add new slow entry if chosen entry is smaller than supplied size
      if(slotSize < size) throw Util.notExpected("Free slot is too small: % < %", slotSize, size);
      if(slotSize > size) add(size - slotSize, off + slotSize);
    }
    return off == null ? offset : off;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(free.size() + " entries:\n");
    for(final Entry<Integer, LinkedList<Long>> entry : free.entrySet()) {
      sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
    }
    return sb.toString();
  }
}
