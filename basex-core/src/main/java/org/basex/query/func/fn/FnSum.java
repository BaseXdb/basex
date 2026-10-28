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
import org.basex.query.util.*;
import org.basex.query.util.index.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

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
    if(reordered != null && values.seqType().type.instanceOf(DECIMAL)) {
      final Expr[] args = exprs.clone();
      args[0] = reordered;
      return cc.function(avg ? Function.AVG : Function.SUM, info, args);
    }

    // sum(1 to 10) → 55, avg(1 to 10) → 5.5
    if(values instanceof final RangeSeq rs) {
      return range(rs, avg);
    } else if(!avg && !defined(1) && values instanceof final SimpleMap map) {
      return linear(map, cc);
    } else if(values instanceof final SingletonSeq ss && ss.singleItem()) {
      // exact arithmetic only: floating-point results depend on the summation order
      // sum(replicate(2, 5)) → 10, avg(replicate(2, 5)) → 2.0
      final Item item = ss.itemAt(0);
      if(item.type.instanceOf(DECIMAL)) {
        return avg ? Dec.get(item.dec(info)) : Calc.MULTIPLY.eval(item, Itr.get(ss.size()), info);
      }
    } else if(values instanceof final Path path) {
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
            if(d != 0) exp = Math.min(exp, lowestBit(d));
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
    if(arg(0).seqType().type.isNumberOrUntyped()) {
      arg(0, arg -> arg.simplifyFor(Simplify.NUMBER, cc));
    }
  }

  /**
   * Moves a linear arithmetic operation out of the summed values.
   * @param map simple map
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr linear(final SimpleMap map, final CompileContext cc) throws QueryException {
    final Expr[] ops = map.args();
    final int ol = ops.length;
    final Expr last = ops[ol - 1];
    final Expr[] operands;
    final Calc calc;
    int pos = 0;
    if(last instanceof final Unary unary && unary.minus) {
      operands = unary.args();
      calc = null;
    } else if(last instanceof final Arith arith &&
        arith.calc.oneOf(Calc.ADD, Calc.SUBTRACT, Calc.MULTIPLY)) {
      operands = arith.args();
      calc = arith.calc;
      if(calc == Calc.SUBTRACT && operands[0] instanceof Itr) pos = 1;
      if(!(operands[1 - pos] instanceof Itr)) return null;
    } else {
      return null;
    }

    // exact arithmetic only; integer constants: sum(()) → 0
    final Expr arg = operands[pos];
    final SeqType st = arg.seqType();
    if(!st.zeroOrOne() || !st.type.instanceOf(DECIMAL) || arg.has(Flag.NDT)) return null;
    // sum(E ! (X + 1)) → sum(E ! X) + count(E): only if E can be counted cheaply
    final Expr input = ops[0];
    if(calc != null && calc.oneOf(Calc.ADD, Calc.SUBTRACT) && (ol != 2 || !st.one() ||
        !(input instanceof Value || input instanceof final Range range &&
        cheap(range.arg(0)) && cheap(range.arg(1))))) return null;

    final Expr[] args = ops.clone();
    args[ol - 1] = arg;
    final Expr sum = cc.function(Function.SUM, info, SimpleMap.get(cc, info, args));
    // sum(E ! -X) → -sum(E ! X)
    if(calc == null) return new Unary(info, sum, true).optimize(cc);
    // sum(E ! (X * 2)) → sum(E ! X) * 2
    final Expr num = operands[1 - pos];
    if(calc == Calc.MULTIPLY) return new Arith(info, sum, num, calc).optimize(cc);
    // sum(E ! (X - 1)) → sum(E ! X) - count(E), sum(E ! (1 - X)) → count(E) - sum(E ! X)
    final Expr count = new Arith(info, cc.function(Function.COUNT, info,
        input.copy(cc, new IntObjectMap<>())), num, Calc.MULTIPLY).optimize(cc);
    return new Arith(info, pos == 0 ? sum : count, pos == 0 ? count : sum, calc).optimize(cc);
  }

  /**
   * Checks if an expression can be evaluated repeatedly at no cost.
   * @param expr expression
   * @return result of check
   */
  private static boolean cheap(final Expr expr) {
    return expr instanceof Value || expr instanceof VarRef;
  }

  /**
   * Returns the exponent of the lowest set bit of a finite, non-zero double.
   * @param d double value
   * @return exponent
   */
  private static int lowestBit(final double d) {
    return Math.max(Math.getExponent(d), Double.MIN_EXPONENT) - 52 +
        Long.numberOfTrailingZeros(Double.doubleToRawLongBits(d) | 1L << 52);
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

    Item result = item.type.isUntyped() ? Dbl.get(item.dbl(info)) : item;
    final Type type = result.type;
    final boolean num = result instanceof ANum;
    final boolean dtd = type == DAY_TIME_DURATION, ymd = type == YEAR_MONTH_DURATION;
    if(!num && !dtd && !ymd) throw NUMDUR_X_X.get(info, type, result);

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
      result = Calc.ADD.eval(result, it, info);
      c++;
    }
    return avg ? Calc.DIVIDE.eval(result, Itr.get(c), info) : result;
  }
}
