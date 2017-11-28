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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class Var extends ExprInfo {
  /** Variable name. */
  public final QNm name;
  /** Variable ID. */
  public final int id;
  /** Input info. */
  public final InputInfo info;

  /** Declared type, {@code null} if not specified. */
  public SeqType declType;
  /** Data reference. */
  public Data data;

  /** Stack slot number. */
  int slot = -1;

  /** Actual type (by type inference). */
  private final ExprType exprType = new ExprType(SeqType.ITEM_ZM);
  /** Static context. */
  private final StaticContext sc;
  /** Flag for function parameters. */
  private final boolean param;

  /** Flag for function conversion. */
  private boolean promote;

  /**
   * Constructor for a variable with an already known stack slot.
   * @param name variable name
   * @param declType declared type, {@code null} for no check
   * @param param function parameter flag
   * @param slot stack slot
   * @param qc query context, used for generating a variable ID
   * @param sc static context
   * @param info input info
   */
  public Var(final QNm name, final SeqType declType, final boolean param, final int slot,
      final QueryContext qc, final StaticContext sc, final InputInfo info) {
    this.name = name;
    this.param = param;
    this.sc = sc;
    this.info = info;
    this.slot = slot;
    this.declType = declType == null || declType.eq(SeqType.ITEM_ZM) ? null : declType;
    promote = param;
    id = qc.varIDs++;
  }

  /**
   * Constructor.
   * @param name variable name
   * @param declType declared sequence type, {@code null} for no check
   * @param param function parameter flag
   * @param qc query context, used for generating a variable ID
   * @param sc static context
   * @param info input info
   */
  public Var(final QNm name, final SeqType declType, final boolean param, final QueryContext qc,
      final StaticContext sc, final InputInfo info) {
    this(name, declType, param, -1, qc, sc, info);
  }

  /**
   * Copy constructor.
   * @param var variable to copy
   * @param qc query context
   * @param sc static context
   */
  public Var(final Var var, final QueryContext qc, final StaticContext sc) {
    this(var.name, var.declType, var.param, qc, sc, var.info);
    promote = var.promote;
    exprType.assign(var.exprType);
  }

  /**
   * Sequence type of values bound to this variable.
   * @return sequence type
   */
  public SeqType seqType() {
    final SeqType st = exprType.seqType(), dt = declType;
    final SeqType it = dt != null ? dt.intersect(st) : null;
    return it != null ? it : dt != null ? dt : st;
  }

  /**
   * Returns the result size.
   * @return result size
   */
  public long size() {
    return exprType.size();
  }

  /**
   * Declared type of this variable.
   * @return declared type
   */
  public SeqType declaredType() {
    return declType == null ? SeqType.ITEM_ZM : declType;
  }

  /**
   * Tries to refine the type of this variable through the type of the bound expression.
   * @param st sequence type of the bound expression
   * @param cc compilation context (can be {@code null})
   * @throws QueryException query exception
   */
  public void refineType(final SeqType st, final CompileContext cc) throws QueryException {
    refineType(st, st.zero() ? 0 : st.one() ? 1 : -1, cc);
  }

  /**
   * Tries to refine the type of this variable through the type of the bound expression.
   * @param st sequence type of the bound expression
   * @param sz size
   * @param cc compilation context (can be {@code null})
   * @throws QueryException query exception
   */
  public void refineType(final SeqType st, final long sz, final CompileContext cc)
      throws QueryException {

    if(declType != null) {
      if(declType.occ.intersect(st.occ) == null)
        throw typeError(st, declType, name, info);
      if(st.instanceOf(declType)) {
        if(cc != null) cc.info(QueryText.OPTTYPE_X, this);
        declType = null;
      } else if(!st.promotable(declType)) {
        return;
      }
    }

    final SeqType dt = exprType.seqType();
    if(!dt.eq(st) && !dt.instanceOf(st)) {
      // the new type provides new information
      final SeqType it = dt.intersect(st);
      if(it != null) exprType.assign(it, sz);
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
   * @param cc compilation context
   * @return checked expression
   * @throws QueryException query exception
   */
  public Expr checked(final Expr ex, final CompileContext cc) throws QueryException {
    return checksType() ? new TypeCheck(sc, info, ex, declType, promote).optimize(cc) : ex;
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

    if(!checksType() || declType.instance(val)) return val;
    if(promote) return declType.promote(val, name, qc, sc, info, opt);
    throw typeError(val, declType, name, info);
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
      throw typeError(expr, vt, name, info);
    }
  }

  /**
   * Checks whether the given variable is identical to this one, i.e. has the same id.
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
    final FElem e = planElem(QueryText.NAME, '$' + Token.string(name.string()),
        Token.ID, Token.token(id), QueryText.TYPE, seqType());
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
    tb.add(QueryText.DOLLAR).add(name.string()).add('_').addInt(id);
    if(declType != null) tb.add(' ').add(QueryText.AS).add(' ').addExt(declType);
    return tb.toString();
  }
}
