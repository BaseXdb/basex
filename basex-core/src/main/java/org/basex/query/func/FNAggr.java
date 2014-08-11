package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.query.value.type.AtomType.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Aggregating functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNAggr extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case COUNT: return count(qc);
      case MIN:   return minmax(OpV.GT, qc);
      case MAX:   return minmax(OpV.LT, qc);
      case SUM:   return sum(qc, ii);
      case AVG:   return avg(qc, ii);
      default:    return super.item(qc, ii);
    }
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    // skip non-deterministic and variable expressions
    final Expr e = exprs[0];
    if(e.has(Flag.NDT) || e.has(Flag.UPD) || e instanceof VarRef) return this;

    final long c = e.size();
    switch(func) {
      case COUNT:
        if(c >= 0) return Int.get(c);
        if(e instanceof FNMap) {
          final FNMap f = (FNMap) e;
          if(f.func == Function._MAP_KEYS) return Function._MAP_SIZE.get(sc, info, f.exprs);
        }
        break;
      case SUM:
        if(c == 0) return exprs.length == 2 ? exprs[1] : Int.get(0);
        final Type a = e.seqType().type, b = exprs.length == 2 ? exprs[1].seqType().type : a;
        if(a.isNumberOrUntyped() && b.isNumberOrUntyped()) seqType = Calc.type(a, b).seqType();
        break;
      default:
        break;
    }
    return this;
  }

  /**
   * Computes the number of resulting items.
   * @param qc query context
   * @return number
   * @throws QueryException query exception
   */
  private Item count(final QueryContext qc) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    long c = iter.size();
    if(c == -1) {
      do {
        qc.checkStop();
        ++c;
      } while(iter.next() != null);
    }
    return Int.get(c);
  }

  /**
   * Returns a minimum or maximum item.
   * @param cmp comparator
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item minmax(final OpV cmp, final QueryContext qc) throws QueryException {
    final Collation coll = toCollation(1, qc);

    final Iter iter = exprs[0].atomIter(qc, info);
    Item rs = iter.next();
    if(rs == null) return null;

    // check if first item is comparable
    cmp.eval(rs, rs, coll, info);

    // strings
    if(rs instanceof AStr) {
      for(Item it; (it = iter.next()) != null;) {
        if(!(it instanceof AStr)) throw EXPTYPE_X_X_X.get(info, rs.type, it.type, it);
        if(cmp.eval(rs, it, coll, info)) rs = it;
      }
      return rs;
    }
    // dates, durations, booleans, binary values
    if(rs instanceof ADate || rs instanceof Dur || rs instanceof Bin || rs.type == BLN) {
      for(Item it; (it = iter.next()) != null;) {
        if(rs.type != it.type) throw EXPTYPE_X_X_X.get(info, rs.type, it.type, it);
        if(cmp.eval(rs, it, coll, info)) rs = it;
      }
      return rs;
    }
    // numbers
    if(rs.type.isUntyped()) rs = DBL.cast(rs, qc, sc, info);
    for(Item it; (it = iter.next()) != null;) {
      final Type t = numType(rs, it);
      if(cmp.eval(rs, it, coll, info) || Double.isNaN(it.dbl(info))) rs = it;
      if(rs.type != t) rs = (Item) t.cast(rs, qc, sc, info);
    }
    return rs;
  }

  /**
   * Returns a summed up value.
   * @param qc query context
   * @param ii input info
   * @return sum
   * @throws QueryException query exception
   */
  private Item sum(final QueryContext qc, final InputInfo ii) throws QueryException {
    // partial sum calculation (Little Gauss)
    if(exprs[0] instanceof RangeSeq) {
      final RangeSeq rs = (RangeSeq) exprs[0];
      final long s = rs.itemAt(0).itr(ii);
      if(s == 0 || s == 1) {
        final long n = rs.size();
        return Int.get(n < 3037000500L ? n * (n + 1) / 2 : BigInteger.valueOf(n).multiply(
            BigInteger.valueOf(n + 1)).divide(BigInteger.valueOf(2)).longValue());
      }
    }

    final Iter iter = exprs[0].atomIter(qc, ii);
    Item it = iter.next();
    return it != null ? sum(iter, it, false) :
      exprs.length == 2 ? exprs[1].atomItem(qc, ii) : Int.get(0);
  }

  /**
   * Returns an average value.
   * @param qc query context
   * @param ii input info
   * @return sum
   * @throws QueryException query exception
   */
  private Item avg(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = exprs[0].atomIter(qc, ii);
    Item it = iter.next();
    return it == null ? null : sum(iter, it, true);
  }

  /**
   * Sums up the specified item(s).
   * @param iter iterator
   * @param it first item
   * @param avg calculate average
   * @return summed up item
   * @throws QueryException query exception
   */
  private Item sum(final Iter iter, final Item it, final boolean avg) throws QueryException {
    Item rs = it.type.isUntyped() ? Dbl.get(it.string(info), info) : it;
    final boolean num = rs instanceof ANum, dtd = rs.type == DTD, ymd = rs.type == YMD;
    if(!num && (!(rs instanceof Dur) || rs.type == DUR)) throw SUM_X_X.get(info, rs.type, rs);

    int c = 1;
    for(Item i; (i = iter.next()) != null;) {
      if(i.type.isNumberOrUntyped()) {
        if(!num) throw SUMDUR_X_X.get(info, i.type, i);
      } else {
        if(num) throw SUMNUM_X_X.get(info, i.type, i);
        if(dtd && i.type != DTD || ymd && i.type != YMD) throw SUMDUR_X_X.get(info, i.type, i);
      }
      rs = Calc.PLUS.ev(info, rs, i);
      ++c;
    }
    return avg ? Calc.DIV.ev(info, rs, Int.get(c)) : rs;
  }

  /**
   * Returns the numeric type with the highest precedence.
   * @param res result item
   * @param it new item
   * @return result
   * @throws QueryException query exception
   */
  private Type numType(final Item res, final Item it) throws QueryException {
    final Type ti = it.type;
    if(ti.isUntyped()) return DBL;
    final Type tr = res.type;
    if(!(it instanceof ANum)) throw EXPTYPE_X_X_X.get(info, tr, ti, it);

    if(tr == ti) return tr;
    if(tr == DBL || ti == DBL) return DBL;
    if(tr == FLT || ti == FLT) return FLT;
    if(tr == DEC || ti == DEC) return DEC;
    return ITR;
  }
}
