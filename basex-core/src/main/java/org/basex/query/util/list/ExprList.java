package org.basex.query.util.list;

import org.basex.query.expr.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for XQuery expressions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ExprList extends ObjectList<Expr, ExprList> {
  /**
   * Default constructor.
   */
  public ExprList() {
    this(2);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public ExprList(final long capacity) {
    super(new Expr[Array.initialCapacity(capacity)]);
  }

  /**
   * Constructor, assigning the specified array.
   * @param elements initial array
   */
  public ExprList(final Expr... elements) {
    super(elements);
    size = elements.length;
  }

  /**
   * Concatenates entries.
   * @param first first elements
   * @param second next element(s)
   * @return array
   */
  public static Expr[] concat(final Expr[] first, final Expr... second) {
    return new ExprList(first.length + second.length).add(first).add(second).finish();
  }

  /**
   * Concatenates entries.
   * @param first first element
   * @param second next element(s)
   * @return array
   */
  public static Expr[] concat(final Expr first, final Expr... second) {
    return new ExprList(second.length + 1).add(first).add(second).finish();
  }

  @Override
  protected Expr[] newArray(final int s) {
    return new Expr[s];
  }
}
