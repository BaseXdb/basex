package org.basex.query.expr;

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
  /** Minimum. */
  final double min;
  /** Include minimum value. */
  final boolean mni;
  /** Maximum. */
  final double max;
  /** Include maximum value. */
  final boolean mxi;
  /** Check for equality. */
  private final boolean eq;

  /** Evaluation flag: atomic evaluation. */
  private boolean atomic;

  /**
   * Constructor.
   * @param expr (compiled) expression
   * @param min minimum value
   * @param mni include minimum value
   * @param max maximum value
   * @param mxi include maximum value
   * @param info input info
   */
  private CmpR(final Expr expr, final double min, final boolean mni, final double max,
      final boolean mxi, final InputInfo info) {

    super(info, expr, SeqType.BLN_O);
    this.min = min;
    this.mni = mni;
    this.max = max;
    this.mxi = mxi;
    eq = min == max;
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
    if(cmp1.has(Flag.NDT)) return cmp;

    // range sequence: retrieve start and end value, switch them if order is descending
    if(cmp2 instanceof RangeSeq) {
      final RangeSeq seq = (RangeSeq) cmp2;
      final long[] range = seq.range(false);
      return get(cmp, range[0], range[1], cc);
    }
    // range expression uses doubles: reject decimal numbers and typed input
    if(cmp2 instanceof ANum && (!(cmp2 instanceof Dec) || cmp1.seqType().type.isUntyped())) {
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
        min < Integer.MIN_VALUE || min > Integer.MAX_VALUE ||
        max < Integer.MIN_VALUE || max > Integer.MAX_VALUE) return cmp;

    ParseExpr expr = null;
    switch(cmp.op) {
      case EQ: expr = new CmpR(cmp1, min, true, max, true, cmp.info); break;
      case GE: expr = new CmpR(cmp1, min, true, Double.POSITIVE_INFINITY, true, cmp.info); break;
      case GT: expr = new CmpR(cmp1, min, false, Double.POSITIVE_INFINITY, true, cmp.info); break;
      case LE: expr = new CmpR(cmp1, Double.NEGATIVE_INFINITY, true, max, true, cmp.info); break;
      case LT: expr = new CmpR(cmp1, Double.NEGATIVE_INFINITY, true, max, false, cmp.info); break;
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
      if(item == null) return Bln.FALSE;
      final double d = item.dbl(info);
      return Bln.get(eq ? d == min : (mni ? d >= min : d > min) && (mxi ? d <= max : d < max));
    }

    // iterative evaluation
    final Iter iter = expr.atomIter(qc, info);
    for(Item item; (item = qc.next(iter)) != null;) {
      final double d = item.dbl(info);
      if((mni ? d >= min : d > min) && (mxi ? d <= max : d < max)) return Bln.TRUE;
    }
    return Bln.FALSE;
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
    return new CmpR(c.expr, mn, mni && c.mni, mx, mxi && c.mxi, info);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) {
    // accept only location path, string and equality expressions
    final Data data = ii.db.data();
    // sequential main memory scan is usually faster than range index access
    if(data == null ? !ii.enforce() : data.inMemory()) return false;

    if(!mni || !mxi) return false;
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
    if(min == Double.NEGATIVE_INFINITY && max == Double.POSITIVE_INFINITY ||
        token((int) nr.min).length != token((int) nr.max).length) return false;

    final TokenBuilder tb = new TokenBuilder();
    tb.add(mni ? '[' : '(').addExt(min).add(',').addExt(max).add(mxi ? ']' : ')');
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
    final CmpR cmp = new CmpR(expr.copy(cc, vm), min, mni, max, mxi, info);
    cmp.atomic = atomic;
    return cmp;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof CmpR)) return false;
    final CmpR c = (CmpR) obj;
    return min == c.min && mni == c.mni && max == c.max && mxi && c.mxi && super.equals(obj);
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
      if(min != Double.NEGATIVE_INFINITY) sb.append(min).append(mni ? " <= " : " < ");
      sb.append(expr);
      if(max != Double.POSITIVE_INFINITY) sb.append(mxi ? " <= " : " < ").append(max);
    }
    return sb.append(PAREN2).toString();
  }
}
