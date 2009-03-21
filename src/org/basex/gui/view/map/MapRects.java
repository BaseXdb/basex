package org.basex.gui.view.map;

import java.util.Arrays;
import java.util.Iterator;
import org.basex.BaseX;
import org.basex.util.Array;

/**
 * This class organizes all map rectangles in a simple list.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class MapRects implements Iterable<MapRect> {
  /** Value array. */
  protected MapRect[] list;
  /** Sorted values. */
  protected MapRect[] sorted;
  /** Number of entries. */
  protected int size;

  /**
   * Default constructor.
   */
  public MapRects() {
    this(8);
  }

  /**
   * Constructor, specifying an initial list size.
   * @param is initial size of the list
   */
  public MapRects(final int is) {
    list = new MapRect[is];
  }

  /**
   * Adds a new value.
   * @param v value to be added
   */
  public void add(final MapRect v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
  }

  /**
   * Adds several new values.
   * @param v values to be added
   */
  public void add(final MapRects v) {
    for(final MapRect m : v) add(m);
  }

  /**
   * Returns the specified value.
   * @param i value index
   * @return value
   */
  public MapRect get(final int i) {
    return list[i];
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return size;
  }

  /**
   * Resets the integer list.
   */
  public void reset() {
    size = 0;
  }

  /**
   * Returns the position of the specified node or the negative value - 1 of
   * the position where it should have been found.
   * @param r rectangle (pre value) to be found
   * @return true if the node was found
   */
  public int find(final MapRect r) {
    if(sorted == null) sort();
    return Arrays.binarySearch(sorted, 0, size, r);
  }

  /**
   * Creates a sorted array. If the original array is already sorted,
   * the same reference is used.
   */
  public void sort() {
    int i = Integer.MIN_VALUE;
    for(final MapRect r : this) {
      if(i > r.pre) {
        sorted = Array.finish(list, size);
        Arrays.sort(sorted);
        return;
      }
      i = r.pre;
    }
    sorted = list;
  }

  public Iterator<MapRect> iterator() {
    return new Iterator<MapRect>() {
      private int c = -1;
      public boolean hasNext() { return ++c < size; }
      public MapRect next() { return list[c]; }
      public void remove() { BaseX.notimplemented(); }
    };
  }
}
