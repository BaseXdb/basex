package org.basex.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import org.basex.core.Main;

/**
 * This is a simple container for string values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class StringList implements Iterable<String> {
  /** Current string array. */
  protected String[] list = new String[8];
  /** Number of strings. */
  protected int size;

  /**
   * Adds a string to the array.
   * @param s string to be added
   */
  public void add(final String s) {
    if(size == list.length) list = Arrays.copyOf(list, size << 1);
    list[size++] = s;
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return size;
  }

  /**
   * Returns the specified value.
   * @param p position
   * @return value
   */
  public String get(final int p) {
    return list[p];
  }

  /**
   * Checks if the specified string is found in the list.
   * @param v string to be checked
   * @return true if value is found
   */
  public boolean contains(final String v) {
    for(int i = 0; i < size; i++) if(list[i].equals(v)) return true;
    return false;
  }

  /**
   * Deletes the specified entry.
   * @param i entry to be deleted
   */
  public void delete(final int i) {
    Array.move(list, i + 1, -1, --size - i);
  }

  /**
   * Returns the string array.
   * @return array
   */
  public String[] finish() {
    return Arrays.copyOf(list, size);
  }

  /**
   * Sorts the strings.
   * @param cs respect case sensitivity
   * @param asc ascending/descending flag
   */
  public void sort(final boolean cs, final boolean asc) {
    Arrays.sort(list, 0, size, new Comparator<String>() {
      public int compare(final String s1, final String s2) {
        final int c = cs ? s1.compareTo(s2) :
          s1.toLowerCase().compareTo(s2.toLowerCase());
        return asc ? c : -c;
      }
    });
  }

  public Iterator<String> iterator() {
    return new Iterator<String>() {
      private int c = -1;
      public boolean hasNext() { return ++c < size; }
      public String next() { return list[c]; }
      public void remove() { Main.notexpected(); }
    };
  }
}
