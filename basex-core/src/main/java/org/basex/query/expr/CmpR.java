package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.path.Test.Mode;
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
 * @author BaseX Team 2005-13, BSD License
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
  /** Index container. */
  private NumericRange rt;
  /** Flag for atomic evaluation. */
  private final boolean atomic;

  /**
   * Constructor.
   * @param e (compiled) expression
   * @param mn minimum value
   * @param in include minimum value
   * @param mx maximum value
   * @param ix include maximum value
   * @param ii input info
   */
  private CmpR(final Expr e, final double mn, final boolean in, final double mx,
      final boolean ix, final InputInfo ii) {

    super(ii, e);
    min = mn;
    mni = in;
    max = mx;
    mxi = ix;
    type = SeqType.BLN;
    atomic = e.type().zeroOrOne();
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param ex expression to be converted
   * @return new or original expression
   */
  static Expr get(final CmpG ex) {
    if(!(ex.expr[1] instanceof ANum)) return ex;
    final double d = ((ANum) ex.expr[1]).dbl();
    final Expr e = ex.expr[0];
    switch(ex.op.op) {
      case GE: return new CmpR(e, d, true, Double.POSITIVE_INFINITY, true, ex.info);
      case GT: return new CmpR(e, d, false, Double.POSITIVE_INFINITY, true, ex.info);
      case LE: return new CmpR(e, Double.NEGATIVE_INFINITY, true, d, true, ex.info);
      case LT: return new CmpR(e, Double.NEGATIVE_INFINITY, true, d, false, ex.info);
      default: return ex;
    }
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item it = expr.item(ctx, info);
      if(it == null) return Bln.FALSE;
      final double d = it.dbl(info);
      return Bln.get((mni ? d >= min : d > min) && (mxi ? d <= max : d < max));
    }

    // iterative evaluation
    final Iter ir = ctx.iter(expr);
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
      return mni && mxi ? new CmpG(expr, Dbl.get(mn), CmpG.OpG.EQ, info) : Bln.FALSE;
    }

    return new CmpR(c.expr, mn, mni && c.mni, mx, mxi && c.mxi, info);
  }

  @Override
  public boolean indexAccessible(final IndexCosts ic) {
    // accept only location path, string and equality expressions
    final Step s = CmpG.indexStep(expr);
    // sequential main memory is assumed to be faster than range index access
    final Data data = ic.ictx.data;
    if(s == null || data.inMemory()) return false;

    // check which index applies
    final boolean text = s.test.type == NodeType.TXT && data.meta.textindex;
    final boolean attr = s.test.type == NodeType.ATT && data.meta.attrindex;
    if(!text && !attr || !mni || !mxi) return false;

    final Stats key = key(ic, text);
    if(key == null) return false;

    // estimate costs for range access; all values out of range: no results
    rt = new NumericRange(text ? IndexType.TEXT : IndexType.ATTRIBUTE,
        Math.max(min, key.min), Math.min(max, key.max));
    ic.costs(rt.min > rt.max || rt.max < key.min || rt.min > key.max ? 0 :
      Math.max(1, data.meta.size / 5));

    // use index if costs are zero, or if min/max is not infinite
    return ic.costs() == 0 || min != Double.NEGATIVE_INFINITY &&
        max != Double.POSITIVE_INFINITY;
  }

  @Override
  public Expr indexEquivalent(final IndexCosts ic) {
    final boolean text = rt.type() == IndexType.TEXT;
    ic.ctx.compInfo(OPTRNGINDEX);
    return ic.invert(expr, new RangeAccess(info, rt, ic.ictx), text);
  }

  /**
   * Retrieves the statistics key for the tag/attribute name.
   * @param ic index context
   * @param text text flag
   * @return key
   */
  private Stats key(final IndexCosts ic, final boolean text) {
    // statistics are not up-to-date
    final Data data = ic.ictx.data;
    if(!data.meta.uptodate || data.nspaces.size() != 0) return null;

    final AxisPath path = (AxisPath) expr;
    final int st = path.steps.length;

    final Step step;
    if(text) {
      step = st == 1 ? ic.step : path.step(st - 2);
      if(!(step.test.mode == Mode.LN)) return null;
    } else {
      step = path.step(st - 1);
      if(!step.simple(Axis.ATTR, true)) return null;
    }

    final Names names = text ? data.tagindex : data.atnindex;
    final Stats key = names.stat(names.id(((NameTest) step.test).ln));
    return key == null || key.type == StatsType.INTEGER ||
        key.type == StatsType.DOUBLE ? key : null;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new CmpR(expr.copy(ctx, scp, vs), min, mni, max, mxi, info);
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
