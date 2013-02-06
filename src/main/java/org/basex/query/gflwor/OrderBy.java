package org.basex.query.gflwor;

import static org.basex.util.Array.*;
import static org.basex.query.QueryText.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.GFLWOR.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;


/**
 * FLWOR {@code order by}-expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class OrderBy extends GFLWOR.Clause {
  /** Variables to sort. */
  Var[] tvars;
  /** Expressions bound to the variables. */
  Expr[] texpr;
  /** Sort keys. */
  final Key[] keys;
  /** Stable sort flag. */
  final boolean stable;

  /**
   * Constructor.
   * @param vs variables to sort
   * @param ks sort keys
   * @param stbl stable sort
   * @param ii input info
   */
  public OrderBy(final VarRef[] vs, final Key[] ks, final boolean stbl,
      final InputInfo ii) {
    super(ii);
    tvars = new Var[vs.length];
    texpr = new Expr[vs.length];
    for(int i = 0; i < vs.length; i++) {
      tvars[i] = vs[i].var;
      texpr[i] = vs[i];
    }
    keys = ks;
    stable = stbl;
  }

  /**
   * Copy constructor.
   * @param vs variables
   * @param es expressions
   * @param ks sort keys
   * @param stbl stable sort
   * @param ii input info
   */
  private OrderBy(final Var[] vs, final Expr[] es, final Key[] ks, final boolean stbl,
      final InputInfo ii) {
    super(ii);
    tvars = vs;
    texpr = es;
    keys = ks;
    stable = stbl;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      /** Sorted output tuples. */
      private Value[][] tpls;
      /** Permutation of the values. */
      private int[] perm;
      /** Current position. */
      int pos;
      @Override
      public boolean next(final QueryContext ctx) throws QueryException {
        if(tpls == null) init(ctx);
        if(pos == tpls.length) return false;
        final int p = perm[pos++];
        final Value[] tuple = tpls[p];
        // free the space occupied by the tuple
        tpls[p] = null;
        for(int i = 0; i < tvars.length; i++) ctx.set(tvars[i], tuple[i], info);
        return true;
      }

      /**
       * Caches and sorts all incoming tuples.
       * @param ctx query context
       * @throws QueryException evaluation exception
       */
      private void init(final QueryContext ctx) throws QueryException {
        // keys are stored at off positions, values ad even ones
        List<Value[]> tuples = new ArrayList<Value[]>();
        while(sub.next(ctx)) {
          final Item[] key = new Item[keys.length];
          for(int i = 0; i < keys.length; i++)
            key[i] = keys[i].expr.item(ctx, keys[i].info);
          tuples.add(key);

          final Value[] vals = new Value[tvars.length];
          for(int i = 0; i < tvars.length; i++) vals[i] = texpr[i].value(ctx);
          tuples.add(vals);
        }

        final int len = tuples.size() >>> 1;
        final Item[][] ks = new Item[len][];
        perm = new int[len];
        tpls = new Value[len][];
        for(int i = 0; i < len; i++) {
          perm[i] = i;
          tpls[i] = tuples.get((i << 1) | 1);
          ks[i] = (Item[]) tuples.get(i << 1);
        }
        // be nice to the garbage collector
        tuples = null;
        sort(ks, 0, len);
      }

      /**
       * Recursively sorts the specified items.
       * The algorithm is derived from {@link Arrays#sort(int[])}.
       * @param start start position
       * @param len end position
       * @throws QueryException query exception
       */
      private void sort(final Item[][] ks, final int start, final int len)
          throws QueryException {
        if(len < 7) {
          // use insertion sort of small arrays
          for(int i = start; i < len + start; i++)
            for(int j = i; j > start && cmp(ks, perm[j - 1], perm[j]) > 0; j--)
              swap(perm, j, j - 1);
          return;
        }

        // find a good pivot element
        int mid = start + (len >> 1);
        if(len > 7) {
          int left = start, right = start + len - 1;
          if(len > 40) {
            final int k = len >>> 3;
            left = median(ks, left, left + k, left + (k << 1));
            mid = median(ks, mid - k, mid, mid + k);
            right = median(ks, right - (k << 1), right - k, right);
          }
          mid = median(ks, left, mid, right);
        }

        final int pivot = perm[mid];

        // partition the values
        int a = start, b = a, c = start + len - 1, d = c;
        while(true) {
          while(b <= c) {
            final int h = cmp(ks, perm[b], pivot);
            if(h > 0) break;
            if(h == 0) swap(perm, a++, b);
            ++b;
          }
          while(c >= b) {
            final int h = cmp(ks, perm[c], pivot);
            if(h < 0) break;
            if(h == 0) swap(perm, c, d--);
            --c;
          }
          if(b > c) break;
          swap(perm, b++, c--);
        }

        // Swap pivot elements back to middle
        int k;
        final int n = start + len;
        k = Math.min(a - start, b - a);
        swap(perm, start, b - k, k);
        k = Math.min(d - c, n - d - 1);
        swap(perm, b, n - k, k);

        // recursively sort non-pivot elements
        if((k = b - a) > 1) sort(ks, start, k);
        if((k = d - c) > 1) sort(ks, n - k, k);
      }

      /**
       * Returns the difference of two entries (part of QuickSort).
       * @return result
       * @throws QueryException query exception
       */
      private int cmp(final Item[][] ks, final int x, final int y) throws QueryException {
        final Item[] a = ks[x], b = ks[y];
        for(int k = 0; k < keys.length; k++) {
          final Key or = keys[k];
          final Item m = a[k] == Dbl.NAN || a[k] == Flt.NAN ? null : a[k],
              n = b[k] == Dbl.NAN || b[k] == Flt.NAN ? null : b[k];
          final int c = m == null ? n == null ? 0 : or.least ? -1 : 1 :
            n == null ? or.least ? 1 : -1 : m.diff(or.info, n);
          if(c != 0) return or.desc ? -c : c;
        }

        // optional stable sorting
        return stable ? x - y : 0;
      }

      /**
       * Returns the index of the median of the three indexed integers.
       * @param ks key array
       * @param a first offset
       * @param b second offset
       * @param c thirst offset
       * @return median
       * @throws QueryException query exception
       */
      private int median(final Item[][] ks, final int a, final int b, final int c)
          throws QueryException {
        final int ka = perm[a], kb = perm[b], kc = perm[c];
        return cmp(ks, ka, kb) < 0
            ? cmp(ks, kb, kc) < 0 ? b : cmp(ks, ka, kc) < 0 ? c : a
            : cmp(ks, kb, kc) > 0 ? b : cmp(ks, ka, kc) > 0 ? c : a;
      }
    };
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem(Token.token(STABLE), Token.token(stable));
    for(final Key k : keys) k.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(ORDER).append(' ').append(BY);
    for(int i = 0; i < keys.length; i++) sb.append(i == 0 ? " " : SEP).append(keys[i]);
    if(stable) sb.append(' ').append(STABLE);
    return sb.toString();
  }

  @Override
  public boolean uses(final Use u) {
    if(u == Use.VAR) return true;
    for(final Key k : keys) if(k.uses(u)) return true;
    return false;
  }

  @Override
  public OrderBy compile(final QueryContext cx, final VarScope sc) throws QueryException {
    for(final Key k : keys) k.compile(cx, sc);
    return this;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Key k : keys) if(!k.removable(v)) return false;
    return true;
  }

  @Override
  public OrderBy remove(final Var v) {
    for(final Key k : keys) k.remove(v);
    return this;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.maximum(v, texpr) != VarUsage.NEVER
        ? VarUsage.MORE_THAN_ONCE : VarUsage.sum(v, keys);
  }

  @Override
  public GFLWOR.Clause inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    return inlineAll(ctx, scp, keys, v, e) | inlineAll(ctx, scp, texpr, v, e)
        ? optimize(ctx, scp) : null;
  }

  @Override
  public OrderBy copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    final Var[] tv = tvars.clone();
    for(int i = 0; i < tv.length; i++) {
      final Var v = tv[i], cpy = vs.get(v.id);
      if(cpy != null) tv[i] = cpy;
    }
    return new OrderBy(tv, Arr.copyAll(ctx, scp, vs, texpr),
        Arr.copyAll(ctx, scp, vs, keys), stable, info);
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    return visitor.visitAll(texpr) && visitor.visitAll(keys);
  }

  @Override
  boolean clean(final QueryContext ctx, final BitArray used) {
    final int len = tvars.length;
    for(int i = 0; i < tvars.length; i++)
      if(!used.get(tvars[i].id)) {
        texpr = Array.delete(texpr, i);
        tvars = Array.delete(tvars, i--);
      }
    return tvars.length < len;
  }

  @Override
  boolean skippable(final GFLWOR.Clause cl) {
    return cl instanceof Where;
  }

  @Override
  public void checkUp() throws QueryException {
    for(final Key key : keys) key.checkUp();
  }

  @Override
  public boolean databases(final StringList db) {
    for(final Key key : keys) if(!key.databases(db)) return false;
    return true;
  }

  @Override
  long calcSize(final long cnt) {
    return cnt;
  }

  /**
   * Sort key.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Leo Woerteler
   */
  public static class Key extends Single {
    /** Descending order flag. */
    final boolean desc;
    /** Position of empty sort keys. */
    final boolean least;

    /**
     * Constructor.
     * @param ii input info
     * @param k sort key expression
     * @param dsc descending order
     * @param lst empty least
     */
    public Key(final InputInfo ii, final Expr k, final boolean dsc, final boolean lst) {
      super(ii, k);
      desc = dsc;
      least = lst;
    }

    @Override
    public Key copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
      return new Key(info, expr.copy(ctx, scp, vs), desc, least);
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
      final StringBuilder sb = new StringBuilder(expr.toString());
      if(desc) sb.append(' ').append(DESCENDING);
      sb.append(' ').append(EMPTYORD).append(' ').append(least ? LEAST : GREATEST);
      return sb.toString();
    }
  }
}
