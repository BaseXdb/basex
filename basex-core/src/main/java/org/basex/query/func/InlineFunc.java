package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.*;
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
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class InlineFunc extends Single implements Scope, XQFunctionExpr {
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
  private final EnumMap<Flag, Boolean> map = new EnumMap<Flag, Boolean>(Flag.class);
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
  public int arity() {
    return args.length;
  }

  @Override
  public QNm funcName() {
    // inline functions have no name
    return null;
  }

  @Override
  public QNm argName(final int pos) {
    return args[pos].name;
  }

  @Override
  public FuncType funcType() {
    return FuncType.get(ann, args, ret);
  }

  @Override
  public Ann annotations() {
    return ann;
  }

  @Override
  public void compile(final QueryContext ctx) throws QueryException {
    compile(ctx, null);
  }

  /**
   * Removes and returns all static bindings from this function's closure.
   * @return static variable bindings
   */
  private Collection<Entry<Var, Value>> staticBindings() {
    Collection<Entry<Var, Value>> propagate = null;
    final Iterator<Entry<Var, Expr>> cls = scope.closure().entrySet().iterator();
    while(cls.hasNext()) {
      final Entry<Var, Expr> e = cls.next();
      final Expr c = e.getValue();
      if(c instanceof Value) {
        @SuppressWarnings({ "unchecked", "rawtypes"})
        final Entry<Var, Value> e2 = (Entry) e;
        if(propagate == null) propagate = new ArrayList<Entry<Var, Value>>();
        propagate.add(e2);
        cls.remove();
      }
    }
    return propagate == null ? Collections.<Entry<Var, Value>>emptyList() : propagate;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(compiled) return this;
    compiled = true;

    // compile closure
    for(final Entry<Var, Expr> e : scope.closure().entrySet()) {
      final Expr bound = e.getValue().compile(ctx, scp);
      e.setValue(bound);
      e.getKey().refineType(bound.type(), ctx, info);
    }

    final int fp = scope.enter(ctx);
    try {
      // constant propagation
      for(final Entry<Var, Value> e : staticBindings())
        ctx.set(e.getKey(), e.getValue(), info);

      expr = expr.compile(ctx, scope);
    } catch(final QueryException qe) {
      expr = FNInfo.error(qe, ret != null ? ret : expr.type());
    } finally {
      scope.cleanUp(this);
      scope.exit(ctx, fp);
    }

    // convert all function calls in tail position to proper tail calls
    expr.markTailCalls(ctx);

    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    final SeqType r = expr.type();
    final SeqType retType = ret == null || r.instanceOf(ret) ? r : ret;
    type = FuncType.get(ann, args, retType).seqType();
    size = 1;

    final int fp = scope.enter(ctx);
    try {
      // inline all values in the closure
      for(final Entry<Var, Value> e : staticBindings()) {
        final Expr inlined = expr.inline(ctx, scope, e.getKey(), e.getValue());
        if (inlined != null) expr = inlined;
      }
    } catch(final QueryException qe) {
      expr = FNInfo.error(qe, ret != null ? ret : expr.type());
    } finally {
      scope.cleanUp(this);
      scope.exit(ctx, fp);
    }

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
    boolean change = false;

    for(final Entry<Var, Expr> entry : scope.closure().entrySet()) {
      final Expr ex = entry.getValue().inline(ctx, scp, v, e);
      if(ex != null) {
        change = true;
        entry.setValue(ex);
      }
    }

    return change ? optimize(ctx, scp) : null;
  }

  @Override
  public Expr copy(final QueryContext cx, final VarScope scp, final IntObjMap<Var> vs) {
    final VarScope v = scope.copy(cx, scp, vs);
    final Var[] a = args.clone();
    for(int i = 0; i < a.length; i++) a[i] = vs.get(a[i].id);
    final Expr e = expr.copy(cx, v, vs);
    e.markTailCalls(null);
    return copyType(new InlineFunc(info, name, ret, a, e, ann, sc, v));
  }

  @Override
  public Expr inlineExpr(final Expr[] exprs, final QueryContext ctx, final VarScope scp,
      final InputInfo ii) throws QueryException {
    if(expr.has(Flag.CTX)) return null;
    // create let bindings for all variables
    final Map<Var, Expr> closure = scope.closure();
    final LinkedList<GFLWOR.Clause> cls =
        exprs.length == 0 && closure.isEmpty() ? null : new LinkedList<GFLWOR.Clause>();
    final IntObjMap<Var> vs = new IntObjMap<Var>();
    for(int i = 0; i < args.length; i++) {
      final Var old = args[i], v = scp.newCopyOf(ctx, old);
      vs.put(old.id, v);
      cls.add(new Let(v, exprs[i], false, ii).optimize(ctx, scp));
    }

    for(final Entry<Var, Expr> e : closure.entrySet()) {
      final Var old = e.getKey(), v = scp.newCopyOf(ctx, old);
      vs.put(old.id, v);
      cls.add(new Let(v, e.getValue(), false, ii).optimize(ctx, scp));
    }

    // copy the function body
    final Expr cpy = expr.copy(ctx, scp, vs), rt = ret == null ? cpy :
      new TypeCheck(sc, ii, cpy, ret, true).optimize(ctx, scp);

    return cls == null ? rt : new GFLWOR(ii, cls, rt).optimize(ctx, scp);
  }

  @Override
  public FuncItem item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final FuncType ft = (FuncType) type().type;
    final boolean c = ret != null && !expr.type().instanceOf(ret);

    // collect closure
    final Map<Var, Value> clos = new HashMap<Var, Value>();
    for(final Entry<Var, Expr> e : scope.closure().entrySet())
      clos.put(e.getKey(), e.getValue().value(ctx));

    return new FuncItem(args, expr, ft, clos, c, scope, sc, ann);
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
  public boolean has(final Flag flag) {
    // handle recursive calls: set dummy value, eventually replace it with final value
    Boolean b = map.get(flag);
    if(b == null) {
      map.put(flag, false);
      b = expr == null || super.has(flag);
      map.put(flag, b);
    }
    return b;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Entry<Var, Expr> e : scope.closure().entrySet())
      if(!e.getValue().removable(v)) return false;
    return true;
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
    final StringBuilder sb = new StringBuilder();
    if (!scope.closure().isEmpty()) {
      sb.append("((: inline-closure :) ");
      for (final Entry<Var, Expr> e : scope.closure().entrySet())
        sb.append("let ").append(e.getKey()).append(" := ").append(e.getValue()).append(' ');
      sb.append(RETURN).append(' ');
    }
    sb.append(FUNCTION).append(PAR1);
    for(int i = 0; i < args.length; i++) {
      if(i > 0) sb.append(", ");
      sb.append(args[i].toString());
    }
    sb.append(PAR2).append(' ');
    if(ret != null) sb.append("as ").append(ret.toString()).append(' ');
    sb.append("{ ").append(expr).append(" }");
    if(!scope.closure().isEmpty()) sb.append(')');
    return sb.toString();
  }

  @Override
  public void checkUp() throws QueryException {
    final boolean u = expr.has(Flag.UPD);
    if(u) expr.checkUp();
    if(updating) {
      // updating function
      if(ret != null) throw UPFUNCTYPE.get(info);
      if(!u && !expr.isVacuous()) throw UPEXPECTF.get(info);
    } else if(u) {
      // uses updates, but is not declared as such
      throw UPNOT.get(info, description());
    }
  }

  @Override
  public boolean isVacuous() {
    return !has(Flag.UPD) && ret != null && ret.eq(SeqType.EMP);
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
