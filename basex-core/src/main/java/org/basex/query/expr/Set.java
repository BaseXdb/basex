package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.util.*;

/**
 * Set expression.
 *
 * @author BaseX Team 2005-17, BSD License
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
    super(info, SeqType.NOD_ZM, exprs);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    boolean i = true;
    for(final Expr expr : exprs) {
      if(!expr.iterable()) {
        i = false;
        break;
      }
    }
    iterable = i;

    Type t = null;
    for(final Expr expr : exprs) {
      final Type t2 = expr.seqType().type;
      t = t == null ? t2 : t.union(t2);
    }
    if(t instanceof NodeType) seqType = SeqType.get(t, Occ.ZERO_MORE);

    return this;
  }

  @Override
  public final NodeIter iter(final QueryContext qc) throws QueryException {
    final int el = exprs.length;
    final Iter[] iter = new Iter[el];
    for(int e = 0; e < el; e++) iter[e] = qc.iter(exprs[e]);
    return iterable ? iter(iter) : eval(iter, qc).iter();
  }

  /**
   * Evaluates the specified iterators.
   * @param iters iterators
   * @param qc query context
   * @return resulting node list
   * @throws QueryException query exception
   */
  protected abstract ANodeBuilder eval(Iter[] iters, QueryContext qc) throws QueryException;

  /**
   * Evaluates the specified iterators in an iterative manner.
   * @param iters iterators
   * @return resulting iterator
   */
  protected abstract NodeIter iter(Iter[] iters);

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
    ANode[] nodes;

    /**
     * Constructor.
     * @param iter iterator
     */
    SetIter(final Iter[] iter) {
      this.iter = iter;
    }

    /**
     * Sets the next iterator item.
     * @param i index
     * @return true if another item was found
     * @throws QueryException query exception
     */
    final boolean next(final int i) throws QueryException {
      final ANode n = toEmptyNode(iter[i].next());
      nodes[i] = n;
      return n != null;
    }
  }
}
