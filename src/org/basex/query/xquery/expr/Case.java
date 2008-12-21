package org.basex.query.xquery.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.xquery.XQTokens.*;
import org.basex.BaseX;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Var;

/**
 * Case expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Case extends Single {
  /** Variable. */
  private final Var var;

  /**
   * Constructor.
   * @param v variable
   * @param r return expression
   */
  public Case(final Var v, final Expr r) {
    super(r);
    var = v;
  }

  @Override
  public Expr comp(final XQContext ctx) {
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) {
    BaseX.notexpected();
    return null;
  }

  /**
   * Evaluates the given sequence.
   * @param ctx query context
   * @param seq sequence to be checked
   * @return resulting item
   * @throws XQException evaluation exception
   */
  Iter iter(final XQContext ctx, final SeqIter seq) throws XQException {
    if(!var.type.instance(seq)) return null;
    if(var.name == null) return ctx.iter(expr);

    final int s = ctx.vars.size();
    ctx.vars.add(var.item(seq.finish(), ctx));
    final Iter sb = ctx.iter(expr);
    ctx.vars.reset(s);
    return sb;
  }

  @Override
  public Type returned() {
    return expr.returned();
  }

  @Override
  public String toString() {
    return CASE + " " + var.type + " " + RETURN + " " + expr;
  }
}
