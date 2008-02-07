package org.basex.query.pf;

import org.basex.BaseX;
import org.basex.core.proc.Create;
import org.basex.core.proc.Open;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import static org.basex.query.pf.PFT.*;

/**
 * XQuery operators.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Alexander Holupirek
 */
final class Ach extends Opr {
  @Override
  void e() throws QueryException {
    // adopt table from argument
    tbl.a(arg[0].tbl);

    // add new column
    final int i = q(XPCOL)[0];
    final int t = VB.type(pf.t(XPTYPE, i));
    final Col cn = a(pf.a(NAME, i), t);
    final V v = VB.v(t(XPVAL, i), t);

    // add constant value
    final int s = tbl.c(0).sz;
    for(int r = 0; r != s; r++) cn.a(v);
  }
}

/** XQuery operator. */
final class All extends Opr {
  @Override
  void e() {
    BaseX.notimplemented();
  }
}

/** XQuery operator. */
final class And extends Opr {
  @Override
  void e() throws QueryException {
    tbl.a(arg[0].tbl);

    // get arguments and new column
    final Col c1 = c(XPPOS1);
    final Col c2 = c(XPPOS2);
    final Col cn = a(n(XPNEW), BLN);

    cn.a(B.v(c1.r(0).b() && c2.r(0).b()));
  }
}

/** XQuery operator. */
final class Atr extends Opr {
  @Override
  void e() {
    BaseX.notimplemented();
  }
}

/** XQuery operator. */
final class Avg extends Opr {
  @Override
  void e() throws QueryException {
    final Col co = c(XPITEM, arg[0].tbl);
    final Col cn = tbl.s(co);

    // calculate sum - assuming that result is always of kind double
    final int s = co.sz;
    double n = 0;
    for(int r = 0; r < s; r++) n += co.r(r).d();
    cn.a(new D(n / s));
  }
}

/** XQuery operator. */
final class Cnt extends Opr {
  @Override
  void e() throws QueryException {
    // add number of rows of first input column
    c(XPNEW).a(new I(arg[0].tbl.c(0).sz));
  }
}

/** XQuery operator. */
final class Crs extends Opr {
  @Override
  void e() {
    final Tbl t1 = arg[0].tbl;
    final Tbl t2 = arg[1].tbl;
    final int[] m1 = add(t1);
    final int[] m2 = add(t2);

    final int s1 = arg[0].tbl.c(0).sz;
    final int s2 = arg[1].tbl.c(0).sz;
    for(int r1 = 0; r1 < s1; r1++) {
      for(int r2 = 0; r2 < s2; r2++) {
        for(int c = 0; c < t1.size; c++) tbl.c(m1[c]).a(t1.c(c).r(r1));
        for(int c = 0; c < t2.size; c++) tbl.c(m2[c]).a(t2.c(c).r(r2));
      }
    }
  }

  /**
   * Creates new columns and returns the column assignment.
   * @param tb tbl to be added
   * @return column assignment
   */
  private int[] add(final Tbl tb) {
    final int[] m = new int[tb.size];
    for(int t = 0; t < tb.size; t++) {
      m[t] = tbl.size;
      tbl.s(tb.c(t));
    }
    return m;
  }
}

/** XQuery operator. */
final class Cst extends Opr {
  @Override
  void e() throws QueryException {
    // attach table
    tbl.a(arg[0].tbl);

    // find input and output columns
    final Col co = c(XPOLD);
    final int nt = VB.type(t(XPCTYPE));
    final Col cn = a(n(XPNEW), nt);

    // cast all values... no efficient solution in fact
    for(int r = 0; r < co.sz; r++) cn.a(VB.v(co.r(r).s(), nt));
  }
}

