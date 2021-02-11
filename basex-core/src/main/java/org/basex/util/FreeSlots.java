package org.basex.util;

import java.util.*;
import java.util.Map.Entry;

import org.basex.util.list.*;

/**
 * Organizes free slots in heap files.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FreeSlots {
  /** Free slots: byte sizes referencing file offsets. */
  private final TreeMap<Integer, LongList> free = new TreeMap<>();
  /** Number of slots. */
  private int slots;

  /**
   * Adds a value for the specified slot size.
   * @param size byte size
   * @param offset file offset
   */
  public void add(final int size, final long offset) {
    add(size, offset, true);
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
      slots--;
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

  /**
   * Adds a value for the specified slot size.
   * @param size byte size
   * @param offset file offset
   * @param opt optimize
   */
  private void add(final int size, final long offset, final boolean opt) {
    free.computeIfAbsent(size, k -> new LongList()).add(offset);
    slots++;
    if(opt) optimize();
  }

  /**
   * Optimizes the free slot list structure by merging adjacent entries.
   * Currently, this function is called after every addition of a new slot value.
   */
  private void optimize() {
    if(free.isEmpty()) return;

    // sort all entries by their offset (use native arrays; faster than TreeMap)
    final int size = slots;
    final LongList offList = new LongList(size);
    final IntList sizeList = new IntList(size);
    free.forEach((slotSize, list) -> {
      final int ll = list.size();
      for(int l = 0; l < ll; l++) {
        offList.add(list.get(l));
        sizeList.add(slotSize);
      }
    });
    if(size != offList.size())
      throw Util.notExpected("Wrong slot count: % vs. %", size, offList.size());

    final long[] offsets = offList.finish();
    final int[] slotSizes = sizeList.finish();
    final int[] index = Array.createOrder(offsets, true);

    // rebuild map with merged slots
    free.clear();
    slots = 0;
    long offset = offsets[0];
    int slotSize = slotSizes[index[0]];
    for(int c = 1; c < size; c++) {
      final long o = offsets[c];
      final int s = slotSizes[index[c]];
      if(o == offset + slotSize) {
        slotSize += s;
      } else {
        add(slotSize, offset, false);
        offset = o;
        slotSize = s;
      }
    }
    add(slotSize, offset, false);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FREE SLOTS: " + free.size() + '\n');
    free.forEach((key, value) ->
      sb.append("  ").append(key).append(": ").append(value).append('\n'));
    return sb.toString();
  }
}
