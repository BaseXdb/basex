package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.item.Type.*;

import org.basex.BaseX;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Calc;
import org.basex.query.xquery.expr.CmpV;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Date;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;

/**
 * Aggregating functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNAggr extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    final Iter iter = arg[0];

    switch(func) {
      case COUNT:
        long c = iter.size();
        if(c == -1) do ++c; while(iter.next() != null);
        return Itr.iter(c);
      case MIN:
        if(arg.length == 2) checkColl(arg[1]);
        return minmax(iter, CmpV.COMP.GT, ctx);
      case MAX:
        if(arg.length == 2) checkColl(arg[1]);
        return minmax(iter, CmpV.COMP.LT, ctx);
      case SUM:
        final Iter zero = arg.length == 2 ? arg[1] : null;
        Item it = iter.next();
        return it == null ? zero != null ? zero : Itr.ZERO.iter() :
          sum(iter, it, false);
      case AVG:
        it = iter.next();
        return it == null ? Iter.EMPTY : sum(iter, it, true);
      default:
        BaseX.notexpected(func); return null;
    }
  }

  @Override
  public Expr comp(final XQContext ctx, final Expr[] arg) {
    switch(func) {
      case AVG: return arg[0].e() ? Seq.EMPTY : this;
      default:  return this;
    }
  }

  /**
   * Sums up the specified item(s).
   * @param iter iterator
   * @param it first item
   * @param avg calculating the average
   * @return summed up item.
   * @throws XQException thrown if the items can't be compared
   */
  private Iter sum(final Iter iter, final Item it, final boolean avg)
      throws XQException {
    Item res = it.u() ? Dbl.get(it.str()) : it;
    if(!res.n() && !res.d()) Err.or(FUNNUMDUR, this, res.type);
    final boolean n = res.n();

    int c = 1;
    Item i;
    while((i = iter.next()) != null) {
      final boolean un = i.u() || i.n();
      if(n && !un) Err.or(FUNNUM, this, i.type);
      if(!n && un) Err.or(FUNDUR, this, i.type);
      res = Calc.PLUS.ev(res, i);
      c++;
    }
    return avg ? Calc.DIV.ev(res, Itr.get(c)).iter() : res.iter();
  }

  /**
   * Returns a minimum or maximum item.
   * @param iter input iterator
   * @param cmp comparator
   * @param ctx xquery context
   * @return resulting item
   * @throws XQException thrown if the items can't be compared
   */
  private Iter minmax(final Iter iter, final CmpV.COMP cmp, final XQContext ctx)
      throws XQException {

    Item res = iter.next();
    if(res == null) return Iter.EMPTY;

    cmp.e(res, res);
    if(!res.u() && res.s() || res instanceof Date)
      return evalStr(iter, res, cmp);

    Type t = res.u() ? DBL : res.type;
    if(res.type != t) res = t.e(res, ctx);

    Item it;
    while((it = iter.next()) != null) {
      t = type(res, it);
      final double d = it.dbl();
      if(d != d || cmp.e(res, it)) res = it;
      if(res.type != t) res = t.e(res, ctx);
    }
    return res.iter();
  }

  /**
   * Returns the type with the highest precedence.
   * @param a input item
   * @param b result item
   * @return result
   * @throws XQException thrown if the items can't be compared
   */
  private Type type(final Item a, final Item b) throws XQException {
    if(b.u()) {
      if(!a.n()) Err.or(FUNCMP, this, a.type, b.type);
      return DBL;
    }
    if(a.n() && !b.u() && b.s()) Err.or(FUNCMP, this, a.type, b.type);
    if(a.type == b.type) return a.type;
    if(a.type == DBL || b.type == DBL) return DBL;
    if(a.type == FLT || b.type == FLT) return FLT;
    if(a.type == DEC || b.type == DEC) return DEC;
    if(a.type == BLN || a.n() && !b.n() || b.n() && !a.n())
      Err.or(FUNCMP, this, a.type, b.type);
    return a.n() || b.n() ? ITR : a.type;
  }

  /**
   * Compares strings.
   * @param iter input iterator
   * @param r first item
   * @param cmp comparator
   * @return resulting item
   * @throws XQException thrown if the items can't be compared
   */
  private Iter evalStr(final Iter iter, final Item r, final CmpV.COMP cmp)
      throws XQException {

    Item res = r;
    Item it;
    while((it = iter.next()) != null) {
      if(it.type != res.type) Err.or(FUNCMP, info(), res.type, it.type);
      if(cmp.e(res, it)) res = it;
    }
    return res.iter();
  }
}
