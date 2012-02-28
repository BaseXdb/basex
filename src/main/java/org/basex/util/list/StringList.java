package org.basex.util.list;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.basex.util.Array;
import org.basex.util.Util;

/**
 * This is a simple container for strings.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class StringList extends ElementList implements Iterable<String> {
  /** Element container. */
  protected String[] list;

  /**
   * Default constructor.
   */
  public StringList() {
    this(CAP);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param c array capacity
   */
  public StringList(final int c) {
    list = new String[c];
  }

  /**
   * Adds an element to the array.
   * @param e element to be added
   * @return self reference
   */
  public final StringList add(final String e) {
    if(size == list.length) list = Array.copyOf(list, newSize());
    list[size++] = e;
    return this;
  }

  /**
   * Adds elements to the array.
   * @param e element to be added
   * @return self reference
   */
  public final StringList add(final String[] e) {
    for(final String s : e) add(s);
    return this;
  }

  /**
   * Returns the specified element.
   * @param p position
   * @return value
   */
  public final String get(final int p) {
    return list[p];
  }

  /**
   * Checks if the specified element is found in the list.
   * @param e element to be found
   * @return result of check
   */
  public final boolean contains(final String e) {
    for(int i = 0; i < size; ++i) if(list[i].equals(e)) return true;
    return false;
  }

  /**
   * Deletes the specified element.
   * @param i element to be deleted
   */
  public final void delete(final int i) {
    Array.move(list, i + 1, -1, --size - i);
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final String[] toArray() {
    return Array.copyOf(list, size);
  }

  /**
   * Sorts the elements.
   * @param cs respect case sensitivity
   * @param asc ascending/descending flag
   */
  public final void sort(final boolean cs, final boolean asc) {
    sort(cs, asc, 0);
  }

  /**
   * Sorts the elements.
   * @param cs respect case sensitivity
   * @param asc ascending/descending flag
   * @param pos position where sorting starts
   */
  public final void sort(final boolean cs, final boolean asc, final int pos) {
    final Comparator<String> comp = cs ? null : String.CASE_INSENSITIVE_ORDER;
    Arrays.sort(list, pos, size, asc ? comp : Collections.reverseOrder(comp));
  }

  @Override
  public final Iterator<String> iterator() {
    return new Iterator<String>() {
      private int c;
      @Override
      public boolean hasNext() { return c < size; }
      @Override
      public String next() { return list[c++]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }

  /**
   * Removes double elements in a sorted list. Does not change input.
   * @param l Sorted list
   * @return Unique sorted list
   */
  public static StringList unique(final StringList l) {
    final StringList out = new StringList(l.size());
    out.add(l.get(0));
    for(int i = 1; i < l.size(); i++) {
      if(!l.get(i).equals(l.get(i - 1))) out.add(l.get(i));
    }
    return out;
  }
}
