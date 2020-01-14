package org.basex.query.expr;

import static java.lang.Double.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class CmpR extends Single {
  /** Maximum integer value that can be represented losslessly as double value. */
  private static final double MAX_INTEGER = 1L << 53;

  /** Minimum. */
  private final double min;
  /** Maximum. */
  private final double max;

  /** Evaluation flag: atomic evaluation. */
  private boolean single;

  /**
   * Constructor.
   * @param expr (compiled) expression
   * @param min minimum value
   * @param max maximum value
   * @param info input info
   */
  private CmpR(final Expr expr, final double min, final double max, final InputInfo info) {
    super(info, expr, SeqType.BLN_O);
    this.min = min;
    this.max = max;
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param expr expression to be compared
   * @param min minimum position
   * @param max minimum position (inclusive)
   * @param info input info
   * @return expression
   */
  private static Expr get(final Expr expr, final double min, final double max,
      final InputInfo info) {
    return min > max ? Bln.FALSE : min == NEGATIVE_INFINITY && max == POSITIVE_INFINITY ? Bln.TRUE :
      new CmpR(expr, min, max, info);
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cmp expression to be converted
   * @param cc compilation context
   * @return new or original expression
   * @throws QueryException query exception
   */
  static Expr get(final CmpG cmp, final CompileContext cc) throws QueryException {
    final Expr expr1 = cmp.exprs[0], expr2 = cmp.exprs[1];
    final Type type1 = expr1.seqType().type, type2 = expr2.seqType().type;

    // only rewrite deterministic comparisons if input is not decimal
    if(cmp.has(Flag.NDT) || !type1.isNumberOrUntyped() || type1 == AtomType.DEC) return cmp;

    // if value to be compared is a decimal, input must be untyped or integer
    if(!(expr2 instanceof ANum) || type2 == AtomType.DEC && !type1.isUntyped() &&
        !type1.instanceOf(AtomType.ITR)) return cmp;

    // reject numbers that are too large or small to be safely compared as doubles
    final double d = ((ANum) expr2).dbl();
    if(d < -MAX_INTEGER || d > MAX_INTEGER) return cmp;

    double mn = d, mx = d;
    switch(cmp.op) {
      case GE: mx = POSITIVE_INFINITY; break;
      case GT: mn = Math.nextUp(d); mx = POSITIVE_INFINITY; break;
      case LE: mn = NEGATIVE_INFINITY; break;
      case LT: mn = NEGATIVE_INFINITY; mx = Math.nextDown(d); break;
      // do not rewrite (non-)equality comparisons
      default: return cmp;
    }
    return get(expr1, mn, mx, cmp.info).optimize(cc);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = cc.simplifyAtom(expr);

    final SeqType st = expr.seqType();
    single = st.zeroOrOne() && !st.mayBeArray();

    Expr ex = this;
    if(expr instanceof Value) {
      ex = item(cc.qc, info);
    } else if(Function.POSITION.is(expr)) {
      final long mn = Math.max((long) Math.ceil(min), 1), mx = (long) Math.floor(max);
      ex = ItrPos.get(RangeSeq.get(mn, mx - mn + 1, true), OpV.EQ, info);
    }
    return cc.replaceWith(this, ex);
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(single) {
      final Item item = expr.item(qc, info);
      return Bln.get(item != Empty.VALUE && inRange(item.dbl(info)));
    }

    // pre-evaluate ranges
    if(expr instanceof Range || expr instanceof RangeSeq) {
      final Value value = expr.value(qc);
      final long size = value.size();
      if(size == 0) return Bln.FALSE;
      if(size == 1) return Bln.get(inRange(((Item) value).dbl(info)));
      final long[] range = ((RangeSeq) value).range(false);
      return Bln.get(range[1] >= min && range[0] <= max);
    }

    // iterative evaluation
    final Iter iter = expr.atomIter(qc, info);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(inRange(item.dbl(info))) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Checks if the specified value is within the allowed range.
   * @param value double value
   * @return result of check
   */
  private boolean inRange(final double value) {
    return value >= min && value <= max;
  }

  @Override
  public Expr merge(final Expr ex, final boolean union, final CompileContext cc) {
    if(!(ex instanceof CmpR)) return null;

    // do not merge if expressions to be compared are different
    final CmpR cmp = (CmpR) ex;
    if(!expr.equals(cmp.expr)) return null;

    // do not merge if ranges are exclusive
    if(union && (max < cmp.min || cmp.max < min)) return null;

    // merge min and max values
    final double mn = union ? Math.min(min, cmp.min) : Math.max(min, cmp.min);
    final double mx = union ? Math.max(max, cmp.max) : Math.min(max, cmp.max);
    return get(expr, mn, mx, info);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) {
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
    ii.costs = ii.costs(data, nr);
    if(ii.costs == null) return false;

    // skip if numbers are negative, doubles, or of different string length
    final int mnl = min >= 0 && (long) min == min ? token(min).length : -1;
    final int mxl = max >= 0 && (long) max == max ? token(max).length : -1;
    if(mnl != mxl || mnl == -1) return false;

    // don't use index if min/max values are infinite
    if(min == NEGATIVE_INFINITY && max == POSITIVE_INFINITY ||
        token((int) nr.min).length != token((int) nr.max).length) return false;

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
      if(test.part != NamePart.LOCAL) return null;
    }

    final Names names = type == IndexType.TEXT ? data.elemNames : data.attrNames;
    final Stats stats = names.stats(names.id(test.name.local()));
    return stats == null || StatsType.isNumeric(stats.type) ? stats : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final CmpR cmp = new CmpR(expr.copy(cc, vm), min, max, info);
    cmp.single = single;
    return cmp;
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
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, MIN, min, MAX, max, SINGLE, single), expr);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add(PAREN1);
    if(min == max) {
      tb.add(expr).add(" = ").add(min);
    } else {
      if(min != NEGATIVE_INFINITY) tb.add(expr).add(" >= ").add(min);
      if(min != NEGATIVE_INFINITY && max != POSITIVE_INFINITY) tb.add(' ').add(AND).add(' ');
      if(max != POSITIVE_INFINITY) tb.add(expr).add(" <= ").add(max);
    }
    return tb.add(PAREN2).toString();
  }
}
