package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

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

/**
 * Inline function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class InlineFunc extends Single implements Scope {
  /** Function name. */
  private final QNm name;
  /** Arguments. */
  private final Var[] args;
  /** Return type. */
  private final SeqType ret;
  /** Annotations. */
  private final Ann ann;
  /** Updating flag. */
  private final boolean updating;

  /** Map with requested function properties. */
  private final EnumMap<Use, Boolean> map = new EnumMap<Expr.Use, Boolean>(Use.class);
  /** Static context. */
  private final StaticContext sc;
  /** Compilation flag. */
  private boolean compiled;

  /** Local variables in the scope of this function. */
  private final VarScope scope;
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
    super(ii, e);
    name = nm;
    args = v;
    ret = r;
    ann = a == null ? new Ann() : a;
    updating = ann.contains(Ann.Q_UPDATING);
    scope = scp;
    sc = stc;
  }

  @Override
  public void compile(final QueryContext ctx) throws QueryException {
    compile(ctx, null);
  }

  /**
   * Removes and returns all static bindings from this function's closure.
   * @return static variable bindings
   */
  private Collection<Entry<Var, Expr>> staticBindings() {
    Collection<Entry<Var, Expr>> propagate = null;
    final Iterator<Entry<Var, Expr>> cls = scope.closure().entrySet().iterator();
    while(cls.hasNext()) {
      final Entry<Var, Expr> e = cls.next();
      final Expr c = e.getValue();
      if(c.isValue()) {
        if(propagate == null) propagate = new ArrayList<Entry<Var, Expr>>();
        propagate.add(e);
        cls.remove();
      }
    }
    return propagate == null ? Collections.<Entry<Var, Expr>>emptyList() : propagate;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(compiled) return this;
    compiled = true;

    // compile closure
    for(final Entry<Var, Expr> e : scope.closure().entrySet())
      e.setValue(e.getValue().compile(ctx, scp));

    final StaticContext cs = ctx.sc;
    ctx.sc = sc;

    final int fp = scope.enter(ctx);
    try {
      // constant propagation
      for(final Entry<Var, Expr> e : staticBindings())
        ctx.set(e.getKey(), e.getValue().value(ctx), info);

      expr = expr.compile(ctx, scope);
    } catch(final QueryException qe) {
      expr = FNInfo.error(qe, info);
    } finally {
      scope.cleanUp(this);
      scope.exit(ctx, fp);
      ctx.sc = cs;
    }

    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    type = FuncType.get(ann, args, ret).seqType();
    size = 1;
    // only evaluate if the closure is empty, so we don't lose variables
    return scope.closure().isEmpty() ? preEval(ctx) : this;
  }

  @Override
  public VarUsage count(final Var v) {
    VarUsage all = VarUsage.NEVER;
    for(final Entry<Var, Expr> e : scope.closure().entrySet())
      if((all = all.plus(e.getValue().count(v))) == VarUsage.MORE_THAN_ONCE) break;
    return all;
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    boolean change = false, val = false;

    for(final Entry<Var, Expr> entry : scope.closure().entrySet()) {
      final Expr ex = entry.getValue().inline(ctx, scp, v, e);
      if(ex != null) {
        change = true;
        val |= ex.isValue();
        entry.setValue(ex);
      }
    }

    final StaticContext cs = ctx.sc;
    ctx.sc = sc;

    if(val) {
      final int fp = scope.enter(ctx);
      try {
        for(final Entry<Var, Expr> entry : staticBindings()) {
          final Expr inl = expr.inline(ctx, scope, entry.getKey(), entry.getValue());
          if(inl != null) expr = inl;
        }
      } catch(final QueryException qe) {
        expr = FNInfo.error(qe, info);
      } finally {
        scope.cleanUp(this);
        scope.exit(ctx, fp);
        ctx.sc = cs;
      }
    }

    return change ? optimize(ctx, scp) : null;
  }

  @Override
  public Expr copy(final QueryContext cx, final VarScope scp, final IntObjMap<Var> vs) {
    final VarScope v = scope.copy(cx, scp, vs);
    final Var[] a = args.clone();
    for(int i = 0; i < a.length; i++) a[i] = vs.get(a[i].id);
    return copyType(new InlineFunc(info, name, ret, a, expr.copy(cx, v, vs), ann, sc, v));
  }

  @Override
  public FuncItem item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final FuncType ft = FuncType.get(ann, args, ret);
    final boolean c = ft.ret != null && !expr.type().instanceOf(ft.ret);

    // collect closure
    final Map<Var, Value> clos = new HashMap<Var, Value>();
    for(final Entry<Var, Expr> e : scope.closure().entrySet())
      clos.put(e.getKey(), e.getValue().value(ctx));

    return new FuncItem(args, expr, ft, clos, c, scope, ctx.sc);
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
    if(u == Use.X30) return true;

    // handle recursive calls: set dummy value, eventually replace it with final value
    Boolean b = map.get(u);
    if(b == null) {
      map.put(u, false);
      b = expr == null || super.uses(u);
      map.put(u, b);
    }
    return b;
  }

  @Override
  public boolean removable(final Var v) {
    // [LW] Variables are removable from the closure.
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
  public boolean visit(final ASTVisitor visitor) {
    final Map<Var, Expr> clos = scope.closure();
    for(final Entry<Var, Expr> v : clos.entrySet())
      if(!(v.getValue().accept(visitor) && visitor.declared(v.getKey()))) return false;
    for(final Var v : args) if(!visitor.declared(v)) return false;
    return expr.accept(visitor);
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

  @Override
  public void checkUp() throws QueryException {
    final boolean u = expr.uses(Use.UPD);
    if(u) expr.checkUp();
    if(updating) {
      // updating function
      if(ret != null) UPFUNCTYPE.thrw(info);
      if(!u && !expr.isVacuous()) UPEXPECTF.thrw(info);
    } else if(u) {
      // uses updates, but is not declared as such
      UPNOT.thrw(info, description());
    }
  }

  @Override
  public boolean isVacuous() {
    return !uses(Use.UPD) && ret != null && ret.eq(SeqType.EMP);
  }

  @Override
  public SeqType type() {
    return updating ? SeqType.EMP : super.type();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Entry<Var, Expr> e : scope.closure().entrySet())
      if(!e.getValue().accept(visitor)) return false;
    return visitor.inlineFunc(this);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Entry<Var, Expr> e : scope.closure().entrySet())
      sz += e.getValue().exprSize();
    return sz + expr.exprSize();
  }

  @Override
  public boolean compiled() {
    return compiled;
  }
}
