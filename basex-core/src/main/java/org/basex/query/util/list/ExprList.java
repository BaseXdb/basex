package org.basex.query.util.list;

import org.basex.query.expr.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for XQuery expressions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ExprList extends ElementList {
  /** Element container. */
  private Expr[] list;

  /**
   * Default constructor.
   */
  public ExprList() {
    this(1);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param capacity array capacity
   */
  public ExprList(final int capacity) {
    list = new Expr[capacity];
  }

  /**
   * Constructor, specifying an initial entry.
   * @param element array capacity
   */
  public ExprList(final Expr element) {
    list = new Expr[] { element };
    size = 1;
  }

  /**
   * Returns the specified element.
   * @param p position
   * @return value
   */
  public Expr get(final int p) {
    return list[p];
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  public ExprList add(final Expr element) {
    if(size == list.length) resize(newSize());
    list[size++] = element;
    return this;
  }

  /**
   * Adds elements to the array.
   * @param elements elements to be added
   * @return self reference
   */
  public ExprList add(final Expr... elements) {
    final int l = elements.length, s = size, ns = s + l;
    if(ns > list.length) resize(newSize(ns));
    System.arraycopy(elements, 0, list, s, l);
    size = ns;
    return this;
  }

  /**
   * Sets an element at the specified index position.
   * @param index index
   * @param element element to be set
   */
  public void set(final int index, final Expr element) {
    if(index >= list.length) resize(newSize(index + 1));
    list[index] = element;
    size = Math.max(size, index + 1);
  }

  /**
   * Checks if the specified element is found in the list.
   * @param element element to be found
   * @return result of check
   */
  public boolean contains(final Expr element) {
    for(int i = 0; i < size; ++i) if(list[i].sameAs(element)) return true;
    return false;
  }

  /**
   * Resizes the array.
   * @param sz new size
   */
  private void resize(final int sz) {
    final Expr[] tmp = new Expr[sz];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public Expr[] finish() {
    Expr[] lst = list;
    final int s = size;
    if(s != lst.length) {
      lst = new Expr[s];
      System.arraycopy(list, 0, lst, 0, s);
    }
    list = null;
    return lst;
  }
}
