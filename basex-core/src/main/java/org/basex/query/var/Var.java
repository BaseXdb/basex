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
 * @author BaseX Team 2005-14, BSD License
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
  /** Declared sequence type, {@code null} if not specified. */
  public SeqType declType;

  /** Stack slot number. */
  int slot = -1;
  /** Expected result size. */
  public long size = -1;
  /** Data reference. */
  public Data data;

  /** Flag for function parameters. */
  private final boolean param;
  /** Actual return type (by type inference). */
  private SeqType seqType;
  /** Flag for function conversion. */
  private boolean promote;

  /**
   * Constructor.
   * @param qc query context, used for generating a variable ID
   * @param sc static context
   * @param name variable name, {@code null} for unnamed variable
   * @param declType declared sequence type, {@code null} for no check
   * @param param function parameter flag
   */
  Var(final QueryContext qc, final StaticContext sc, final QNm name, final SeqType declType,
      final boolean param) {
    this.sc = sc;
    this.name = name;
    this.param = param;
    this.promote = param;
    this.declType = declType == null || declType.eq(SeqType.ITEM_ZM) ? null : declType;
    seqType = SeqType.ITEM_ZM;
    id = qc.varIDs++;
    size = seqType.occ();
  }

  /**
   * Copy constructor.
   * @param qc query context
   * @param sc static context
   * @param var variable to copy
   */
  Var(final QueryContext qc, final StaticContext sc, final Var var) {
    this(qc, sc, var.name, var.declType, var.param);
    promote = var.promote;
    seqType = var.seqType;
    size = var.size;
  }

  /**
   * Sequence type of values bound to this variable.
   * @return sequence type (not {@code null})
   */
  public SeqType seqType() {
    final SeqType intersect = declType != null ? declType.intersect(seqType) : null;
    return intersect != null ? intersect : declType != null ? declType : seqType;
  }

  /**
   * Declared type of this variable.
   * @return declared type (not {@code null})
   */
  public SeqType declaredType() {
    return declType == null ? SeqType.ITEM_ZM : declType;
  }

  /**
   * Tries to refine the compile-time type of this variable through the type of the bound
   * expression.
   * @param st sequence type of the bound expression
   * @param qc query context
   * @param ii input info
   * @throws QueryException query exception
   */
  public void refineType(final SeqType st, final QueryContext qc, final InputInfo ii)
      throws QueryException {

    if(st == null) return;

    if(declType != null) {
      if(declType.occ.intersect(st.occ) == null) throw INVCAST.get(ii, st, declType);
      if(st.instanceOf(declType)) {
        qc.compInfo(QueryText.OPTCAST, this);
        declType = null;
      } else if(!st.promotable(declType)) {
        return;
      }
    }

    if(!seqType.eq(st) && !seqType.instanceOf(st)) {
      // the new type provides new information
      final SeqType is = seqType.intersect(st);
      if(is != null) seqType = is;
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
   * @param ex expression
   * @param scp variable scope
   * @param qc query context
   * @param ii input info
   * @return checked expression
   * @throws QueryException query exception
   */
  public Expr checked(final Expr ex, final QueryContext qc, final VarScope scp, final InputInfo ii)
      throws QueryException {
    return checksType() ? new TypeCheck(sc, ii, ex, declType, promote).optimize(qc, scp) : ex;
  }

  /**
   * Checks the type of this value and casts/promotes it when necessary.
   * @param val value to be checked
   * @param qc query context
   * @param ii input info
   * @param opt if the result should be optimized
   * @return checked and possibly cast value
   * @throws QueryException if the check failed
   */
  public Value checkType(final Value val, final QueryContext qc, final InputInfo ii,
      final boolean opt) throws QueryException {

    if(!checksType() || declType.instance(val)) return val;
    if(promote) return declType.promote(qc, sc, ii, val, opt);
    throw INVCAST.get(ii, val.seqType(), declType);
  }

  /**
   * Checks if the type of the specified expression could be converted to the sequence type
   * of this variable.
   *
   * Due to insufficient typing, the check will only be performed if:
   * - the variable type is an instance of the specified type.
   *   This way, expressions with super types like item() will not be rejected
   * - the expression is to be promoted, and it is not of type node (eg: function-declaration-016)
   *
   * @param expr expression
   * @param info input info
   * @throws QueryException query exception
   */
  public void checkType(final Expr expr, final InputInfo info) throws QueryException {
    final SeqType et = expr.seqType(), vt = seqType();
    if(!checksType() || vt.type.instanceOf(et.type) ||
        et.type.instanceOf(vt.type) && et.occ.instanceOf(vt.occ)) return;

    if(!promote || !et.type.isNode() && !et.promotable(vt)) {
      if(vt.type.nsSensitive() && sc.xquery3()) throw NSSENS.get(info, et, vt);
      throw INVCAST.get(info, et, vt);
    }
  }

  /**
   * Checks whether the given variable is identical to this one, i.e. has the same ID.
   * @param var variable to check
   * @return {@code true}, if the IDs are equal, {@code false} otherwise
   */
  public boolean is(final Var var) {
    return id == var.id;
  }

  /**
   * Checks if this variable performs function conversion on its bound values.
   * @return result of check
   */
  public boolean promotes() {
    return promote;
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
   * @param st type to check
   * @param prom if function conversion should be applied
   * @return {@code true} if the check could be adopted, {@code false} otherwise
   */
  public boolean adoptCheck(final SeqType st, final boolean prom) {
    if(declType == null || st.instanceOf(declType)) {
      declType = st;
    } else if(!declType.instanceOf(st)) {
      return false;
    }
    promote |= prom;
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem(QueryText.NAM, '$' + Token.string(name.string()),
        QueryText.ID, Token.token(id));
    if(declType != null) e.add(planAttr(QueryText.AS, declType.toString()));
    addPlan(plan, e);
  }

  @Override
  public String toErrorString() {
    return new TokenBuilder().add(QueryText.DOLLAR).add(name.string()).toString();
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
}
