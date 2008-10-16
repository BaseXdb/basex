package org.basex.util;

import java.util.Arrays;

/**
 * This is a simple container for string values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StringList {
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
   * Resets the integer list.
   */
  public void reset() {
    size = 0;
  }

  /**
   * Sorts the strings.
   */
  public void sort() {
    Arrays.sort(list, 0, size);
  }
}
