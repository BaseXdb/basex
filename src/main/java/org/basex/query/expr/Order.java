package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import java.util.Arrays;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.ValueList;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Order by expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Order extends ParseExpr {
  /** Sort list. */
  final OrderBy[] ob;
  /** Values to be sorted. */
  ValueList vl;

 /**
   * Constructor.
   * @param ii input info
   * @param o order by expressions
   */
  public Order(final InputInfo ii, final OrderBy[] o) {
    super(ii);
    ob = o;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final OrderBy o : ob) o.comp(ctx);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      // cast to int, as expected number of results will be small enough
      final int e = vl.size();
      int[] order;
      Iter ir;
      int p = -1;

      @Override
      public Item next() throws QueryException {
        if(order == null) {
          // enumerate sort array and sort entries
          order = new int[e];
          for(int i = 0; i < e; ++i) order[i] = i;
          sort(order, 0, e);
          for(final OrderBy o : ob) o.reset();
        }

        while(true) {
          if(ir != null) {
            final Item i = ir.next();
            if(i != null) return i;
            ir = null;
          } else {
            if(++p == e) return null;
            ir = vl.get(order[p]).iter(ctx);
          }
        }
      }
    };
  }

  /**
   * Adds the items to be sorted.
   * @param ctx query context
   * @throws QueryException query exception
   */
  void add(final QueryContext ctx) throws QueryException {
    for(final OrderBy o : ob) o.add(ctx);
  }

  @Override
  public boolean uses(final Use u) {
    for(final OrderBy o : ob) if(o.uses(u)) return true;
    return false;
  }

  @Override
  public Order remove(final Var v) {
    for(int o = 0; o < ob.length; ++o) ob[o] = ob[o].remove(v);
    return this;
  }

  /**
   * Recursively sorts the specified items.
   * Sorting is derived from Java's sort algorithms in the {Arrays} class.
   * @param o order array
   * @param s start position
   * @param e end position
   * @throws QueryException query exception
   */
  void sort(final int[] o, final int s, final int e) throws QueryException {
    if(e < 7) {
      for(int i = s; i < e + s; ++i)
        for(int j = i; j > s && d(o, j - 1, j) > 0; j--) s(o, j, j - 1);
      return;
    }

    int m = s + (e >> 1);
    if(e > 7) {
      int l = s;
      int n = s + e - 1;
      if(e > 40) {
        final int k = e >>> 3;
        l = m(o, l, l + k, l + (k << 1));
        m = m(o, m - k, m, m + k);
        n = m(o, n - (k << 1), n - k, n);
      }
      m = m(o, l, m, n);
    }

    final Item[] im = new Item[ob.length];
    for(int k = 0; k < ob.length; ++k) im[k] = ob[k].item(o[m]);

    int a = s, b = a, c = s + e - 1, d = c;
    while(true) {
      while(b <= c) {
        final int h = d(o, b, im);
        if(h > 0) break;
        if(h == 0) s(o, a++, b);
        b++;
      }
      while(c >= b) {
        final int h = d(o, c, im);
        if(h < 0) break;
        if(h == 0) s(o, c, d--);
        c--;
      }
      if(b > c) break;
      s(o, b++, c--);
    }

    int k;
    final int n = s + e;
    k = Math.min(a - s, b - a);
    s(o, s, b - k, k);
    k = Math.min(d - c, n - d - 1);
    s(o, b, n - k, k);

    if((k = b - a) > 1) sort(o, s, k);
    if((k = d - c) > 1) sort(o, n - k, k);
  }

  /**
   * Returns the difference of two entries (part of QuickSort).
   * @param o order array
   * @param a first position
   * @param it second item
   * @return result
   * @throws QueryException query exception
   */
  private int d(final int[] o, final int a, final Item[] it)
      throws QueryException {

    for(int k = 0; k < ob.length; ++k) {
      final OrderBy or = ob[k];
      final Item m = or.item(o[a]);
      final Item n = it[k];
      final boolean x = m == null;
      final boolean y = n == null;
      final int c = x ? y ? 0 : or.lst ? -1 : 1 : y ? or.lst ? 1 : -1 :
        m.diff(input, n);
      if(c != 0) return or.desc ? -c : c;
    }
    return 0;
  }

  /**
   * Returns the difference of two entries (part of QuickSort).
   * @param o order array
   * @param a first position
   * @param b second position
   * @return result
   * @throws QueryException query exception
   */
  private int d(final int[] o, final int a, final int b) throws QueryException {
    for(final OrderBy l : ob) {
      final Item m = l.item(o[a]);
      final Item n = l.item(o[b]);
      final boolean x = m == null;
      final boolean y = n == null;
      final int c = x ? y ? 0 : l.lst ? -1 : 1 : y ? l.lst ? 1 : -1 :
        m.diff(input, n);
      if(c != 0) return l.desc ? -c : c;
    }
    return 0;
  }

  /**
   * Returns the index of the median of the three indexed integers.
   * @param o order array
   * @param a first offset
   * @param b second offset
   * @param c thirst offset
   * @return median
   * @throws QueryException query exception
   */
  private int m(final int[] o, final int a, final int b, final int c)
      throws QueryException {
    return d(o, a, b) < 0 ?
        (d(o, b, c) < 0 ? b : d(o, a, c) < 0 ? c : a) :
        d(o, b, c) > 0 ? b : d(o, a, c) > 0 ? c : a;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param o order array
   * @param a first offset
   * @param b second offset
   * @param n number of values
   */
  private void s(final int[] o, final int a, final int b, final int n) {
    for(int i = 0; i < n; ++i) s(o, a + i, b + i);
  }

  /**
   * Swaps two entries (part of QuickSort).
   * @param o order array
   * @param a first position
   * @param b second position
   */
  private void s(final int[] o, final int a, final int b) {
    final int c = o[a];
    o[a] = o[b];
    o[b] = c;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(int o = 0; o != ob.length - 1; ++o) ob[o].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return new TokenBuilder(' ' + ORDER + ' ' + BY + ' ').add(
        Arrays.copyOf(ob, ob.length - 1), ", ").toString();
  }
}
