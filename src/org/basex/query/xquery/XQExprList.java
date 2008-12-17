package org.basex.query.xquery;

import org.basex.query.xquery.expr.Expr;
import org.basex.util.Array;

/**
 * This is a simple container for XQuery expressions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class XQExprList {
  /** Value array. */
  public Expr[] list;
  /** Current array size. */
  public int size;
  
  /**
   * Constructor, specifying an initial list size.
   * @param is initial size of the list
   */
  public XQExprList(final int is) {
    list = new Expr[is];
  }
  
  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final Expr v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
  }

  /**
   * Finishes the int array.
   * @return int array
   */
  public Expr[] finish() {
    return size == list.length ? list : Array.finish(list, size);   
  }
}
