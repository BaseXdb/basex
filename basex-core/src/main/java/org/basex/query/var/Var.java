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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class Var extends ExprInfo {
  /** Variable name. */
  public final QNm name;
  /** Variable ID. */
  public final int id;
  /** Input info (can be {@code null}). */
  public final InputInfo info;

  /** Declared type, {@code null} if not specified. */
  public SeqType declType;
  /** Flag for function coercion. */
  public boolean coerce;
  /** Stack slot ({@code -1} if unused). */
  public int slot;

  /** Actual type (by type inference). */
  private final ExprType exprType;
  /** Flag for function parameters. */
  private final boolean param;
  /** Input expression, from which the data reference and DDO flag will be requested. */
  private Expr ex;

  /**
   * Constructor for a variable with an already known stack slot.
   * @param name variable name
   * @param declType declared type, {@code null} for no check
   * @param qc query context, used for generating a variable ID
   * @param info input info (can be {@code null})
   * @param param function parameter flag
   * @param slot stack slot ({@code -1} if unused)
   * @param exprType expression type (can be {@code null})
   */
  public Var(final QNm name, final SeqType declType, final QueryContext qc, final InputInfo info,
      final boolean param, final int slot, final ExprType exprType) {
    this.name = name;
    this.param = param;
    this.info = info;
    this.slot = slot;
    this.declType = declType == null || declType.eq(SeqType.ITEM_ZM) ? null : declType;
    this.exprType = exprType != null ? exprType : new ExprType(SeqType.ITEM_ZM);
    coerce = param;
    id = qc.varIDs++;
  }

  /**
   * Constructor.
   * @param name variable name
   * @param declType declared sequence type, {@code null} for no check
   * @param qc query context, used for generating a variable ID
   * @param info input info (can be {@code null})
   */
  public Var(final QNm name, final SeqType declType, final QueryContext qc, final InputInfo info) {
    this(name, declType, qc, info, false);
  }

  /**
   * Constructor.
   * @param name variable name
   * @param declType declared sequence type, {@code null} for no check
   * @param qc query context, used for generating a variable ID
   * @param info input info (can be {@code null})
   * @param param function parameter flag
   */
  public Var(final QNm name, final SeqType declType, final QueryContext qc, final InputInfo info,
      final boolean param) {
    this(name, declType, qc, info, param, -1, null);
  }

  /**
   * Copy constructor.
   * @param var variable to copy
   * @param qc query context
   */
  public Var(final Var var, final QueryContext qc) {
    this(var.name, var.declType, qc, var.info, var.param, -1, new ExprType(var.exprType));
    coerce = var.coerce;
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
   * Returns an equivalent to the given expression that checks this variable's type.
   * @param expr expression
   * @param cc compilation context
   * @return checked expression
   * @throws QueryException query exception
   */
  public Expr checked(final Expr expr, final CompileContext cc) throws QueryException {
    return declType != null ? new TypeCheck(info, expr, declType, coerce).optimize(cc) : expr;
  }

  /**
   * Checks the type of this value and casts/promotes it when necessary.
   * @param value value to be checked
   * @param qc query context
   * @param cc compilation context (can be {@code null})
   * @return checked and possibly cast value
   * @throws QueryException if the check failed
   */
  public Value checkType(final Value value, final QueryContext qc, final CompileContext cc)
      throws QueryException {

    if(declType == null || declType.instance(value)) return value;
    if(coerce) return declType.coerce(value, name, qc, cc, info);
    throw typeError(value, declType, name, info, false);
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
    if(declType == null || vt.type.instanceOf(et.type) ||
        et.type.instanceOf(vt.type) && et.occ.instanceOf(vt.occ)) return;

    if(!coerce || !(et.type instanceof NodeType) && !et.promotable(vt)) {
      throw vt.type.nsSensitive() ? NSSENS_X_X.get(info, et, vt) :
        typeError(expr, vt, name, info, coerce);
    }
  }

  /**
   * Tries to adopt the given type check.
   * @param st type to check
   * @param crc if function coercion should be applied
   * @return {@code true} if the check could be adopted, {@code false} otherwise
   */
  public boolean adoptCheck(final SeqType st, final boolean crc) {
    if(declType == null || st.instanceOf(declType)) {
      declType = st;
    } else if(!declType.instanceOf(st)) {
      return false;
    }
    coerce |= crc;
    return true;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.attachVariable(plan.create(this), this, true));
  }

  /**
   * Returns a unique representation of the variable.
   * @return variable id
   */
  public byte[] id() {
    return Token.concat("$", name.string(), "_", id);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(id());
    if(declType != null) qs.token(AS).token(declType);
  }

  @Override
  public String toErrorString() {
    return Strings.concat("$", name.string());
  }
}
