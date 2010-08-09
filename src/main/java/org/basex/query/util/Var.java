package org.basex.query.util;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Variable.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Var extends ParseExpr {
  /** Return type. */
  public SeqType ret = SeqType.ITEM_ZM;
  /** Variable name. */
  public final QNm name;
  /** Global flag. */
  public boolean global;
  /** Data type. */
  public SeqType type;
  /** Variable expressions. */
  public Expr expr;
  /** Variable results. */
  public Value value;

  /**
   * Constructor, specifying a global variable.
   * @param n variable name
   */
  public Var(final QNm n) {
    this(null, n, null);
    global();
  }

  /**
   * Constructor, specifying a local variable.
   * @param ii input info
   * @param n variable name
   */
  public Var(final InputInfo ii, final QNm n) {
    this(ii, n, null);
  }

  /**
   * Constructor, specifying a local variable.
   * @param ii input info
   * @param n variable name
   * @param t data type
   */
  public Var(final InputInfo ii, final QNm n, final SeqType t) {
    super(ii);
    name = n;
    type = t;
  }

  /**
   * Sets the global flag.
   * @return self reference
   */
  public Var global() {
    global = true;
    return this;
  }

  @Override
  public Var comp(final QueryContext ctx) throws QueryException {
    if(expr != null) bind(checkUp(expr, ctx).comp(ctx), ctx);
    return this;
  }

  /**
   * Binds the specified expression to the variable.
   * @param e expression to be set
   * @param ctx query context
   * @return self reference
   * @throws QueryException query exception
   */
  public Var bind(final Expr e, final QueryContext ctx) throws QueryException {
    expr = e;
    return e.value() ? bind((Value) e, ctx) : this;
  }

  /**
   * Binds the specified value to the variable.
   * @param v value to be set
   * @param ctx query context
   * @return self reference
   * @throws QueryException query exception
   */
  public Var bind(final Value v, final QueryContext ctx) throws QueryException {
    expr = v;
    value = cast(v, ctx);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter(ctx);
  }

  /**
   * Evaluates the variable and returns the resulting value.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  public Value value(final QueryContext ctx) throws QueryException {
    if(value == null) {
      if(expr == null) Err.or(input, VAREMPTY, this);
      final Value v = ctx.value;
      ctx.value = null;
      value = cast(ctx.iter(expr).finish(), ctx);
      ctx.value = v;
    }
    return value;
  }

  /**
   * Compares the variables for reference or name equality.
   * @param v variable
   * @return result of check
   */
  public boolean eq(final Var v) {
    return v == this || v.name.eq(name);
  }

  /**
   * Checks if the variable is not shadowed by the specified variable.
   * @param v variable
   * @return result of check
   */
  public boolean visible(final Var v) {
    return v == null || !v.name.eq(name);
  }

  /**
   * Casts the specified value or checks its type.
   * @param v input value
   * @param ctx query context
   * @return cast value
   * @throws QueryException query exception
   */
  private Value cast(final Value v, final QueryContext ctx)
      throws QueryException {

    if(type == null) return v;
    if(!global && type.zeroOrOne() && !v.type.instance(type.type))
      Err.or(input, XPINVCAST, v.type, type, v);
    return type.cast(v, ctx, input);
  }

  /**
   * Returns a copy of the variable.
   * @return copied variable
   */
  public Var copy() {
    final Var v = new Var(input, name, type);
    if(global) v.global();
    v.value = value;
    v.expr = expr;
    v.ret = ret;
    return v;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.VAR;
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return type != null ? type : expr != null ? expr.returned(ctx) : ret;
  }

  @Override
  public String color() {
    return "66CC66";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, name.atom());
    if(expr != null) expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(name != null) {
      tb.add(DOLLAR);
      tb.add(name.atom());
      if(type != null) tb.add(" " + AS + " ");
    }
    if(type != null) tb.add(type);
    return tb.toString();
  }
}
