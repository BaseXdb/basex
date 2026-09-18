package org.basex.query.expr;

import static java.lang.Double.*;
import static org.basex.query.QueryText.*;

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

  /** Minimum. */
  final double min;
  /** Maximum. */
  final double max;

  /**
   * Constructor.
   * @param expr (compiled) expression
   * @param min minimum value
   * @param max maximum value
   * @param info input info (can be {@code null})
   */
  private CmpR(final Expr expr, final double min, final double max, final InputInfo info) {
    super(expr, info);
    this.min = min;
    this.max = max;
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
      new CmpR(expr, min, max, info).optimize(cc);
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
    } else if(expr2 instanceof final ANum num && !(num instanceof Dec && int1)) {
      mn = num.dbl();
      mx = mn;
    } else {
      return cmp;
    }
    // integer comparisons: reject numbers that are too large to be safely compared as doubles
    if(int1 && (Math.abs(mn) >= MAX_INTEGER || Math.abs(mx) >= MAX_INTEGER)) return cmp;

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
    return value >= min && value <= max;
  }

  @Override
  boolean inRange(final RangeSeq seq) {
    return seq.max() >= min && seq.min() <= max;
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {

    Double newMin = null, newMax = null;
    if(ex instanceof final CmpR cmp) {
      newMin = cmp.min;
      newMax = cmp.max;
    } else if(ex instanceof final CmpG cmp && cmp.op == CmpOp.EQ &&
        ex.arg(1) instanceof final ANum num) {
      newMin = num.dbl();
      newMax = newMin;
    }
    if(newMin == null || !expr.equals(ex.arg(0)) || or && (max < newMin || min > newMax))
      return null;

    // determine common minimum and maximum value
    newMin = or ? Math.min(min, newMin) : Math.max(min, newMin);
    newMax = or ? Math.max(max, newMax) : Math.min(max, newMax);
    return get(cc, info, expr, newMin, newMax);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    final Data data = ii.db.data();
    // sequential main memory scan is usually faster than range index access
    if(data == null ? !ii.enforce() : data.inMemory()) return false;

    final IndexType type = ii.type(expr, null);
    if(type == null) return false;

    // try the value index first: when the targeted node has integer-category statistics,
    // the double range can be projected losslessly onto a long range and looked up exactly
    final long lmin = (long) Math.ceil(min), lmax = (long) Math.floor(max);
    if(lmin <= lmax) {
      final IndexCosts costs = ii.costs;
      if(ii.create(lmin, lmax, info)) return true;
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

    final TokenBuilder tb = new TokenBuilder();
    tb.add('[').add(min).add(',').add(max).add(']');
    return ii.create(new RangeAccess(info, nr, ii.db), true,
        Util.info(OPTINDEX_X_X, "range", tb), info);
  }

  @Override
  Expr with(final Expr operand, final CompileContext cc) throws QueryException {
    return get(cc, info, operand, min, max);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final CmpR cmp = new CmpR(expr.copy(cc, vm), min, max, info);
    cmp.single = single;
    return copyType(cmp);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final CmpR cmp && min == cmp.min && max == cmp.max &&
        super.equals(obj);
  }

  @Override
  public String description() {
    return "range comparison";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, MIN, min, MAX, max, SINGLE, single), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    if(min == max) {
      qs.token(expr).token("=").token(min);
    } else {
      if(min != NEGATIVE_INFINITY) qs.token(expr).token(">=").token(min);
      if(min != NEGATIVE_INFINITY && max != POSITIVE_INFINITY) qs.token(AND);
      if(max != POSITIVE_INFINITY) qs.token(expr).token("<=").token(max);
    }
  }
}
