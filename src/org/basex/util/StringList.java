package org.basex.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import org.basex.core.Main;

/**
 * This is a simple container for string values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class StringList implements Iterable<String> {
  /** Current string array. */
  String[] list = new String[8];
  /** Number of strings. */
  int size;

  /**
   * Adds a string to the array.
   * @param s string to be added
   */
  public void add(final String s) {
    if(size == list.length) list = Arrays.copyOf(list, size << 1);
    list[size++] = s;
  }

  /**
   * Adds an integer as string to the array.
   * @param s string to be added
   */
  public void add(final int s) {
    add(Integer.toString(s));
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
   * Sets a value at the specified position.
   * @param v value to be added
   * @param p position
   */
  public void set(final String v, final int p) {
    if(p >= list.length) list = Arrays.copyOf(list, p + 1);
    list[p] = v;
    size = Math.max(size, p + 1);
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
    return Arrays.copyOf(list, size);
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
      public void remove() { Main.notexpected(); }
    };
  }
}
