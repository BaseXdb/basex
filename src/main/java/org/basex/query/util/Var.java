package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Variable expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Var extends ParseExpr {
  /** Variable name. */
  public final QNm name;
  /** Annotations. */
  public final Ann ann;

  /** Expected return type. */
  public SeqType ret;
  /** Global flag. */
  public boolean global;
  /** Declaration flag. */
  public boolean declared;
  /** Cast flag (default: {@code true}). */
  public boolean cast = true;

  /** Static context. */
  private final StaticContext sc;
  /** Variable ID. */
  private final int id;
  /** Bound value. */
  private Value value;
  /** Bound expression. */
  private Expr expr;

  /**
   * Constructor.
   * @param ii input info
   * @param n variable name
   * @param t data type
   * @param i variable ID
   * @param a annotations
   * @param s static context
   */
  private Var(final InputInfo ii, final QNm n, final SeqType t, final int i,
      final Ann a, final StaticContext s) {
    super(ii);
    name = n;
    type = t;
    id = i;
    ann = a == null ? new Ann() : a;
    sc = s;
  }

  /**
   * Creates a new variable.
   * @param ctx query context
   * @param ii input info
   * @param n variable name
   * @param t type
   * @param a annotations
   * @return variable
   */
  public static Var create(final QueryContext ctx, final InputInfo ii, final QNm n,
      final SeqType t, final Ann a) {
    return new Var(ii, n, t, ctx.varIDs++, a, ctx.sc);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public Var compile(final QueryContext ctx) throws QueryException {
    if(expr != null) {
      final StaticContext s = ctx.sc;
      ctx.sc = sc;
      try {
        bind(expr.compile(ctx), ctx);
      } finally {
        ctx.sc = s;
      }
    }
    return this;
  }

  /**
   * Sets the specified variable type.
   * @param t type
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void reset(final SeqType t, final QueryContext ctx) throws QueryException {
    type = t;
    if(value instanceof Item && !value.type.instanceOf(t.type)) {
      value = type.type.cast((Item) value, ctx, info);
    }
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
    return e.isValue() ? bind((Value) e, ctx) : this;
  }

  /**
   * Returns the bound expression.
   * @return expression
   */
  public Expr expr() {
    return expr;
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
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return value(ctx).item(ctx, ii);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    if(value == null) {
      if(expr == null) VAREMPTY.thrw(info, this);
      final StaticContext s = ctx.sc;
      ctx.sc = sc;
      try {
        value = cast(ctx.value(expr.compile(ctx)), ctx);
      } finally {
        ctx.sc = s;
      }
    }
    return value;
  }

  /**
   * Checks whether the given variable is identical to this one, i.e. has the same ID.
   * @param v variable to check
   * @return {@code true}, if the IDs are equal, {@code false} otherwise
   */
  public boolean is(final Var v) {
    return id == v.id;
  }

  /**
   * If necessary, casts the specified value if a type is specified.
   * @param v input value
   * @param ctx query context
   * @return cast value
   * @throws QueryException query exception
   */
  private Value cast(final Value v, final QueryContext ctx) throws QueryException {
    if(type != null) {
      if(cast) return type.promote(v, ctx, info);
      type.treat(v, info);
    }
    return v;
  }

  @Override
  public Var copy() {
    final Var v = new Var(info, name, type, id, ann, sc);
    v.global = global;
    v.value = value;
    v.expr = expr;
    v.type = type;
    v.ret = ret;
    v.cast = cast;
    return v;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.VAR;
  }

  @Override
  public int count(final Var v) {
    return is(v) ? 1 : 0;
  }

  @Override
  public boolean removable(final Var v) {
    // only VarRefs can be removed
    return false;
  }

  @Override
  public Var remove(final Var v) {
    return this;
  }

  @Override
  public boolean databases(final StringList db) {
    return true;
  }

  @Override
  public SeqType type() {
    return ret != null ? ret : type != null ? type :
      expr != null ? expr.type() : SeqType.ITEM_ZM;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Var)) return false;
    final Var v = (Var) cmp;
    return name.eq(v.name) && type().eq(v.type());
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, this, ID, id));
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(name != null) {
      tb.add(DOLLAR).add(name.string());
      if(type != null) tb.add(' ' + AS);
    }
    if(type != null) tb.add(" " + type);
    return tb.toString();
  }
}
