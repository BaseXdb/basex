package org.basex.query.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.List;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;


/**
 * FLWOR {@code order by}-expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class OrderBy extends GFLWOR.Clause {
  /** References to the variables to be sorted. */
  VarRef[] refs;
  /** Sort keys. */
  final Key[] keys;

  /**
   * Constructor.
   * @param vs variables to sort
   * @param ks sort keys
   * @param ii input info
   */
  public OrderBy(final VarRef[] vs, final Key[] ks, final InputInfo ii) {
    super(ii);
    refs = vs;
    keys = ks;
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
      public boolean next(final QueryContext ctx) throws QueryException {
        if(tpls == null) sort(ctx);
        if(pos == tpls.length) return false;
        final int p = perm[pos++];
        final Value[] tuple = tpls[p];
        // free the space occupied by the tuple
        tpls[p] = null;
        for(int i = 0; i < refs.length; i++) ctx.set(refs[i].var, tuple[i], info);
        return true;
      }

      /**
       * Caches and sorts all incoming tuples.
       * @param ctx query context
       * @throws QueryException evaluation exception
       */
      private void sort(final QueryContext ctx) throws QueryException {
        // keys are stored at odd positions, values at even ones
        List<Value[]> tuples = new ArrayList<Value[]>();
        while(sub.next(ctx)) {
          final Item[] key = new Item[keys.length];
          for(int i = 0; i < keys.length; i++)
            key[i] = keys[i].expr.item(ctx, keys[i].info);
          tuples.add(key);

          final Value[] vals = new Value[refs.length];
          for(int i = 0; i < refs.length; i++) vals[i] = refs[i].value(ctx);
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
                for(int k = 0; k < keys.length; k++) {
                  final Key or = keys[k];
                  Item m = a[k], n = b[k];
                  if(m == Dbl.NAN || m == Flt.NAN) m = null;
                  if(n == Dbl.NAN || n == Flt.NAN) n = null;
                  if(m != null && n != null && !m.comparable(n))
                    Err.cast(or.info, m.type, n);

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
    for(int i = 0; i < keys.length; i++) sb.append(i == 0 ? " " : SEP).append(keys[i]);
    return sb.toString();
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Key k : keys) if(k.has(flag)) return true;
    return false;
  }

  @Override
  public OrderBy compile(final QueryContext cx, final VarScope sc) throws QueryException {
    for(final Key k : keys) k.compile(cx, sc);
    return this;
  }

  @Override
  public OrderBy optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    return this;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Key k : keys) if(!k.removable(v)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.sum(v, keys);
  }

  @Override
  public GFLWOR.Clause inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    for(int i = refs.length; --i >= 0;)
      if(v.is(refs[i].var)) refs = Array.delete(refs, i);
    return inlineAll(ctx, scp, keys, v, e) ? optimize(ctx, scp) : null;
  }

  @Override
  public OrderBy copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    return new OrderBy(Arr.copyAll(ctx, scp, vs, refs),
        Arr.copyAll(ctx, scp, vs, keys), info);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, keys);
  }

  @Override
  boolean clean(final QueryContext ctx, final IntObjMap<Var> decl, final BitArray used) {
    // delete unused variables
    final int len = refs.length;
    for(int i = refs.length; --i >= 0;)
      if(!used.get(refs[i].var.id)) refs = Array.delete(refs, i);
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
  long calcSize(final long cnt) {
    return cnt;
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
   * @author BaseX Team 2005-13, BSD License
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
     * @param ii input info
     * @param k sort key expression
     * @param dsc descending order
     * @param lst empty least
     * @param cl collation
     */
    public Key(final InputInfo ii, final Expr k, final boolean dsc, final boolean lst,
        final Collation cl) {
      super(ii, k);
      desc = dsc;
      least = lst;
      coll = cl;
    }

    @Override
    public Key copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
      return new Key(info, expr.copy(ctx, scp, vs), desc, least, coll);
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
