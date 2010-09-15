package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.util.InputInfo;

/**
 * Set expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Set extends Arr {
  /** Duplicate flag; {@code true} if arguments contain duplicates. */
  protected boolean dupl;
  
  /**
   * Constructor.
   * @param ii input info
   * @param l expression list
   */
  public Set(final InputInfo ii, final Expr[] l) {
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
    return dupl ? eval(iter) : iter(iter);
  }

  /**
   * Evaluates the specified iterators.
   * @param iter iterators
   * @return resulting iterator
   * @throws QueryException query exception
   */
  protected abstract NodeIter eval(final Iter[] iter) throws QueryException;

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
}
