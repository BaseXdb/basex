package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.BasicType.*;

import java.math.*;
import java.util.*;

import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.index.*;
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
    return item != null ? item : defined(1) ? atomItem(arg(1), qc) : Itr.ZERO;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = opt(false, cc);
    if(expr != null) return expr;

    final Expr values = arg(0), zero = arg(1);
    final SeqType st = values.seqType(), stZero = zero.seqType();
    if(!defined(1)) {
      if(st.zero()) return cc.voidAndReturn(values, Itr.ZERO, info);
      // empty input: integer zero
      final Type type = sumType(values);
      if(type != null) {
        exprType.assign(st.atomOcc().min >= 1 ? type : type.union(INTEGER), Occ.EXACTLY_ONE);
      }
    } else if(st.zero()) {
      if(stZero.instanceOf(Types.ANY_ATOMIC_TYPE_ZO)) return cc.voidAndReturn(values, zero, info);
    } else {
      // sum($nonempty, $zero) → sum($nonempty)  (default value unused)
      if(st.atomOcc().min >= 1) return cc.function(Function.SUM, info, values);
      // the default value is returned unconverted
      final SeqType ost = optType(values);
      final Type type = ost == null ? ANY_ATOMIC_TYPE : stZero.zero() ? ost.type :
        ost.type.union(stZero.type.atomicOrAny());
      final Occ occ = stZero.atomOcc().min >= 1 ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE;
      exprType.assign(type, occ);
    }
    return this;
  }

  /**
   * Pre-evaluates or rewrites the summed values.
   * @param avg calculate average
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  final Expr opt(final boolean avg, final CompileContext cc) throws QueryException {
    // sum(reverse($values)) → sum($values), avg(sort($values)) → avg($values)
    // exact arithmetic only: floating-point results depend on the summation order
    final Expr values = arg(0), reordered = reordered(values);
    if(reordered != null && values.seqType().atomType().instanceOf(DECIMAL)) {
      final Expr[] args = exprs.clone();
      args[0] = reordered;
      return cc.function(avg ? Function.AVG : Function.SUM, info, args);
    }

    // sum(1 to 10) → 55, avg(1 to 10) → 5.5
    if(values instanceof final RangeSeq rs) {
      return range(rs, avg);
    } else if(!avg && !defined(1) && values instanceof final SimpleMap map) {
      // no addition or subtraction: sum(E ! X) may overflow while sum(E ! (X - 1)) does not
      final Expr[] ops = map.args();
      final int ol = ops.length;
      final Expr last = ops[ol - 1], arg, factor;
      if(last instanceof final Unary unary && unary.minus) {
        arg = unary.expr;
        factor = null;
      } else if(last instanceof final Arith arith && arith.calc == Calc.MULTIPLY &&
          arith.arg(1) instanceof Itr) {
        // integer factor: sum(()) → 0
        arg = arith.arg(0);
        factor = arith.arg(1);
      } else {
        return null;
      }

      // exact arithmetic only
      final SeqType st = arg.seqType();
      if(!st.zeroOrOne() || !st.type.instanceOf(DECIMAL) || arg.has(Flag.NDT)) return null;

      final Expr[] args = ops.clone();
      args[ol - 1] = arg;
      final Expr sum = cc.function(Function.SUM, info, SimpleMap.get(cc, info, args));
      // sum(E ! -X) → -sum(E ! X), sum(E ! (X * 2)) → sum(E ! X) * 2
      return (factor == null ? new Unary(info, sum, true) :
        new Arith(info, sum, factor, Calc.MULTIPLY)).optimize(cc);
    } else if(values instanceof final SingletonSeq ss && ss.singleItem()) {
      // exact arithmetic only: repeated addition may differ from multiplication
      // sum(replicate(2, 5)) → 10, avg(replicate(2, 5)) → 2.0
      final Item item = ss.itemAt(0);
      if(item.type.instanceOf(DECIMAL)) {
        return avg ? Dec.get(item.dec(info)) :
          Calc.MULTIPLY.eval(item, Itr.get(ss.size()), sc().numericType(), info);
      }
    } else if(sc().numericType() == DOUBLE && values instanceof final Path path) {
      // sum(db:get('db')//price) → 1234.5e0
      final ArrayList<Stats> list = PathNodes.stats(path);
      if(list != null) {
        // negative zero: result if all values are negative zeros
        double sum = -0d, abs = 0;
        int exp = Double.MAX_EXPONENT - 53;
        long count = 0;
        for(final Stats stats : list) {
          if(!StatsType.isNumeric(stats.type) || !StatsType.isCategory(stats.type)) return null;
          for(final byte[] value : stats.values) {
            final double d = Token.toDouble(value);
            if(!Double.isFinite(d)) return null;
            if(d != 0) {
              // exponent of the lowest set bit
              exp = Math.min(exp, Math.max(Math.getExponent(d), Double.MIN_EXPONENT) - 52 +
                  Long.numberOfTrailingZeros(Double.doubleToRawLongBits(d) | 1L << 52));
            }
            final long c = stats.values.get(value);
            sum += c * d;
            abs += c * Math.abs(d);
            // exact results only: all partial sums must be representable, regardless of the order
            if(abs >= Math.scalb(1d, exp + 53)) return null;
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
    if(arg(0).seqType().atomType().isNumberOrUntyped()) {
      arg(0, arg -> arg.simplifyFor(Simplify.numeric(sc()), cc));
    }
  }

  /**
   * Returns the numeric type of the summed values.
   * @param values values
   * @return type (can be {@code null})
   */
  final Type sumType(final Expr values) {
    final SeqType st = optType(values);
    if(st == null) return null;
    if(!(values instanceof final List list)) return st.type;
    // sum(($int, $dbl)) → xs:double: non-empty operands are promoted
    final BasicType preferred = sc().numericType();
    Type type = null;
    for(final Expr expr : list.args()) {
      final SeqType ost = optType(expr);
      if(ost == null || !ost.oneOrMore()) return st.type;
      type = type == null ? ost.type : Calc.ADD.type(type, ost.type, preferred);
    }
    return type;
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
    return integer(BigInteger.valueOf(first).add(BigInteger.valueOf(last)).
        multiply(BigInteger.valueOf(size)).shiftRight(1));
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
    final Item item = iter.next();
    if(item == null) return null;

    final BasicType preferred = sc().numericType();
    Item result = item.type.isUntyped() ? toNumber(item, null) : item;
    final Type type = result.type;
    final boolean num = result instanceof ANum;
    final boolean dtd = type == DAY_TIME_DURATION, ymd = type == YEAR_MONTH_DURATION;
    if(!num && !dtd && !ymd) throw NUMDUR_X_X.get(info, type, result);

    // integers: exact partial sums, so that the result does not depend on the summation order
    boolean itr = type.instanceOf(INTEGER);
    long c = 1;
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
      itr &= tp.instanceOf(INTEGER);
      if(itr) {
        // integer overflow: switch to decimal
        final long sum = result instanceof Itr ? Util.add(result.itr(info), it.itr(info)) :
          Long.MIN_VALUE;
        result = sum != Long.MIN_VALUE ? Itr.get(sum) : Dec.get(result.dec(info).add(it.dec(info)));
      } else {
        result = Calc.ADD.eval(result, it, preferred, info);
      }
      c++;
    }
    if(itr && !avg && result instanceof Dec) return integer(result.dec(info).toBigInteger());
    if(avg) return Calc.DIVIDE.eval(result, Itr.get(c), preferred, info);
    return result;
  }

  /**
   * Returns an integer item for the specified big integer.
   * @param bi big integer
   * @return integer item
   * @throws QueryException query exception
   */
  private Itr integer(final BigInteger bi) throws QueryException {
    if(bi.bitLength() < 64) return Itr.get(bi.longValue());
    throw RANGE_X.get(info, bi);
  }
}
