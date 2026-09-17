package org.basex.util;

import java.util.*;
import java.util.Map.Entry;

/**
 * Organizes free slots in heap files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FreeSlots {
  /** Free slots, ordered by size and offset. */
  private final TreeSet<Slot> sizes = new TreeSet<>();
  /** Free slots, indexed by offset. */
  private final TreeMap<Long, Slot> offsets = new TreeMap<>();

  /**
   * Adds a free slot and merges it with adjacent slots.
   * @param size byte size
   * @param offset file offset
   */
  public void add(final int size, final long offset) {
    long off = offset, sz = size;
    final Entry<Long, Slot> before = offsets.floorEntry(offset);
    if(before != null && before.getValue().end() == offset) {
      final Slot slot = remove(before.getValue());
      off = slot.offset;
      sz += slot.size;
    }
    final Slot after = offsets.get(offset + size);
    if(after != null) sz += remove(after).size;
    add(new Slot(sz, off));
  }

  /**
   * Returns the offset of a slot that is greater than or equal to the specified size.
   * @param size ideal (minimum) slot size
   * @param offset offset used as fallback if no free slot is available
   * @return insertion offset
   */
  public long get(final int size, final long offset) {
    final Slot slot = sizes.ceiling(new Slot(size, -1));
    if(slot == null) return offset;
    if(slot.end() > offset) {
      throw Util.notExpected("Free slot exceeds file offset: % + % > %", slot.offset, slot.size,
          offset);
    }
    remove(slot);
    if(slot.size > size) add(new Slot(slot.size - size, slot.offset + size));
    return slot.offset;
  }

  /**
   * Adds a slot to both structures.
   * @param slot slot
   */
  private void add(final Slot slot) {
    sizes.add(slot);
    offsets.put(slot.offset, slot);
  }

  /**
   * Removes a slot from both structures.
   * @param slot slot
   * @return slot
   */
  private Slot remove(final Slot slot) {
    sizes.remove(slot);
    offsets.remove(slot.offset);
    return slot;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FREE SLOTS: " + sizes.size() + '\n');
    for(final Slot slot : offsets.values()) {
      sb.append("  ").append(slot.offset).append(": ").append(slot.size).append('\n');
    }
    return sb.toString();
  }

  /**
   * Free slot.
   * @param size byte size
   * @param offset file offset
   */
  private record Slot(long size, long offset) implements Comparable<Slot> {
    /**
     * Returns the file offset behind the slot.
     * @return offset
     */
    long end() {
      return offset + size;
    }

    @Override
    public int compareTo(final Slot slot) {
      final int c = Long.compare(size, slot.size);
      return c != 0 ? c : Long.compare(offset, slot.offset);
    }
  }
}
