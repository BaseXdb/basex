package org.basex.query.expr;

import static java.lang.Double.*;
import static org.basex.query.QueryText.*;

import java.math.*;
import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.path.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
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
 * Numeric range expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CmpR extends CmpRange {
  /** Maximum integer value that can be represented losslessly as double value. */
  private static final double MAX_INTEGER = 1L << 53;

  /** Minimum (superset of exact bounds, if specified). */
  final double min;
  /** Maximum (superset of exact bounds, if specified). */
  final double max;
  /** Exact lower bound for untyped values (can be {@code null}). */
  private final ANum lower;
  /** Exact upper bound for untyped values (can be {@code null}). */
  private final ANum upper;
  /** Inclusive lower bound. */
  private final boolean lowerInc;
  /** Inclusive upper bound. */
  private final boolean upperInc;

  /**
   * Constructor.
   * @param expr (compiled) expression
   * @param min minimum value
   * @param max maximum value
   * @param lower exact lower bound for untyped values (can be {@code null})
   * @param lowerInc inclusive lower bound
   * @param upper exact upper bound for untyped values (can be {@code null})
   * @param upperInc inclusive upper bound
   * @param info input info (can be {@code null})
   */
  private CmpR(final Expr expr, final double min, final double max, final ANum lower,
      final boolean lowerInc, final ANum upper, final boolean upperInc, final InputInfo info) {
    super(expr, info);
    this.min = min;
    this.max = max;
    this.lower = lower;
    this.lowerInc = lowerInc;
    this.upper = upper;
    this.upperInc = upperInc;
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @param expr expression to be compared
   * @param min minimum position
   * @param max minimum position (inclusive)
   * @return expression
   * @throws QueryException query exception
   */
  static Expr get(final CompileContext cc, final InputInfo info, final Expr expr,
      final double min, final double max) throws QueryException {
    return min > max ? Bln.FALSE : min == NEGATIVE_INFINITY && max == POSITIVE_INFINITY ?
      cc.function(Function.EXISTS, info, expr) :
      new CmpR(expr, min, max, null, false, null, false, info).optimize(cc);
  }

  /**
   * Tries to convert the specified expression into a range expression with exact bounds.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @param expr expression to be compared
   * @param lower exact lower bound (can be {@code null})
   * @param lowerInc inclusive lower bound
   * @param upper exact upper bound (can be {@code null})
   * @param upperInc inclusive upper bound
   * @return expression
   * @throws QueryException query exception
   */
  private static Expr get(final CompileContext cc, final InputInfo info, final Expr expr,
      final ANum lower, final boolean lowerInc, final ANum upper, final boolean upperInc)
      throws QueryException {
    if(lower != null && upper != null) {
      final int c = compare(lower, upper, info);
      if(c > 0 || c == 0 && !(lowerInc && upperInc)) return Bln.FALSE;
    }
    // double range: superset of exact range (rounding is monotonic)
    final double mn = lower != null ? lower.dbl() : NEGATIVE_INFINITY;
    final double mx = upper != null ? upper.dbl() : POSITIVE_INFINITY;
    return new CmpR(expr, mn, mx, lower, lowerInc, upper, upperInc, info).optimize(cc);
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cc compilation context
   * @param cmp expression to be converted
   * @return new or original expression
   * @throws QueryException query exception
   */
  static Expr get(final CompileContext cc, final CmpG cmp) throws QueryException {
    // only rewrite deterministic expressions
    final Expr expr1 = cmp.exprs[0], expr2 = cmp.exprs[1];
    if(cmp.has(Flag.NDT)) return cmp;

    // only rewrite numeric comparisons, skip decimals
    // allowed: $node > 20; rejected: $decimal = 1 to 10
    final Type type1 = expr1.seqType().type;
    final boolean int1 = type1.instanceOf(BasicType.INTEGER);
    if(!(type1.isUntyped() || type1.oneOf(BasicType.FLOAT, BasicType.DOUBLE) || int1))
      return cmp;

    double mn, mx;
    if(expr2 instanceof final RangeSeq rs) {
      mn = rs.min();
      mx = rs.max();
    } else if(expr2 instanceof final ANum num && !(num instanceof Dec && int1) &&
        !(num instanceof Flt && type1.isUntyped())) {
      // untyped values compared with floats are cast to xs:float, not xs:double
      mn = num.dbl();
      mx = mn;
    } else {
      return cmp;
    }
    // integer comparisons: reject numbers that are too large to be safely compared as doubles
    if(int1 && (Math.abs(mn) >= MAX_INTEGER || Math.abs(mx) >= MAX_INTEGER)) return cmp;

    // use exact bounds if untyped values are cast to decimals (comparison with integers or
    // decimals), or if numbers are compared with values that cannot be represented as doubles
    if(type1.isUntyped() ? !(expr2 instanceof final ANum num && num.floating()) :
      !int1 && !exactDouble(expr2)) {
      final ANum lower, upper;
      if(expr2 instanceof final RangeSeq rs) {
        lower = Itr.get(rs.min());
        upper = Itr.get(rs.max());
      } else {
        lower = upper = (ANum) expr2;
      }
      return switch(cmp.op) {
        case GE -> get(cc, cmp.info, expr1, lower, true, null, false);
        case GT -> get(cc, cmp.info, expr1, lower, false, null, false);
        case LE -> get(cc, cmp.info, expr1, null, false, upper, true);
        case LT -> get(cc, cmp.info, expr1, null, false, upper, false);
        default -> cmp;
      };
    }

    switch(cmp.op) {
      case GE -> mx = POSITIVE_INFINITY;
      case GT -> {
        mn = Math.nextUp(mn);
        mx = POSITIVE_INFINITY;
      }
      case LE -> mn = NEGATIVE_INFINITY;
      case LT -> {
        mn = NEGATIVE_INFINITY;
        mx = Math.nextDown(mx);
      }
      default -> {
        return cmp;
      }
    }
    return get(cc, cmp.info, expr1, mn, mx);
  }

  /**
   * Checks if the specified numbers can be represented exactly as doubles.
   * @param expr numbers (numeric item or range sequence)
   * @return result of check
   * @throws QueryException query exception
   */
  private static boolean exactDouble(final Expr expr) throws QueryException {
    if(expr instanceof final RangeSeq rs) {
      return Math.abs((double) rs.min()) < MAX_INTEGER && Math.abs((double) rs.max()) < MAX_INTEGER;
    }
    final ANum num = (ANum) expr;
    if(num.floating()) return true;
    final BigDecimal bd = num.dec(null);
    final double d = bd.doubleValue();
    return Double.isFinite(d) && new BigDecimal(d).compareTo(bd) == 0;
  }

  /**
   * Indicates if this range has exact bounds.
   * @return result of check
   */
  private boolean exact() {
    return lower != null || upper != null;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.NUMBER, cc);

    final SeqType st = expr.seqType();
    single = st.zeroOrOne() && !st.mayBeWrapped();

    // position() = .1e0 → false()
    if(Function.POSITION.is(expr)) {
      final long mn = Math.max((long) Math.ceil(min), 1), mx = (long) Math.floor(max);
      return cc.replaceWith(this, IntPos.get(mn, mx, info));
    }
    // //a/@n > 100 → false() (statistics report no values in range)
    if(noMatches(null, expr.data())) return cc.replaceWith(this, Bln.FALSE);

    return expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public boolean noMatches(final ArrayList<PathNode> nodes, final Data data)
      throws QueryException {
    final ArrayList<Stats> list = Path.stats(expr, nodes, data);
    return list != null && noMatches(list);
  }

  /**
   * Checks if the specified statistics contain no values in the range of this expression.
   * @param stats statistics
   * @return result of check
   */
  private boolean noMatches(final ArrayList<Stats> stats) {
    return Checks.all(stats, st -> StatsType.isNumeric(st.type) && (st.min > max || st.max < min));
  }

  @Override
  boolean inRange(final Item item) throws QueryException {
    final double value = item.dbl(info);
    if(!(value >= min && value <= max)) return false;
    // exact bounds: values that equal a bound as doubles are compared exactly
    return (value != min || lower == null ||
        (lowerInc ? CmpOp.LE : CmpOp.LT).eval(compare(lower, item, info))) &&
      (value != max || upper == null ||
        (upperInc ? CmpOp.GE : CmpOp.GT).eval(compare(upper, item, info)));
  }

  @Override
  boolean inRange(final RangeSeq seq) {
    return seq.max() >= min && seq.min() <= max;
  }

  @Override
  Expr merge(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {

    // exact bounds: create intersection with other exact range or with single value
    if(exact()) {
      if(or) return null;
      ANum lw, up;
      boolean lwInc, upInc;
      if(ex instanceof final CmpR cmp && cmp.exact()) {
        lw = cmp.lower;
        lwInc = cmp.lowerInc;
        up = cmp.upper;
        upInc = cmp.upperInc;
      } else {
        // single value: $x = 5
        final ANum value = ex instanceof final CmpG cmp && cmp.op == CmpOp.EQ &&
          ex.arg(1) instanceof final ANum num && !num.floating() ? num :
          ex instanceof final CmpIR cmp && cmp.min == cmp.max ? Itr.get(cmp.min) : null;
        if(value == null) return null;
        lw = up = value;
        lwInc = upInc = true;
      }
      // choose larger lower bound and smaller upper bound
      if(lower != null) {
        final int c = lw == null ? 1 : compare(lower, lw, info);
        if(c > 0 || c == 0 && !lowerInc) {
          lw = lower;
          lwInc = lowerInc;
        }
      }
      if(upper != null) {
        final int c = up == null ? -1 : compare(upper, up, info);
        if(c < 0 || c == 0 && !upperInc) {
          up = upper;
          upInc = upperInc;
        }
      }
      return get(cc, info, expr, lw, lwInc, up, upInc);
    }
    if(ex instanceof final CmpR cr && cr.exact()) return null;

    Double newMin = null, newMax = null;
    if(ex instanceof final CmpR cmp) {
      newMin = cmp.min;
      newMax = cmp.max;
    } else if(ex instanceof final CmpG cmp && cmp.op == CmpOp.EQ &&
        ex.arg(1) instanceof final ANum num) {
      newMin = num.dbl();
      newMax = newMin;
    }
    if(newMin == null || or && (max < newMin || min > newMax)) return null;

    // determine common minimum and maximum value
    newMin = or ? Math.min(min, newMin) : Math.max(min, newMin);
    newMax = or ? Math.max(max, newMax) : Math.min(max, newMax);
    return get(cc, info, expr, newMin, newMax);
  }

  /**
   * Compares a number with the numeric value of an item.
   * @param num number
   * @param item item to be compared
   * @param info input info (can be {@code null})
   * @return result of comparison
   * @throws QueryException query exception
   */
  private static int compare(final ANum num, final Item item, final InputInfo info)
      throws QueryException {
    return num.compare(item, null, false, null, info);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    final Data data = ii.db.data();
    // sequential main memory scan is usually faster than range index access
    if(data == null ? !ii.enforce() : data.inMemory()) return false;

    final IndexType type = ii.type(expr, null);
    if(type == null) return false;

    // try the value index first: when the targeted node has integer-category statistics,
    // the range can be projected losslessly onto a long range and looked up exactly
    final long[] range = longRange();
    if(range != null && range[0] <= range[1]) {
      final IndexCosts costs = ii.costs;
      if(ii.create(range[0], range[1], info)) return true;
      ii.costs = costs;
    }

    // statistics of the indexed values
    final ArrayList<Stats> stats = ii.stats();
    if(stats == null || !Checks.all(stats, st -> StatsType.isNumeric(st.type))) return false;
    // all values out of range: no results
    if(noMatches(stats)) {
      ii.costs = IndexCosts.ZERO;
      return true;
    }

    // estimate costs for the range of the indexed values
    double kmin = POSITIVE_INFINITY, kmax = NEGATIVE_INFINITY;
    for(final Stats st : stats) {
      kmin = Math.min(kmin, st.min);
      kmax = Math.max(kmax, st.max);
    }
    final NumericRange nr = new NumericRange(type, Math.max(min, kmin), Math.min(max, kmax));
    ii.costs = IndexInfo.costs(data, nr);
    if(ii.costs == null) return false;

    // skip if numbers are negative, doubles, or of different string length
    final int mnl = min >= 0 && (long) min == min ? Token.token(min).length : -1;
    final int mxl = max >= 0 && (long) max == max ? Token.token(max).length : -1;
    if(mnl == -1 || mnl != mxl) return false;

    // don't use index if min/max values are infinite
    if(Token.token((int) nr.min()).length != Token.token((int) nr.max()).length) return false;

    // exact bounds: check results of range access
    ParseExpr root = new RangeAccess(info, nr, ii.db);
    if(exact()) {
      final Expr filter = Filter.get(ii.cc, info, root, with(new ContextValue(info), ii.cc));
      if(!(filter instanceof final ParseExpr pe)) return false;
      root = pe;
    }
    final TokenBuilder tb = new TokenBuilder();
    tb.add('[').add(min).add(',').add(max).add(']');
    return ii.create(root, true, Util.info(OPTINDEX_X_X, "range", tb), info);
  }

  /**
   * Returns the range as integer bounds.
   * @return minimum and maximum, or {@code null} if the range cannot be represented
   * @throws QueryException query exception
   */
  private long[] longRange() throws QueryException {
    if(!exact()) return new long[] { (long) Math.ceil(min), (long) Math.floor(max) };

    // smallest integer above (or at) lower bound, largest integer below (or at) upper bound
    final BigDecimal lmin = lower == null ? null : lowerInc ?
      lower.dec(info).setScale(0, RoundingMode.CEILING) :
      lower.dec(info).setScale(0, RoundingMode.FLOOR).add(BigDecimal.ONE);
    final BigDecimal lmax = upper == null ? null : upperInc ?
      upper.dec(info).setScale(0, RoundingMode.FLOOR) :
      upper.dec(info).setScale(0, RoundingMode.CEILING).subtract(BigDecimal.ONE);
    // bounds beyond the integer range
    if(lmin != null && lmin.compareTo(Dec.BD_MAXLONG) > 0 ||
       lmax != null && lmax.compareTo(Dec.BD_MINLONG) < 0) return null;
    return new long[] {
      lmin == null ? Long.MIN_VALUE : lmin.max(Dec.BD_MINLONG).longValue(),
      lmax == null ? Long.MAX_VALUE : lmax.min(Dec.BD_MAXLONG).longValue()
    };
  }

  @Override
  Expr with(final Expr operand, final CompileContext cc) throws QueryException {
    return exact() ? get(cc, info, operand, lower, lowerInc, upper, upperInc) :
      get(cc, info, operand, min, max);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final CmpR cmp = new CmpR(expr.copy(cc, vm), min, max, lower, lowerInc, upper, upperInc,
        info);
    cmp.single = single;
    return copyType(cmp);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final CmpR cmp && min == cmp.min && max == cmp.max &&
        Objects.equals(lower, cmp.lower) && lowerInc == cmp.lowerInc &&
        Objects.equals(upper, cmp.upper) && upperInc == cmp.upperInc && super.equals(obj);
  }

  @Override
  public String description() {
    return "range comparison";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(exact() ? plan.create(this, MIN, lower, MAX, upper, SINGLE, single) :
      plan.create(this, MIN, min, MAX, max, SINGLE, single), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    if(exact()) {
      if(lower != null) qs.token(expr).token(lowerInc ? ">=" : ">").token(lower);
      if(lower != null && upper != null) qs.token(AND);
      if(upper != null) qs.token(expr).token(upperInc ? "<=" : "<").token(upper);
    } else if(min == max) {
      qs.token(expr).token("=").token(min);
    } else {
      if(min != NEGATIVE_INFINITY) qs.token(expr).token(">=").token(min);
      if(min != NEGATIVE_INFINITY && max != POSITIVE_INFINITY) qs.token(AND);
      if(max != POSITIVE_INFINITY) qs.token(expr).token("<=").token(max);
    }
  }
}
