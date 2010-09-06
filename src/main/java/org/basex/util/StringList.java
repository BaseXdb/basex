package org.basex.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

/**
 * This is a simple container for strings.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class StringList extends ElementList implements Iterable<String> {
  /** Element container. */
  protected String[] list = new String[CAP];

  /**
   * Adds an element to the array.
   * @param e element to be added
   */
  public final void add(final String e) {
    if(size == list.length) list = Array.copyOf(list, newSize());
    list[size++] = e;
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
   * @param e element to be checked
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
    Arrays.sort(list, 0, size, new Comparator<String>() {
      @Override
      public int compare(final String s1, final String s2) {
        final int c = cs ? s1.compareTo(s2) :
          s1.toLowerCase().compareTo(s2.toLowerCase());
        return asc ? c : -c;
      }
    });
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {
      private int c = -1;
      @Override
      public boolean hasNext() { return ++c < size; }
      @Override
      public String next() { return list[c]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }
}
