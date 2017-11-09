package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.GFLWOR.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * The GFLWOR {@code group by} expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class GroupBy extends Clause {
  /** Grouping specs. */
  private final Spec[] specs;
  /** Non-grouping variable expressions. */
  private Expr[] preExpr;
  /** Non-grouping variables. */
  private Var[] post;
  /** Number of non-occluded grouping variables. */
  private final int nonOcc;

  /**
   * Constructor.
   * @param specs grouping specs
   * @param pre references to pre-grouping variables
   * @param post post-grouping variables
   * @param info input info
   */
  public GroupBy(final Spec[] specs, final VarRef[] pre, final Var[] post, final InputInfo info) {
    super(info, vars(specs, post));
    this.specs = specs;
    this.post = post;
    preExpr = Array.copy(pre, new Expr[pre.length]);
    int n = 0;
    for(final Spec spec : specs) if(!spec.occluded) n++;
    nonOcc = n;
  }

  /**
   * Copy constructor.
   * @param specs grouping specs
   * @param pre pre-grouping expressions
   * @param post post-grouping variables
   * @param nonOcc number of non-occluded grouping variables
   * @param info input info
   */
  private GroupBy(final Spec[] specs, final Expr[] pre, final Var[] post, final int nonOcc,
      final InputInfo info) {
    super(info, vars(specs, post));
    this.specs = specs;
    preExpr = pre;
    this.post = post;
    this.nonOcc = nonOcc;
  }

  /**
   * Gathers all declared variables.
   * @param gs grouping specs
   * @param vs non-grouping variables
   * @return declared variables
   */
  private static Var[] vars(final Spec[] gs, final Var[] vs) {
    final int gl = gs.length, vl = vs.length;
    final Var[] res = new Var[gl + vl];
    for(int g = 0; g < gl; g++) res[g] = gs[g].var;
    System.arraycopy(vs, 0, res, gl, vl);
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
      public boolean next(final QueryContext qc) throws QueryException {
        if(groups == null) groups = init(qc);
        if(pos == groups.length) return false;

        final Group curr = groups[pos];
        // be nice to the garbage collector
        groups[pos++] = null;

        int p = 0;
        for(final Spec spec : specs) {
          if(!spec.occluded) {
            final Item key = curr.key[p++];
            qc.set(spec.var, key == null ? Empty.SEQ : key);
          }
        }
        final int pl = post.length;
        for(int i = 0; i < pl; i++) qc.set(post[i], curr.ngv[i].value());
        return true;
      }

      /**
       * Builds up the groups.
       * @param qc query context
       * @throws QueryException query exception
       */
      private Group[] init(final QueryContext qc) throws QueryException {
        final ArrayList<Group> grps = new ArrayList<>();
        final IntObjMap<Group> map = new IntObjMap<>();
        final Collation[] colls = new Collation[nonOcc];
        int c = 0;
        for(final Spec spec : specs) {
          if(!spec.occluded) colls[c++] = spec.coll;
        }

        while(sub.next(qc)) {
          final Item[] key = new Item[nonOcc];
          int p = 0, hash = 1;
          for(final Spec spec : specs) {
            final Item atom = spec.atomItem(qc, info);
            if(!spec.occluded) {
              key[p++] = atom;
              // If the values are compared using a special collation, we let them collide
              // here and let the comparison do all the work later.
              // This enables other non-collation specs to avoid the collision.
              hash = 31 * hash + (atom == null || spec.coll != null ? 0 : atom.hash(info));
            }
            qc.set(spec.var, atom == null ? Empty.SEQ : atom);
          }

          // find the group for this key
          final Group fst;
          Group grp = null;
          // no collations, so we can use hashing
          for(Group g = fst = map.get(hash); g != null; g = g.next) {
            if(eq(key, g.key, colls)) {
              grp = g;
              break;
            }
          }

          final int pl = preExpr.length;
          if(grp == null) {
            // new group, add it to the list
            final ValueBuilder[] ngs = new ValueBuilder[pl];
            final int nl = ngs.length;
            for(int n = 0; n < nl; n++) ngs[n] = new ValueBuilder();
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
          for(int g = 0; g < pl; g++) grp.ngv[g].add(qc.value(preExpr[g]));
        }

        // we're finished, copy the array so the list can be garbage-collected
        return grps.toArray(new Group[grps.size()]);
      }
    };
  }

  /**
   * Checks two keys for equality.
   * @param its1 first keys
   * @param its2 second keys
   * @param coll collations
   * @return {@code true} if the compare as equal, {@code false} otherwise
   * @throws QueryException query exception
   */
  private boolean eq(final Item[] its1, final Item[] its2, final Collation[] coll)
      throws QueryException {

    final int il = its1.length;
    for(int i = 0; i < il; i++) {
      final Item it1 = its1[i], it2 = its2[i];
      if(it1 == null ^ it2 == null || it1 != null && !it1.equiv(it2, coll[i], info)) return false;
    }
    return true;
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final Spec spec : specs) if(spec.has(flags)) return true;
    return false;
  }

  @Override
  public GroupBy compile(final CompileContext cc) throws QueryException {
    for(final Expr expr : preExpr) expr.compile(cc);
    for(final Spec spec : specs) spec.compile(cc);
    return optimize(cc);
  }

  @Override
  public GroupBy optimize(final CompileContext cc) throws QueryException {
    final int pl = preExpr.length;
    for(int p = 0; p < pl; p++) {
      final SeqType it = preExpr[p].seqType();
      post[p].refineType(it.withOcc(it.occ.union(Occ.ONE_MORE)), cc);
    }
    return this;
  }

  @Override
  public boolean removable(final Var var) {
    for(final Spec b : specs) if(!b.removable(var)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, specs).plus(VarUsage.sum(var, preExpr));
  }

  @Override
  public Clause inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {
    final boolean b = inlineAll(specs, var, ex, cc), p = inlineAll(preExpr, var, ex, cc);
    return b || p ? optimize(cc) : null;
  }

  @Override
  public GroupBy copy(final CompileContext cc, final IntObjMap<Var> vm) {
    // copy the pre-grouping expressions
    final Expr[] pEx = Arr.copyAll(cc, vm, preExpr);

    // create fresh copies of the post-grouping variables
    final Var[] ps = new Var[post.length];
    final int pl = ps.length;
    for(int p = 0; p < pl; p++) ps[p] = cc.copy(post[p], vm);

    // done
    return new GroupBy(Arr.copyAll(cc, vm, specs), pEx, ps, nonOcc, info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(!visitAll(visitor, specs)) return false;
    for(final Expr ng : preExpr) if(!ng.accept(visitor)) return false;
    for(final Var ng : post) if(!visitor.declared(ng)) return false;
    return true;
  }

  @Override
  boolean clean(final IntObjMap<Var> decl, final BitArray used) {
    // [LW] does not fix {@link #vars}
    final int len = preExpr.length;
    for(int p = 0; p < post.length; p++) {
      if(!used.get(post[p].id)) {
        preExpr = Array.delete(preExpr, p);
        post = Array.delete(post, p--);
      }
    }
    return preExpr.length < len;
  }

  @Override
  boolean skippable(final Clause cl) {
    return false;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(preExpr);
    checkNoneUp(specs);
  }

  @Override
  void calcSize(final long[] minMax) {
    minMax[0] = Math.min(minMax[0], 1);
  }

  @Override
  public int exprSize() {
    int sz = 0;
    for(final Expr expr : preExpr) sz += expr.exprSize();
    for(final Expr spec : specs) sz += spec.exprSize();
    return sz;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof GroupBy)) return false;
    final GroupBy g = (GroupBy) obj;
    return Array.equals(specs, g.specs) && Array.equals(preExpr, g.preExpr) &&
        Array.equals(post, g.post);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    for(final Spec spec : specs) spec.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final int pl = post.length;
    for(int p = 0; p < pl; p++) {
      sb.append(LET).append(" (: post-group :) ").append(post[p]);
      sb.append(' ').append(ASSIGN).append(' ').append(preExpr[p]).append(' ');
    }
    sb.append(GROUP).append(' ').append(BY);
    final int sl = specs.length;
    for(int s = 0; s < sl; s++) sb.append(s == 0 ? " " : SEP).append(specs[s]);
    return sb.toString();
  }

  /**
   * Grouping spec.
   *
   * @author BaseX Team 2005-17, BSD License
   * @author Leo Woerteler
   */
  public static final class Spec extends Single {
    /** Grouping variable. */
    public final Var var;
    /** Occlusion flag, {@code true} if another grouping variable shadows this one. */
    public boolean occluded;
    /** Collation (can be {@code null}). */
    private final Collation coll;

    /**
     * Constructor.
     *
     * @param info input info
     * @param var grouping variable
     * @param expr grouping expression
     * @param coll collation (can be {@code null})
     */
    public Spec(final InputInfo info, final Var var, final Expr expr, final Collation coll) {
      super(info, expr);
      this.var = var;
      this.coll = coll;
    }

    @Override
    public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
      return expr.item(qc, info);
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
      final Spec spec = new Spec(info, cc.copy(var, vm), expr.copy(cc, vm), coll);
      spec.occluded = occluded;
      return spec;
    }

    @Override
    public Expr compile(final CompileContext cc) throws QueryException {
      return super.compile(cc).optimize(cc);
    }

    @Override
    public Spec optimize(final CompileContext cc) throws QueryException {
      final SeqType st = expr.seqType();
      seqType = (st.type instanceof NodeType ? AtomType.ATM :
        st.mayBeArray() ? AtomType.ITEM : st.type).seqType();
      var.refineType(seqType, cc);
      return this;
    }

    @Override
    public boolean accept(final ASTVisitor visitor) {
      return expr.accept(visitor) && visitor.declared(var);
    }

    @Override
    public int exprSize() {
      return expr.exprSize();
    }

    @Override
    public boolean equals(final Object obj) {
      if(this == obj) return true;
      if(!(obj instanceof Spec)) return false;
      final Spec s = (Spec) obj;
      return var.equals(s.var) && occluded == s.occluded && Objects.equals(coll, s.coll) &&
          super.equals(obj);
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
      final TokenBuilder tb = new TokenBuilder();
      tb.addExt(var).add(' ').add(ASSIGN).add(' ').addExt(expr);
      if(coll != null) tb.add(' ').add(COLLATION).add(" \"").add(coll.uri()).add('"');
      return tb.toString();
    }
  }

  /**
   * A group of tuples of post-grouping variables.
   *
   * @author BaseX Team 2005-17, BSD License
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
