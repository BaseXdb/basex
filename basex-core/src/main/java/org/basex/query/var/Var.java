package org.basex.query.var;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Variable expression.
 *
 * @author BaseX Team 2005-21, BSD License
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
  /** Flag for function conversion. */
  public boolean promote;
  /** Stack slot number. */
  int slot;

  /** Actual type (by type inference). */
  private final ExprType exprType = new ExprType(SeqType.ITEM_ZM);
  /** Static context. */
  private final StaticContext sc;
  /** Flag for function parameters. */
  private final boolean param;
  /** Input expression, from which the data reference and DDO flag will be requested. */
  private Expr ex;

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
   * Attaches an input expression.
   * @param expr input expression
   */
  public void expr(final Expr expr) {
    ex = expr;
  }

  /**
   * Returns the result size.
   * @return result size
   */
  public long size() {
    return exprType.size();
  }

  /**
   * Returns the distinct document order flag. See {@link Expr#ddo} for details.
   * @return result of check
   */
  public boolean ddo() {
    if(ex != null) return ex.ddo();
    final SeqType st = seqType();
    return st.zeroOrOne() && st.type instanceof NodeType;
  }

  /**
   * Returns the data reference bound to this variable. See {@link Expr#data} for details.
   * @return data reference (can be {@code null})
   */
  public Data data() {
    return ex != null ? ex.data() : null;
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
   * @param size size (can be {@code -1})
   * @param cc compilation context (can be {@code null})
   * @throws QueryException query exception
   */
  public void refineType(final SeqType st, final long size, final CompileContext cc)
      throws QueryException {

    if(declType != null) {
      if(declType.occ.intersect(st.occ) == null)
        throw INVTREAT_X_X_X.get(info, st, declType, Token.concat('$', name.string()));
      if(st.instanceOf(declType)) {
        if(cc != null) cc.info(OPTTYPE_X, this);
        declType = null;
      } else if(!st.promotable(declType)) {
        return;
      }
    }

    final SeqType dt = exprType.seqType();
    if(!dt.instanceOf(st)) {
      // the new type provides new information
      final SeqType it = dt.intersect(st);
      if(it != null) exprType.assign(it, size);
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
   * @param expr expression
   * @param cc compilation context
   * @return checked expression
   * @throws QueryException query exception
   */
  public Expr checked(final Expr expr, final CompileContext cc) throws QueryException {
    return checksType() ? new TypeCheck(sc, info, expr, declType, promote).optimize(cc) : expr;
  }

  /**
   * Checks the type of this value and casts/promotes it when necessary.
   * @param value value to be checked
   * @param qc query context
   * @param opt if the result should be optimized
   * @return checked and possibly cast value
   * @throws QueryException if the check failed
   */
  public Value checkType(final Value value, final QueryContext qc, final boolean opt)
      throws QueryException {

    if(!checksType() || declType.instance(value)) return value;
    if(promote) return declType.promote(value, name, qc, sc, info, opt);
    throw typeError(value, declType, name, info, promote);
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
      throw vt.type.nsSensitive() ? NSSENS_X_X.get(info, et, vt) :
        typeError(expr, vt, name, info, promote);
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
  public boolean equals(final Object obj) {
    return obj instanceof Var && is((Var) obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.attachVariable(plan.create(this), this, true));
  }

  /**
   * Returns a unique representation of the variable.
   * @return variable id
   */
  public byte[] id() {
    return Token.concat(DOLLAR, name.string(), "_", id);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(id());
    if(declType != null) qs.token(AS).token(declType);
  }

  @Override
  public String toErrorString() {
    return Strings.concat(DOLLAR, name.string());
  }
}
