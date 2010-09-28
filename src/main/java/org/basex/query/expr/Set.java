package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Set expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
abstract class Set extends Arr {
  /** Duplicate flag; {@code true} if arguments contain duplicates. */
  protected boolean dupl;

  /**
   * Constructor.
   * @param ii input info
   * @param l expression list
   */
  protected Set(final InputInfo ii, final Expr[] l) {
    super(ii, l);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    type = SeqType.NOD_ZM;
    super.comp(ctx);
    for(final Expr e : expr) dupl |= e.duplicates();
    return this;
  }

  @Override
  public final NodeIter iter(final QueryContext ctx) throws QueryException {
    final Iter[] iter = new Iter[expr.length];
    for(int e = 0; e != expr.length; ++e) iter[e] = ctx.iter(expr[e]);
    return dupl ? eval(iter).sort() : iter(iter);
  }

  /**
   * Evaluates the specified iterators.
   * @param iter iterators
   * @return resulting iterator
   * @throws QueryException query exception
   */
  protected abstract NodIter eval(final Iter[] iter) throws QueryException;

  /**
   * Evaluates the specified iterators in an iterative manner.
   * @param iter iterators
   * @return resulting iterator
   */
  protected abstract NodeIter iter(final Iter[] iter);
  @Override
  public boolean duplicates() {
    return false;
  }

  /**
   * Abstract set iterator.
   */
  abstract class SetIter extends NodeIter {
    /** Iterator. */
    protected final Iter[] iter;
    /** Items. */
    protected Nod[] item;

    /**
     * Constructor.
     * @param ir iterator
     */
    protected SetIter(final Iter[] ir) {
      iter = ir;
    }

    @Override
    public abstract Nod next() throws QueryException;

    /**
     * Sets the next iterator item.
     * @param i index
     * @return true if another item was found
     * @throws QueryException query exception
     */
    protected boolean next(final int i) throws QueryException {
      final Item it = iter[i].next();
      item[i] = it == null ? null : checkNode(it);
      return it != null;
    }
  }

  @Override
  public final String toString() {
    return PAR1 + toString(" " + Util.name(this).toUpperCase() + " ") + PAR2;
  }
}
