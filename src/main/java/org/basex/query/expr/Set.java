package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.Locale;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.NodeIter;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Set expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class Set extends Arr {
  /** Iterable flag. */
  boolean iterable = true;

  /**
   * Constructor.
   * @param ii input info
   * @param l expression list
   */
  Set(final InputInfo ii, final Expr[] l) {
    super(ii, l);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    type = SeqType.NOD_ZM;
    super.comp(ctx);
    for(final Expr e : expr) {
      if(e.iterable()) continue;
      iterable = false;
      break;
    }
    return this;
  }

  @Override
  public final NodeIter iter(final QueryContext ctx) throws QueryException {
    final Iter[] iter = new Iter[expr.length];
    for(int e = 0; e != expr.length; ++e) iter[e] = ctx.iter(expr[e]);
    return iterable ? iter(iter) : eval(iter).sort();
  }

  /**
   * Evaluates the specified iterators.
   * @param iter iterators
   * @return resulting iterator
   * @throws QueryException query exception
   */
  protected abstract NodeCache eval(final Iter[] iter) throws QueryException;

  /**
   * Evaluates the specified iterators in an iterative manner.
   * @param iter iterators
   * @return resulting iterator
   */
  protected abstract NodeIter iter(final Iter[] iter);

  @Override
  public boolean iterable() {
    return true;
  }

  /**
   * Abstract set iterator.
   */
  abstract class SetIter extends NodeIter {
    /** Iterator. */
    final Iter[] iter;
    /** Items. */
    ANode[] item;

    /**
     * Constructor.
     * @param ir iterator
     */
    SetIter(final Iter[] ir) {
      iter = ir;
    }

    @Override
    public abstract ANode next() throws QueryException;

    /**
     * Sets the next iterator item.
     * @param i index
     * @return true if another item was found
     * @throws QueryException query exception
     */
    boolean next(final int i) throws QueryException {
      final Item it = iter[i].next();
      item[i] = it == null ? null : checkNode(it);
      return it != null;
    }
  }

  @Override
  public final String toString() {
    return PAR1 + toString(" " +
        Util.name(this).toUpperCase(Locale.ENGLISH) + " ") + PAR2;
  }
}
