package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.SeqBuilder;

/**
 * Expression List.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class List extends Arr {
  /**
   * Constructor.
   * @param l expression list
   */
  public List(final Expr[] l) {
    super(l);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    for(final Expr e : expr) if(!e.i()) return this;

    // all values are items - return simple sequence
    final SeqBuilder seq = new SeqBuilder();
    for(final Expr e : expr) seq.add(e.iter(ctx));
    return seq.finish();
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final SeqIter seq = new SeqIter();
    for(final Expr e : expr) seq.add(ctx.iter(e));
    return seq;
  }

  @Override
  public String toString() {
    return "(" + toString(", ") + ")";
  }
}
