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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class TypeCheck extends Single {
  /** Check: 1: only check occurrence indicator. */
  private int check;

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
    SeqType st = seqType();
    final Type type = st.type;

    if(type.instanceOf(AtomType.ANY_ATOMIC_TYPE)) {
      // data(EXPR) coerce to xs:int  ->  EXPR coerce to xs:int
      expr = expr.simplifyFor(Simplify.DATA, cc);
    }
    if((ZERO_OR_ONE.is(expr) || EXACTLY_ONE.is(expr) || ONE_OR_MORE.is(expr)) &&
        st.occ.instanceOf(expr.seqType().occ)) {
      // exactly-one(ITEM) coerce to item()  ->  ITEM coerce to item()
      expr = cc.replaceWith(expr, expr.arg(0));
    }

    final SeqType et = expr.seqType(), nst = et.with(st.occ);
    check = nst.instanceOf(st) ? 1 : 0;

    // refine type check (ignore arrays as coerced result may have a different size)
    if(!et.mayBeArray() || !type.instanceOf(AtomType.ANY_ATOMIC_TYPE)) {
      // occurrence indicator:
      //   exactly-one/one-or-more  ->  exactly-one
      final Occ nocc = et.occ.intersect(st.occ);
      // raise static error if no intersection is possible:
      //   () coerce to item()
      if(nocc == null) throw typeError(expr, st, info);
      // refine result type:
      //   INTEGERS coerce to item()  ->  INTEGERS coerce to xs:integer
      if(check == 1) st = nst;
      // adopt result size and data reference from input expression
      exprType.assign(st, nocc, et.occ == st.occ ? expr.size() : -1).
        data(type instanceof NodeType ? expr : null);
    }

    // remove redundant type check
    if(expr instanceof TypeCheck && st.instanceOf(et)) {
      // (EXPR coerce to xs:integer) coerce to xs:int  ->  EXPR coerce to xs:int
      return cc.replaceWith(this, new TypeCheck(info, ((TypeCheck) expr).expr, st).optimize(cc));
    }

    // skip check if return type is correct
    if(et.instanceOf(st)) {
      // (1, 3) coerce to xs:integer*  ->  (1, 3)
      cc.info(OPTTYPE_X_X, st, expr);
      return expr;
    }

    // function item coercion
    if(expr instanceof FuncItem && type instanceof FuncType) {
      if(!st.occ.check(1)) throw typeError(expr, st, info);
      return cc.replaceWith(this, ((FuncItem) expr).coerceTo((FuncType) type, cc.qc, cc, info));
    }

    // pre-evaluate
    if(cc.values(true, expr)) return cc.preEval(this);

    // push type check inside expression
    final Expr checked = expr.typeCheck(this, cc);
    if(checked != null) {
      cc.info(OPTTYPE_X_X, st, checked);
      return checked;
    }

    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = expr.value(qc);
    final SeqType st = seqType();

    // only check occurrence indicator
    if(check == 1) {
      if(!st.occ.check(value.size())) throw typeError(value, st, info);
      return value;
    }
    // ignore type of result returned by tail call function
    return qc.tcFunc != null ? value : st.coerce(value, null, qc, null, info);
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

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final TypeCheck ex = copyType(new TypeCheck(info, expr.copy(cc, vm), seqType()));
    ex.check = check;
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
