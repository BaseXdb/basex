package org.basex.query.util.regex;

import org.basex.util.list.*;

/**
 * This is a simple container for expressions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class RegExpList extends ElementList {
  /** Element container. */
  private RegExp[] list = new RegExp[1];

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  public RegExpList add(final RegExp element) {
    if(size == list.length) resize(newSize());
    list[size++] = element;
    return this;
  }

  /**
   * Returns the specified element.
   * @param p position
   * @return value
   */
  public RegExp get(final int p) {
    return list[p];
  }

  /**
   * Resizes the array.
   * @param sz new size
   */
  private void resize(final int sz) {
    final RegExp[] tmp = new RegExp[sz];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public RegExp[] finish() {
    RegExp[] lst = list;
    final int s = size;
    if(s != lst.length) {
      lst = new RegExp[s];
      System.arraycopy(list, 0, lst, 0, s);
    }
    list = null;
    return lst;
  }
}
