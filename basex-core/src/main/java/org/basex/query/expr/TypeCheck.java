package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.iter.*;
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
 * @author BaseX Team 2005-15, BSD License
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
   * @param seqType type to promote to
   * @param promote flag for function promotion
   */
  public TypeCheck(final StaticContext sc, final InputInfo info, final Expr expr,
      final SeqType seqType, final boolean promote) {
    super(info, expr);
    this.sc = sc;
    this.seqType = seqType;
    this.promote = promote;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    expr = expr.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    final SeqType argType = expr.seqType();

    // return type is already correct
    if(argType.instanceOf(seqType)) {
      qc.compInfo(QueryText.OPTCAST, seqType);
      return expr;
    }

    // function item coercion
    if(expr instanceof FuncItem && seqType.type instanceof FuncType) {
      if(!seqType.occ.check(1)) throw INVTREAT_X_X.get(info, argType, seqType);
      final FuncItem fi = (FuncItem) expr;
      return optPre(fi.coerceTo((FuncType) seqType.type, qc, info, true), qc);
    }

    // we can type check immediately
    if(expr.isValue()) {
      return optPre(value(qc), qc);
    }

    // check at each call
    if(argType.type.instanceOf(seqType.type) && !expr.has(Flag.NDT) && !expr.has(Flag.UPD)) {
      final SeqType.Occ occ = argType.occ.intersect(seqType.occ);
      if(occ == null) throw INVCAST_X_X.get(info, argType, seqType);
    }

    final Expr opt = expr.typeCheck(this, qc, scp);
    if(opt != null) return optPre(opt, qc);

    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = expr.iter(qc);

    return new Iter() {
      /** Item cache. */
      final ValueBuilder vb = new ValueBuilder();
      /** Item cache index. */
      int c;
      /** Result index. */
      int i;

      @Override
      public Item next() throws QueryException {
        final SeqType st = seqType;
        while(c == vb.size()) {
          qc.checkStop();
          vb.size(0);
          c = 0;

          final Item it = iter.next();
          if(it == null || st.instance(it, true)) {
            vb.add(it);
          } else {
            st.promote(qc, sc, info, it, false, vb);
          }
        }

        final Item it = vb.get(c);
        vb.set(c++, null);

        if(it == null && i < st.occ.min || i > st.occ.max)
          throw INVTREAT_X_X.get(info, expr.seqType(), st);

        i++;
        return it;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = expr.value(qc);
    if(seqType.instance(val)) return val;
    if(promote) return seqType.promote(qc, sc, info, val, false);
    throw INVCAST_X_X_X.get(info, val.seqType(), seqType, val);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new TypeCheck(sc, info, expr.copy(qc, scp, vs), seqType, promote);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem elem = planElem(QueryText.TYP, seqType);
    if(promote) elem.add(planAttr(QueryText.FUNCTION, Token.TRUE));
    addPlan(plan, elem, expr);
  }

  @Override
  public String toString() {
    return "((: " + seqType + ", " + promote + " :) " + expr + ')';
  }

  /**
   * Checks if this type check is redundant if the result is bound to the given variable.
   * @param var variable
   * @return result of check
   */
  public boolean isRedundant(final Var var) {
    return (!promote || var.promotes()) && var.declaredType().instanceOf(seqType);
  }

  /**
   * Creates an expression that checks the given expression's return type.
   * @param ex expression to check
   * @param qc query context
   * @param scp variable scope
   * @return the resulting expression
   * @throws QueryException query exception
   */
  public Expr check(final Expr ex, final QueryContext qc, final VarScope scp)
      throws QueryException {
    return new TypeCheck(sc, info, ex, seqType, promote).optimize(qc, scp);
  }
}