/** XQuery operator. */
final class Dif extends Opr {
  @Override
  void e() {
    final Tbl t1 = arg[0].tbl;
    final Tbl t2 = arg[1].tbl;
    tbl.s(t1);

    final int r = t1.c(0).sz;
    // scan all rows (slow.. is input sorted?)
    for(int r1 = 0; r1 < r; r1++) {
      // row is unique?
      if(un(t1, t2, r1)) {
        for(int c = 0; c < t1.size; c++) tbl.c(c).a(t1.c(c).r(r1));
      }
    }
  }

  /**
   * Checks if the row of the first relation does not occur in the second one.
   * @param t1 first table
   * @param t2 second table
   * @param r1 row position
   * @return if row is unique
   */
  private boolean un(final Tbl t1, final Tbl t2, final int r1) {
    final int r = t2.c(0).sz;
    for(int r2 = 0; r2 < r; r2++) {
      if(eq(t1, t2, r1, r2)) return false;
    }
    return true;
  }

  /**
   * Compares a column for equality.
   * @param t1 first table
   * @param t2 second table
   * @param r1 first row position
   * @param r2 second row position
   * @return if the lines are equal
   */
  private boolean eq(final Tbl t1, final Tbl t2, final int r1, final int r2) {
    for(int c = 0; c < t1.size; c++) {
      if(!t1.c(c).r(r1).eq(t2.c(c).r(r2))) return false;
    }
    return true;
  }
}

/** XQuery operator. */
final class DoA extends Opr {
  @Override
  void e() throws QueryException {
    // attach table and data reference
    tbl.addOLD(arg[1].tbl);

    if(1 == 1) BaseX.notimplemented();

    final Data data = pf.f();

    // ...buggy?
    final Col c1 = c(XPOLD);
    final Col c2 = c(XPNEW);
    for(int r = 0; r < c1.sz; r++) c2.a(new S(data.atom(c1.r(r).i())));
  }
}

/** XQuery operator. */
final class Doc extends Opr {
  @Override
  void e() throws QueryException {
    final String fn = Token.string(c(XPITEM, arg[0].tbl).r(0).s());

    // no main memory mode - check database instance on disk
    final String db = Create.chopPath(fn);
    Data d = Open.open(db);

    // if no instance exists, create new database
    if(d == null) {
      try {
        d = Create.xml(db, fn);
      } catch(final Exception e) {
        throw new QueryException(e.toString());
      }
    }
    pf.a(d);

    // set root node
    a(n(XPITER), INT);
    a(n(XPITEM), PRE);
    tbl.c(1).a(N.Z);
  }
}

/** XQuery operator. */
final class Dst extends Opr {
  @Override
  void e() {
    final Tbl t1 = arg[0].tbl;
    tbl.s(t1);

    // perform selection
    final int s = t1.c(0).sz;
    if(s == 0) return;

    // add first row
    for(int c = 0; c < tbl.size; c++) tbl.c(c).a(t1.c(c).r(0));

    // compare subsequent rows
    final int cols = tbl.size;
    for(int r = 1; r < s; r++) {
      // compare tuples
      boolean eq = true;
      for(int c = 0; c < cols; c++) eq &= t1.c(c).r(r).eq(t1.c(c).r(r - 1));

      // tuples are distinct.. add row
      if(!eq) {
        for(int c = 0; c < cols; c++) tbl.c(c).a(t1.c(c).r(r));
      }
    }
  }
}

/** XQuery operator. */
final class Elm extends Opr {
  @Override
  void e() throws QueryException {
    tbl = arg[1].tbl;

    // get tag from argument
    final byte[] tag = c(XPITEM, arg[1].tbl).r(0).s();

    final Opr op = arg[1].arg[1];
    final Tbl t1 = op != null ? op.tbl : null;
    final int size = t1 != null ? t1.c(0).sz : 0;

    final Frag f = new Frag();
    f.addElem(f.addTag(tag), 1, 1, size + 1, Data.ELEM);
    if(t1 != null) f.copy(pf.f());
    pf.a(f);

    // set 0 as root node
    c(XPITEM).r(N.Z, 0);
  }
}

