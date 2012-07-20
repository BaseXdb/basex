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
  Expr root;

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
  public final Expr compile(final QueryContext ctx) throws QueryException {
    // invalidate current context value (will be overwritten by filter)
    final Value cv = ctx.value;
    try {
      root = root.compile(ctx);
      // return empty root
      if(root.isEmpty()) return optPre(null, ctx);
      // convert filters without numeric predicates to axis paths
      if(root instanceof AxisPath && !super.uses(Use.POS))
        return ((AxisPath) root).copy().addPreds(preds).compile(ctx);

      // optimize filter expressions
      ctx.value = null;
      final Expr e = super.compile(ctx);
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

    // one single position() or last() function specified:
    if(preds.length == 1 && (last || pos != null)) {
      // return single value
      if(root.isValue() && t.one() && (last || pos.min == 1 && pos.max == 1)) {
        return optPre(root, ctx);
      }
    }

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
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(root);
    final Value cv = ctx.value;
    final long cs = ctx.size;
    final long cp = ctx.pos;

    try {
      // cache results to support last() function
      final ValueBuilder vb = new ValueBuilder();
      for(Item i; (i = iter.next()) != null;) vb.add(i);

      // evaluate predicates
      for(final Expr p : preds) {
        final long is = vb.size();
        ctx.size = is;
        ctx.pos = 1;
        int c = 0;
        for(int s = 0; s < is; ++s) {
          ctx.value = vb.get(s);
          if(p.test(ctx, info) != null) vb.set(vb.get(s), c++);
          ctx.pos++;
        }
        vb.size(c);
      }
      return vb;
    } finally {
      ctx.value = cv;
      ctx.size = cs;
      ctx.pos = cp;
    }
  }

  /**
   * Adds a predicate to the filter.
   * @param p predicate to be added
   * @return self reference
   */
  public final Filter addPred(final Expr p) {
    preds = Array.add(preds, p);
    return this;
  }

  @Override
  public final boolean uses(final Use u) {
    return root.uses(u) || u != Use.CTX && super.uses(u);
  }

  @Override
  public final int count(final Var v) {
    return root.count(v) + super.count(v);
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
    return "(" + root + ')' + super.toString();
  }
}
