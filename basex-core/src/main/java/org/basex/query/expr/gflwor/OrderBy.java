package org.basex.query.expr.gflwor;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.List;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.GFLWOR.Eval;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * FLWOR {@code order by}-expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class OrderBy extends GFLWOR.Clause {
  /** References to the variables to be sorted. */
  private VarRef[] refs;
  /** Sort keys. */
  private final Key[] keys;

  /**
   * Constructor.
   * @param refs variables to sort
   * @param keys sort keys
   * @param info input info
   */
  public OrderBy(final VarRef[] refs, final Key[] keys, final InputInfo info) {
    super(info);
    this.refs = refs;
    this.keys = keys;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      /** Sorted output tuples. */
      private Value[][] tpls;
      /** Permutation of the values. */
      private Integer[] perm;
      /** Current position. */
      int pos;
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        if(tpls == null) sort(qc);
        if(pos == tpls.length) return false;
        final int p = perm[pos++];
        final Value[] tuple = tpls[p];
        // free the space occupied by the tuple
        tpls[p] = null;
        final int rl = refs.length;
        for(int r = 0; r < rl; r++) qc.set(refs[r].var, tuple[r], info);
        return true;
      }

      /**
       * Caches and sorts all incoming tuples.
       * @param qc query context
       * @throws QueryException evaluation exception
       */
      private void sort(final QueryContext qc) throws QueryException {
        // keys are stored at odd positions, values at even ones
        List<Value[]> tuples = new ArrayList<>();
        while(sub.next(qc)) {
          final int kl = keys.length;
          final Item[] key = new Item[kl];
          for(int k = 0; k < kl; k++) key[k] = keys[k].expr.atomItem(qc, keys[k].info);
          tuples.add(key);

          final int rl = refs.length;
          final Value[] vals = new Value[rl];
          for(int r = 0; r < rl; r++) vals[r] = refs[r].value(qc);
          tuples.add(vals);
        }

        final int len = tuples.size() >>> 1;
        final Item[][] ks = new Item[len][];
        perm = new Integer[len];
        tpls = new Value[len][];
        for(int i = 0; i < len; i++) {
          perm[i] = i;
          tpls[i] = tuples.get(i << 1 | 1);
          ks[i] = (Item[]) tuples.get(i << 1);
        }
        // be nice to the garbage collector
        tuples = null;
        try {
          Arrays.sort(perm, new Comparator<Integer>() {
            @Override
            public int compare(final Integer x, final Integer y) {
              try {
                final Item[] a = ks[x], b = ks[y];
                final int kl = keys.length;
                for(int k = 0; k < kl; k++) {
                  final Key or = keys[k];
                  Item m = a[k], n = b[k];
                  if(m == Dbl.NAN || m == Flt.NAN) m = null;
                  if(n == Dbl.NAN || n == Flt.NAN) n = null;
                  if(m != null && n != null && !m.comparable(n))
                    throw castError(or.info, n, m.type);

                  final int c = m == null
                      ? n == null ? 0                 : or.least ? -1 : 1
                      : n == null ? or.least ? 1 : -1 : m.diff(n, or.coll, or.info);
                  if(c != 0) return or.desc ? -c : c;
                }
                return 0;
              } catch(final QueryException ex) {
                throw new QueryRTException(ex);
              }
            }
          });
        } catch(final QueryRTException ex) {
          throw ex.getCause();
        }
      }
    };
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    for(final Key k : keys) k.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(ORDER).append(' ').append(BY);
    final int kl = keys.length;
    for(int k = 0; k < kl; k++) sb.append(k == 0 ? " " : SEP).append(keys[k]);
    return sb.toString();
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Key k : keys) if(k.has(flag)) return true;
    return false;
  }

  @Override
  public OrderBy compile(final QueryContext qc, final VarScope sc) throws QueryException {
    for(final Key k : keys) k.compile(qc, sc);
    return this;
  }

  @Override
  public OrderBy optimize(final QueryContext qc, final VarScope scp) {
    return this;
  }

  @Override
  public boolean removable(final Var var) {
    for(final Key k : keys) if(!k.removable(var)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, keys);
  }

  @Override
  public GFLWOR.Clause inline(final QueryContext qc, final VarScope scp, final Var var,
      final Expr ex) throws QueryException {
    for(int r = refs.length; --r >= 0;) {
      if(var.is(refs[r].var)) refs = Array.delete(refs, r);
    }
    return inlineAll(qc, scp, keys, var, ex) ? optimize(qc, scp) : null;
  }

  @Override
  public OrderBy copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new OrderBy(Arr.copyAll(qc, scp, vs, refs), Arr.copyAll(qc, scp, vs, keys), info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, keys);
  }

  @Override
  boolean clean(final IntObjMap<Var> decl, final BitArray used) {
    // delete unused variables
    final int len = refs.length;
    for(int r = refs.length; --r >= 0;) {
      if(!used.get(refs[r].var.id)) refs = Array.delete(refs, r);
    }
    if(refs.length == used.cardinality()) return refs.length != len;

    // add new variables, possible when an expression is inlined below this clause
    outer: for(int id = used.nextSet(0); id >= 0; id = used.nextSet(id + 1)) {
      for(final VarRef ref : refs) if(ref.var.id == id) continue outer;
      refs = Array.add(refs, new VarRef(info, decl.get(id)));
    }

    return true;
  }

  @Override
  boolean skippable(final GFLWOR.Clause cl) {
    return cl instanceof Where;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(keys);
  }

  @Override
  void calcSize(final long[] minMax) {
  }

  @Override
  public int exprSize() {
    int sz = 0;
    for(final Expr e : refs) sz += e.exprSize();
    for(final Expr e : keys) sz += e.exprSize();
    return sz;
  }

  /**
   * Sort key.
   *
   * @author BaseX Team 2005-14, BSD License
   * @author Leo Woerteler
   */
  public static final class Key extends Single {
    /** Descending order flag. */
    final boolean desc;
    /** Position of empty sort keys. */
    final boolean least;
    /** Collation. */
    final Collation coll;

    /**
     * Constructor.
     * @param info input info
     * @param key sort key expression
     * @param desc descending order
     * @param least empty least
     * @param coll collation
     */
    public Key(final InputInfo info, final Expr key, final boolean desc, final boolean least,
        final Collation coll) {
      super(info, key);
      this.desc = desc;
      this.least = least;
      this.coll = coll;
    }

    @Override
    public Key copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
      return new Key(info, expr.copy(qc, scp, vs), desc, least, coll);
    }

    @Override
    public void plan(final FElem plan) {
      final FElem e = planElem(DIR, Token.token(desc ? DESCENDING : ASCENDING),
          Token.token(EMPTYORD), Token.token(least ? LEAST : GREATEST));
      expr.plan(e);
      plan.add(e);
    }

    @Override
    public String toString() {
      final TokenBuilder tb = new TokenBuilder(expr.toString());
      if(desc) tb.add(' ').add(DESCENDING);
      tb.add(' ').add(EMPTYORD).add(' ').add(least ? LEAST : GREATEST);
      if(coll != null) tb.add(' ').add(COLLATION).add(" \"").add(coll.uri()).add('"');
      return tb.toString();
    }

    @Override
    public int exprSize() {
      return expr.exprSize();
    }
  }
}
