package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.path.Test.Kind;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Numeric range expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CmpR extends Single {
  /** Minimum. */
  private final double min;
  /** Include minimum value. */
  private final boolean mni;
  /** Maximum. */
  private final double max;
  /** Include maximum value. */
  private final boolean mxi;
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
    type = SeqType.BLN;
    atomic = expr.type().zeroOrOne();
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cmp expression to be converted
   * @return new or original expression
   */
  static Expr get(final CmpG cmp) {
    if(!(cmp.exprs[1] instanceof ANum)) return cmp;
    final double d = ((ANum) cmp.exprs[1]).dbl();
    final Expr expr = cmp.exprs[0];
    switch(cmp.op.op) {
      case GE: return new CmpR(expr, d, true, Double.POSITIVE_INFINITY, true, cmp.info);
      case GT: return new CmpR(expr, d, false, Double.POSITIVE_INFINITY, true, cmp.info);
      case LE: return new CmpR(expr, Double.NEGATIVE_INFINITY, true, d, true, cmp.info);
      case LT: return new CmpR(expr, Double.NEGATIVE_INFINITY, true, d, false, cmp.info);
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
    final Iter ir = qc.iter(expr);
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
    if(mn == mx) {
      // return simplified comparison for exact hit, or false if value is not included
      return mni && mxi ? new CmpG(expr, Dbl.get(mn), CmpG.OpG.EQ, null, info)
                        : Bln.FALSE;
    }
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
    ii.costs = nr.min > nr.max || nr.max < key.min || nr.min > key.max ? 0 :
      Math.max(1, data.meta.size / 5);

    // skip queries with no results
    if(ii.costs == 0) return true;
    // don't use index if min/max values are infinite
    if(min == Double.NEGATIVE_INFINITY && max == Double.POSITIVE_INFINITY) return false;

    final TokenBuilder tb = new TokenBuilder();
    tb.add(mni ? '[' : '(').addExt(min).add(',').addExt(max).add(mxi ? ']' : ')');
    ii.create(new RangeAccess(info, nr, ii.ic), info, Util.info(OPTRNGINDEX, tb), true);
    return true;
  }

  /**
   * Retrieves the statistics key for the tag/attribute name.
   * @param ii index info
   * @param text text flag
   * @return key
   */
  private Stats key(final IndexInfo ii, final boolean text) {
    // statistics are not up-to-date
    final Data data = ii.ic.data;
    if(!data.meta.uptodate || data.nspaces.size() != 0) return null;

    byte[] name = ii.name;
    if(name == null) {
      final Step step;
      final AxisPath path = (AxisPath) expr;
      final int st = path.steps.length - 1;
      if(text) {
        step = st == 0 ? ii.step : path.step(st - 1);
        if(step.test.kind != Kind.NAME) return null;
      } else {
        step =  path.step(st);
        if(!step.simple(Axis.ATTR, true)) return null;
      }
      name = ((NameTest) step.test).local;
    }

    final Names names = text ? data.tagindex : data.atnindex;
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
    if(min != Double.NEGATIVE_INFINITY) sb.append(min).append(mni ? " <= " : " < ");
    sb.append(expr);
    if(max != Double.POSITIVE_INFINITY) sb.append(mxi ? " <= " : " < ").append(max);
    return sb.toString();
  }
}
