package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.item.Type.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Calc;
import org.basex.query.expr.CmpV;
import org.basex.query.expr.Expr;
import org.basex.query.item.Date;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Seq;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;

/**
 * Aggregating functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FNAggr extends Fun {
  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr[0]);

    switch(func) {
      case COUNT:
        int c = iter.size();
        if(c == -1) do ++c; while(iter.next() != null);
        return Itr.get(c);
      case MIN:
        return minmax(iter, CmpV.Comp.GT, ctx);
      case MAX:
        return minmax(iter, CmpV.Comp.LT, ctx);
      case SUM:
        Item it = iter.next();
        return it != null ? sum(iter, it, false) :
          expr.length == 2 ? expr[1].atomic(ctx) : Itr.ZERO;
      case AVG:
        it = iter.next();
        return it == null ? null : sum(iter, it, true);
      default:
        return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    switch(func) {
      case COUNT:
        final long c = expr[0].size(ctx);
        return c >= 0 ? Itr.get(c) : this;
      case MIN:
      case MAX:
      case AVG:
        return expr[0].e() ? Seq.EMPTY : this;
      default:
        return this;
    }
  }

  /**
   * Sums up the specified item(s).
   * @param iter iterator
   * @param it first item
   * @param avg calculating the average
   * @return summed up item
   * @throws QueryException query exception
   */
  private Item sum(final Iter iter, final Item it, final boolean avg)
      throws QueryException {

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
    return avg ? Calc.DIV.ev(res, Itr.get(c)) : res;
  }

  /**
   * Returns a minimum or maximum item.
   * @param iter1 first argument
   * @param cmp comparator
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item minmax(final Iter iter1, final CmpV.Comp cmp,
      final QueryContext ctx) throws QueryException {

    if(expr.length == 2) checkColl(expr[1], ctx);

    Item res = iter1.next();
    if(res == null) return null;

    cmp.e(res, res);
    if(!res.u() && res.s() || res instanceof Date)
      return evalStr(iter1, res, cmp);

    Type t = res.u() ? DBL : res.type;
    if(res.type != t) res = t.e(res, ctx);

    Item it;
    while((it = iter1.next()) != null) {
      t = type(res, it);
      final double d = it.dbl();
      if(d != d || cmp.e(res, it)) res = it;
      if(res.type != t) res = t.e(res, ctx);
    }
    return res;
  }

  /**
   * Returns the type with the highest precedence.
   * @param a input item
   * @param b result item
   * @return result
   * @throws QueryException query exception
   */
  private Type type(final Item a, final Item b) throws QueryException {
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
   * @throws QueryException query exception
   */
  private Item evalStr(final Iter iter, final Item r, final CmpV.Comp cmp)
      throws QueryException {

    Item res = r;
    Item it;
    while((it = iter.next()) != null) {
      if(it.type != res.type) Err.or(FUNCMP, info(), res.type, it.type);
      if(cmp.e(res, it)) res = it;
    }
    return res;
  }
}