/** XQuery operator. */
final class EqJ extends Opr {
  @Override
  void e() throws QueryException {
    final Tbl t1 = arg[0].tbl;
    final Tbl t2 = arg[1].tbl;
    final int[] m1 = add(t1);
    final int[] m2 = add(t2);

    // determine columns to compare
    final Col c1 = c(XPPOS1, t1);
    final Col c2 = c(XPPOS2, t2);

    // compare columns
    final int s = c1.sz;
    for(int r = 0; r < s; r++) {
      if(eq(c1, c2, r)) {
        for(int c = 0; c < t1.size; c++) tbl.c(m1[c]).a(t1.c(c).r(r));
        for(int c = 0; c < t2.size; c++) tbl.c(m2[c]).a(t2.c(c).r(r));
      }
    }
  }

  /**
   * Checks if the second column contains an equil entry.
   * @param c1 first column
   * @param c2 second column
   * @param r1 counter of first row
   * @return result of check
   */
  private boolean eq(final Col c1, final Col c2, final int r1) {
    for(int r2 = 0; r2 < c2.sz; r2++) if(c1.r(r1).eq(c2.r(r2))) return true;
    return false;
  }


  /**
   * Creates new columns and returns the column assignment.
   * @param tb tbl to be added
   * @return column assignment
   */
  private int[] add(final Tbl tb) {
    final int[] m = new int[tb.size];
    for(int t = 0; t < tb.size; t++) {
      m[t] = tbl.size;
      tbl.s(tb.c(t));
    }
    return m;
  }
}

/** XQuery operator. */
final class Equ extends Opr {
  @Override
  void e() throws QueryException {
    tbl.a(arg[0].tbl);

    // get arguments
    final Col c1 = c(XPPOS1);
    final Col c2 = c(XPPOS2);
    final Col cn = a(n(XPNEW), BLN);

    for(int r = 0; r < c1.sz; r++) cn.a(B.v(c1.r(r).eq(c2.r(r))));
  }
}

/** XQuery operator. */
final class Err extends Opr {
  @Override
  void e() {
    BaseX.notimplemented();
  }
}

/** XQuery operator. */
final class FrE extends Opr {
  @Override
  void e() {
  }
}

/** XQuery operator. */
final class Frg extends Opr {
  @Override
  void e() {
  }
}

/** XQuery operator. */
final class FrU extends Opr {
  @Override
  void e() {
  }
}

/** XQuery operator. */
final class Fun extends Opr {
  @Override
  void e() throws QueryException {
    // attach table
    tbl.a(arg[0].tbl);

    // get arguments and new column
    final Col c1 = c(XPPOS1);
    final Col c2 = q(XPPOS2).length != 0 ? c(XPPOS2) : c1;
    final Col cn = a(n(XPNEW), c1.tp);

    // find operation to be performed and get function
    final Fnc f = Fnc.f(t(XPKIND));

    // evaluate function
    for(int r = 0; r < c1.sz; r++) cn.a(f.e(c1.r(r), c2.r(r)));
  }
}

/** XQuery operator. */
final class Grt extends Opr {
  @Override
  void e() throws QueryException {
    tbl.a(arg[0].tbl);

    // get arguments
    final Col c1 = c(XPPOS1);
    final Col c2 = c(XPPOS2);
    final Col cn = a(n(XPNEW), BLN);
    for(int r = 0; r < c1.sz; r++) cn.a(B.v(c1.r(r).df(c2.r(r)) > 0));
  }
}

/** XQuery operator. */
final class ISc extends Opr {
  @Override
  void e() {
    BaseX.notimplemented();
  }
}

