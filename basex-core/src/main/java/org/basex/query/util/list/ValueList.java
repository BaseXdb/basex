package org.basex.query.util.list;

import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.util.list.*;

/**
 * This is a simple container for values.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ValueList extends ElementList {
  /** Element container. */
  private Value[] list;

  /**
   * Default constructor.
   */
  public ValueList() {
    this(1);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param capacity array capacity
   */
  public ValueList(final int capacity) {
    list = new Value[capacity];
  }

  /**
   * Constructor, specifying an initial entry.
   * @param element array capacity
   */
  public ValueList(final Value element) {
    list = new Value[] { element };
    size = 1;
  }

  /**
   * Returns the specified element.
   * @param p position
   * @return value
   */
  public Value get(final int p) {
    return list[p];
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  public ValueList add(final Value element) {
    if(size == list.length) resize(newSize());
    list[size++] = element;
    return this;
  }

  /**
   * Resizes the array.
   * @param sz new size
   */
  private void resize(final int sz) {
    final Value[] tmp = new Value[sz];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public Value[] finish() {
    Value[] lst = list;
    final int s = size;
    if(s != lst.length) {
      lst = new Value[s];
      System.arraycopy(list, 0, lst, 0, s);
    }
    list = null;
    return lst;
  }

  /**
   * Returns an XQuery array and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public Array array() {
    return Array.get(finish());
  }
}
