package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.MemData;
import org.basex.data.Serializer;
import org.basex.data.StatsKey;
import org.basex.data.Data.IndexType;
import org.basex.index.RangeToken;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.path.Axis;
import org.basex.query.path.AxisPath;
import org.basex.query.path.NameTest;
import org.basex.query.path.Step;
import org.basex.query.path.Test.Kind;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Range comparison expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class CmpR extends Single {
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

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
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
  }

  /**
   * Creates an intersection of the existing and the specified expressions.
   * @param ex expression
   * @return resulting expression
   * @throws QueryException query exception
   */
  static Expr get(final ParseExpr ex) throws QueryException {
    if(ex instanceof CmpG || ex instanceof CmpV) {
      final Arr c = (Arr) ex;
      if(!c.pathAndItem(true)) return null;
      final Expr e = c.expr[0];
      final double d = ((Item) c.expr[1]).dbl(ex.input);
      switch(c instanceof CmpG ? ((CmpG) c).cmp.cmp : ((CmpV) c).cmp) {
        case EQ: return new CmpR(
            ex.input, e, d, true, d, true);
        case GE: return new CmpR(
            ex.input, e, d, true, Double.POSITIVE_INFINITY, true);
        case GT: return new CmpR(
            ex.input, e, d, false, Double.POSITIVE_INFINITY, true);
        case LE: return new CmpR(
            ex.input, e, Double.NEGATIVE_INFINITY, true, d, true);
        case LT: return new CmpR(
            ex.input, e, Double.NEGATIVE_INFINITY, true, d, false);
        default: return null;
      }
    }
    return null;
  }

  @Override
  public Bln atomic(final QueryContext ctx) throws QueryException {
    final Iter ir = ctx.iter(expr);

    // evaluate iterator
    boolean mn = false;
    boolean mx = false;
    Item it;
    while((it = ir.next()) != null) {
      final double d = it.dbl(input);
      mn |= mni ? d >= min : d > min;
      mx |= mxi ? d <= max : d < max;
      if(mn && mx) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Creates an intersection of the existing and the specified expressions.
   * @param c range comparison
   * @return resulting expression or null
   */
  Expr intersect(final CmpR c) {
    if(c == null || !c.expr.sameAs(expr)) return null;
    final double mn = Math.max(min, c.min);
    final double mx = Math.min(max, c.max);
    return mn > mx ? Bln.FALSE :
      new CmpR(input, c.expr, mn, mni && c.mni, mx, mxi && c.mxi);
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) {
    // accept only location path, string and equality expressions
    final Step s = CmpG.indexStep(expr);
    if(s == null || ic.data instanceof MemData) return false;

    final boolean text = ic.data.meta.txtindex && s.test.type == Type.TXT;
    final boolean attr = !text && ic.data.meta.atvindex &&
      s.simple(Axis.ATTR, true);

    // no text or attribute index applicable, min/max not included in range
    if(!text && !attr || !mni || !mxi || min == Double.NEGATIVE_INFINITY ||
        max == Double.POSITIVE_INFINITY) return false;

    final StatsKey key = getKey(ic, text);
    if(key == null) return false;

    rt = new RangeToken(text, Math.max(min, key.min), Math.min(max, key.max));

    // estimate costs for range access; all values out of range: no results
    ic.is = rt.min > rt.max || rt.max < key.min || rt.min > key.max ? 0 :
      Math.max(1, ic.data.meta.size / 5);
    return true;
  }

  @Override
  public AxisPath indexEquivalent(final IndexContext ic) {
    final Expr root = new RangeAccess(input, rt, ic);

    final AxisPath orig = (AxisPath) expr;
    final AxisPath path = orig.invertPath(root, ic.step);

    ic.ctx.compInfo(OPTRNGINDEX);
    if(rt.type() == IndexType.ATV) {
      // add attribute step
      final Step step = orig.step[0];
      Step[] steps = { Step.get(input, Axis.SELF, step.test) };
      for(final Step s : path.step) steps = Array.add(steps, s);
      path.step = steps;
    }
    return path;
  }

  /**
   * Retrieves the statistics key for the tag/attribute name.
   * @param ic index context
   * @param text text flag
   * @return key
   */
  private StatsKey getKey(final IndexContext ic, final boolean text) {
    // statistics are not up-to-date
    if(!ic.data.meta.uptodate || ic.data.ns.size() != 0) return null;

    final AxisPath path = (AxisPath) expr;
    final int st = path.step.length;
    if(text) {
      final Step step = st == 1 ? ic.step : path.step[st - 2];
      if(!(step.test.kind == Kind.NAME)) return null;
      final byte[] nm = ((NameTest) step.test).ln;
      return ic.data.tags.stat(ic.data.tags.id(nm));
    }

    final Step step = path.step[st - 1];
    return !step.simple(Axis.ATTR, true) ? null :
      ic.data.atts.stat(ic.data.atts.id(((NameTest) step.test).ln));
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.BLN;
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
