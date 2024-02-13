package org.basex.query.expr;

import static java.lang.Double.*;
import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.index.*;
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
 * Numeric range expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CmpR extends Single {
  /** Maximum integer value that can be represented losslessly as double value. */
  private static final double MAX_INTEGER = 1L << 53;

  /** Minimum. */
  final double min;
  /** Maximum. */
  final double max;

  /** Evaluation flag: atomic evaluation. */
  private boolean single;

  /**
   * Constructor.
   * @param expr (compiled) expression
   * @param min minimum value
   * @param max maximum value
   * @param info input info (can be {@code null})
   */
  private CmpR(final Expr expr, final double min, final double max, final InputInfo info) {
    super(info, expr, SeqType.BOOLEAN_O);
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
    final boolean int1 = type1.instanceOf(AtomType.INTEGER);
    if(!(type1.isUntyped() || type1.oneOf(AtomType.FLOAT, AtomType.DOUBLE) || int1))
      return cmp;

    double mn, mx;
    if(expr2 instanceof RangeSeq) {
      final RangeSeq rs = (RangeSeq) expr2;
      mn = rs.min();
      mx = rs.max();
    } else if(expr2 instanceof ANum && !(expr2 instanceof Dec && int1)) {
      mn = ((ANum) expr2).dbl();
      mx = mn;
    } else {
      return cmp;
    }
    // integer comparisons: reject numbers that are too large to be safely compared as doubles
    if(int1 && (Math.abs(mn) >= MAX_INTEGER || Math.abs(mx) >= MAX_INTEGER)) return cmp;

    switch(cmp.op) {
      case GE: mx = POSITIVE_INFINITY; break;
      case GT: mn = Math.nextUp(mn); mx = POSITIVE_INFINITY; break;
      case LE: mn = NEGATIVE_INFINITY; break;
      case LT: mn = NEGATIVE_INFINITY; mx = Math.nextDown(mx); break;
      // do not rewrite (non-)equality comparisons
      default: return cmp;
    }
    return get(cc, cmp.info, expr1, mn, mx);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.NUMBER, cc);

    final SeqType st = expr.seqType();
    single = st.zeroOrOne() && !st.mayBeArray();

    if(Function.POSITION.is(expr)) {
      // E[let $p := position() return $p > .1e0]
      final long mn = Math.max((long) Math.ceil(min), 1), size = (long) Math.floor(max) - mn + 1;
      final Expr pos = RangeSeq.get(mn, size, true).optimizePos(OpV.EQ, cc);
      return cc.replaceWith(this, pos instanceof Bln ? pos : IntPos.get(pos, OpV.EQ, info));
    }

    return expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(single) {
      final Item item = expr.item(qc, info);
      return Bln.get(!item.isEmpty() && inRange(item));
    }

    // pre-evaluate ranges
    if(expr instanceof Range || expr instanceof RangeSeq) {
      final Value value = expr.value(qc);
      final long size = value.size();
      if(size == 0) return Bln.FALSE;
      if(size == 1) return Bln.get(inRange((Item) value));
      final RangeSeq rs = (RangeSeq) value;
      return Bln.get(rs.max() >= min && rs.min() <= max);
    }

    // iterative evaluation
    final Iter iter = expr.atomIter(qc, info);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(inRange(item)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Checks if the specified value is within the allowed range.
   * @param item value to check
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean inRange(final Item item) throws QueryException {
    final double value = item.dbl(info);
    return value >= min && value <= max;
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {

    Double newMin = null, newMax = null;
    if(ex instanceof CmpR) {
      final CmpR cmp = (CmpR) ex;
      newMin = cmp.min;
      newMax = cmp.max;
    } else if(ex instanceof CmpG && ((CmpG) ex).op == OpG.EQ && ex.arg(1) instanceof ANum) {
      newMin = ((ANum) ex.arg(1)).dbl();
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
    // accept only location path, string and equality expressions
    final Data data = ii.db.data();
    // sequential main memory scan is usually faster than range index access
    if(data == null ? !ii.enforce() : data.inMemory()) return false;

    final IndexType type = ii.type(expr, null);
    if(type == null) return false;

    final Stats key = key(ii, type);
    if(key == null) return false;

    // estimate costs for range access; all values out of range: no results
    final NumericRange nr = new NumericRange(type, Math.max(min, key.min), Math.min(max, key.max));
    // skip queries with no results
    if(nr.min > nr.max || nr.max < key.min || nr.min > key.max) {
      ii.costs = IndexCosts.get(0);
      return true;
    }

    // estimate costs
    ii.costs = IndexInfo.costs(data, nr);
    if(ii.costs == null) return false;

    // skip if numbers are negative, doubles, or of different string length
    final int mnl = min >= 0 && (long) min == min ? Token.token(min).length : -1;
    final int mxl = max >= 0 && (long) max == max ? Token.token(max).length : -1;
    if(mnl == -1 || mnl != mxl) return false;

    // don't use index if min/max values are infinite
    if(Token.token((int) nr.min).length != Token.token((int) nr.max).length) return false;

    final TokenBuilder tb = new TokenBuilder();
    tb.add('[').add(min).add(',').add(max).add(']');
    ii.create(new RangeAccess(info, nr, ii.db), true, Util.info(OPTINDEX_X_X, "range", tb), info);
    return true;
  }

  /**
   * Retrieves the statistics key for the element/attribute name.
   * @param ii index info
   * @param type index type
   * @return key, or {@code null} if statistics are not available
   */
  private Stats key(final IndexInfo ii, final IndexType type) {
    // statistics are not up-to-date
    final Data data = ii.db.data();
    if(data == null || !data.meta.uptodate || !data.nspaces.isEmpty() ||
        !(expr instanceof AxisPath)) return null;

    NameTest test = ii.test;
    if(test == null) {
      final Step step;
      final AxisPath path = (AxisPath) expr;
      final int st = path.steps.length - 1;
      if(type == IndexType.TEXT) {
        step = st == 0 ? ii.step : path.step(st - 1);
      } else {
        step = path.step(st);
        if(step.axis != Axis.ATTRIBUTE || step.exprs.length > 0) return null;
      }
      if(!(step.test instanceof NameTest)) return null;
      test = (NameTest) step.test;
      if(test.part() != NamePart.LOCAL) return null;
    }

    final Names names = type == IndexType.TEXT ? data.elemNames : data.attrNames;
    final Stats stats = names.stats(names.id(test.qname.local()));
    return stats != null && StatsType.isNumeric(stats.type) ? stats : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final CmpR cmp = new CmpR(expr.copy(cc, vm), min, max, info);
    cmp.single = single;
    return copyType(cmp);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof CmpR)) return false;
    final CmpR c = (CmpR) obj;
    return min == c.min && max == c.max && super.equals(obj);
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
