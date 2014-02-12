package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Set expression.
 *
 * @author BaseX Team 2005-13, BSD License
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
    type = SeqType.NOD_ZM;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
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
  protected abstract NodeSeqBuilder eval(final Iter[] iter) throws QueryException;

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
    return PAR1 + toString(' ' +
        Util.className(this).toLowerCase(Locale.ENGLISH) + ' ') + PAR2;
  }
}
