package org.basex.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import org.basex.BaseX;

/**
 * This is a simple container for string values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StringList implements Iterable<String> {
  /** Current string array. */
  public String[] list = new String[8];
  /** Number of strings. */
  public int size;

  /**
   * Adds a string to the array.
   * @param s string to be added
   */
  public void add(final String s) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = s;
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
   * Returns the string array.
   * @return array
   */
  public String[] finish() {
    return Array.finish(list, size);
  }

  /**
   * Sorts the strings.
   * @param cs respect case sensitivity
   */
  public void sort(final boolean cs) {
    Arrays.sort(list, 0, size, new Comparator<String>() {
      public int compare(final String s1, final String s2) {
        return cs ? s1.compareTo(s2) :
          s1.toLowerCase().compareTo(s2.toLowerCase());
      }
    });
  }

  public Iterator<String> iterator() {
    return new Iterator<String>() {
      private int c = -1;
      public boolean hasNext() { return ++c < size; }
      public String next() { return list[c]; }
      public void remove() { BaseX.notimplemented(); }
    };
  }
}
