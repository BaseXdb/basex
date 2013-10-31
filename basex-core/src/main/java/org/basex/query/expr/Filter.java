package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract filter expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class Filter extends Preds {
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param ii input info
   * @param r expression
   * @param p predicates
   */
  Filter(final InputInfo ii, final Expr r, final Expr... p) {
    super(ii, p);
    root = r;
  }

  /**
   * Creates a filter expression for the given root and predicates.
   * @param ii input info
   * @param r root expression
   * @param p predicate expressions
   * @return filter expression
   */
  public static Filter get(final InputInfo ii, final Expr r, final Expr... p) {
    return new CachedFilter(ii, r, p);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(root);
    super.checkUp();
  }

  @Override
  public final Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    // invalidate current context value (will be overwritten by filter)
    final Value cv = ctx.value;
    try {
      root = root.compile(ctx, scp);
      // return empty root
      if(root.isEmpty()) return optPre(null, ctx);
      // convert filters without numeric predicates to axis paths
      if(root instanceof AxisPath && !super.has(Flag.FCS))
        return ((Path) root.copy(ctx,
          scp)).addPreds(ctx, scp, preds).compile(ctx, scp);

      // optimize filter expressions
      ctx.value = null;
      final Expr e = super.compile(ctx, scp);
      if(e != this) return e;

      // no predicates.. return root; otherwise, do some advanced compilations
      return preds.length == 0 ? root : opt(ctx);
    } finally {
      ctx.value = cv;
    }
  }

  /**
   * Compiles the filter expression, excluding the root node.
   * @param ctx query context
   * @return compiled expression
   */
  private Expr opt(final QueryContext ctx) {
    // evaluate return type
    final SeqType t = root.type();

    // determine number of results and type
    final long s = root.size();
    if(s == -1) {
      type = SeqType.get(t.type, t.zeroOrOne() ? Occ.ZERO_ONE : Occ.ZERO_MORE);
    } else {
      if(pos != null) {
        size = Math.max(0, s + 1 - pos.min) - Math.max(0, s - pos.max);
      } else if(last) {
        size = s > 0 ? 1 : 0;
      }
      // no results will remain: return empty sequence
      if (size == 0) return optPre(null, ctx);
      type = SeqType.get(t.type, size);
    }

    // no numeric predicates.. use simple iterator
    if(!super.has(Flag.FCS)) return new IterFilter(this);

    // one single position() or last() function specified: return single value
    if(preds.length == 1 && (last || pos != null) && root.isValue() && t.one() &&
        (last || pos.min == 1 && pos.max == 1)) return optPre(root, ctx);

    // only choose deterministic and context-independent offsets; e.g., skip:
    // (1 to 10)[random:integer(10)]  or  (1 to 10)[.]
    boolean off = false;
    if(preds.length == 1) {
      final Expr p = preds[0];
      final SeqType st = p.type();
      off = st.type.isNumber() && st.zeroOrOne() && !p.has(Flag.CTX) && !p.has(Flag.NDT);
      if(off) type = SeqType.get(type.type, Occ.ZERO_ONE);
    }

    // iterator for simple numeric predicate
    return off || useIterator() ? new IterPosFilter(this, off) : this;
  }

  /**
   * Adds a predicate to the filter.
   * @param ctx query context
   * @param scp variable scope
   * @param p predicate to be added
   * @return self reference
   * @throws QueryException query exception
   */
  public abstract Filter addPred(final QueryContext ctx, final VarScope scp, final Expr p)
      throws QueryException;

  @Override
  public final Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    // invalidate current context value (will be overwritten by filter)
    final Value cv = ctx.value;
    try {
      // return empty root
      if(root.isEmpty()) return optPre(null, ctx);
      // convert filters without numeric predicates to axis paths
      if(root instanceof AxisPath && !super.has(Flag.FCS))
        return ((Path) root.copy(ctx, scp)).addPreds(ctx, scp, preds);

      // no predicates.. return root; otherwise, do some advanced compilations
      return preds.length == 0 ? root : opt(ctx);
    } finally {
      ctx.value = cv;
    }
  }

  @Override
  public final boolean has(final Flag flag) {
    return root.has(flag) || flag != Flag.CTX && super.has(flag);
  }

  @Override
  public final boolean removable(final Var v) {
    return root.removable(v) && super.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    final VarUsage inPreds = super.count(v), inRoot = root.count(v);
    if(inPreds == VarUsage.NEVER) return inRoot;
    final long sz = root.size();
    return sz >= 0 && sz <= 1 || root.type().zeroOrOne()
        ? inRoot.plus(inPreds) : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    final boolean pr = super.inline(ctx, scp, v, e) != null;
    final Expr rt = root == null ? null : root.inline(ctx, scp, v, e);
    if(rt != null) root = rt;
    return pr || rt != null ? optimize(ctx, scp) : null;
  }

  @Override
  public final void plan(final FElem plan) {
    final FElem el = planElem();
    addPlan(plan, el, root);
    super.plan(el);
  }

  @Override
  public final String toString() {
    return root + super.toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Expr e : preds) {
      visitor.enterFocus();
      if(!e.accept(visitor)) return false;
      visitor.exitFocus();
    }
    return root.accept(visitor);
  }

  @Override
  public final int exprSize() {
    int sz = 1;
    for(final Expr e : preds) sz += e.exprSize();
    return sz + root.exprSize();
  }
}