/** XQuery operator. */
final class Max extends Opr {
  @Override
  void e() throws QueryException {
    final Col co = c(XPITEM, arg[0].tbl);
    final Col cn = tbl.s(co);

    final int s = co.sz;
    if(cn.tp == INT) {
      int n = 0;
      for(int r = 0; r < s; r++) n = Math.max(co.r(r).i(), n);
      cn.a(new I(n));
    } else {
      double n = 0;
      for(int r = 0; r < s; r++) n = Math.max(co.r(r).d(), n);
      cn.a(new D(n));
    }
  }
}

/** XQuery operator. */
final class Min extends Opr {
  @Override
  void e() throws QueryException {
    final Col co = c(XPITEM, arg[0].tbl);
    final Col cn = tbl.s(co);

    final int s = co.sz;
    if(cn.tp == INT) {
      int n = 0;
      for(int r = 0; r < s; r++) n = Math.min(co.r(r).i(), n);
      cn.a(new I(n));
    } else {
      double n = 0;
      for(int r = 0; r < s; r++) n = Math.min(co.r(r).d(), n);
      cn.a(new D(n));
    }
  }
}

/** XQuery operator. */
final class Mrg extends Opr {
  @Override
  void e() {
    // adopt table
    tbl = arg[1].tbl;
    //data = arg[0].data;
  }
}

/** XQuery operator. */
final class Not extends Opr {
  @Override
  void e() throws QueryException {
    tbl.a(arg[0].tbl);
    final Col co = c(XPOLD);
    final Col cn = a(n(XPNEW), co.tp);

    for(int r = 0; r < co.sz; r++) cn.a(co.r(r) == B.T ? B.F : B.T);
  }
}

/** XQuery operator. */
final class Num extends Opr {
  @Override
  void e() throws QueryException {
    tbl.a(arg[0].tbl);
    final Col cn = a(n(XPNEW), INT);

    // determine existing row
    final int s = arg[0].tbl.c(0).sz;
    for(int i = 0; i < s; i++) cn.a(new I(i));
  }
}

/** XQuery operator. */
final class Orr extends Opr {
  @Override
  void e() throws QueryException {
    tbl.a(arg[0].tbl);

    // get arguments and new column
    final Col c1 = c(XPPOS1);
    final Col c2 = c(XPPOS2);
    final Col cn = a(n(XPNEW), BLN);

    cn.a(B.v(c1.r(0).b() || c2.r(0).b()));
  }
}

/** XQuery operator. */
final class Prj extends Opr {
  @Override
  void e() throws QueryException {
    final Tbl to = arg[0].tbl;

    // project columns
    for(final int i : q(XPCOL)) {
      final int n = pf.a(NAME, i);
      final byte[] att = pf.v(OLDNAME, i);
      final int o = att != null ? pf.i(att) : n;
      for(int p = 0; p < to.size; p++) {
        final Col co = to.c(p);
        if(co.nm == o) a(n, co.tp).r(co);
      }
    }
  }
}

/** XQuery operator. */
final class RoN extends Opr {
  @Override
  void e() throws QueryException {
    tbl.a(arg[0].tbl);

    // get sort columns
    final int[] i = q(XPPOS);
    Col[] co = new Col[i.length];
    for(int c = 0; c < i.length; c++) co[c] = tbl.c(tbl.p(pf.a(NAME, i[c])));

    // get partition column and add it as sort argument
    final Col cp = c(XPPART, tbl);
    if(cp != null) {
      final Col[] t = new Col[i.length + 1];
      System.arraycopy(co, 0, t, 1, i.length);
      t[0] = cp;
      co = t;
    }

    // sort data
    s(co, 0, 0, co[0].sz);

    // number the new column
    final int s = co[0].sz;
    final Col cn = a(n(XPNEW), INT);
    for(int r = 1; r <= s; r++) cn.a(new I(r));
  }

