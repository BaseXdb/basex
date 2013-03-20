package org.basex.query.func;

import static org.basex.query.QueryText.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * A static User-defined function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class StaticUserFunc extends UserFunc {
  /** Declaration flag. */
  public final boolean declared;
  /** Flag that is turned on during compilation and prevents premature inlining. */
  boolean compiling;
  /** Flag for avoiding loops in {@link #databases(org.basex.util.list.StringList)}. */
  private boolean dontEnter;

  /**
   * Function constructor.
   * @param ii input info
   * @param n function name
   * @param v arguments
   * @param r return type
   * @param a annotations
   * @param d declaration flag
   * @param stc static context
   * @param scp variable scope
   */
  public StaticUserFunc(final InputInfo ii, final QNm n, final Var[] v, final SeqType r,
      final Ann a, final boolean d, final StaticContext stc, final VarScope scp) {
    super(ii, n, v, r, a, null, stc, scp);
    declared = d;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(!compiling) {
      compiling = true;
      comp(ctx, scp);
      compiling = false;
    }
    return this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    // reset context and evaluate function
    final Value cv = ctx.value;
    final StaticContext tmp = ctx.sc;
    ctx.sc = sc;
    ctx.value = null;
    try {
      final Item it = expr.item(ctx, ii);
      final Value v = it == null ? Empty.SEQ : it;
      // optionally promote return value to target type
      return cast ? ret.funcConvert(ctx, ii, v).item(ctx, ii) : it;
    } finally {
      ctx.value = cv;
      ctx.sc = tmp;
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    // reset context and evaluate function
    final Value cv = ctx.value;
    final StaticContext tmp = ctx.sc;
    ctx.sc = sc;
    ctx.value = null;
    try {
      final Value v = ctx.value(expr);
      // optionally promote return value to target type
      return cast ? ret.funcConvert(ctx, info, v) : v;
    } finally {
      ctx.value = cv;
      ctx.sc = tmp;
    }
  }

  @Override
  public ValueIter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  /**
   * Checks if this function can be inlined.
   * @param ctx query context
   * @return result of check
   */
  boolean inline(final QueryContext ctx) {
    return expr.isValue() || expr.exprSize() < ctx.context.prop.num(Prop.INLINELIMIT) &&
        !(compiling || uses(Use.NDT) || uses(Use.CTX) || selfRecursive());
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

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    throw Util.notexpected();
  }

  /**
   * Checks if this function calls itself recursively.
   * @return result of check
   */
  public boolean selfRecursive() {
    return !expr.accept(new ASTVisitor() {
      @Override
      public boolean funcCall(final UserFuncCall call) {
        return call.func != StaticUserFunc.this;
      }

      @Override
      public boolean inlineFunc(final Scope sub) {
        return sub.visit(this);
      }
    });
  }

  @Override
  protected boolean tco() {
    return true;
  }

  @Override
  public boolean databases(final StringList db) {
    if(dontEnter) return true;
    dontEnter = true;
    final boolean res = expr.databases(db);
    dontEnter = false;
    return res;
  }
}
