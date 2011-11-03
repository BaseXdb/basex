package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.query.item.AtomType.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Calc;
import org.basex.query.expr.CmpV;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Aggregating functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNAggr extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNAggr(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Iter iter = ctx.iter(expr[0]);
    switch(def) {
      case COUNT:
        long c = iter.size();
        if(c == -1) do ++c; while(iter.next() != null);
        return Itr.get(c);
      case MIN:
        return minmax(iter, CmpV.Op.GT, ctx);
      case MAX:
        return minmax(iter, CmpV.Op.LT, ctx);
      case SUM:
        Item it = iter.next();
        return it != null ? sum(iter, it, false) :
          expr.length == 2 ? expr[1].item(ctx, input) : Itr.get(0);
      case AVG:
        it = iter.next();
        return it == null ? null : sum(iter, it, true);
      default:
        return super.item(ctx, ii);
    }
  }

  @Override
  public Expr cmp(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];
    final long c = e.size();
    if(c < 0 || e.uses(Use.NDT)) return this;

    switch(def) {
      case COUNT:
        return Itr.get(c);
      case SUM:
        return c == 0 ? expr.length == 2 ? expr[1] : Itr.get(0) : this;
      default:
        return this;
    }
  }

  /**
   * Sums up the specified item(s).
   * @param iter iterator
   * @param it first item
   * @param avg calculate average
   * @return summed up item
   * @throws QueryException query exception
   */
  private Item sum(final Iter iter, final Item it, final boolean avg)
      throws QueryException {

    Item res = it.unt() ? Dbl.get(it.atom(input), input) : it;
    if(!res.num() && (!res.dur() || res.type == DUR))
      SUMTYPE.thrw(input, this, res.type);
    final boolean n = res.num();

    int c = 1;
    for(Item i; (i = iter.next()) != null;) {
      final boolean un = i.unt() || i.num();
      if(n && !un) FUNNUM.thrw(input, this, i.type);
      if(!n && un) FUNDUR.thrw(input, this, i.type);
      res = Calc.PLUS.ev(input, res, i);
      ++c;
    }
    return avg ? Calc.DIV.ev(input, res, Itr.get(c)) : res;
  }

  /**
   * Returns a minimum or maximum item.
   * @param iter values to be compared
   * @param cmp comparator
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item minmax(final Iter iter, final CmpV.Op cmp,
      final QueryContext ctx) throws QueryException {

    if(expr.length == 2) checkColl(expr[1], ctx);

    Item res = iter.next();
    if(res == null) return null;

    // check if first item is comparable
    cmp.e(input, res, res);

    // strings or dates
    if(!res.unt() && res.str() || res.date()) {
      for(Item it; (it = iter.next()) != null;) {
        if(it.type != res.type) {
          FUNCMP.thrw(input, desc(), res.type, it.type);
        }
        if(cmp.e(input, res, it)) res = it;
      }
      return res;
    }

    // durations or numbers
    Type t = res.unt() ? DBL : res.type;
    if(res.type != t) res = t.e(res, ctx, input);

    for(Item it; (it = iter.next()) != null;) {
      t = type(res, it);
      if(!it.dur() && Double.isNaN(it.dbl(input)) || cmp.e(input, res, it))
        res = it;
      if(res.type != t) res = t.e(res, ctx, input);
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
    final Type ta = a.type, tb = b.type;
    if(b.unt()) {
      if(!a.num()) FUNCMP.thrw(input, this, ta, tb);
      return DBL;
    }
    if(a.num() && !b.unt() && b.str()) FUNCMP.thrw(input, this, ta, tb);

    if(ta == tb) return ta;
    if(ta == DBL || tb == DBL) return DBL;
    if(ta == FLT || tb == FLT) return FLT;
    if(ta == DEC || tb == DEC) return DEC;
    if(ta == BLN || a.num() && !b.num() || b.num() && !a.num())
      FUNCMP.thrw(input, this, ta, tb);
    return a.num() || b.num() ? ITR : ta;
  }
}
