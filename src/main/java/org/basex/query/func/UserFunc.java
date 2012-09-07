package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * User-defined function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class UserFunc extends Single {
  /** Function name. */
  public final QNm name;
  /** Arguments. */
  public final Var[] args;
  /** Declaration flag. */
  public final boolean declared;
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
  private boolean cast;
  /** Compilation flag. */
  private boolean compiled;

  /**
   * Function constructor.
   * @param ii input info
   * @param n function name
   * @param v arguments
   * @param r return type
   * @param a annotations
   * @param qc query context
   */
  protected UserFunc(final InputInfo ii, final QNm n, final Var[] v, final SeqType r,
      final Ann a, final QueryContext qc) {
    this(ii, n, v, r, a, true, qc);
  }

  /**
   * Function constructor.
   * @param ii input info
   * @param n function name
   * @param v arguments
   * @param r return type
   * @param a annotations
   * @param d declaration flag
   * @param qc query context
   */
  public UserFunc(final InputInfo ii, final QNm n, final Var[] v, final SeqType r,
      final Ann a, final boolean d, final QueryContext qc) {

    super(ii, null);
    name = n;
    args = v;
    ret = r;
    cast = r != null;
    ann = a == null ? new Ann() : a;
    declared = d;
    updating = ann.contains(Ann.Q_UPDATING);
    sc = qc.sc;
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
  public Expr compile(final QueryContext ctx) throws QueryException {
    compile(ctx, true);
    return this;
  }

  /**
   * Compiles the expression.
   * @param ctx query context
   * @param cache cache variables
   * @throws QueryException query exception
   */
  protected final void compile(final QueryContext ctx, final boolean cache)
      throws QueryException {

    if(compiled) return;
    compiled = true;

    final StaticContext s = ctx.sc;
    ctx.sc = sc;
    final Atts ns = ctx.sc.ns.reset();
    final int vs = ctx.vars.size();
    final VarStack vl = cache ? ctx.vars.cache(args.length) : null;
    try {
      for(final Var v : args) ctx.vars.add(v);
      expr = expr.compile(ctx);
    } finally {
      if(cache) ctx.vars.reset(vl);
      else ctx.vars.size(vs);
      ctx.sc.ns.stack(ns);
      ctx.sc = s;
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

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    // reset context and evaluate function
    final Value cv = ctx.value;
    ctx.value = null;
    final StaticContext s = ctx.sc;
    ctx.sc = sc;
    final Atts ns = ctx.sc.ns.reset();
    try {
      final Item it = expr.item(ctx, ii);
      // optionally promote return value to target type
      return cast ? ret.cast(it, false, ctx, info, this) : it;
    } finally {
      ctx.sc.ns.stack(ns);
      ctx.sc = s;
      ctx.value = cv;
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    // reset context and evaluate function
    final Value cv = ctx.value;
    ctx.value = null;
    final StaticContext s = ctx.sc;
    ctx.sc = sc;
    final Atts ns = ctx.sc.ns.reset();
    try {
      final Value v = ctx.value(expr);
      // optionally promote return value to target type
      return cast ? ret.promote(v, ctx, info) : v;
    } finally {
      ctx.sc.ns.stack(ns);
      ctx.sc = s;
      ctx.value = cv;
    }
  }

  @Override
  public ValueIter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public final boolean isVacuous() {
    return !uses(Use.UPD) && ret.eq(SeqType.EMP);
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
  public void plan(final FElem plan) {
    final FElem el = planElem(NAM, name.string());
    addPlan(plan, el, expr);
    for(int i = 0; i < args.length; ++i) {
      el.add(planAttr(ARG + i, args[i].name.string()));
    }
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(DECLARE).add(' ').addExt(ann);
    if(updating) tb.add(UPDATING).add(' ');
    tb.add(FUNCTION).add(' ').add(name.string());
    tb.add(PAR1).addSep(args, SEP).add(PAR2);
    if(ret != null) tb.add(' ' + AS + ' ' + ret);
    if(expr != null) tb.add(" { " + expr + " }; ");
    return tb.toString();
  }

  /**
   * Checks if this function is tail-call optimizable.
   * @return {@code true} if it is optimizable, {@code false} otherwise
   */
  protected boolean tco() {
    return true;
  }
}
