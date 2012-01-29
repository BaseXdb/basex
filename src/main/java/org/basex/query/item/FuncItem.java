package org.basex.query.item;

import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import static org.basex.query.QueryText.*;
import org.basex.query.expr.Expr;
import org.basex.query.expr.VarRef;
import org.basex.query.func.DynamicFunc;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.query.util.VarStack;
import org.basex.util.InputInfo;
import static org.basex.util.Token.*;
import org.basex.util.Util;

/**
 * Function item.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FuncItem extends FItem {
  /** Variables. */
  private final Var[] vars;
  /** Function expression. */
  private final Expr expr;
  /** Function name. */
  private final QNm name;
  /** Optional type to cast to. */
  private final SeqType cast;

  /** The closure of this function item. */
  private final VarStack closure = new VarStack();

  /**
   * Constructor.
   * @param n function name
   * @param arg function arguments
   * @param body function body
   * @param t function type
   * @param cst cast flag
   */
  public FuncItem(final QNm n, final Var[] arg, final Expr body,
      final FuncType t, final boolean cst) {
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
  public FuncItem(final Var[] arg, final Expr body, final FuncType t,
      final VarStack cl, final boolean cst) {

    this(null, arg, body, t, cst);
    if(cl != null) {
      for(int i = cl.size; --i >= 0;) {
        final Var v = cl.vars[i];
        if(body.count(v) != 0 && !closure.contains(v)) closure.add(v.copy());
      }
    }
  }

  @Override
  public int arity() {
    return vars.length;
  }

  @Override
  public QNm fName() {
    return name;
  }

  /**
   * Binds all variables to the context.
   * @param ctx query context
   * @param args argument values
   * @throws QueryException query exception
   */
  private void bindVars(final QueryContext ctx, final Value[] args)
      throws QueryException {
    for(int v = closure.size; --v >= 0;)
      ctx.vars.add(closure.vars[v].copy());
    for(int v = vars.length; --v >= 0;)
      ctx.vars.add(vars[v].bind(args[v], ctx).copy());
  }

  @Override
  public Value invValue(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {

    // bind variables and cache context
    final VarStack cs = ctx.vars.cache(args.length);
    final Value cv = ctx.value;
    try {
      bindVars(ctx, args);
      ctx.value = null;
      final Value v = ctx.value(expr);
      // optionally cast return value to target type
      return cast != null ? cast.promote(v, ctx, ii) : v;
    } finally {
      ctx.value = cv;
      ctx.vars.reset(cs);
    }
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

    // bind variables and cache context
    final VarStack cs = ctx.vars.cache(args.length);
    final Value cv = ctx.value;
    try {
      bindVars(ctx, args);
      ctx.value = null;
      final Item it = expr.item(ctx, ii);
      // optionally cast return value to target type
      return cast != null ? cast.cast(it, expr, false, ctx, ii) : it;
    } finally {
      ctx.value = cv;
      ctx.vars.reset(cs);
    }
  }

  @Override
  public String toString() {
    final FuncType ft = (FuncType) type;
    final StringBuilder sb = new StringBuilder(FUNCTION).append('(');
    for(final Var v : vars)
      sb.append(v).append(v == vars[vars.length - 1] ? "" : ", ");
    return sb.append(")").append(ft.ret != null ? " as " + ft.ret :
      "").append(" { ").append(expr).append(" }").toString();
  }

  @Override
  public boolean uses(final Use u) {
    return expr.uses(u);
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
  public static FuncItem coerce(final QueryContext ctx, final InputInfo ii,
      final FuncItem fun, final FuncType t) {
    final Var[] vars = new Var[fun.vars.length];
    final Expr[] refs = new Expr[vars.length];
    for(int i = vars.length; i-- > 0;) {
      vars[i] = ctx.uniqueVar(ii, t.args[i]);
      refs[i] = new VarRef(ii, vars[i]);
    }
    return new FuncItem(fun.name, vars, new DynamicFunc(ii, fun, refs), t,
        fun.cast != null);
  }

  @Override
  public FItem coerceTo(final FuncType ft, final QueryContext ctx,
      final InputInfo ii) throws QueryException {

    if(vars.length != ft.args.length) throw Err.cast(ii, ft, this);
    return type.instanceOf(ft) ? this : FuncItem.coerce(ctx, ii, this, ft);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(token(Util.name(this)), token(TYPE),
        token(type.toString()));
    for(final Var v : vars) v.plan(ser);
    expr.plan(ser);
    ser.closeElement();
  }
}