  /**
   * Sorts the data by the specified column.
   * @param c column to be sorted
   * @param p column position
   * @param f first value
   * @param l last value + 1
   */
  private void s(final Col[] c, final int p, final int f, final int l) {
    if(!e(c[p], f, l)) {
      for(int i = f; i < l; i++) {
        for(int j = i + 1; j < l; j++) {
          if(c[p].r(i).df(c[p].r(j)) > 0) {
            for(int k = 0; k < tbl.size; k++) {
              final Col t = tbl.c(k);
              final V v = t.r(i);
              t.r(t.r(j), i);
              t.r(v, j);
            }
          }
        }
      }
    }
    if(p + 1 == c.length) return;

    // find and sort sub regions
    int e = 0;
    while(e < l) {
      final int s = e;
      final V v = c[p].r(s);
      while(++e < l && v.eq(c[p].r(e)));
      s(c, p + 1, s, e);
    }
  }

  /**
   * Checks if all values are already sorted.
   * @param c column to be sorted
   * @param f first value
   * @param l last value + 1
   * @return true if all values are sorted
   */
  private boolean e(final Col c, final int f, final int l) {
    for(int i = f + 1; i < l; i++) if(c.r(i - 1).df(c.r(i)) > 0) return false;
    return true;
  }
}

/** XQuery operator. */
final class Roo extends Opr {
  @Override
  void e() {
    tbl.a(arg[0].tbl);
  }
}

/** XQuery operator. */
class ScJ extends Opr {
  @Override
  void e() throws QueryException {
    tbl.s(arg[1].tbl);

    final Data dt = pf.f();

    // parse location step
    final int stp = q(XPSTEP)[0];
    final ScA axis = ScA.get(pf.v(AXIS, stp));
    final ScT test = new ScT(dt, pf.v(TYPE, stp));

    // get input, reference and new table
    final Col co = c(XPITEM, arg[1].tbl);
    final Col cr = c(XPITER, arg[1].tbl);
    final Col cn = c(XPITEM, tbl);

    final ScD d = new ScD(co, cr, cn);
    axis.e(dt, test, d);
    d.finish();
  }
}

/** XQuery operator. */
final class Sel extends Opr {
  @Override
  void e() throws QueryException {
    final Tbl t1 = arg[0].tbl;
    tbl.s(t1);

    // get new column
    final Col co = c(XPOLD, arg[0].tbl);

    // perform selection - old column is modified...
    for(int r = 0; r < co.sz; r++) {
      if(co.r(r) == B.T) {
        for(int c = 0; c < tbl.size; c++) tbl.c(c).a(t1.c(c).r(r));
      }
    }
  }
}

/** XQuery operator. */
final class Seq extends Opr {
  @Override
  void e() {
    BaseX.notimplemented();
  }
}

/** XQuery operator. */
final class Ser extends Opr {
  @Override
  void e() {
    // adopts table from argument
    tbl = arg[1].tbl;
  }
}

/** XQuery operator. */
final class SmJ extends Opr {
  @Override
  void e() throws QueryException {
    // adopt schema of left relation
    tbl.s(arg[0].tbl);

    // determine columns to compare
    final Col c1 = c(XPPOS1, arg[0].tbl);
    final Col c2 = c(XPPOS2, arg[1].tbl);

    // compare columns
    final int s1 = c1.sz;
    for(int r1 = 0; r1 < s1; r1++) {
      if(eq(c1, c2, r1)) {
        for(int c = 0; c < tbl.size; c++) tbl.c(c).a(arg[0].tbl.c(c).r(r1));
      }
    }
  }

  /**
   * Checks if the second column contains an equil entry.
   * @param c1 first column
   * @param c2 second column
   * @param r1 counter of first row
   * @return result of check
   */
  private boolean eq(final Col c1, final Col c2, final int r1) {
    for(int r2 = 0; r2 < c2.sz; r2++) if(c1.r(r1).eq(c2.r(r2))) return true;
    return false;
  }

}

/** XQuery operator. */
final class StJ extends Opr {
  @Override
  void e() throws QueryException {
    // attach table
    tbl.addOLD(arg[1].tbl);

    final Col cr = c(XPITEM, arg[0].tbl);

    final TokenBuilder t = new TokenBuilder();
    for(int r = 0; r < cr.sz; r++) t.add(cr.r(r).s());
    c(XPITEM).a(new S(t.finish()));
  }
}

