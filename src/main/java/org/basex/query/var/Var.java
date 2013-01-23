package org.basex.query.var;

import org.basex.data.ExprInfo;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Variable expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class Var extends ExprInfo {
  /** Variable name. */
  public final QNm name;
  /** Variable ID. */
  public final int id;

  /** Stack slot number. */
  public int slot = -1;

  /** Expected result size. */
  public long size = -1;

  /** Expected return type, {@code null} if not important. */
  private SeqType ret;
  /** Actual return type (by type inference). */
  private SeqType type;
  /** Flag for global variables. */
  public final boolean param;

  /**
   * Constructor.
   * @param ctx query context, used for generating a variable ID
   * @param n variable name, {@code null} for unnamed variable
   * @param typ expected type, {@code null} for no check
   * @param fun function parameter flag
   */
  Var(final QueryContext ctx, final QNm n, final SeqType typ, final boolean fun) {
    name = n;
    ret = typ;
    type = typ != null ? typ : SeqType.ITEM_ZM;
    id = ctx.varIDs++;
    param = fun;
    size = type.occ();
  }

  /**
   * Constructor for local variables.
   * @param ctx query context, used for generating a variable ID
   * @param n variable name, {@code null} for unnamed variable
   * @param typ expected type, {@code null} for no check
   */
  Var(final QueryContext ctx, final QNm n, final SeqType typ) {
    this(ctx, n, typ, false);
  }

  /**
   * Type of values bound to this variable.
   * @return (non-{@code null}) type
   */
  public SeqType type() {
    return type;
  }

  /**
   * Tries to refine the compile-time type of this variable through the type of the bound
   * expression.
   * @param t type of the bound expression
   * @param ii input info for errors
   * @throws QueryException if the types are incompatible
   */
  public void refineType(final SeqType t, final InputInfo ii) throws QueryException {
    if(t == null || type == null || type.eq(t) || type.instance(t)) {
      if(type == null) type = t;
      return;
    }

    final Type tp = type.type.instanceOf(t.type)
        ? t.type
        : t.type.instanceOf(type.type)
            ? t.type
            : null;
    final Occ occ = type.occ.instance(t.occ)
        ? t.occ
        : t.occ.instance(type.occ)
            ? t.occ
            : null;

    if(tp == null || occ == null) throw Err.XPTYPE.thrw(ii, toString(), type, t);
    type = SeqType.get(tp, occ);
  }

  /**
   * Determines if this variable checks the type of the expression bound to it.
   * @return {@code true} if the type is checked or promoted, {@code false} otherwise
   */
  public boolean checksType() {
    return ret != null;
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

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem(QueryText.NAM, Token.token(toString()),
        QueryText.ID, Token.token(id));
    addPlan(plan, e);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(name != null) {
      tb.add(QueryText.DOLLAR).add(name.string());
      if(ret != null) tb.add(' ' + QueryText.AS);
    }
    if(ret != null) tb.add(" " + ret);
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
   * Sets the return type of this variable.
   * @param rt return type
   * @param ii input info
   * @throws QueryException if the return type is incompatible
   */
  public void setRetType(final SeqType rt, final InputInfo ii) throws QueryException {
    refineType(ret, ii);
    ret = rt;
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
    if(ret == null || ret.instance(val)) return val;
    if(param) return ret.promote(val, ctx, ii);
    throw Err.XPTYPE.thrw(ii, val.description(), ret, val.type());
  }
}