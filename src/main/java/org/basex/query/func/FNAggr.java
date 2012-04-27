package org.basex.query.func;

import static org.basex.query.item.AtomType.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Aggregating functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNAggr extends StandardFunc {
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
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter iter = ctx.iter(expr[0]);
    switch(sig) {
      case COUNT:
        long c = iter.size();
        if(c == -1) do ++c; while(iter.next() != null);
        return Int.get(c);
      case MIN:
        return minmax(iter, OpV.GT, ctx);
      case MAX:
        return minmax(iter, OpV.LT, ctx);
      case SUM:
        Item it = iter.next();
        return it != null ? sum(iter, it, false) :
          expr.length == 2 ? expr[1].item(ctx, info) : Int.get(0);
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
    if(c < 0 || e.uses(Use.NDT) || e.uses(Use.CNS)) return this;

    switch(sig) {
      case COUNT:
        return Int.get(c);
      case SUM:
        return c == 0 ? expr.length == 2 ? expr[1] : Int.get(0) : this;
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

    Item res = it.type.isUntyped() ? Dbl.get(it.string(info), info) : it;
    Type t = res.type;
    if(!t.isNumber() && (!t.isDuration() || t == DUR))
      SUMTYPE.thrw(info, this, t);
    final boolean n = t.isNumber();

    int c = 1;
    for(Item i; (i = iter.next()) != null;) {
      t = i.type;
      final boolean un = t.isUntyped() || t.isNumber();
      if(n && !un) FUNNUM.thrw(info, this, t);
      if(!n && un) FUNDUR.thrw(info, this, t);
      res = Calc.PLUS.ev(info, res, i);
      ++c;
    }
    return avg ? Calc.DIV.ev(info, res, Int.get(c)) : res;
  }

  /**
   * Returns a minimum or maximum item.
   * @param iter values to be compared
   * @param cmp comparator
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item minmax(final Iter iter, final OpV cmp,
      final QueryContext ctx) throws QueryException {

    if(expr.length == 2) checkColl(expr[1], ctx);

    Item res = iter.next();
    if(res == null) return null;

    // check if first item is comparable
    cmp.eval(info, res, res);

    // strings or dates
    if(!res.type.isUntyped() && res.type.isString() || res.type.isDate()) {
      for(Item it; (it = iter.next()) != null;) {
        if(it.type != res.type) {
          FUNCMP.thrw(info, description(), res.type, it.type);
        }
        if(cmp.eval(info, res, it)) res = it;
      }
      return res;
    }

    // durations or numbers
    Type t = res.type.isUntyped() ? DBL : res.type;
    if(res.type != t) res = t.cast(res, ctx, info);

    for(Item it; (it = iter.next()) != null;) {
      t = type(res, it);
      if(!it.type.isDuration() && Double.isNaN(it.dbl(info)) ||
          cmp.eval(info, res, it))
        res = it;
      if(res.type != t) res = t.cast(res, ctx, info);
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
    if(tb.isUntyped()) {
      if(!ta.isNumber()) FUNCMP.thrw(info, this, ta, tb);
      return DBL;
    }
    if(ta.isNumber() && !tb.isUntyped() && tb.isString())
      FUNCMP.thrw(info, this, ta, tb);

    if(ta == tb) return ta;
    if(ta == DBL || tb == DBL) return DBL;
    if(ta == FLT || tb == FLT) return FLT;
    if(ta == DEC || tb == DEC) return DEC;
    if(ta == BLN || ta.isNumber() && !tb.isNumber() ||
        tb.isNumber() && !ta.isNumber()) FUNCMP.thrw(info, this, ta, tb);
    return ta.isNumber() || tb.isNumber() ? ITR : ta;
  }
}
