package org.basex.query.gflwor;

import static org.basex.query.QueryText.*;
import java.util.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.gflwor.GFLWOR.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * The GFLWOR {@code group by} expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class GroupBy extends GFLWOR.Clause {
  /** Grouping specs. */
  final Spec[] by;
  /** Pre-grouping variables, only for type propagation. */
  Var[] pre;
  /** Non-grouping variable expressions. */
  Expr[] preExpr;
  /** Non-grouping variables. */
  Var[] post;
  /** Number of non-occluded grouping variables. */
  final int nonOcc;

  /**
   * Constructor.
   * @param specs grouping specs
   * @param pr references to pre-grouping variables
   * @param pst post-grouping variables
   * @param ii input info
   */
  public GroupBy(final Spec[] specs, final VarRef[] pr, final Var[] pst,
      final InputInfo ii) {
    super(ii, vars(specs, pst));
    by = specs;
    pre = new Var[pr.length];
    for(int i = 0; i < pre.length; i++) pre[i] = pr[i].var;
    preExpr = new Expr[pr.length];
    System.arraycopy(pr, 0, preExpr, 0, pr.length);
    post = pst;
    int n = 0;
    for(final Spec spec : by) if(!spec.occluded) n++;
    nonOcc = n;
  }

  /**
   * Copy constructor.
   * @param specs grouping specs
   * @param pr pre-grouping variables
   * @param pe pre-grouping expressions
   * @param pst post-grouping variables
   * @param no number of non-occluded grouping variables
   * @param ii input info
   */
  private GroupBy(final Spec[] specs, final Var[] pr, final Expr[] pe, final Var[] pst,
      final int no, final InputInfo ii) {
    super(ii, vars(specs, pst));
    by = specs;
    pre = pr;
    preExpr = pe;
    post = pst;
    nonOcc = no;
  }

  /**
   * Gathers all declared variables.
   * @param specs grouping specs
   * @param vs non-grouping variables
   * @return declared variables
   */
  private static Var[] vars(final Spec[] specs, final Var[] vs) {
    final Var[] res = new Var[specs.length + vs.length];
    for(int i = 0; i < specs.length; i++) res[i] = specs[i].var;
    System.arraycopy(vs, 0, res, specs.length, vs.length);
    return res;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      /** Groups to iterate over. */
      private Group[] groups;
      /** Current position. */
      private int pos;

      @Override
      public boolean next(final QueryContext ctx) throws QueryException {
        if(groups == null) init(ctx);
        if(pos == groups.length) return false;

        final Group curr = groups[pos];
        // be nice to the garbage collector
        groups[pos++] = null;

        int p = 0;
        for(final Spec spec : by) {
          if(!spec.occluded) {
            final Item key = curr.key[p++];
            ctx.set(spec.var, key == null ? Empty.SEQ : key, info);
          }
        }
        for(int i = 0; i < post.length; i++) ctx.set(post[i], curr.ngv[i].value(), info);
        return true;
      }

      /**
       * Initializes the groups.
       * @param ctx query context
       * @throws QueryException query exception
       */
      private void init(final QueryContext ctx) throws QueryException {
        final ArrayList<Group> grps = new ArrayList<Group>();
        final IntMap<Group> hashMap = new IntMap<Group>();
        while(sub.next(ctx)) {
          final Item[] key = new Item[nonOcc];
          int p = 0, hash = 1;
          for(int i = 0; i < by.length; i++) {
            final Item ki = by[i].item(ctx, info),
                atom = ki == null ? null : StandardFunc.atom(ki, info);
            if(!by[i].occluded) {
              key[p++] = atom;
              hash = 31 * hash + (atom == null ? 0 : atom.hash(info));
            }
            ctx.set(by[i].var, atom == null ? Empty.SEQ : atom, info);
          }

          // find the group for this key
          final Group fst = hashMap.get(hash);
          Group grp = null;
          for(Group g = fst; g != null; g = g.next) {
            if(eq(key, g.key)) {
              grp = g;
              break;
            }
          }

          if(grp == null) {
            // new group, add it to the list
            final ValueBuilder[] ngs = new ValueBuilder[preExpr.length];
            for(int i = 0; i < ngs.length; i++) ngs[i] = new ValueBuilder();
            grp = new Group(key, ngs);
            grps.add(grp);

            // insert the group into the hash table
            if(fst == null) {
              hashMap.add(hash, grp);
            } else {
              final Group nxt = fst.next;
              fst.next = grp;
              grp.next = nxt;
            }
          }

          for(int j = 0; j < preExpr.length; j++) grp.ngv[j].add(preExpr[j].value(ctx));
        }

        // we're finished, copy the array so the list can be garbage-collected
        groups = grps.toArray(new Group[grps.size()]);
      }
    };
  }

  /**
   * Checks two keys for equality.
   * @param as first key
   * @param bs second key
   * @return {@code true} if the compare as equal, {@code false} otherwise
   * @throws QueryException query exception
   */
  final boolean eq(final Item[] as, final Item[] bs) throws QueryException {
    for(int i = 0; i < as.length; i++) {
      final Item a = as[i], b = bs[i];
      if(a == null ^ b == null || a != null && !a.equiv(info, b)) return false;
    }
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    for(final Spec spec : by) spec.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(GROUP).append(' ').append(BY);
    for(int i = 0; i < by.length; i++) sb.append(i == 0 ? " " : SEP).append(by[i]);
    return sb.toString();
  }

  @Override
  public boolean uses(final Use u) {
    if(u == Use.VAR || u == Use.X30) return true;
    for(final Spec sp : by) if(sp.uses(u)) return true;
    return false;
  }

  @Override
  public GroupBy compile(final QueryContext cx, final VarScope sc) throws QueryException {
    for(int i = 0; i < by.length; i++) by[i].compile(cx, sc);
    for(int i = 0; i < pre.length; i++) {
      final SeqType it = pre[i].declaredType();
      post[i].refineType(it.withOcc(it.mayBeZero() ? Occ.ZERO_MORE : Occ.ONE_MORE),
          cx, info);
    }
    return this;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Spec b : by) if(!b.removable(v)) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    for(final Spec b : by) b.remove(v);
    return this;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.sum(v, by).plus(VarUsage.sum(v, preExpr));
  }

  @Override
  public GFLWOR.Clause inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    final boolean b = inlineAll(ctx, scp, by, v, e),
        p = inlineAll(ctx, scp, preExpr, v, e);
    return b || p ? optimize(ctx, scp) : null;
  }

  @Override
  public GroupBy copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    // update all pre-grouping variables
    final Var[] pr = new Var[pre.length];
    for(int i = 0; i < pr.length; i++) {
      final Var p = pre[i], q = vs.get(p.id);
      pr[i] = q == null ? p : q;
    }

    // copy the pre-grouping expressions
    final Expr[] pEx = Arr.copyAll(ctx, scp, vs, preExpr);

    // create fresh copies of the post-grouping variables
    final Var[] ps = new Var[post.length];
    for(int i = 0; i < ps.length; i++) {
      final Var old = post[i];
      ps[i] = scp.newCopyOf(ctx, old);
      vs.add(old.id, ps[i]);
    }

    // done
    return new GroupBy(Arr.copyAll(ctx, scp, vs, by), pr, pEx, ps, nonOcc, info);
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    if(!visitor.visitAll(by)) return false;
    for(final Expr ng : preExpr) if(!ng.visitVars(visitor)) return false;
    for(final Var ng : post) if(!visitor.declared(ng)) return false;
    return true;
  }

  @Override
  boolean clean(final QueryContext ctx, final BitArray used) {
    // [LW] does not fix {@link #vars}
    final int len = pre.length;
    for(int i = 0; i < post.length; i++) {
      if(!used.get(post[i].id)) {
        pre  = Array.delete(pre, i);
        preExpr = Array.delete(preExpr, i);
        post = Array.delete(post, i--);
      }
    }
    return preExpr.length < len;
  }

  @Override
  boolean skippable(final GFLWOR.Clause cl) {
    return false;
  }

  @Override
  public void checkUp() throws QueryException {
    for(final Spec spec : by) spec.checkUp();
  }

  @Override
  public boolean databases(final StringList db) {
    for(final Spec spec : by) if(!spec.databases(db)) return false;
    return true;
  }

  @Override
  long calcSize(final long cnt) {
    return -1;
  }

  /**
   * Grouping spec.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Leo Woerteler
   */
  public static final class Spec extends Single {
    /** Grouping variable. */
    public final Var var;
    /** Occlusion flag, {@code true} if another grouping variable shadows this one. */
    public boolean occluded;

    /**
     * Constructor.
     *
     * @param ii input info
     * @param v grouping variable
     * @param e grouping expression
     */
    public Spec(final InputInfo ii, final Var v, final Expr e) {
      super(ii, e);
      var = v;
    }

    @Override
    public void plan(final FElem plan) {
      final FElem e = planElem();
      var.plan(e);
      expr.plan(e);
      plan.add(e);
    }

    @Override
    public String toString() {
      return var + " " + ASSIGN + ' ' + expr;
    }

    @Override
    public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
      return expr.item(ctx, ii);
    }

    @Override
    public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
      final Var v = scp.newCopyOf(ctx, var);
      vs.add(var.id, v);
      final Spec spec = new Spec(info, v, expr.copy(ctx, scp, vs));
      spec.occluded = occluded;
      return spec;
    }

    @Override
    public boolean visitVars(final VarVisitor visitor) {
      return expr.visitVars(visitor) && visitor.declared(var);
    }
  }

  /**
   * A group of tuples of post-grouping variables.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Leo Woerteler
   */
  private static final class Group {
    /** Grouping key, may contain {@code null} values. */
    final Item[] key;
    /** Non-grouping variables. */
    final ValueBuilder[] ngv;
    /** Overflow list. */
    Group next;

    /**
     * Constructor.
     * @param k grouping key
     * @param ng non-grouping variables
     */
    Group(final Item[] k, final ValueBuilder[] ng) {
      key = k;
      ngv = ng;
    }
  }
}
