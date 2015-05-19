package org.basex.util;

import java.util.*;
import java.util.Map.Entry;

import org.basex.util.list.*;

/**
 * Organizes free slots in heap files.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FreeSlots {
  /** Free slots: byte sizes referencing file offsets. */
  private final TreeMap<Integer, LongList> free = new TreeMap<>();

  /**
   * Adds a value for the specified slot size.
   * @param size byte size
   * @param offset file offset
   */
  public void add(final int size, final long offset) {
    LongList ll = free.get(size);
    if(ll == null) {
      ll = new LongList();
      free.put(size, ll);
    }
    ll.add(offset);
  }

  /**
   * Returns the offset of a slot that is greater than or equal to the specified size.
   * @param size ideal (minimum) slot size
   * @param offset offset used as fallback if no free slot is available
   * @return insertion offset
   */
  public long get(final int size, final long offset) {
    long off = -1;
    final Entry<Integer, LongList> entry = free.ceilingEntry(size);
    if(entry != null) {
      final int slotSize = entry.getKey();
      if(slotSize < size) throw Util.notExpected("Free slot is too small: % < %", slotSize, size);

      final LongList offsets = entry.getValue();
      off = offsets.pop();
      if(offsets.isEmpty()) free.remove(slotSize);

      if(slotSize > size) {
        if(off + slotSize > offset)
          throw Util.notExpected("Free slot exceeds file offset: % + % > %", off, slotSize, offset);
        // chosen entry is smaller than supplied size: add entry for remaining free slot
        add(slotSize - size, off + size);
      }
    }
    return off == -1 ? offset : off;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(free.size() + " entries:\n");
    for(final Entry<Integer, LongList> entry : free.entrySet()) {
      sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
    }
    return sb.toString();
  }
}
