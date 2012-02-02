package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.io.IOException;

import org.basex.data.MemData;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.StatsType;
import org.basex.index.Names;
import org.basex.index.RangeToken;
import org.basex.index.Stats;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.path.Axis;
import org.basex.query.path.AxisPath;
import org.basex.query.path.AxisStep;
import org.basex.query.path.NameTest;
import org.basex.query.path.Test.Name;
import org.basex.query.util.IndexContext;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Range comparison expression.
 *
 * @author BaseX Team 2005-12, BSD License
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
  private RangeToken rt;
  /** Flag for atomic evaluation. */
  private final boolean atomic;

  /**
   * Constructor.
   * @param ii input info
   * @param e (compiled) expression
   * @param mn minimum value
   * @param in include minimum value
   * @param mx maximum value
   * @param ix include maximum value
   */
  private CmpR(final InputInfo ii, final Expr e, final double mn,
      final boolean in, final double mx, final boolean ix) {
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
   * @param ex expression
   * @return resulting or specified expression
   * @throws QueryException query exception
   */
  static Expr get(final CmpG ex) throws QueryException {
    if(!ex.expr[1].isItem()) return ex;
    final Item it = (Item) ex.expr[1];
    if(!it.type.isNumber()) return ex;

    final Expr e = ex.expr[0];
    final double d = it.dbl(ex.input);
    switch(ex.op.op) {
      case EQ:
        return new CmpR(ex.input, e, d, true, d, true);
      case GE:
        return new CmpR(ex.input, e, d, true, Double.POSITIVE_INFINITY, true);
      case GT:
        return new CmpR(ex.input, e, d, false, Double.POSITIVE_INFINITY, true);
      case LE:
        return new CmpR(ex.input, e, Double.NEGATIVE_INFINITY, true, d, true);
      case LT:
        return new CmpR(ex.input, e, Double.NEGATIVE_INFINITY, true, d, false);
      default:
        return ex;
    }
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item it = expr.item(ctx, input);
      if(it == null) return Bln.FALSE;
      final double d = it.dbl(input);
      return Bln.get((mni ? d >= min : d > min) && (mxi ? d <= max : d < max));
    }

    // iterative evaluation
    final Iter ir = ctx.iter(expr);
    for(Item it; (it = ir.next()) != null;) {
      final double d = it.dbl(input);
      if((mni ? d >= min : d > min) && (mxi ? d <= max : d < max))
        return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Creates an intersection of the existing and the specified expressions.
   * @param c range comparison
   * @return resulting expression or {@code null}
   */
  Expr intersect(final CmpR c) {
    if(!c.expr.sameAs(expr)) return null;
    final double mn = Math.max(min, c.min);
    final double mx = Math.min(max, c.max);
    return mn > mx ? Bln.FALSE :
      new CmpR(input, c.expr, mn, mni && c.mni, mx, mxi && c.mxi);
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) {
    // accept only location path, string and equality expressions
    final AxisStep s = CmpG.indexStep(expr);
    // sequential main memory is assumed to be faster than range index access
    if(s == null || ic.data instanceof MemData) return false;

    // check which index applies
    final boolean text = s.test.type == NodeType.TXT && ic.data.meta.textindex;
    final boolean attr = s.test.type == NodeType.ATT && ic.data.meta.attrindex;
    if(!text && !attr || !mni || !mxi) return false;

    final Stats key = key(ic, text);
    if(key == null) return false;

    // estimate costs for range access; all values out of range: no results
    rt = new RangeToken(text, Math.max(min, key.min), Math.min(max, key.max));
    ic.costs(rt.min > rt.max || rt.max < key.min || rt.min > key.max ? 0 :
      Math.max(1, ic.data.meta.size / 5));

    // use index if costs are zero, or if min/max is not infinite
    return ic.costs() == 0 || min != Double.NEGATIVE_INFINITY &&
      max != Double.POSITIVE_INFINITY;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) {
    final boolean text = rt.type() == IndexType.TEXT;
    ic.ctx.compInfo(OPTRNGINDEX);
    return ic.invert(expr, new RangeAccess(input, rt, ic), text);
  }

  /**
   * Retrieves the statistics key for the tag/attribute name.
   * @param ic index context
   * @param text text flag
   * @return key
   */
  private Stats key(final IndexContext ic, final boolean text) {
    // statistics are not up-to-date
    if(!ic.data.meta.uptodate || ic.data.nspaces.size() != 0) return null;

    final AxisPath path = (AxisPath) expr;
    final int st = path.steps.length;

    AxisStep step;
    if(text) {
      step = st == 1 ? ic.step : path.step(st - 2);
      if(!(step.test.test == Name.NAME)) return null;
    } else {
      step = path.step(st - 1);
      if(!step.simple(Axis.ATTR, true)) return null;
    }

    final Names names = text ? ic.data.tagindex : ic.data.atnindex;
    final Stats key = names.stat(names.id(((NameTest) step.test).ln));
    return key == null || key.type == StatsType.INTEGER ||
        key.type == StatsType.DOUBLE ? key : null;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, MIN, Token.token(min), MAX, Token.token(max));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(min != Double.NEGATIVE_INFINITY) sb.append(min + (mni ? " <= " : " < "));
    sb.append(expr);
    if(max != Double.POSITIVE_INFINITY) sb.append((mxi ? " <= " : " < ") + max);
    return sb.toString();
  }
}
