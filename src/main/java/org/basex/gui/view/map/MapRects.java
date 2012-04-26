package org.basex.gui.view.map;

import java.util.*;

import org.basex.util.*;

/**
 * This class organizes all map rectangles in a simple list.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class MapRects implements Iterable<MapRect> {
  /** Value array. */
  MapRect[] list = new MapRect[8];
  /** Sorted values. */
  MapRect[] sorted;
  /** Number of entries. */
  int size;

  /**
   * Default constructor.
   */
  MapRects() {
    this(8);
  }

  /**
   * Constructor, specifying an initial list size.
   * @param is initial size of the list
   */
  private MapRects(final int is) {
    list = new MapRect[is];
  }

  /**
   * Adds a new value.
   * @param v value to be added
   */
  void add(final MapRect v) {
    if(size == list.length) list = Arrays.copyOf(list, size << 1);
    list[size++] = v;
  }

  /**
   * Adds several new values.
   * @param v values to be added
   */
  void add(final MapRects v) {
    for(final MapRect m : v) add(m);
  }

  /**
   * Returns the specified value.
   * @param i value index
   * @return value
   */
  MapRect get(final int i) {
    return list[i];
  }

  /**
   * Resets the integer list.
   */
  void reset() {
    size = 0;
  }

  /**
   * Returns the position of the specified value or -1 if it has not been found.
   * @param r rectangle (pre value) to be found
   * @return rectangle position of -1
   */
  int find(final MapRect r) {
    if(sorted == null) sort();
    final int p = r.pre;
    int l = 0;
    int h = size - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      final int c = sorted[m].pre - p;
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -1;
  }

  /**
   * Creates a sorted array. If the original array is already sorted,
   * the same reference is used.
   */
  private void sort() {
    int i = Integer.MIN_VALUE;
    for(final MapRect r : this) {
      if(i > r.pre) {
        sorted = Arrays.copyOf(list, size);
        Arrays.sort(sorted);
        return;
      }
      i = r.pre;
    }
    sorted = list;
  }

  @Override
  public Iterator<MapRect> iterator() {
    return new Iterator<MapRect>() {
      private int c = -1;
      @Override
      public boolean hasNext() { return ++c < size; }
      @Override
      public MapRect next() { return list[c]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }

  /**
   * Creates a copy of the rectangles.
   * @return copy
   */
  MapRects copy() {
    final MapRects rects = new MapRects(size);
    rects.add(this);
    return rects;
  }
}
