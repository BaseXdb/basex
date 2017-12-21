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
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.expr.path.Test.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Numeric range expression.
 *
 * @author BaseX Team 2005-17, BSD License
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
  private boolean atomic;

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
   * @param cmp expression to be converted
   * @param cc compilation context
   * @return new or original expression
   * @throws QueryException query exception
   */
  static Expr get(final CmpG cmp, final CompileContext cc) throws QueryException {
    final Expr cmp1 = cmp.exprs[0], cmp2 = cmp.exprs[1];
    final Type type1 = cmp1.seqType().type;
    if(cmp1.has(Flag.NDT) || (type1.isNumber() ? type1 == AtomType.DEC : !type1.isUntyped())) {
      return cmp;
    }

    // range sequence: retrieve start and end value
    if(cmp2 instanceof RangeSeq) {
      final RangeSeq seq = (RangeSeq) cmp2;
      final long[] range = seq.range(false);
      return get(cmp, range[0], range[1], cc);
    }
    // numbers: do not rewrite decimals that will be compared against numbers with fractional digits
    if(cmp2 instanceof ANum && (!(cmp2 instanceof Dec) || type1.isUntyped() ||
        type1.instanceOf(AtomType.ITR))) {
      final double d = ((ANum) cmp2).dbl();
      return get(cmp, d, d, cc);
    }
    return cmp;
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cmp expression to be converted
   * @param min minimum value
   * @param max maximum value (must be larger than end)
   * @param cc compilation context
   * @return new or original expression
   * @throws QueryException query exception
   */
  private static Expr get(final CmpG cmp, final double min, final double max,
      final CompileContext cc) throws QueryException {

    /* reject:
     * - input that cannot be compared as number
     * - numbers that are too large or small to be safely compared as doubles */
    final Expr cmp1 = cmp.exprs[0];
    if(!cmp1.seqType().type.isNumberOrUntyped() ||
        min < -MAX_INTEGER || min > MAX_INTEGER ||
        max < -MAX_INTEGER || max > MAX_INTEGER) return cmp;

    ParseExpr expr = null;
    final InputInfo ii = cmp.info;
    switch(cmp.op) {
      case EQ: expr = new CmpR(cmp1, min, max, ii); break;
      case GE: expr = new CmpR(cmp1, min, POSITIVE_INFINITY, ii); break;
      case GT: expr = new CmpR(cmp1, Math.nextUp(min), POSITIVE_INFINITY, ii); break;
      case LE: expr = new CmpR(cmp1, NEGATIVE_INFINITY, max, ii); break;
      case LT: expr = new CmpR(cmp1, NEGATIVE_INFINITY, Math.nextDown(max), ii); break;
      default:
    }
    return expr != null ? expr.optimize(cc) : cmp;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final SeqType st = expr.seqType();
    atomic = st.zeroOrOne() && !st.mayBeArray();
    return expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item item = expr.item(qc, info);
      return Bln.get(item != null && inRange(item.dbl(info)));
    }

    // pre-evaluate ranges
    if(expr instanceof Range || expr instanceof RangeSeq) {
      final Value value = expr.value(qc);
      if(value.isEmpty()) return Bln.FALSE;
      if(value instanceof Item) return Bln.get(inRange(((Item) value).dbl(info)));
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

  /**
   * Creates an intersection of the existing and the specified expressions.
   * @param c range comparison
   * @return resulting expression or {@code null}
   */
  Expr intersect(final CmpR c) {
    // skip intersection if expressions to be compared are different
    if(!c.expr.equals(expr)) return null;

    // remove comparisons that will never yield results
    final double mn = Math.max(min, c.min), mx = Math.min(max, c.max);
    if(mn > mx) return Bln.FALSE;

    // do not rewrite checks for identical values (will be evaluated faster by index)
    return new CmpR(c.expr, mn, mx, info);
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
    tb.add('[').addExt(min).add(',').addExt(max).add(']');
    ii.create(new RangeAccess(info, nr, ii.db), true, info, Util.info(OPTINDEX_X_X, "range", tb));
    return true;
  }

  /**
   * Retrieves the statistics key for the element/attribute name.
   * @param ii index info
   * @param type index type
   * @return key
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
        if(step.test.kind != Kind.NAME) return null;
      } else {
        step = path.step(st);
        if(!step.simple(Axis.ATTR, true)) return null;
      }
      test = (NameTest) step.test;
    }

    final Names names = type == IndexType.TEXT ? data.elemNames : data.attrNames;
    final Stats stats = names.stats(names.id(test.name.local()));
    return stats == null || StatsType.isNumeric(stats.type) ? stats : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final CmpR cmp = new CmpR(expr.copy(cc, vm), min, max, info);
    cmp.atomic = atomic;
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
  public void plan(final FElem plan) {
    addPlan(plan, planElem(MIN, min, MAX, max), expr);
  }

  @Override
  public String description() {
    return "range comparison";
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(PAREN1);
    if(min == max) {
      sb.append(expr).append(" = ").append(min);
    } else {
      if(min != NEGATIVE_INFINITY) sb.append(min).append(" <= ");
      sb.append(expr);
      if(max != POSITIVE_INFINITY) sb.append(" <= ").append(max);
    }
    return sb.append(PAREN2).toString();
  }
}
