package org.basex.query.item;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import static org.basex.query.QueryTokens.*;

import org.basex.query.expr.Expr;
import org.basex.query.iter.ItemIter;
import static org.basex.query.util.Err.*;

import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Function item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public class FunItem extends Item {

  /** Variables. */
  private final Var[] vars;
  /** Function expression. */
  private Expr expr;

  /**
   * Constructor.
   * @param arg function arguments
   * @param body function body
   * @param t function type
   */
  public FunItem(final Var[] arg, final Expr body, final FunType t) {
    super(t);
    vars = arg;
    expr = body;
  }

  /**
   * Number of arguments this function item takes.
   * @return function arity
   */
  public int arity() {
    return vars.length;
  }

  /**
   * Invokes this function item with the given arguments.
   * @param args arguments
   * @param ctx query context
   * @param ii input info
   * @return resulting item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public ItemIter invIter(final Value[] args, final QueryContext ctx,
       final InputInfo ii) throws QueryException {

    // move variables to stack
    final int s = ctx.vars.size();
    for(int a = vars.length; a-- > 0;)
      ctx.vars.add(vars[a].bind(args[a], ctx).copy());

    // evaluate function and reset variable scope
    final ItemIter ir = ItemIter.get(ctx.iter(expr));
    ctx.vars.reset(s);
    return ir;
  }

  /**
   * Invokes this function item with the given arguments.
   * @param args arguments
   * @param ctx query context
   * @param ii input info
   * @return resulting item
   * @throws QueryException query exception
   */
  public Item invItem(final Value[] args, final QueryContext ctx,
       final InputInfo ii) throws QueryException {

    final ItemIter ir = invIter(args, ctx, ii);

    final Item it = ir.next();
    if(it == null || ir.size() == 1) return it;

    final Item n = ir.next();
    if(n != null) XPSEQ.thrw(ii, PAR1 + it + SEP + n +
        (ir.next() != null ? SEP + DOTS : "") + PAR2);
    return it;
  }

  @Override
  public byte[] atom(final InputInfo ii) throws QueryException {
    NOATM.thrw(ii);
    return null;
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    TYPECMP.thrw(ii, FITM);
    return false;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FUNCTION).append('(');
    for(final Var v : vars)
      sb.append(v).append(v == vars[vars.length - 1] ? "" : ", ");
    return sb.append(") { ").append(expr).append(" }").toString();
  }
}
