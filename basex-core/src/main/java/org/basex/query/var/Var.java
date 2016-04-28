package org.basex.query.var;

import static org.basex.query.QueryError.*;

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
 * @author BaseX Team 2005-16, BSD License
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
  public SeqType type;
  /** Input info. */
  public final InputInfo info;

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
   * @param name variable name, {@code null} for unnamed variable
   * @param declType declared sequence type, {@code null} for no check
   * @param param function parameter flag
   * @param qc query context, used for generating a variable ID
   * @param sc static context
   * @param info input info
   */
  public Var(final QNm name, final SeqType declType, final boolean param, final QueryContext qc,
      final StaticContext sc, final InputInfo info) {
    this.name = name;
    this.param = param;
    this.sc = sc;
    this.info = info;
    promote = param;
    type = declType == null || declType.eq(SeqType.ITEM_ZM) ? null : declType;
    seqType = SeqType.ITEM_ZM;
    id = qc.varIDs++;
    size = seqType.occ();
  }

  /**
   * Copy constructor.
   * @param var variable to copy
   * @param qc query context
   * @param sc static context
   */
  Var(final Var var, final QueryContext qc, final StaticContext sc) {
    this(var.name, var.type, var.param, qc, sc, var.info);
    promote = var.promote;
    seqType = var.seqType;
    size = var.size;
  }

  /**
   * Sequence type of values bound to this variable.
   * @return sequence type (not {@code null})
   */
  public SeqType seqType() {
    final SeqType intersect = type != null ? type.intersect(seqType) : null;
    return intersect != null ? intersect : type != null ? type : seqType;
  }

  /**
   * Declared type of this variable.
   * @return declared type (not {@code null})
   */
  public SeqType declaredType() {
    return type == null ? SeqType.ITEM_ZM : type;
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

    if(type != null) {
      if(type.occ.intersect(st.occ) == null) throw INVPROMOTE_X_X_X.get(ii, this, st, type);
      if(st.instanceOf(type)) {
        qc.compInfo(QueryText.OPTTYPE_X, this);
        type = null;
      } else if(!st.promotable(type)) {
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
    return type != null;
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
    return checksType() ? new TypeCheck(sc, ii, ex, type, promote).optimize(qc, scp) : ex;
  }

  /**
   * Checks the type of this value and casts/promotes it when necessary.
   * @param val value to be checked
   * @param qc query context
   * @param opt if the result should be optimized
   * @return checked and possibly cast value
   * @throws QueryException if the check failed
   */
  public Value checkType(final Value val, final QueryContext qc, final boolean opt)
      throws QueryException {

    if(!checksType() || type.instance(val)) return val;
    if(promote) return type.promote(val, name, opt, qc, sc, info);
    throw QueryError.typeError(val, type, name, info);
  }

  /**
   * Checks if the type of the specified expression could be converted to the sequence type
   * of this variable.
   *
   * Due to insufficient typing, the check will only be performed if:
   * <ul>
   *   <li> The variable type is an instance of the specified type.
   *        This way, expressions with super types like item() will not be rejected.</li>
   *   <li> The expression is to be promoted, and it is not of type node
   *        (eg: function-declaration-016)</li>
   * </ul>
   *
   * @param expr expression
   * @throws QueryException query exception
   */
  public void checkType(final Expr expr) throws QueryException {
    final SeqType et = expr.seqType(), vt = seqType();
    if(!checksType() || vt.type.instanceOf(et.type) ||
        et.type.instanceOf(vt.type) && et.occ.instanceOf(vt.occ)) return;

    if(!promote || !(et.type instanceof NodeType) && !et.promotable(vt)) {
      if(vt.type.nsSensitive()) throw NSSENS_X_X.get(info, et, vt);
      throw QueryError.typeError(expr, vt, name, info);
    }
  }

  /**
   * Checks whether the given variable is identical to this one, i.e. has the same ID.
   * @param var variable to check
   * @return {@code true} if the IDs are equal, {@code false} otherwise
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
    if(type == null || st.instanceOf(type)) {
      type = st;
    } else if(!type.instanceOf(st)) {
      return false;
    }
    promote |= prom;
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem(QueryText.NAM, '$' + Token.string(name.string()),
        Token.ID, Token.token(id));
    if(type != null) e.add(planAttr(QueryText.AS, type.toString()));
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
      if(type != null) tb.add(' ' + QueryText.AS);
    }
    if(type != null) tb.add(" " + type);
    return tb.toString();
  }
}
