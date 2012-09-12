package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.query.value.type.AtomType.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.item.ANum;
import org.basex.query.value.type.*;
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
        if(c == -1) {
          do {
            ctx.checkStop();
            ++c;
          } while(iter.next() != null);
        }
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
  public Expr comp(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];
    final long c = e.size();
    if(c < 0 || e.uses(Use.NDT)) return this;

    switch(sig) {
      case COUNT: return Int.get(c);
      case SUM:   return c == 0 ? expr.length == 2 ? expr[1] : Int.get(0) : this;
      default:    return this;
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

    Item rs = it.type.isUntyped() ? Dbl.get(it.string(info), info) : it;
    final boolean n = rs instanceof ANum;
    if(!n && (!(rs instanceof Dur) || rs.type == DUR)) SUMTYPE.thrw(info, this, rs.type);

    int c = 1;
    for(Item i; (i = iter.next()) != null;) {
      final boolean un = i.type.isNumberOrUntyped();
      if(n && !un) FUNNUM.thrw(info, this, i.type);
      if(!n && un) FUNDUR.thrw(info, this, i.type);
      rs = Calc.PLUS.ev(info, rs, i);
      ++c;
    }
    return avg ? Calc.DIV.ev(info, rs, Int.get(c)) : rs;
  }

  /**
   * Returns a minimum or maximum item.
   * @param iter values to be compared
   * @param cmp comparator
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item minmax(final Iter iter, final OpV cmp, final QueryContext ctx)
      throws QueryException {

    if(expr.length == 2) checkColl(expr[1], ctx);

    Item rs = iter.next();
    if(rs == null) return null;

    // check if first item is comparable
    cmp.eval(info, rs, rs);

    // strings
    if(rs instanceof AStr) {
      for(Item it; (it = iter.next()) != null;) {
        if(!(it instanceof AStr)) FUNCMP.thrw(info, this, rs.type, it.type);
        if(cmp.eval(info, rs, it)) rs = it;
      }
      return rs;
    }
    // dates, durations and booleans
    if(rs instanceof ADate || rs instanceof Dur || rs.type == AtomType.BLN) {
      for(Item it; (it = iter.next()) != null;) {
        if(rs.type != it.type) FUNCMP.thrw(info, this, rs.type, it.type);
        if(cmp.eval(info, rs, it)) rs = it;
      }
      return rs;
    }
    // numbers
    if(rs.type.isUntyped()) rs = DBL.cast(rs, ctx, info);
    for(Item it; (it = iter.next()) != null;) {
      final Type t = numType(rs, it);
      if(cmp.eval(info, rs, it) || Double.isNaN(it.dbl(info))) rs = it;
      if(rs.type != t) rs = t.cast(rs, ctx, info);
    }
    return rs;
  }

  /**
   * Returns the numeric type with the highest precedence.
   * @param r result item
   * @param i new item
   * @return result
   * @throws QueryException query exception
   */
  private Type numType(final Item r, final Item i) throws QueryException {
    final Type tr = r.type, ti = i.type;
    if(ti.isUntyped()) return DBL;
    if(!(i instanceof ANum)) FUNCMP.thrw(info, this, tr, ti);

    if(tr == ti) return tr;
    if(tr == DBL || ti == DBL) return DBL;
    if(tr == FLT || ti == FLT) return FLT;
    if(tr == DEC || ti == DEC) return DEC;
    return ITR;
  }
}
