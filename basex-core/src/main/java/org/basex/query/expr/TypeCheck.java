package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Checks the argument expression's result type.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class TypeCheck extends Single {
  /** Only check occurrence indicator. */
  private boolean occ;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression to be promoted
   * @param seqType target type
   */
  public TypeCheck(final InputInfo info, final Expr expr, final SeqType seqType) {
    super(info, expr, seqType);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final SeqType st = seqType();
    final Type type = st.type;
    if(type.instanceOf(AtomType.ANY_ATOMIC_TYPE)) {
      expr = expr.simplifyFor(Simplify.DATA, cc);
    }

    if((ZERO_OR_ONE.is(expr) || EXACTLY_ONE.is(expr) || ONE_OR_MORE.is(expr)) &&
        st.occ.instanceOf(expr.seqType().occ)) {
      expr = cc.replaceWith(expr, expr.arg(0));
    }

    final SeqType et = expr.seqType();
    occ = et.type.instanceOf(type) && et.kindInstanceOf(st);

    // remove redundant type check
    if(expr instanceof TypeCheck && st.instanceOf(et)) {
      final TypeCheck tc = (TypeCheck) expr;
      return cc.replaceWith(this, new TypeCheck(info, tc.expr, st).optimize(cc));
    }

    // skip check if return type is already correct
    if(et.instanceOf(st)) {
      cc.info(OPTTYPE_X_X, st, expr);
      return expr;
    }

    // function item coercion
    if(expr instanceof FuncItem && type instanceof FuncType) {
      if(!st.occ.check(1)) throw error(expr, st);
      return cc.replaceWith(this, ((FuncItem) expr).coerceTo((FuncType) type, cc.qc, cc, info));
    }

    // pre-evaluate (check value and result size)
    final long es = expr.size();
    if(expr instanceof Value && es <= CompileContext.MAX_PREEVAL) {
      return cc.preEval(this);
    }

    // push type check inside expression
    final Expr checked = expr.typeCheck(this, cc);
    if(checked != null) {
      cc.info(OPTTYPE_X_X, st, checked);
      return checked;
    }

    // refine occurrence indicator and result size
    if(!et.mayBeArray()) {
      final Occ o = et.occ.intersect(st.occ);
      if(o == null) throw error(expr, st);
      exprType.assign(st, o, et.occ == st.occ ? es : -1).
        data(type instanceof NodeType ? expr : null);
    }

    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = expr.value(qc);
    final SeqType st = seqType();

    // only check occurrence indicator
    if(occ) {
      if(!st.occ.check(value.size())) throw error(value, st);
      return value;
    }
    // check occurrence indicator and item type
    return st.coerce(value, null, qc, null, info);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    return simplifyForCast(mode, cc);
  }

  /**
   * Creates an expression that checks the given expression's return type.
   * @param ex expression to check
   * @param cc compilation context
   * @return the resulting expression, or {@code null} if no type check is necessary
   * @throws QueryException query exception
   */
  public Expr check(final Expr ex, final CompileContext cc) throws QueryException {
    final SeqType st = seqType();
    return ex.seqType().instanceOf(st) ? null : new TypeCheck(info, ex, st).optimize(cc);
  }

  /**
   * Throws a type error.
   * @param ex expression that triggers the error
   * @param st target type
   * @return query exception
   */
  private QueryException error(final Expr ex, final SeqType st) {
    return typeError(ex, st, null, info);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final TypeCheck ex = copyType(new TypeCheck(info, expr.copy(cc, vm), seqType()));
    ex.occ = occ;
    return ex;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof TypeCheck)) return false;
    final TypeCheck tc = (TypeCheck) obj;
    return seqType().eq(tc.seqType()) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, TO, seqType()), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token("(").token(expr).token(COERCE).token(TO).token(seqType()).token(')');
  }
}
