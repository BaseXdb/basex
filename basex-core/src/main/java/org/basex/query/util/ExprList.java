package org.basex.query.util;

import org.basex.query.expr.*;
import org.basex.util.list.*;

/**
 * This is a simple container for expressions.
 *
 * @author BaseX Team 2005-14, BSD License
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
    for(final Expr e : elements) add(e);
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
   * Resizes the array.
   * @param sz new size
   */
  private void resize(final int sz) {
    final Expr[] tmp = new Expr[sz];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }

  /**
   * Returns an array with all elements.
   * Warning: the internal array representation may be returned to improve performance.
   * @return internal or internal array
   */
  public Expr[] finish() {
    if(size != list.length) {
      final Expr[] tmp = new Expr[size];
      System.arraycopy(list, 0, tmp, 0, size);
      list = tmp;
    }
    return list;
  }
}
