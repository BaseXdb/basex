package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * User-defined function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class UserFunc extends Single implements Scope {
  /** Function name. */
  public final QNm name;
  /** Arguments. */
  public final Var[] args;
  /** Return type. */
  public final SeqType ret;
  /** Annotations. */
  public final Ann ann;
  /** Updating flag. */
  public final boolean updating;

  /** Map with requested function properties. */
  protected final EnumMap<Use, Boolean> map = new EnumMap<Expr.Use, Boolean>(Use.class);
  /** Static context. */
  protected final StaticContext sc;
  /** Cast flag. */
  boolean cast;
  /** Compilation flag. */
  private boolean compiled;

  /** Local variables in the scope of this function. */
  protected final VarScope scope;

  /**
   * Function constructor.
   * @param ii input info
   * @param n function name
   * @param v arguments
   * @param r return type
   * @param a annotations
   * @param e function body expression
   * @param stc static context
   * @param scp variable scope
   */
  public UserFunc(final InputInfo ii, final QNm n, final Var[] v, final SeqType r,
      final Ann a, final Expr e, final StaticContext stc,
      final VarScope scp) {

    super(ii, e);
    name = n;
    args = v;
    ret = r;
    cast = r != null;
    ann = a == null ? new Ann() : a;
    updating = ann.contains(Ann.Q_UPDATING);
    scope = scp;
    sc = stc;
  }

  @Override
  public final void checkUp() throws QueryException {
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
  public final void compile(final QueryContext ctx) throws QueryException {
    compile(ctx, null);
  }

  /**
   * Compiles the expression.
   * @param ctx query context
   * @param outer outer variable scope
   * @throws QueryException query exception
   */
  protected final void comp(final QueryContext ctx, final VarScope outer)
      throws QueryException {

    if(compiled) return;
    compiled = true;

    // compile closure
    for(Entry<Var, Expr> e : scope.closure().entrySet())
      e.setValue(e.getValue().compile(ctx, outer));

    final StaticContext tmp = ctx.sc;
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
      ctx.sc = tmp;
    }

    // convert all function calls in tail position to proper tail calls
    if(tco()) expr = expr.markTailCalls();

    if(ret == null) return;
    // adopt expected return type
    type = ret;
    // remove redundant casts
    if((ret.type == AtomType.BLN || ret.type == AtomType.FLT ||
        ret.type == AtomType.DBL || ret.type == AtomType.QNM ||
        ret.type == AtomType.URI) && ret.eq(expr.type())) {
      ctx.compInfo(OPTCAST, ret);
      cast = false;
    }
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
  public final boolean isVacuous() {
    return !uses(Use.UPD) && ret != null && ret.eq(SeqType.EMP);
  }

  @Override
  public boolean uses(final Use u) {
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
  public VarUsage count(final Var v) {
    VarUsage all = VarUsage.NEVER;
    for(final Entry<Var, Expr> e : scope.closure().entrySet())
      if((all = all.plus(e.getValue().count(v))) == VarUsage.MORE_THAN_ONCE) break;
    return all;
  }

  @Override
  public final Expr inline(final QueryContext ctx, final VarScope scp,
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

    final StaticContext tmp = ctx.sc;
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
        ctx.sc = tmp;
      }
    }

    return change ? optimize(ctx, scp) : null;
  }

  @Override
  public SeqType type() {
    return updating ? SeqType.EMP : super.type();
  }

  /**
   * Checks if this function is tail-call optimizable.
   * @return {@code true} if it is optimizable, {@code false} otherwise
   */
  protected  abstract boolean tco();

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Var v : args) if(!visitor.declared(v)) return false;
    return expr.accept(visitor);
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
  public final boolean compiled() {
    return compiled;
  }
}
