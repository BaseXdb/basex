package org.basex.util;

import java.util.*;
import java.util.Map.*;

/**
 * Organizes free slots for heap files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FreeSlots {
  /** Free slots. */
  private final TreeMap<Integer, LinkedList<Long>> free = new TreeMap<>();

  /**
   * Adds a value for the specified slot size.
   * @param size size
   * @param value value
   */
  public void add(final int size, final long value) {
    LinkedList<Long> ll = free.get(size);
    if(ll == null) {
      ll = new LinkedList<>();
      free.put(size, ll);
    }
    ll.add(value);
  }

  /**
   * Returns an insertion position for the specified slot size.
   * @param size ideal size
   * @return insertion position, or {@code null} if none has been found
   */
  public Long replace(final int size) {
    Long value = null;
    final Entry<Integer, LinkedList<Long>> ps = free.floorEntry(size);
    if(ps != null) {
      final int sz = ps.getKey();
      final LinkedList<Long> ll = ps.getValue();
      value = ll.pop();
      if(ll.isEmpty()) free.remove(sz);
      // add new slow entry if chosen entry is smaller than supplied size
      if(sz != size) add(size - sz, value + sz);
    }
    return value;
  }
}
