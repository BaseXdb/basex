package org.basex.query.util;

import org.basex.query.expr.*;
import org.basex.util.list.*;

/**
 * This is a simple container for expressions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ExprList extends ElementList {
  /** Element container. */
  private Expr[] list;

  /**
   * Default constructor.
   */
  public ExprList() {
    list = new Expr[1];
  }

  /**
   * Constructor, specifying an initial entry.
   * @param c array capacity
   */
  public ExprList(final Expr c) {
    list = new Expr[] { c };
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
   * @param e element to be added
   */
  public void add(final Expr e) {
    if(size == list.length) resize(newSize());
    list[size++] = e;
  }

  /**
   * Sets an element at the specified index position.
   * @param i index
   * @param e element to be set
   */
  public void set(final int i, final Expr e) {
    if(i >= list.length) resize(newSize(i + 1));
    list[i] = e;
    size = Math.max(size, i + 1);
  }

  /**
   * Resizes the array.
   * @param s new size
   */
  private void resize(final int s) {
    final Expr[] tmp = new Expr[s];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }

  /**
   * Returns an array with all elements.
   * Warning: returns the internal array representation for better performance.
   * @return internal array
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
