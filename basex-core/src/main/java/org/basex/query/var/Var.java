package org.basex.query.var;

import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Variable expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class Var extends ExprInfo {
  /** Static context. */
  private final StaticContext sc;

  /** Variable name. */
  public final QNm name;
  /** Variable ID. */
  public final int id;
  /** Declared type, {@code null} if not specified. */
  public SeqType declType;

  /** Stack slot number. */
  int slot = -1;
  /** Expected result size. */
  public long size = -1;

  /** Flag for function parameters. */
  private final boolean param;
  /** Actual return type (by type inference). */
  private SeqType inType;
  /** Flag for function conversion. */
  private boolean promote;

  /**
   * Constructor.
   * @param ctx query context, used for generating a variable ID
   * @param sctx static context
   * @param n variable name, {@code null} for unnamed variable
   * @param typ expected type, {@code null} for no check
   * @param fun function parameter flag
   */
  Var(final QueryContext ctx, final StaticContext sctx, final QNm n, final SeqType typ,
      final boolean fun) {
    sc = sctx;
    name = n;
    declType = typ == null || typ.eq(SeqType.ITEM_ZM) ? null : typ;
    inType = SeqType.ITEM_ZM;
    id = ctx.varIDs++;
    param = fun;
    promote = fun;
    size = inType.occ();
  }

  /**
   * Constructor for local variables.
   * @param ctx query context, used for generating a variable ID
   * @param sctx static context
   * @param n variable name, {@code null} for unnamed variable
   * @param typ expected type, {@code null} for no check
   */
  Var(final QueryContext ctx, final StaticContext sctx, final QNm n, final SeqType typ) {
    this(ctx, sctx, n, typ, false);
  }

  /**
   * Copy constructor.
   * @param ctx query context
   * @param sctx static context
   * @param var variable to copy
   */
  Var(final QueryContext ctx, final StaticContext sctx, final Var var) {
    this(ctx, sctx, var.name, var.declType, var.param);
    promote = var.promote;
    inType = var.inType;
    size = var.size;
  }

  /**
   * Type of values bound to this variable.
   * @return type (not {@code null})
   */
  public SeqType type() {
    final SeqType intersect = declType != null ? declType.intersect(inType) : null;
    return intersect != null ? intersect : declType != null ? declType : inType;
  }

  /**
   * Declared type of this variable.
   * @return declared type, possibly {@code null}
   */
  public SeqType declaredType() {
    return declType == null ? SeqType.ITEM_ZM : declType;
  }

  /**
   * Tries to refine the compile-time type of this variable through the type of the bound
   * expression.
   * @param t type of the bound expression
   * @param ctx query context
   * @param ii input info
   * @throws QueryException query exception
   */
  public void refineType(final SeqType t, final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    if(t == null) return;
    if(declType != null) {
      if(declType.occ.intersect(t.occ) == null) throw INVCAST.get(ii, t, declType);
      if(!t.convertibleTo(declType)) return;
    }

    if(!inType.eq(t) && !inType.instanceOf(t)) {
      final SeqType is = inType.intersect(t);
      if(is != null) {
        inType = is;
        if(declType != null && inType.instanceOf(declType)) {
          ctx.compInfo(QueryText.OPTCAST, this);
          declType = null;
        }
      }
    }
  }

  /**
   * Determines if this variable checks the type of the expression bound to it.
   * @return {@code true} if the type is checked or promoted, {@code false} otherwise
   */
  public boolean checksType() {
    return declType != null;
  }

  /**
   * Returns an equivalent to the given expression that checks this variable's type.
   * @param e expression
   * @param scp variable scope
   * @param ctx query context
   * @param ii input info
   * @return checked expression
   * @throws QueryException query exception
   */
  public Expr checked(final Expr e, final QueryContext ctx, final VarScope scp,
      final InputInfo ii) throws QueryException {
    return checksType()
        ? new TypeCheck(sc, ii, e, declType, promote).optimize(ctx, scp) : e;
  }

  /**
   * Checks the type of this value and casts/promotes it when necessary.
   * @param val value to be checked
   * @param ctx query context
   * @param ii input info
   * @return checked and possibly cast value
   * @throws QueryException if the check failed
   */
  public Value checkType(final Value val, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(!checksType() || declType.instance(val)) return val;
    if(promote) return declType.funcConvert(ctx, sc, ii, val, true);
    throw INVCAST.get(ii, val.type(), declType);
  }

  /**
   * Checks whether the given variable is identical to this one, i.e. has the
   * same ID.
   * @param v variable to check
   * @return {@code true}, if the IDs are equal, {@code false} otherwise
   */
  public boolean is(final Var v) {
    return id == v.id;
  }

  /**
   * Checks if this variable performs function conversion on its bound values.
   * @return result of check
   */
  public boolean promotes() {
    return promote;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem(QueryText.NAM, '$' + Token.string(name.string()),
        QueryText.ID, Token.token(id));
    if(declType != null) e.add(planAttr(QueryText.AS, declType.toString()));
    addPlan(plan, e);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(name != null) {
      tb.add(QueryText.DOLLAR).add(name.string()).add('_').addInt(id);
      if(declType != null) tb.add(' ' + QueryText.AS);
    }
    if(declType != null) tb.add(" " + declType);
    return tb.toString();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Var && is((Var) obj);
  }

  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Tries to adopt the given type check.
   * @param t type to check
   * @param prom if function conversion should be applied
   * @return {@code true} if the check could be adopted, {@code false} otherwise
   */
  public boolean adoptCheck(final SeqType t, final boolean prom) {
    if(declType == null || t.instanceOf(declType)) {
      declType = t;
    } else if(!declType.instanceOf(t)) {
      return false;
    }

    promote |= prom;
    return true;
  }
}