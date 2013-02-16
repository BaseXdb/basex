package org.basex.query.func;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Inline function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class InlineFunc extends UserFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param r return type
   * @param v arguments
   * @param e function body
   * @param a annotations
   * @param stc static context
   * @param scp scope
   */
  public InlineFunc(final InputInfo ii, final SeqType r, final Var[] v,
      final Expr e, final Ann a, final StaticContext stc, final VarScope scp) {
    this(ii, null, r, v, e, a, stc, scp);
  }

  /**
   * Package-private constructor allowing a name.
   * @param ii input info
   * @param nm name of the function
   * @param r return type
   * @param v argument variables
   * @param e function expression
   * @param a annotations
   * @param stc static context
   * @param scp variable scope
   */
  InlineFunc(final InputInfo ii, final QNm nm, final SeqType r, final Var[] v,
      final Expr e, final Ann a, final StaticContext stc, final VarScope scp) {
    super(ii, nm, v, r, a, e, stc, scp);
    expr = e;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    comp(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    type = FuncType.get(this).seqType();
    size = 1;
    // only evaluate if the closure is empty, so we don't lose variables
    return scope.closure().isEmpty() ? preEval(ctx) : this;
  }

  @Override
  public FuncItem item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final FuncType ft = FuncType.get(this);
    final boolean c = ft.ret != null && !expr.type().instanceOf(ft.ret);

    // collect closure
    final Map<Var, Value> clos = new HashMap<Var, Value>();
    for(final Entry<Var, Expr> e : scope.closure().entrySet())
      clos.put(e.getKey(), e.getValue().value(ctx));

    return new FuncItem(args, expr, ft, clos, c, scope);
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return item(ctx, info);
  }

  @Override
  public ValueIter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || super.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    return false;
  }

  @Override
  public boolean databases(final StringList db) {
    return false;
  }

  @Override
  protected boolean tco() {
    return false;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
    addPlan(plan, el, expr);
    for(int i = 0; i < args.length; ++i) {
      el.add(planAttr(ARG + i, args[i].name.string()));
    }
  }

  @Override
  public Expr copy(final QueryContext cx, final VarScope scp, final IntMap<Var> vs) {
    final VarScope v = scope.copy(cx, scp, vs);
    final Var[] a = args.clone();
    for(int i = 0; i < a.length; i++) a[i] = vs.get(a[i].id);
    return copyType(new InlineFunc(info, name, ret, a, expr.copy(cx, v, vs), ann, sc, v));
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    final Map<Var, Expr> clos = scope.closure();
    for(final Entry<Var, Expr> v : clos.entrySet())
      if(!(v.getValue().accept(visitor) && visitor.declared(v.getKey()))) return false;
    for(final Var v : args) if(!visitor.declared(v)) return false;
    return visitAll(visitor, expr);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FUNCTION).append(PAR1);
    for(int i = 0; i < args.length; i++) {
      if(i > 0) sb.append(", ");
      sb.append(args[i].toString());
    }
    sb.append(PAR2).append(' ');
    if(ret != null) sb.append("as ").append(ret.toString()).append(' ');
    return sb.append("{ ").append(expr).append(" }").toString();
  }
}
