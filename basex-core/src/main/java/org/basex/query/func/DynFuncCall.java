package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Dynamic function call.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class DynFuncCall extends FuncCall {
  /** Static context. */
  private final StaticContext sc;
  /** Hash values of all function items that this call was copied from, possibly {@code null}. */
  private int[] inlinedFrom;
  /** Updating flag. */
  private final boolean updating;

  /**
   * Function constructor.
   * @param info input info
   * @param sc static context
   * @param updating updating flag
   * @param expr function expression
   * @param arg arguments
   */
  public DynFuncCall(final InputInfo info, final StaticContext sc, final boolean updating,
      final Expr expr, final Expr... arg) {

    super(info, Array.add(arg, expr));
    this.sc = sc;
    this.updating = updating;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    final int ar = exprs.length - 1;
    final Expr f = exprs[ar];
    final Type t = f.seqType().type;
    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      if(ft.argTypes != null && ft.argTypes.length != ar) throw INVARITY.get(info, f, ar);
      if(ft.retType != null) seqType = ft.retType;
    }

    if(f instanceof XQFunctionExpr) {
      // maps can only contain fully evaluated Values, so this is safe
      if(allAreValues() && f instanceof Map) return optPre(value(qc), qc);

      // try to inline the function
      if(!(f instanceof FuncItem && comesFrom((FuncItem) f)) && !updating) {
        final Expr[] args = Arrays.copyOf(exprs, exprs.length - 1);
        final Expr inl = ((XQFunctionExpr) f).inlineExpr(args, qc, scp, info);
        if(inl != null) return inl;
      }
    }
    return this;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(Arrays.copyOf(exprs, exprs.length - 1));
  }

  /**
   * Marks this call after it was inlined from the given function item.
   * @param it the function item
   */
  public void markInlined(final FuncItem it) {
    final int hash = it.hashCode();
    inlinedFrom = inlinedFrom == null ? new int[] { hash } : Array.add(inlinedFrom, hash);
  }

  /**
   * Checks if this call was inlined from the body of the given function item.
   * @param it function item
   * @return result of check
   */
  private boolean comesFrom(final FuncItem it) {
    if(inlinedFrom != null) {
      final int hash = it.hashCode();
      for(final int h : inlinedFrom) if(hash == h) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr[] copy = copyAll(qc, scp, vs, exprs);
    final int last = copy.length - 1;
    final Expr[] args = Arrays.copyOf(copy, last);
    final DynFuncCall call = new DynFuncCall(info, sc, updating, copy[last], args);
    if(inlinedFrom != null) call.inlinedFrom = inlinedFrom.clone();
    return copyType(call);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.dynFuncCall(this) && visitAll(visitor, exprs);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem(TCL, tailCall);
    final int es = exprs.length;
    addPlan(plan, el, exprs[es - 1]);
    for(int e = 0; e < es - 1; e++) exprs[e].plan(el);
  }

  @Override
  public String description() {
    return exprs[exprs.length - 1].description() + "(...)";
  }

  @Override
  public String toString() {
    final int es = exprs.length;
    final TokenBuilder tb = new TokenBuilder(exprs[es - 1].toString()).add('(');
    for(int e = 0; e < es - 1; e++) {
      tb.add(exprs[e].toString());
      if(e < es - 2) tb.add(", ");
    }
    return tb.add(')').toString();
  }

  @Override
  FItem evalFunc(final QueryContext qc) throws QueryException {
    final int ar = exprs.length - 1;
    final Item it = checkItem(exprs[ar], qc);
    if(!(it instanceof FItem)) throw INVFUNCITEM.get(info, it.type);
    final FItem fit = (FItem) it;
    if(fit.arity() != ar) throw INVARITY.get(info, fit, ar);
    if(!sc.mixUpdates && updating != fit.annotations().contains(Ann.Q_UPDATING))
      throw (updating ? UPFUNCNOTUP : UPFUNCUP).get(info);

    return fit;
  }

  @Override
  Value[] evalArgs(final QueryContext qc) throws QueryException {
    final int al = exprs.length - 1;
    final Value[] args = new Value[al];
    for(int a = 0; a < al; ++a) args[a] = qc.value(exprs[a]);
    return args;
  }

  @Override
  public boolean has(final Flag flag) {
    // MIXUPDATES: all function calls may be updating
    return flag == Flag.UPD ? sc.mixUpdates || updating : super.has(flag);
  }
}
