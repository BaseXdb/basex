package org.basex.query.item;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import static org.basex.query.QueryTokens.*;
import org.basex.query.expr.Expr;
import org.basex.query.iter.ItemCache;
import static org.basex.query.util.Err.*;

import org.basex.query.util.Var;
import org.basex.query.util.VarList;
import org.basex.util.InputInfo;
import org.basex.util.Util;

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
  /** Function name. */
  private QNm name;

  /** The closure of this function item. */
  private final VarList closure = new VarList();

  /**
   * Constructor.
   * @param n function name
   * @param arg function arguments
   * @param body function body
   * @param t function type
   */
  public FunItem(final QNm n, final Var[] arg, final Expr body,
      final FunType t) {
    super(t);
    name = n;
    vars = arg;
    expr = body;
  }

  /**
   * Constructor for anonymous functions.
   * @param arg function arguments
   * @param body function body
   * @param t function type
   * @param cl variables in the closure
   */
  public FunItem(final Var[] arg, final Expr body, final FunType t,
      final VarList cl) {
    this(null, arg, body, t);
    if(cl != null)
      for(int i = 0; i < cl.size; i++) closure.set(cl.vars[i].copy());
  }

  /**
   * Number of arguments this function item takes.
   * @return function arity
   */
  public int arity() {
    return vars.length;
  }

  /**
   * Name of this function, {@code null} means anonymous function.
   * @return name or {@code null}
   */
  public QNm fName() {
    return name;
  }

  /**
   * Variables of this function item.
   * @return the vars
   */
  public Var[] vars() {
    return vars;
  }

  /**
   * Function body of this function item.
   * @return the function body
   */
  public Expr body() {
    return expr;
  }

  /**
   * Invokes this function item with the given arguments.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public ItemCache invIter(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {

    // move variables to stack
    final int s = ctx.vars.size();
    for(int i = closure.size; i-- > 0;)
      ctx.vars.add(closure.vars[i].copy());
    for(int a = vars.length; a-- > 0;)
      ctx.vars.add(vars[a].bind(args[a], ctx).copy());

    // evaluate function and reset variable scope
    final ItemCache ir = ItemCache.get(ctx.iter(expr));
    ctx.vars.reset(s);
    return ir;
  }

  /**
   * Invokes this function item with the given arguments.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting item
   * @throws QueryException query exception
   */
  public Item invItem(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {

    final ItemCache ir = invIter(ctx, ii, args);

    final Item it = ir.next();
    if(it == null || ir.size() == 1) return it;

    final Item n = ir.next();
    if(n != null) XPSEQ.thrw(ii, PAR1 + it + SEP + n +
        (ir.next() != null ? SEP + DOTS : "") + PAR2);
    return it;
  }

  @Override
  public byte[] atom(final InputInfo ii) throws QueryException {
    throw NOTYP.thrw(ii, this);
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    throw FNEQ.thrw(ii, this);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FUNCTION).append('(');
    for(final Var v : vars)
      sb.append(v).append(v == vars[vars.length - 1] ? "" : ", ");
    return sb.append(") { ").append(expr).append(" }").toString();
  }

  @Override
  public boolean uses(final Use u) {
    return super.uses(u);
  }

  @Override
  public int count(final Var v) {
    return expr.count(v);
  }

  @Override
  public Object toJava() {
    throw Util.notexpected();
  }
}
