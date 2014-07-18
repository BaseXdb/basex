package org.basex.query.expr;

import static org.basex.query.util.Err.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Checks the argument expression's result type.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class TypeCheck extends Single {
  /** Flag for function conversion. */
  public final boolean promote;
  /** Static context. */
  private final StaticContext sc;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param expr expression to be promoted
   * @param type type to promote to
   * @param promote flag for function promotion
   */
  public TypeCheck(final StaticContext sc, final InputInfo info, final Expr expr,
      final SeqType type, final boolean promote) {
    super(info, expr);
    this.sc = sc;
    this.type = type;
    this.promote = promote;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    expr = expr.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    final SeqType argType = expr.type();
    if(argType.instanceOf(this.type)) {
      qc.compInfo(QueryText.OPTCAST, this.type);
      return expr;
    }

    if(expr.isValue()) {
      if(expr instanceof FuncItem && this.type.type instanceof FuncType) {
        if(!this.type.occ.check(1)) throw Err.treatError(info, expr, this.type);
        final FuncItem fit = (FuncItem) expr;
        return optPre(fit.coerceTo((FuncType) this.type.type, qc, info, true), qc);
      }
      return optPre(value(qc), qc);
    }

    if(argType.type.instanceOf(this.type.type) && !expr.has(Flag.NDT) && !expr.has(Flag.UPD)) {
      final SeqType.Occ occ = argType.occ.intersect(this.type.occ);
      if(occ == null) throw INVCAST.get(info, argType, this.type);
    }

    final Expr opt = expr.typeCheck(this, qc, scp);
    if(opt != null) return optPre(opt, qc);

    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = expr.value(qc);
    if(this.type.instance(val)) return val;
    if(promote) return this.type.promote(qc, sc, info, val, false);
    throw INVCAST.get(info, val.type(), this.type);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new TypeCheck(sc, info, expr.copy(qc, scp, vs), this.type, promote);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem elem = planElem(QueryText.TYP, this.type);
    if(promote) elem.add(planAttr(QueryText.FUNCTION, Token.TRUE));
    addPlan(plan, elem, expr);
  }

  @Override
  public String toString() {
    return "((: " + this.type + ", " + promote + " :) " + expr + ')';
  }

  /**
   * Checks if this type check is redundant if the result is bound to the given variable.
   * @param var variable
   * @return result of check
   */
  public boolean isRedundant(final Var var) {
    return (!promote || var.promotes()) && var.declaredType().instanceOf(this.type);
  }

  /**
   * Creates an expression that checks the given expression's return type.
   * @param e expression to check
   * @param qc query context
   * @param scp variable scope
   * @return the resulting expression
   * @throws QueryException query exception
   */
  public Expr check(final Expr e, final QueryContext qc, final VarScope scp) throws QueryException {
    return new TypeCheck(sc, info, e, this.type, promote).optimize(qc, scp);
  }
}
