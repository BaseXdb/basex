package org.basex.query.pf;


/**
 * NodeSet Constructor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class ScD {
  /** Input column. */
  private final Col ci;
  /** New column. */
  private final Col cn;
  /** Reference column. */
  private final Col cr;
  /** Input size. */
  private final int sz;
  /** Current cursor. */
  private int c;
  /** Last value. */
  private int l = -1;
  /** Uniqueness/Orderedness flag. */
  private boolean u = true;

  /**
   * Constructor.
   * @param i input column
   * @param r reference column
   * @param n new column
   */
  ScD(final Col i, final Col r, final Col n) {
    ci = i; cr = r; cn = n; sz = i.sz;
  }

  /**
   * Checks if more nodes are found.
   * @return result of check
   */
  boolean m() { return c < sz; }

  /**
   * Returns next node.
   * @return next node
   */
  int n() { return ci.r(c++).i(); }

  /**
   * Adds a pre value to the node set. If it equals the last added node, it
   * is ignored; if it is smaller, the {@link #u} flag is remove duplicates
   * and sort data after path traversal.
   * @param p value to be added.
   */
  void a(final int p) {
    if(l != p) { cn.a(new N(p)); cr.a(new I(c - 1)); u &= p > l; l = p; }
  }

  /**
   * Sorts the data and removes duplicates.
   * This sort algorithm is derived from Java's highly optimized
   * Arrays.sort algorithms.
   */
  void finish() {
    if(!u) { s(0, cn.sz); u(); }
  }

  /**
   * Returns duplicate nodes.
   */
  private void u() {
    int j = 0;
    for(int i = 1; i < cn.sz; i++) {
      if(cn.r(j).i() != cn.r(i).i()) {
        cn.r(cn.r(i), ++j);
        cr.r(cr.r(i),  j);
      }
    }
    cn.sz = ++j;
    cr.sz = j;
  }

  /**
   * Sorts the array. This implementation is
   * @param o offset
   * @param p length
   */
  private void s(final int o, final int p) {
    int m = o + (p >> 1);
    if(p > 7) {
      int r = o; int n = o + p - 1;
      if(p > 40) {
        final int s = p >>> 3;
        r = m(r, r + s, r + (s << 1));
        m = m(m - s, m, m + s);
        n = m(n - (s << 1), n - s, n);
      }
      m = m(r, m, n);
    }
    final int v = cn.r(m).i();

    int a = o, b = a, q = o + p - 1, d = q;
    while(true) {
      while(b <= q) {
        final int e = cn.r(b).i() - v;
        if(e > 0) break;
        if(e == 0) w(a++, b);
        b++;
      }
      while(q >= b) {
        final int e = cn.r(q).i() - v;
        if(e < 0) break;
        if(e == 0) w(q, d--);
        q--;
      }
      if(b > q) break;
      w(b++, q--);
    }

    int s;
    final int n = o + p;
    s = Math.min(a - o, b - a); s(o, b - s, s);
    s = Math.min(d - q, n - d - 1); s(b, n - s, s);

    if((s = b - a) > 1) s(o, s);
    if((s = d - q) > 1) s(n - s, s);
  }

  /**
   * Swaps two array values.
   * @param a first offset
   * @param b second offset
   */
  private void w(final int a, final int b) {
    final V p = cr.r(a); cr.r(cr.r(b), a); cr.r(p, b);
    final V q = cn.r(a); cn.r(cn.r(b), a); cn.r(q, b);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param a first offset
   * @param b second offset
   * @param n number of values
   */
  private void s(final int a, final int b, final int n) {
    for(int i = 0; i < n; i++) w(a + i, b + i);
  }

  /**
   * Returns the index of the median of the three indexed integers.
   * @param p first offset
   * @param q second offset
   * @param r thirst offset
   * @return median
   */
  private int m(final int p, final int q, final int r) {
    return cr.r(p).i() < cr.r(q).i() ?
        (cr.r(q).i() < cr.r(r).i() ? q : cr.r(p).i() < cr.r(r).i() ? r : p) :
        (cr.r(q).i() > cr.r(r).i() ? q : cr.r(p).i() > cr.r(r).i() ? r : p);
  }
}
