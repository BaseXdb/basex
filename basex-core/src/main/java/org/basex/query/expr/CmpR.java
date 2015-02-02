package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.expr.path.Test.Kind;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
 * @author BaseX Team 2005-15, BSD License
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
  /** Flag for atomic evaluation. */
  private final boolean atomic;

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

    super(info, expr);
    this.min = min;
    this.mni = mni;
    this.max = max;
    this.mxi = mxi;
    seqType = SeqType.BLN;
    final SeqType st = expr.seqType();
    atomic = st.zeroOrOne() && !st.mayBeArray();
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cmp expression to be converted
   * @return new or original expression
   */
  static ParseExpr get(final CmpG cmp) {
    final Expr e = cmp.exprs[1];
    if(e instanceof RangeSeq) {
      final RangeSeq rs = (RangeSeq) e;
      return get(cmp, rs.start(), rs.end());
    }
    if(e instanceof ANum) {
      final double d = ((ANum) cmp.exprs[1]).dbl();
      return get(cmp, d, d);
    }
    return cmp;
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cmp expression to be converted
   * @param start start
   * @param end end (must be larger than end)
   * @return new or original expression
   */
  private static ParseExpr get(final CmpG cmp, final double start, final double end) {
    final Expr e = cmp.exprs[0];
    // type must be numeric
    if(!e.seqType().type.isNumberOrUntyped()) return cmp;
    switch(cmp.op) {
      case EQ: return new CmpR(e, start, true, end, true, cmp.info);
      case GE: return new CmpR(e, start, true, Double.POSITIVE_INFINITY, true, cmp.info);
      case GT: return new CmpR(e, start, false, Double.POSITIVE_INFINITY, true, cmp.info);
      case LE: return new CmpR(e, Double.NEGATIVE_INFINITY, true, end, true, cmp.info);
      case LT: return new CmpR(e, Double.NEGATIVE_INFINITY, true, end, false, cmp.info);
      default: return cmp;
    }
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item it = expr.item(qc, info);
      if(it == null) return Bln.FALSE;
      final double d = it.dbl(info);
      return Bln.get((mni ? d >= min : d > min) && (mxi ? d <= max : d < max));
    }

    // iterative evaluation
    final Iter ir = expr.atomIter(qc, info);
    for(Item it; (it = ir.next()) != null;) {
      final double d = it.dbl(info);
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
    if(!c.expr.sameAs(expr)) return null;

    // find common minimum and maximum value
    final double mn = Math.max(min, c.min);
    final double mx = Math.min(max, c.max);

    // remove comparisons that will never yield results
    if(mn > mx) return Bln.FALSE;
    // do not rewrite checks for identical values (will be evaluated faster by index)
    return new CmpR(c.expr, mn, mni && c.mni, mx, mxi && c.mxi, info);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) {
    // accept only location path, string and equality expressions
    final Data data = ii.ic.data;
    // sequential main memory scan is assumed to be faster than range index access
    if(!mni || !mxi || data.inMemory() || !ii.check(expr, false)) return false;

    final Stats key = key(ii, ii.text);
    if(key == null) return false;

    // estimate costs for range access; all values out of range: no results
    final NumericRange nr = new NumericRange(ii.text,
        Math.max(min, key.min), Math.min(max, key.max));

    // skip queries with no results
    if(nr.min > nr.max || nr.max < key.min || nr.min > key.max) {
      ii.costs = 0;
      return true;
    }

    // skip if numbers are negative, doubles, or of different string length
    final int mnl = min >= 0 && (long) min == min ? token(min).length : -1;
    final int mxl = max >= 0 && (long) max == max ? token(max).length : -1;
    if(mnl != mxl || mnl == -1) return false;

    // estimate costs (you conservative value)
    ii.costs = Math.max(1, data.meta.size / 3);

    // don't use index if min/max values are infinite
    if(min == Double.NEGATIVE_INFINITY && max == Double.POSITIVE_INFINITY ||
        Token.token((int) nr.min).length != Token.token((int) nr.max).length) return false;

    final TokenBuilder tb = new TokenBuilder();
    tb.add(mni ? '[' : '(').addExt(min).add(',').addExt(max).add(mxi ? ']' : ')');
    ii.create(new RangeAccess(info, nr, ii.ic), info, Util.info(OPTRNGINDEX, tb), true);
    return true;
  }

  /**
   * Retrieves the statistics key for the element/attribute name.
   * @param ii index info
   * @param text text flag
   * @return key
   */
  private Stats key(final IndexInfo ii, final boolean text) {
    // statistics are not up-to-date
    final Data data = ii.ic.data;
    if(!data.meta.uptodate || data.nspaces.size() != 0 || !(expr instanceof AxisPath)) return null;

    byte[] name = ii.name;
    if(name == null) {
      final Step step;
      final AxisPath path = (AxisPath) expr;
      final int st = path.steps.length - 1;
      if(text) {
        step = st == 0 ? ii.step : path.step(st - 1);
        if(step.test.kind != Kind.NAME) return null;
      } else {
        step = path.step(st);
        if(!step.simple(Axis.ATTR, true)) return null;
      }
      name = ((NameTest) step.test).local;
    }

    final Names names = text ? data.elemNames : data.attrNames;
    final Stats key = names.stat(names.id(name));
    return key == null || key.type == StatsType.INTEGER ||
        key.type == StatsType.DOUBLE ? key : null;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CmpR(expr.copy(qc, scp, vs), min, mni, max, mxi, info);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(MIN, min, MAX, max), expr);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(min == max) {
      sb.append(expr).append(" = ").append(min);
    } else {
      if(min != Double.NEGATIVE_INFINITY) sb.append(min).append(mni ? " <= " : " < ");
      sb.append(expr);
      if(max != Double.POSITIVE_INFINITY) sb.append(mxi ? " <= " : " < ").append(max);
    }
    return sb.toString();
  }
}
