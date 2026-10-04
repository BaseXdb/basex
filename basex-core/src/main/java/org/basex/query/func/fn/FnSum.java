package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.BasicType.*;

import java.math.*;
import java.util.*;

import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnSum extends NumericFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item item = sum(false, qc);
    return item != null ? item : defined(1) ? arg(1).atomItem(qc, info) : Itr.ZERO;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = opt(false, cc);
    if(expr != null) return expr;

    final Expr values = arg(0), zero = arg(1);
    final SeqType st = values.seqType(), stZero = zero.seqType();
    if(zero == Empty.UNDEFINED) {
      // no default value
      if(st.zero()) return cc.voidAndReturn(values, Itr.ZERO, info);
      if(!st.mayBeWrapped()) {
        final SeqType ost = optType(values);
        if(ost != null) exprType.assign(ost);
      }
    } else if(st.zero()) {
      if(zero == Empty.VALUE || stZero.instanceOf(Types.ANY_ATOMIC_TYPE_ZO)) {
        return cc.voidAndReturn(values, zero, info);
      }
    } else if(!st.mayBeWrapped() && !stZero.mayBeWrapped()) {
      // sum($nonempty, $zero) → sum($nonempty)  (default value unused)
      if(st.oneOrMore()) return cc.function(Function.SUM, info, values);
      final SeqType ost = optType(values), zst = optType(zero);
      final Type type = ost != null && zst != null ? ost.type.union(zst.type) : ANY_ATOMIC_TYPE;
      final Occ occ = stZero.oneOrMore() ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE;
      exprType.assign(type, occ);
    }
    return this;
  }

  /**
   * Pre-evaluates a value expression.
   * @param avg calculate average
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  final Expr opt(final boolean avg, final CompileContext cc) throws QueryException {
    // sum(reverse($values)) → sum($values), avg(sort($values)) → avg($values)
    final Expr reordered = reordered(arg(0));
    if(reordered != null) {
      final Expr[] args = exprs.clone();
      args[0] = reordered;
      return cc.function(avg ? Function.AVG : Function.SUM, info, args);
    }

    final Expr values = arg(0);
    if(values instanceof final RangeSeq rs) {
      return range(rs, avg);
    } else if(values instanceof final SingletonSeq ss) {
      return singleton(ss, avg);
    } else if(values instanceof final Path path) {
      final ArrayList<Stats> list = path.pathStats();
      if(list != null) {
        double sum = 0;
        long count = 0;
        for(final Stats stats : list) {
          if(!StatsType.isNumeric(stats.type) || !StatsType.isCategory(stats.type)) return this;
          for(final byte[] value : stats.values) {
            if(value.length == 0) return null;
            final long c = stats.values.get(value);
            sum += c * Token.toDouble(value);
            count += c;
          }
        }
        return Dbl.get(avg ? sum / count : sum);
      }
    }
    return null;
  }

  @Override
  protected final void simplifyArgs(final CompileContext cc) throws QueryException {
    super.simplifyArgs(cc);
    if(arg(0).seqType().type.isNumberOrUntyped()) {
      arg(0, arg -> arg.simplifyFor(Simplify.NUMBER, cc));
    }
  }

  /**
   * Computes the result from a range value.
   * @param value sequence
   * @param avg calculate average
   * @return result, or {@code null} if sequence is empty
   * @throws QueryException query exception
   */
  private Item range(final Value value, final boolean avg) throws QueryException {
    if(value.isEmpty()) return null;

    final long first = value.itemAt(0).itr(info), last = value.itemAt(value.size() - 1).itr(info);
    if(avg) {
      final BigDecimal bs = BigDecimal.valueOf(first), be = BigDecimal.valueOf(last);
      return Dec.get(bs.add(be).divide(Dec.BD_2));
    }

    // Little Gauss computation (the product is even)
    final long size = value.size(), product = Util.multiply(Util.add(first, last), size);
    if(product != Long.MIN_VALUE) return Itr.get(product / 2);
    final BigInteger bi = BigInteger.valueOf(first).add(BigInteger.valueOf(last)).
        multiply(BigInteger.valueOf(size)).shiftRight(1);
    if(bi.bitLength() < 64) return Itr.get(bi.longValue());
    throw RANGE_X.get(info, bi);
  }

  /**
   * Computes the result from a sequence with a repeated item.
   * @param ss singleton sequence
   * @param avg calculate average
   * @return result, or {@code null} if the item is not numeric
   * @throws QueryException query exception
   */
  private Item singleton(final SingletonSeq ss, final boolean avg) throws QueryException {
    if(!ss.singleItem()) return null;
    Item item = ss.itemAt(0);
    if(item.type.isUntyped()) item = Dbl.get(item.dbl(info));
    if(!item.type.isNumber()) return null;
    return avg ? item : Calc.MULTIPLY.eval(item, Itr.get(ss.size()), info);
  }

  /**
   * Sums up the specified item(s).
   * @param avg calculate average
   * @param qc query context
   * @return summed up item, or {@code null} if the input is empty
   * @throws QueryException query exception
   */
  final Item sum(final boolean avg, final QueryContext qc) throws QueryException {
    final Expr values = arg(0);
    if(values instanceof Range) return range(values.value(qc), avg);

    final Iter iter = values.atomIter(qc, info);
    final Value value = iter.eagerValue();
    if(value instanceof RangeSeq) return range(value, avg);
    if(value instanceof final SingletonSeq ss) {
      final Item item = singleton(ss, avg);
      if(item != null) return item;
    }

    final Item item = iter.next();
    if(item == null) return null;

    Item result = item.type.isUntyped() ? Dbl.get(item.dbl(info)) : item;
    final Type type = result.type;
    final boolean num = result instanceof ANum;
    final boolean dtd = type == DAY_TIME_DURATION, ymd = type == YEAR_MONTH_DURATION;
    if(!num && !dtd && !ymd) throw NUMDUR_X_X.get(info, type, result);

    int c = 1;
    for(Item it; (it = qc.next(iter)) != null;) {
      final Type tp = it.type;
      Type t = null;
      if(tp.isNumberOrUntyped()) {
        if(!num) t = DURATION;
      } else if(num) {
        t = NUMERIC;
      } else if(dtd && tp != DAY_TIME_DURATION || ymd && tp != YEAR_MONTH_DURATION) {
        t = DURATION;
      }
      if(t != null) throw ARGTYPE_X_X_X.get(info, t, tp, it);
      result = Calc.ADD.eval(result, it, info);
      c++;
    }
    return avg ? Calc.DIVIDE.eval(result, Itr.get(c), info) : result;
  }
}
