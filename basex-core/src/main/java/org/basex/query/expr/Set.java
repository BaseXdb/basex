package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Set expression.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
abstract class Set extends Arr {
  /** Iterable flag. */
  boolean iterable;

  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  Set(final InputInfo info, final Expr[] exprs) {
    super(info, exprs);
    seqType = SeqType.NOD_ZM;
  }

  @Override
  public final Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    boolean i = true;
    for(final Expr e : exprs) {
      if(!e.iterable()) {
        i = false;
        break;
      }
    }
    iterable = i;
    return this;
  }

  @Override
  public final NodeIter iter(final QueryContext qc) throws QueryException {
    final int el = exprs.length;
    final Iter[] iter = new Iter[el];
    for(int e = 0; e < el; e++) iter[e] = qc.iter(exprs[e]);
    return iterable ? iter(iter) : eval(iter).iter();
  }

  /**
   * Evaluates the specified iterators.
   * @param iter iterators
   * @return resulting node list
   * @throws QueryException query exception
   */
  protected abstract ANodeList eval(final Iter[] iter) throws QueryException;

  /**
   * Evaluates the specified iterators in an iterative manner.
   * @param iter iterators
   * @return resulting iterator
   */
  protected abstract NodeIter iter(final Iter[] iter);

  @Override
  public final boolean iterable() {
    return iterable;
  }

  @Override
  public final String toString() {
    return PAREN1 + toString(' ' + Util.className(this).toLowerCase(Locale.ENGLISH) + ' ') + PAREN2;
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
     * @param iter iterator
     */
    SetIter(final Iter[] iter) {
      this.iter = iter;
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
      final ANode n = toEmptyNode(iter[i].next());
      item[i] = n;
      return n != null;
    }
  }
}