/** XQuery operator. */
final class StV extends Opr {
  @Override
  void e() throws QueryException {
    // attach table
    tbl.addOLD(arg[1].tbl);

    if(1 == 1) BaseX.notimplemented();

    final Data data = pf.f();

    final Col cr = c(XPOLD);
    final Col cn = c(XPNEW);

    for(int r = 0; r < cr.sz; r++) {
      cn.a(new S(data.atom(cr.r(r).i())));
    }
    //c(XPNEW, tbl).a(new S(t.finish()));
  }
}

/** XQuery operator. */
final class Sum extends Opr {
  @Override
  void e() throws QueryException {
    final Col co = c(XPITEM, arg[0].tbl);
    final Col cn = tbl.s(co);

    final int s = co.sz;
    if(cn.tp == INT) {
      int n = 0;
      for(int r = 0; r < s; r++) n += co.r(r).i();
      cn.a(new I(n));
    } else {
      double n = 0;
      for(int r = 0; r < s; r++) n += co.r(r).d();
      cn.a(new D(n));
    }
  }
}

/** XQuery operator. */
final class Tab extends Opr {
  @Override
  void e() throws QueryException {
    // adds values to the table
    for(final int i : q(XPCOL)) {
      final int t = VB.type(pf.t(XPTYPE, i));
      a(pf.a(NAME, i), t).a(VB.v(t(XPVAL, i), t));
    }
  }
}

/** XQuery operator. */
final class Tag extends Opr {
  @Override
  void e() {
    // adopt table
    tbl = arg[0].tbl;
  }
}

/** XQuery operator. */
final class TAs extends Opr {
  @Override
  void e() {
    // to be implemented?
    tbl = arg[0].tbl;
  }
}

/** XQuery operator. */
final class TbE extends Opr {
  @Override
  void e() {
    // nothing to do...
  }
}

/** XQuery operator. */
final class Trc extends Opr {
  @Override
  void e() {
    BaseX.notimplemented();
  }
}

/** XQuery operator. */
final class TrM extends Opr {
  @Override
  void e() {
    BaseX.notimplemented();
  }
}

/** XQuery operator. */
final class TrP extends Opr {
  @Override
  void e() {
    BaseX.notimplemented();
  }
}

/** XQuery operator. */
final class Txt extends Opr {
  @Override
  void e() throws QueryException {
    tbl = arg[0].tbl;

    final Frag f = new Frag();
    f.addText(c(XPOLD).r(0).s(), 1, Data.TEXT);
    pf.a(f);

    // replace element with 0 as root node
    a(n(XPNEW), PRE).a(N.Z);
  }
}

/** XQuery operator. */
final class Typ extends Opr {
  @Override
  void e() throws QueryException {
    // attach table
    tbl.a(arg[0].tbl);

    final int nt = VB.type(t(XPCTYPE));
    final Col co = c(XPOLD);
    final Col cn = a(n(XPNEW), BLN);

    // test all values...
    for(int r = 0; r < co.sz; r++) cn.a(B.v(co.r(r).t() == nt));
  }
}

/** XQuery operator. */
final class Uni extends Opr {
  @Override
  void e() {
    final Tbl t1 = arg[0].tbl;
    final Tbl t2 = arg[1].tbl;
    tbl.s(t1);

    for(int c = 0; c < t1.size; c++) {
      final Col c1 = t1.c(c);
      for(int v = 0; v < c1.sz; v++) tbl.c(c).a(c1.r(v));
    }
    for(int c = 0; c < t1.size; c++) {
      final Col c2 = t2.c(c);
      final int cn = tbl.p(c2.nm);
      for(int v = 0; v < c2.sz; v++) tbl.c(cn).a(c2.r(v));
    }
  }
}
