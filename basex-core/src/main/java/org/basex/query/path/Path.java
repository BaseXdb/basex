package org.basex.query.path;

import static org.basex.query.QueryText.*;
import static org.basex.query.path.Axis.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Context;
import org.basex.query.path.Test.Mode;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Path expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Path extends ParseExpr {
  /** Root expression. */
  public Expr root;
  /** Path steps. */
  public Expr[] steps;

  /**
   * Constructor.
   * @param ii input info
   * @param r root expression; can be a {@code null} reference
   * @param s axis steps
   */
  Path(final InputInfo ii, final Expr r, final Expr[] s) {
    super(ii);
    root = r;
    steps = s;
  }

  /**
   * Returns a new path instance.
   * @param ii input info
   * @param r root expression; can be a {@code null} reference
   * @param path path steps
   * @return class instance
   */
  public static Path get(final InputInfo ii, final Expr r, final Expr... path) {
    // check if all steps are axis steps
    boolean axes = true;
    for(int p = 0; p < path.length; p++) {
      Expr e = path[p];
      if(e instanceof Context) {
        e = Step.get(((ParseExpr) e).info, SELF, Test.NOD);
      } else if(e instanceof Filter) {
        final Filter f = (Filter) e;
        if(f.root instanceof Context) {
          e = Step.get(f.info, SELF, Test.NOD, f.preds);
        }
      }
      axes &= e instanceof Step;
      path[p] = e;
    }
    return axes ? new CachedPath(ii, r, path).finish(null) : new MixedPath(ii, r, path);
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(root);
    final int ss = steps.length;
    for(int s = 0; s < ss - 1; ++s) checkNoUp(steps[s]);
    steps[ss - 1].checkUp();
  }

  @Override
  public final Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(root != null) setRoot(ctx, root.compile(ctx, scp));

    final Value v = ctx.value;
    try {
      ctx.value = root(ctx);
      return compilePath(ctx, scp);
    } finally {
      ctx.value = v;
    }
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(root instanceof Context) {
      ctx.compInfo(OPTREMCTX);
      root = null;
    }

    for(final Expr e : steps) {
      // check for empty steps
      if(e.isEmpty()) return optPre(null, ctx);
    }

    return this;
  }

  /**
   * Compiles the location path.
   * @param ctx query context
   * @param scp variable scope
   * @return optimized expression
   * @throws QueryException query exception
   */
  protected abstract Expr compilePath(final QueryContext ctx, VarScope scp)
      throws QueryException;

  /**
   * Returns the root of the current context or {@code null}.
   * @param ctx query context
   * @return root
   */
  final Value root(final QueryContext ctx) {
    final Value v = ctx != null ? ctx.value : null;
    // no root specified: return context, if it does not reference a document
    // as e.g. happens in //a(b|c)
    if(root == null) return v == null || v.type != NodeType.DOC ? v : null;
    // root is value: return root
    if(root.isValue()) return (Value) root;
    // no root reference, no context: return null
    if(!(root instanceof Root) || v == null) return null;
    // return context sequence or root of current context
    return v.size() == 1 ? Root.root(v) : v;
  }

  /**
   * Sets a new root expression and eliminates a superfluous context item.
   * @param ctx query context
   * @param rt root expression
   */
  private void setRoot(final QueryContext ctx, final Expr rt) {
    root = rt;
    if(root instanceof Context) {
      ctx.compInfo(OPTREMCTX);
      root = null;
    }
  }

  @Override
  public final boolean has(final Flag flag) {
    // first step or root expression will be used as context
    if(flag == Flag.CTX) return root == null || root.has(flag);
    for(final Expr s : steps) if(s.has(flag)) return true;
    return root != null && root.has(flag);
  }

  /**
   * Optimizes descendant-or-self steps and static types.
   * @param ctx query context
   */
  void optSteps(final QueryContext ctx) {
    boolean opt = false;
    Expr[] st = steps;
    for(int l = 1; l < st.length; ++l) {
      if(!(st[l - 1] instanceof Step && st[l] instanceof Step)) continue;

      final Step prev = (Step) st[l - 1];
      final Step curr = (Step) st[l];
      if(!prev.simple(DESCORSELF, false)) continue;

      if(curr.axis == CHILD && !curr.has(Flag.FCS)) {
        // descendant-or-self::node()/child::X -> descendant::X
        final int sl = st.length;
        final Expr[] tmp = new Expr[sl - 1];
        System.arraycopy(st, 0, tmp, 0, l - 1);
        System.arraycopy(st, l, tmp, l - 1, sl - l);
        st = tmp;
        curr.axis = DESC;
        opt = true;
      } else if(curr.axis == ATTR && !curr.has(Flag.FCS)) {
        // descendant-or-self::node()/@X -> descendant-or-self::*/@X
        prev.test = new NameTest(false);
        opt = true;
      }
    }
    if(opt) ctx.compInfo(OPTDESC);

    // set atomic type for single attribute steps to speedup predicate tests
    if(root == null && st.length == 1 && st[0] instanceof Step) {
      final Step curr = (Step) st[0];
      if(curr.axis == ATTR && curr.test.mode == Mode.STD) curr.type = SeqType.NOD_ZO;
    }
    steps = st;
  }

  /**
   * Computes the number of results.
   * @param ctx query context
   * @return number of results
   */
  long size(final QueryContext ctx) {
    final Value rt = root(ctx);
    // skip computation if value contains document nodes
    if(rt == null || rt.type != NodeType.DOC) return -1;
    final Data data = rt.data();
    // skip computation if no database instance is available, is out-of-date or
    // if context does not contain all database nodes
    if(data == null || !data.meta.uptodate ||
        data.resources.docs().size() != rt.size()) return -1;

    ArrayList<PathNode> nodes = data.paths.root();
    long m = 1;
    for(int s = 0; s < steps.length; s++) {
      final Step curr = axisStep(s);
      if(curr != null) {
        nodes = curr.nodes(nodes, data);
        if(nodes == null) return -1;
      } else if(s + 1 == steps.length) {
        m = steps[s].size();
      } else {
        // stop if a non-axis step is not placed last
        return -1;
      }
    }

    long sz = 0;
    for(final PathNode pn : nodes) sz += pn.stats.count;
    return sz * m;
  }

  /**
   * Checks if the location path contains steps that will never yield results.
   * @param stps step array
   * @param ctx query context
   */
  void voidStep(final Expr[] stps, final QueryContext ctx) {
    for(int l = 0; l < stps.length; ++l) {
      final Step s = axisStep(l);
      if(s == null) continue;
      final Axis sa = s.axis;
      if(l == 0) {
        if(root instanceof CAttr) {
          // @.../child:: / @.../descendant::
          if(sa == CHILD || sa == DESC) {
            ctx.compInfo(WARNDESC, root);
            return;
          }
        } else if(root instanceof Root || root instanceof Value &&
            ((Value) root).type == NodeType.DOC || root instanceof CDoc) {
          if(sa != CHILD && sa != DESC && sa != DESCORSELF &&
            (sa != SELF && sa != ANCORSELF || s.test != Test.NOD && s.test != Test.DOC)) {
            ctx.compInfo(WARNDOC, root, sa);
            return;
          }
        }
      } else {
        final Step ls = axisStep(l - 1);
        if(ls == null) continue;
        final Axis lsa = ls.axis;
        boolean warning = true;
        if(sa == SELF || sa == DESCORSELF) {
          // .../self:: / .../descendant-or-self::
          if(s.test == Test.NOD) continue;
          // @.../..., text()/...
          warning = lsa == ATTR && s.test.type != NodeType.ATT ||
                    ls.test == Test.TXT && s.test != Test.TXT;
          if(!warning) {
            if(sa == DESCORSELF) continue;
            // .../self::
            final QNm n0 = ls.test.name;
            final QNm n1 = s.test.name;
            if(n0 == null || n1 == null || n0.local().length == 0 ||
                n1.local().length == 0) continue;
            // ...X/...Y
            warning = !n1.eq(n0);
          }
        } else if(sa == FOLLSIBL || sa == PRECSIBL) {
          // .../following-sibling:: / .../preceding-sibling::
          warning = lsa == ATTR;
        } else if(sa == DESC || sa == CHILD || sa == ATTR) {
          // .../descendant:: / .../child:: / .../attribute::
          warning = lsa == ATTR || ls.test == Test.TXT || ls.test == Test.COM ||
             ls.test == Test.PI || sa == ATTR && s.test == Test.NSP;
        } else if(sa == PARENT || sa == ANC) {
          // .../parent:: / .../ancestor::
          warning = ls.test == Test.DOC;
        }
        if(warning) {
          ctx.compInfo(WARNSELF, s);
          return;
        }
      }
    }
  }

  /**
   * Converts descendant to child steps.
   * @param ctx query context
   * @param data data reference
   * @return path
   */
  Expr children(final QueryContext ctx, final Data data) {
    // skip path check if no path index exists, or if it is out-of-date
    if(!data.meta.uptodate || data.nspaces.globalNS() == null) return this;

    Path path = this;
    for(int s = 0; s < steps.length; ++s) {
      // don't allow predicates in preceding location steps
      final Step prev = s > 0 ? axisStep(s - 1) : null;
      if(prev != null && prev.preds.length != 0) break;

      // ignore axes other than descendant, or numeric predicates
      final Step curr = axisStep(s);
      if(curr == null || curr.axis != DESC || curr.has(Flag.FCS)) continue;

      // check if child steps can be retrieved for current step
      ArrayList<PathNode> pn = pathNodes(data, s);
      if(pn == null) continue;

      // cache child steps
      final ArrayList<QNm> qnm = new ArrayList<>();
      while(pn.get(0).par != null) {
        QNm nm = new QNm(data.tagindex.key(pn.get(0).name));
        // skip children with prefixes
        if(nm.hasPrefix()) return this;
        for(final PathNode p : pn) {
          if(pn.get(0).name != p.name) nm = null;
        }
        qnm.add(nm);
        pn = PathSummary.parent(pn);
      }
      ctx.compInfo(OPTCHILD, steps[s]);

      // build new steps
      int ts = qnm.size();
      final Expr[] stps = new Expr[ts + steps.length - s - 1];
      for(int t = 0; t < ts; ++t) {
        final Expr[] preds = t == ts - 1 ?
            ((Preds) steps[s]).preds : new Expr[0];
        final QNm nm = qnm.get(ts - t - 1);
        final NameTest nt = nm == null ? new NameTest(false) :
          new NameTest(nm, Mode.LN, false, null);
        stps[t] = Step.get(info, CHILD, nt, preds);
      }
      while(++s < steps.length) stps[ts++] = steps[s];
      path = get(info, root, stps);
      break;
    }

    // check if the all children in the path exist; don't test with namespaces
    if(data.nspaces.size() == 0) {
      LOOP:
      for(int s = 0; s < path.steps.length; ++s) {
        // only verify child steps; ignore namespaces
        final Step st = path.axisStep(s);
        if(st == null || st.axis != CHILD) break;
        if(st.test.mode == Mode.ALL || st.test.mode == null) continue;
        if(st.test.mode != Mode.LN) break;

        // check if one of the addressed nodes is on the correct level
        final int name = data.tagindex.id(st.test.name.local());
        for(final PathNode pn : data.paths.desc(name, Data.ELEM)) {
          if(pn.level() == s + 1) continue LOOP;
        }
        ctx.compInfo(OPTPATH, path);
        return Empty.SEQ;
      }
    }
    return path;
  }

  /**
   * Casts the specified step into an axis step, or returns a {@code null}
   * reference.
   * @param i index
   * @return step
   */
  Step axisStep(final int i) {
    return steps[i] instanceof Step ? (Step) steps[i] : null;
  }

  /**
   * Returns all summary path nodes for the specified location step or
   * {@code null} if nodes cannot be retrieved or are found on different levels.
   * @param data data reference
   * @param l last step to be checked
   * @return path nodes
   */
  ArrayList<PathNode> pathNodes(final Data data, final int l) {
    // skip request if no path index exists or might be out-of-date
    if(!data.meta.uptodate) return null;

    ArrayList<PathNode> in = data.paths.root();
    for(int s = 0; s <= l; ++s) {
      final Step curr = axisStep(s);
      if(curr == null) return null;
      final boolean desc = curr.axis == DESC;
      if(!desc && curr.axis != CHILD || curr.test.mode != Mode.LN)
        return null;

      final int name = data.tagindex.id(curr.test.name.local());

      final ArrayList<PathNode> al = new ArrayList<>();
      for(final PathNode pn : PathSummary.desc(in, desc)) {
        if(pn.kind == Data.ELEM && name == pn.name) {
          // skip test if a tag is found on different levels
          if(!al.isEmpty() && al.get(0).level() != pn.level()) return null;
          al.add(pn);
        }
      }
      if(al.isEmpty()) return null;
      in = al;
    }
    return in;
  }

  /**
   * Adds a predicate to the last step.
   * @param ctx query context
   * @param scp variable scope
   * @param pred predicate to be added
   * @return resulting path instance
   * @throws QueryException query exception
   */
  public final Expr addPreds(final QueryContext ctx, final VarScope scp,
      final Expr... pred) throws QueryException {
    steps[steps.length - 1] = axisStep(steps.length - 1).addPreds(pred);
    return get(info, root, steps).optimize(ctx, scp);
  }

  @Override
  public boolean removable(final Var v) {
    return root == null || root.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    final VarUsage inRoot = root == null ? VarUsage.NEVER : root.count(v);
    return VarUsage.sum(v, steps) == VarUsage.NEVER ? inRoot : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final Expr inline(final QueryContext ctx, final VarScope scp, final Var v, final Expr e)
      throws QueryException {

    final Value oldVal = ctx.value;
    try {
      ctx.value = root(ctx);
      final Expr rt = root == null ? null : root.inline(ctx, scp, v, e);
      if(rt != null) {
        setRoot(ctx, rt);
        ctx.value = oldVal;
        ctx.value = root(ctx);
      }

      boolean change = rt != null;
      for(int i = 0; i < steps.length; i++) {
        final Expr nw = steps[i].inline(ctx, scp, v, e);
        if(nw != null) {
          steps[i] = nw;
          change = true;
        }
      }
      return change ? optimize(ctx, scp) : null;
    } finally {
      ctx.value = oldVal;
    }
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(), root, steps);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(root != null) sb.append(root);
    for(final Expr s : steps) {
      if(sb.length() != 0) sb.append(s instanceof Bang ? " ! " : "/");
      if(s instanceof Step) sb.append(s);
      else sb.append(s);
    }
    return sb.toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(root == null) {
      if(!visitor.lock(DBLocking.CTX)) return false;
    } else if(!root.accept(visitor)) {
      return false;
    }
    visitor.enterFocus();
    if(!visitAll(visitor, steps)) return false;
    visitor.exitFocus();
    return true;
  }

  @Override
  public final int exprSize() {
    int sz = 1;
    for(final Expr e : steps) sz += e.exprSize();
    return root == null ? sz : sz + root.exprSize();
  }
}
