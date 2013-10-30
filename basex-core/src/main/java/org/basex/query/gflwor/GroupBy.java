package org.basex.query.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * The GFLWOR {@code group by} expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class GroupBy extends GFLWOR.Clause {
  /** Grouping specs. */
  final Spec[] specs;
  /** Non-grouping variable expressions. */
  Expr[] preExpr;
  /** Non-grouping variables. */
  Var[] post;
  /** Number of non-occluded grouping variables. */
  final int nonOcc;

  /**
   * Constructor.
   * @param gs grouping specs
   * @param pr references to pre-grouping variables
   * @param pst post-grouping variables
   * @param ii input info
   */
  public GroupBy(final Spec[] gs, final VarRef[] pr, final Var[] pst, final InputInfo ii) {
    super(ii, vars(gs, pst));
    specs = gs;
    preExpr = new Expr[pr.length];
    System.arraycopy(pr, 0, preExpr, 0, pr.length);
    post = pst;
    int n = 0;
    for(final Spec spec : specs) if(!spec.occluded) n++;
    nonOcc = n;
  }

  /**
   * Copy constructor.
   * @param gs grouping specs
   * @param pe pre-grouping expressions
   * @param pst post-grouping variables
   * @param no number of non-occluded grouping variables
   * @param ii input info
   */
  private GroupBy(final Spec[] gs, final Expr[] pe, final Var[] pst,
      final int no, final InputInfo ii) {
    super(ii, vars(gs, pst));
    specs = gs;
    preExpr = pe;
    post = pst;
    nonOcc = no;
  }

  /**
   * Gathers all declared variables.
   * @param gs grouping specs
   * @param vs non-grouping variables
   * @return declared variables
   */
  private static Var[] vars(final Spec[] gs, final Var[] vs) {
    final Var[] res = new Var[gs.length + vs.length];
    for(int i = 0; i < gs.length; i++) res[i] = gs[i].var;
    System.arraycopy(vs, 0, res, gs.length, vs.length);
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
        if(groups == null) groups = init(ctx);
        if(pos == groups.length) return false;

        final Group curr = groups[pos];
        // be nice to the garbage collector
        groups[pos++] = null;

        int p = 0;
        for(final Spec spec : specs) {
          if(!spec.occluded) {
            final Item key = curr.key[p++];
            ctx.set(spec.var, key == null ? Empty.SEQ : key, info);
          }
        }
        for(int i = 0; i < post.length; i++) ctx.set(post[i], curr.ngv[i].value(), info);
        return true;
      }

      /**
       * Builds up the groups.
       * @param ctx query context
       * @throws QueryException query exception
       */
      private Group[] init(final QueryContext ctx) throws QueryException {
        final ArrayList<Group> grps = new ArrayList<Group>();
        final IntObjMap<Group> map = new IntObjMap<Group>();
        final Collation[] colls = new Collation[nonOcc];
        for(int i = 0, p = 0; i < specs.length; i++)
          if(!specs[i].occluded) colls[p++] = specs[i].coll;

        while(sub.next(ctx)) {
          final Item[] key = new Item[nonOcc];
          int p = 0, hash = 1;
          for(final Spec spec : specs) {
            final Item ki = spec.item(ctx, info),
                atom = ki == null ? null : StandardFunc.atom(ki, info);
            if(!spec.occluded) {
              key[p++] = atom;
              // If the values are compared using a special collation, we let them collide
              // here and let the comparison do all the work later.
              // This enables other non-collation specs to avoid the collision.
              hash = 31 * hash +
                  (atom == null || spec.coll != null ? 0 : atom.hash(info));
            }
            ctx.set(spec.var, atom == null ? Empty.SEQ : atom, info);
          }

          // find the group for this key
          Group fst, grp = null;
          // no collations, so we can use hashing
          for(Group g = fst = map.get(hash); g != null; g = g.next) {
            if(eq(key, g.key, colls)) {
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
              map.put(hash, grp);
            } else {
              final Group nxt = fst.next;
              fst.next = grp;
              grp.next = nxt;
            }
          }

          // add values of non-grouping variables to the group
          for(int j = 0; j < preExpr.length; j++) grp.ngv[j].add(preExpr[j].value(ctx));
        }

        // we're finished, copy the array so the list can be garbage-collected
        return grps.toArray(new Group[grps.size()]);
      }
    };
  }

  /**
   * Checks two keys for equality.
   * @param as first key
   * @param bs second key
   * @param coll collations
   * @return {@code true} if the compare as equal, {@code false} otherwise
   * @throws QueryException query exception
   */
  boolean eq(final Item[] as, final Item[] bs, final Collation[] coll) throws QueryException {

    for(int i = 0; i < as.length; i++) {
      final Item a = as[i], b = bs[i];
      if(a == null ^ b == null || a != null && !a.equiv(b, coll[i], info)) return false;
    }
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    for(final Spec spec : specs) spec.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(GROUP).append(' ').append(BY);
    for(int i = 0; i < specs.length; i++) sb.append(i == 0 ? " " : SEP).append(specs[i]);
    return sb.toString();
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Spec sp : specs) if(sp.has(flag)) return true;
    return false;
  }

  @Override
  public GroupBy compile(final QueryContext cx, final VarScope sc) throws QueryException {
    for(final Expr e : preExpr) e.compile(cx, sc);
    for(final Spec b : specs) b.compile(cx, sc);
    return optimize(cx, sc);
  }

  @Override
  public GroupBy optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    for(int i = 0; i < preExpr.length; i++) {
      final SeqType it = preExpr[i].type();
      post[i].refineType(it.withOcc(it.mayBeZero() ? Occ.ZERO_MORE : Occ.ONE_MORE),
          ctx, info);
    }
    return this;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Spec b : specs) if(!b.removable(v)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.sum(v, specs).plus(VarUsage.sum(v, preExpr));
  }

  @Override
  public GFLWOR.Clause inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    final boolean b = inlineAll(ctx, scp, specs, v, e),
        p = inlineAll(ctx, scp, preExpr, v, e);
    return b || p ? optimize(ctx, scp) : null;
  }

  @Override
  public GroupBy copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    // copy the pre-grouping expressions
    final Expr[] pEx = Arr.copyAll(ctx, scp, vs, preExpr);

    // create fresh copies of the post-grouping variables
    final Var[] ps = new Var[post.length];
    for(int i = 0; i < ps.length; i++) {
      final Var old = post[i];
      ps[i] = scp.newCopyOf(ctx, old);
      vs.put(old.id, ps[i]);
    }

    // done
    return new GroupBy(Arr.copyAll(ctx, scp, vs, specs), pEx, ps, nonOcc, info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(!visitAll(visitor, specs)) return false;
    for(final Expr ng : preExpr) if(!ng.accept(visitor)) return false;
    for(final Var ng : post) if(!visitor.declared(ng)) return false;
    return true;
  }

  @Override
  boolean clean(final QueryContext ctx, final IntObjMap<Var> decl, final BitArray used) {
    // [LW] does not fix {@link #vars}
    final int len = preExpr.length;
    for(int i = 0; i < post.length; i++) {
      if(!used.get(post[i].id)) {
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
    checkNoneUp(preExpr);
    checkNoneUp(specs);
  }

  @Override
  long calcSize(final long cnt) {
    return -1;
  }

  @Override
  public int exprSize() {
    int sz = 0;
    for(final Expr e : preExpr) sz += e.exprSize();
    for(final Expr e : specs) sz += e.exprSize();
    return sz;
  }

  /**
   * Grouping spec.
   *
   * @author BaseX Team 2005-13, BSD License
   * @author Leo Woerteler
   */
  public static final class Spec extends Single {
    /** Grouping variable. */
    public final Var var;
    /** Occlusion flag, {@code true} if another grouping variable shadows this one. */
    public boolean occluded;
    /** Collation. */
    public final Collation coll;

    /**
     * Constructor.
     *
     * @param ii input info
     * @param v grouping variable
     * @param e grouping expression
     * @param cl collation
     */
    public Spec(final InputInfo ii, final Var v, final Expr e, final Collation cl) {
      super(ii, e);
      var = v;
      coll = cl;
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
      final TokenBuilder tb = new TokenBuilder().add(var.toString()).add(' ').add(ASSIGN);
      tb.add(' ').add(expr.toString());
      if(coll != null) tb.add(' ').add(COLLATION).add(" \"").add(coll.uri()).add('"');
      return tb.toString();
    }

    @Override
    public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
      return expr.item(ctx, ii);
    }

    @Override
    public Expr copy(final QueryContext ctx, final VarScope scp,
        final IntObjMap<Var> vs) {
      final Var v = scp.newCopyOf(ctx, var);
      vs.put(var.id, v);
      final Spec spec = new Spec(info, v, expr.copy(ctx, scp, vs), coll);
      spec.occluded = occluded;
      return spec;
    }

    @Override
    public boolean accept(final ASTVisitor visitor) {
      return expr.accept(visitor) && visitor.declared(var);
    }

    @Override
    public int exprSize() {
      return expr.exprSize();
    }
  }

  /**
   * A group of tuples of post-grouping variables.
   *
   * @author BaseX Team 2005-13, BSD License
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
