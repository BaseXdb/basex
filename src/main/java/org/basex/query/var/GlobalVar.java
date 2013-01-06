package org.basex.query.var;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Static variable which can be assigned an expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class GlobalVar extends VarRef {
  /** Annotations. */
  public final Ann ann;
  /** Declaration flag. */
  boolean declared;
  /** Flag for promoting the bound value to the target type. */
  private boolean promote;
  /** Bound value. */
  private Value value;
  /** Bound expression. */
  private Expr expr;

  /** Variables should only be compiled once. */
  private boolean compiled;

  /**
   * Constructor.
   * @param ii input info
   * @param a annotations
   * @param n variable name
   * @param t variable type
   * @param e expression to be bound
   * @param decl declaration flag
   */
  GlobalVar(final InputInfo ii, final Ann a, final QNm n, final SeqType t, final Expr e,
      final boolean decl) {
    super(n, ii);
    ann = a == null ? new Ann() : a;
    type = t;
    expr = e;
    declared = decl;
  }

  @Override
  public void checkUp() throws QueryException {
    if(expr != null && expr.uses(Use.UPD)) UPNOT.thrw(info, description());
  }

  @Override
  public Value compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(compiled) {
      if(value == null) throw Err.CIRCVAR.thrw(info, this);
      return value;
    }
    if(expr == null) throw VAREMPTY.thrw(info, this);

    final Value val = expr.compile(ctx, scp).value(ctx);
    compiled = true;
    return bind(val, ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    if(value != null) return value;
    if(expr == null) throw VAREMPTY.thrw(info, this);
    return bind(expr.value(ctx), ctx);
  }

  /**
   * Binds the specified expression to the variable.
   * @param e expression to be set
   * @param ctx query context
   * @return self reference
   * @throws QueryException query exception
   */
  public Expr bind(final Expr e, final QueryContext ctx) throws QueryException {
    expr = e;
    if(e.isValue()) return bind((Value) e, ctx);
    compiled = false;
    return this;
  }

  /**
   * Binds the specified value to the variable.
   * @param v value to be set
   * @param ctx query context
   * @return self reference
   * @throws QueryException query exception
   */
  private Value bind(final Value v, final QueryContext ctx) throws QueryException {
    expr = v;
    value = cast(v, ctx);
    return value;
  }

  /**
   * If necessary, casts the specified value if a type is specified.
   * @param v input value
   * @param ctx query context
   * @return cast value
   * @throws QueryException query exception
   */
  private Value cast(final Value v, final QueryContext ctx) throws QueryException {
    if(!(type == null || type.instance(v))) {
      if(promote) return type().promote(v, ctx, info);
      XPTYPE.thrw(info, this, type, v.type());
    }
    return v;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem(NAM, name.string());
    if(expr != null) expr.plan(e);
    plan.add(e);
  }

  @Override
  public SeqType type() {
    return type != null ? type : SeqType.ITEM_ZM;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof GlobalVar)) return false;
    final GlobalVar v = (GlobalVar) cmp;
    return name.equals(v.name) && type().eq(v.type());
  }

  @Override
  public boolean uses(final Use u) {
    return expr != null && expr.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    return false;
  }

  @Override
  public Expr remove(final Var v) {
    return this;
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    return expr.visitVars(visitor);
  }

  /**
   * Tries to refine the compile-time type of this variable through the type of the bound
   * expression.
   * @param t type of the bound expression
   * @return {@code true} if the type changed, {@code false} otherwise
   * @throws QueryException if the types are incompatible
   */
  public boolean refineType(final SeqType t) throws QueryException {
    if(t != null && type != t) {
      if(type == null || type.instance(t)) {
        type = t;
        return true;
      }
      if(!t.instance(type)) throw XPTYPE.thrw(info, this, type, t);
    }
    return false;
  }

  /**
   * Getter.
   * @return the bound expression
   */
  protected Expr expr() {
    return expr;
  }

  @Override
  public boolean databases(final StringList db) {
    // [LW] what if {@code expr == null}?
    return expr == null || expr.databases(db);
  }
}
