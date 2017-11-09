package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Checks the argument expression's result type.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class TypeCheck extends Single {
  /** Static context. */
  private final StaticContext sc;
  /** Flag for function conversion. */
  public final boolean promote;

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
    super(info, expr, seqType);
    this.sc = sc;
    this.promote = promote;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final SeqType argType = expr.seqType();

    // return type is already correct
    if(argType.instanceOf(seqType)) {
      cc.info(OPTTYPE_X, seqType);
      return expr;
    }

    // function item coercion
    if(expr instanceof FuncItem && seqType.type instanceof FuncType) {
      if(!seqType.occ.check(1)) throw typeError(expr, seqType, null, info);
      final FuncItem fi = (FuncItem) expr;
      return cc.replaceWith(this, fi.coerceTo((FuncType) seqType.type, cc.qc, info, true));
    }

    // we can type check immediately
    if(expr.isValue()) return cc.preEval(this);

    // check at each call
    if(argType.type.instanceOf(seqType.type) && !expr.has(Flag.NDT, Flag.UPD)) {
      final Occ occ = argType.occ.intersect(seqType.occ);
      if(occ == null) throw typeError(expr, seqType, null, info);
    }

    final Expr opt = expr.typeCheck(this, cc);
    if(opt != null) {
      cc.info(OPTTYPE_X, seqType);
      return opt;
    }

    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = qc.iter(expr);

    return new Iter() {
      /** Item cache. */
      final ItemList cache = new ItemList();
      /** Item cache index. */
      int c;
      /** Result index. */
      int i;

      @Override
      public Item next() throws QueryException {
        final SeqType st = seqType;
        while(c == cache.size()) {
          qc.checkStop();
          cache.size(0);
          c = 0;

          final Item it = iter.next();
          if(it == null || st.instance(it)) {
            cache.add(it);
          } else {
            st.promote(it, null, cache, qc, sc, info, false);
          }
        }

        final Item it = cache.get(c);
        cache.set(c++, null);

        if(it == null && i < st.occ.min || i > st.occ.max)
          throw typeError(expr, st, null, info);

        i++;
        return it;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = qc.value(expr);
    if(seqType.instance(val)) return val;
    if(promote) return seqType.promote(val, null, qc, sc, info, false);
    throw INVCAST_X_X_X.get(info, val.seqType(), seqType, val);
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
   * @param cc compilation context
   * @return the resulting expression
   * @throws QueryException query exception
   */
  public Expr check(final Expr ex, final CompileContext cc) throws QueryException {
    return new TypeCheck(sc, info, ex, seqType, promote).optimize(cc);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof TypeCheck)) return false;
    final TypeCheck t = (TypeCheck) obj;
    return seqType.eq(t.seqType) && promote == t.promote && super.equals(obj);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new TypeCheck(sc, info, expr.copy(cc, vm), seqType, promote);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem elem = planElem(AS, seqType);
    if(promote) elem.add(planAttr(FUNCTION, Token.TRUE));
    addPlan(plan, elem, expr);
  }

  @Override
  public String toString() {
    return "((: " + seqType + ", " + promote + " :) " + expr + ')';
  }
}
