package org.basex.query.flwor;

import static org.basex.query.QueryText.*;
import static org.basex.util.Array.*;

import java.io.IOException;
import java.util.Arrays;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.ValueList;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.list.ObjList;

/**
 * Order by expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Order extends ParseExpr {
  /** Sort list. */
  final OrderBy[] ob;

  /** Keys to sort by. */
  private ObjList<Item[]> keys;
  /** Values to sort. */
  private ValueList values;

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
    return new OrderedIter(keys, values);
  }

  /**
   * Sets the key and value lists for this Order instance.
   * @param ks key list
   * @param vs value list
   * @return reference to this object for convenience
   */
  Order set(final ObjList<Item[]> ks, final ValueList vs) {
    keys = ks;
    values = vs;
    return this;
  }

  /**
   * Adds the items to be sorted.
   * @param ctx query context
   * @param e value to add
   * @param ks key list
   * @param vs value list
   * @throws QueryException query exception
   */
  void add(final QueryContext ctx, final Expr e, final ObjList<Item[]> ks,
      final ValueList vs) throws QueryException {
    final Item[] k = new Item[ob.length];
    for(int o = k.length; o-- > 0;) k[o] = ob[o].key(ctx, ks.size());
    ks.add(k);
    vs.add(ctx.value(e));
  }

  @Override
  public boolean uses(final Use u) {
    for(final OrderBy o : ob) if(o.uses(u)) return true;
    return false;
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final OrderBy o : ob) c += o.count(v);
    return c;
  }

  @Override
  public boolean removable(final Var v) {
    for(final OrderBy o : ob) if(!o.removable(v)) return false;
    return true;
  }

  @Override
  public Order remove(final Var v) {
    for(int o = 0; o < ob.length; ++o) ob[o] = ob[o].remove(v);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(int o = 0; o != ob.length - 1; ++o) ob[o].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return new TokenBuilder(' ' + ORDER + ' ' + BY + ' ').addSep(
        Arrays.copyOf(ob, ob.length - 1), SEP).toString();
  }

  /**
   * An ordered iterator.
   * @author Leo Woerteler
   */
  class OrderedIter extends Iter {
    /** Sort keys. */
    ObjList<Item[]> kl;
    /** Values to sort. */
    ValueList vl;
    /** End position. */
    int end;
    /** Current position. */
    int pos = -1;
    /** Order array. */
    int[] order;
    /** Iterator. */
    Iter ir;

    /**
     * Constructor.
     * @param ks sort keys
     * @param vs values
     */
    public OrderedIter(final ObjList<Item[]> ks, final ValueList vs) {
      kl = ks;
      vl = vs;
    }

    @Override
    public Item next() throws QueryException {
      if(order == null) {
        // enumerate sort array and sort entries
        end = vl.size();
        order = new int[end];
        for(int i = 0; i < end; ++i) order[i] = i;
        sort(order, 0, end);
      }

      while(true) {
        if(ir != null) {
          final Item i = ir.next();
          if(i != null) return i;
          ir = null;
        } else {
          if(++pos == end) return null;
          ir = vl.get(order[pos]).iter();
        }
      }
    }

    /**
     * Recursively sorts the specified items.
     * The algorithm is derived from {@link Arrays#sort(int[])}.
     * @param o order array
     * @param s start position
     * @param e end position
     * @throws QueryException query exception
     */
    void sort(final int[] o, final int s, final int e) throws QueryException {
      if(e < 7) {
        for(int i = s; i < e + s; ++i)
          for(int j = i; j > s && d(o, j - 1, j) > 0; j--) swap(o, j, j - 1);
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

      final Item[] im = kl.get(o[m]);

      int a = s, b = a, c = s + e - 1, d = c;
      while(true) {
        while(b <= c) {
          final int h = d(kl.get(o[b]), im);
          if(h > 0) break;
          if(h == 0) swap(o, a++, b);
          ++b;
        }
        while(c >= b) {
          final int h = d(kl.get(o[c]), im);
          if(h < 0) break;
          if(h == 0) swap(o, c, d--);
          --c;
        }
        if(b > c) break;
        swap(o, b++, c--);
      }

      int k;
      final int n = s + e;
      k = Math.min(a - s, b - a);
      swap(o, s, b - k, k);
      k = Math.min(d - c, n - d - 1);
      swap(o, b, n - k, k);

      if((k = b - a) > 1) sort(o, s, k);
      if((k = d - c) > 1) sort(o, n - k, k);
    }

    /**
     * Returns the difference of two entries (part of QuickSort).
     * @param sa sort keys of first item
     * @param sb sort keys of second item
     * @return result
     * @throws QueryException query exception
     */
    private int d(final Item[] sa, final Item[] sb) throws QueryException {
      for(int k = 0; k < ob.length; ++k) {
        final OrderBy or = ob[k];
        final Item m = sa[k], n = sb[k];
        final int c = m == null ? n == null ? 0 : or.lst ? -1 : 1 :
          n == null ? or.lst ? 1 : -1 : m.diff(input, n);
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
    private int d(final int[] o, final int a, final int b)
        throws QueryException {
      return d(kl.get(o[a]), kl.get(o[b]));
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
      final Item[] ka = kl.get(o[a]), kb = kl.get(o[b]), kc = kl.get(o[c]);
      return d(ka, kb) < 0 ? d(kb, kc) < 0 ? b : d(ka, kc) < 0 ? c : a :
          d(kb, kc) > 0 ? b : d(ka, kc) > 0 ? c : a;
    }
  }
}

