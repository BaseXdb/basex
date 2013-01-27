package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Filter expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class Filter extends Preds {
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param ii input info
   * @param r expression
   * @param p predicates
   */
  public Filter(final InputInfo ii, final Expr r, final Expr... p) {
    super(ii, p);
    root = r;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(root);
    super.checkUp();
  }

  @Override
  public final Expr compile(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    // invalidate current context value (will be overwritten by filter)
    final Value cv = ctx.value;
    try {
      root = root.compile(ctx, scp);
      // return empty root
      if(root.isEmpty()) return optPre(null, ctx);
      // convert filters without numeric predicates to axis paths
      if(root instanceof AxisPath && !super.uses(Use.POS))
        return ((AxisPath) root).copy().addPreds(ctx, scp, preds).compile(ctx, scp);

      // optimize filter expressions
      ctx.value = null;
      final Expr e = super.compile(ctx, scp);
      if(e != this) return e;

      // no predicates.. return root; otherwise, do some advanced compilations
      return preds.length == 0 ? root : comp2(ctx);
    } finally {
      ctx.value = cv;
    }
  }

  /**
   * Compiles the filter expression, excluding the root node.
   * @param ctx query context
   * @return compiled expression
   */
  public final Expr comp2(final QueryContext ctx) {
    // evaluate return type
    final SeqType t = root.type();

    // determine number of results and type
    final long s = root.size();
    if(s != -1) {
      if(pos != null) {
        size = Math.max(0, s + 1 - pos.min) - Math.max(0, s - pos.max);
      } else if(last) {
        size = s > 0 ? 1 : 0;
      }
      // no results will remain: return empty sequence
      if(size == 0) return optPre(null, ctx);
      type = SeqType.get(t.type, size);
    } else {
      type = SeqType.get(t.type, t.zeroOrOne() ? Occ.ZERO_ONE : Occ.ZERO_MORE);
    }

    // no numeric predicates.. use simple iterator
    if(!super.uses(Use.POS)) return new IterFilter(this);

    // one single position() or last() function specified: return single value
    if(preds.length == 1 && (last || pos != null) && root.isValue() && t.one() &&
        (last || pos.min == 1 && pos.max == 1)) return optPre(root, ctx);

    // only choose deterministic and context-independent offsets; e.g., skip:
    // (1 to 10)[random:integer(10)]  or  (1 to 10)[.]
    boolean off = false;
    if(preds.length == 1) {
      final Expr p = preds[0];
      final SeqType st = p.type();
      off = st.type.isNumber() && st.zeroOrOne() && !p.uses(Use.CTX) && !p.uses(Use.NDT);
      if(off) type = SeqType.get(type.type, Occ.ZERO_ONE);
    }

    // iterator for simple numeric predicate
    return off || useIterator() ? new IterPosFilter(this, off) : this;
  }

  @Override
  public Filter optimize(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    Value val = root.value(ctx);
    final Value cv = ctx.value;
    final long cs = ctx.size;
    final long cp = ctx.pos;

    try {
      // evaluate first predicate, based on incoming value
      final ValueBuilder vb = new ValueBuilder();
      Expr p = preds[0];
      long is = val.size();
      ctx.size = is;
      ctx.pos = 1;
      for(int s = 0; s < is; ++s) {
        final Item it = val.itemAt(s);
        ctx.value = it;
        if(p.test(ctx, info) != null) vb.add(it);
        ctx.pos++;
      }
      // save memory
      val = null;

      // evaluate remaining predicates, based on value builder
      final int pl = preds.length;
      for(int i = 1; i < pl; i++) {
        is = vb.size();
        p = preds[i];
        ctx.size = is;
        ctx.pos = 1;
        int c = 0;
        for(int s = 0; s < is; ++s) {
          final Item it = vb.get(s);
          ctx.value = it;
          if(p.test(ctx, info) != null) vb.set(it, c++);
          ctx.pos++;
        }
        vb.size(c);
      }

      // return resulting values
      return vb;
    } finally {
      ctx.value = cv;
      ctx.size = cs;
      ctx.pos = cp;
    }
  }

  /**
   * Adds a predicate to the filter.
   * @param ctx query context
   * @param scp variable scope
   * @param p predicate to be added
   * @return self reference
   * @throws QueryException query exception
   */
  public final Filter addPred(final QueryContext ctx, final VarScope scp, final Expr p)
      throws QueryException {
    preds = Array.add(preds, p);
    return optimize(ctx, scp);
  }

  @Override
  public final boolean uses(final Use u) {
    return root.uses(u) || u != Use.CTX && super.uses(u);
  }

  @Override
  public final boolean removable(final Var v) {
    return root.removable(v) && super.removable(v);
  }

  @Override
  public final Expr remove(final Var v) {
    root = root.remove(v);
    return super.remove(v);
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
  public boolean databases(final StringList db) {
    return root.databases(db) && super.databases(db);
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
  public boolean visitVars(final VarVisitor visitor) {
    return root.visitVars(visitor) && visitor.visitAll(preds);
  }
}
