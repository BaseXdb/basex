package org.basex.query.item;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import static org.basex.query.QueryTokens.*;
import org.basex.query.expr.DynFunCall;
import org.basex.query.expr.Expr;
import org.basex.query.expr.VarRef;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.query.util.VarList;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Function item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public class FunItem extends FItem {

  /** Variables. */
  private final Var[] vars;
  /** Function expression. */
  private final Expr expr;
  /** Function name. */
  private final QNm name;
  /** Optional type to cast to. */
  private final SeqType cast;

  /** The closure of this function item. */
  private final VarList closure = new VarList();

  /**
   * Constructor.
   * @param n function name
   * @param arg function arguments
   * @param body function body
   * @param t function type
   * @param cst cast flag
   */
  public FunItem(final QNm n, final Var[] arg, final Expr body,
      final FunType t, final boolean cst) {
    super(t);
    name = n;
    vars = arg;
    expr = body;
    cast = cst && t.ret != null ? t.ret : null;
  }

  /**
   * Constructor for anonymous functions.
   * @param arg function arguments
   * @param body function body
   * @param t function type
   * @param cl variables in the closure
   * @param cst cast flag
   */
  public FunItem(final Var[] arg, final Expr body, final FunType t,
      final VarList cl, final boolean cst) {
    this(null, arg, body, t, cst);
    if(cl != null)
      for(int i = 0; i < cl.size; i++) closure.set(cl.vars[i].copy());
  }

  @Override
  public int arity() {
    return vars.length;
  }

  @Override
  public QNm fName() {
    return name;
  }

  @Override
  public Value invValue(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {

    // move variables to stack
    final int s = ctx.vars.size();
    for(int i = closure.size; i-- > 0;)
      ctx.vars.add(closure.vars[i].copy());
    for(int a = vars.length; a-- > 0;)
      ctx.vars.add(vars[a].bind(args[a], ctx).copy());

    // evaluate function
    final Value cv = ctx.value;
    ctx.value = null;
    final Value v = ctx.value(expr);
    ctx.value = cv;

    // reset variable scope
    ctx.vars.reset(s);

    // optionally cast return value to target type
    return cast != null ? cast.promote(v, ctx, ii) : v;
  }

  @Override
  public Iter invIter(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {

    // [LW] make result streamable
    return invValue(ctx, ii, args).iter();
  }

  @Override
  public Item invItem(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {

    // move variables to stack
    final int s = ctx.vars.size();
    for(int i = closure.size; i-- > 0;)
      ctx.vars.add(closure.vars[i].copy());
    for(int a = vars.length; a-- > 0;)
      ctx.vars.add(vars[a].bind(args[a], ctx).copy());

    // evaluate function
    final Value cv = ctx.value;
    ctx.value = null;
    final Item it = expr.item(ctx, ii);
    ctx.value = cv;

    // reset variable scope
    ctx.vars.reset(s);

    // optionally promote return value to target type
    return cast != null ? cast.cast(it, expr, false, ctx, ii) : it;
  }

  @Override
  public String toString() {
    final FunType ft = (FunType) type;
    final StringBuilder sb = new StringBuilder(FUNCTION).append('(');
    for(final Var v : vars)
      sb.append(v).append(v == vars[vars.length - 1] ? "" : ", ");
    return sb.append(")").append(ft.ret != null ? " as " + ft.ret :
      "").append(" { ").append(expr).append(" }").toString();
  }

  @Override
  public boolean uses(final Use u) {
    return super.uses(u);
  }

  @Override
  public int count(final Var v) {
    return expr.count(v);
  }

  /**
   * Coerces a function item to the given type.
   * @param ctx query context
   * @param ii input info
   * @param fun function item to coerce
   * @param t type to coerce to
   * @return coerced function item
   */
  public static FunItem coerce(final QueryContext ctx, final InputInfo ii,
      final FunItem fun, final FunType t) {
    final Var[] vars = new Var[fun.vars.length];
    final Expr[] refs = new Expr[vars.length];
    for(int i = vars.length; i-- > 0;) {
      vars[i] = ctx.uniqueVar(ii, t.args[i]);
      refs[i] = new VarRef(ii, vars[i]);
    }
    return new FunItem(fun.name, vars, new DynFunCall(ii, fun, refs), t,
        fun.cast != null);
  }

  @Override
  public FItem coerceTo(final FunType ft, final QueryContext ctx,
      final InputInfo ii) throws QueryException {

    if(vars.length != ft.args.length) throw Err.cast(ii, ft, this);
    return type.instance(ft) ? this : FunItem.coerce(ctx, ii, this, ft);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(Token.token(FUNC));
    ser.openElement(VAR);
    for(final Var v : vars) v.plan(ser);
    ser.closeElement();
    expr.plan(ser);
    ser.closeElement();
  }
}
