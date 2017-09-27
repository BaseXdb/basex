package org.basex.query.util.list;

import org.basex.query.expr.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for XQuery expressions.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ExprList extends ObjectList<Expr, ExprList> {
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
    super(new Expr[capacity]);
  }

  /**
   * Constructor, specifying an initial entry.
   * @param element array capacity
   */
  public ExprList(final Expr element) {
    super(element);
    size = 1;
  }

  /**
   * Concatenates entries.
   * @param source source elements
   * @param add elements to be added
   * @return array
   */
  public static Expr[] concat(final Expr[] source, final Expr... add) {
    final int sl = source.length, al = add.length;
    final Expr[] tmp = new Expr[sl + al];
    System.arraycopy(source, 0, tmp, 0, sl);
    System.arraycopy(add, 0, tmp, sl, al);
    return tmp;
  }

  @Override
  protected Expr[] newList(final int s) {
    return new Expr[s];
  }
}
