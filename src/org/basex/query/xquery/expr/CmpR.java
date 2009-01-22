package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.data.StatsKey;
import org.basex.index.IndexToken;
import org.basex.index.RangeToken;
import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.path.Axis;
import org.basex.query.xquery.path.AxisPath;
import org.basex.query.xquery.path.NameTest;
import org.basex.query.xquery.path.Step;
import org.basex.query.xquery.path.Test.Kind;
import org.basex.util.Array;

/**
 * Range comparison expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  
  /**
   * Constructor.
   * @param e expression
   * @param mn minimum value
   * @param in include minimum value
   * @param mx maximum value
   * @param ix include maximum value
   */
  private CmpR(final Expr e, final double mn, final boolean in,
      final double mx, final boolean ix) {
    super(e);
    min = mn;
    mni = in;
    max = mx;
    mxi = ix;
  }
  
  /**
   * Returns a range or an optimized expression.
   * @param e expression
   * @param mn minimum value
   * @param in include minimum value
   * @param mx maximum value
   * @param ix include maximum value
   * @return expression
   */
  public static Expr get(final CmpR e, final double mn, final boolean in,
      final double mx, final boolean ix) {
    return mn > mx ? Bln.FALSE : new CmpR(e.expr, mn, in, mx, ix);
  }

  /**
   * Creates an intersection of the existing and the specified expressions.
   * @param ex expression
   * @return resulting expression
   * @throws XQException query exception
   */
  public static Expr get(final Expr ex) throws XQException {
    if(ex instanceof CmpG || ex instanceof CmpV) {
      final Arr c = (Arr) ex;
      if(!c.standard(true)) return null;
      final Expr e = c.expr[0];
      final double d = ((Item) c.expr[1]).dbl();
      switch(c instanceof CmpG ? ((CmpG) c).cmp.cmp : ((CmpV) c).cmp) {
        case GE: return new CmpR(e, d, true, Double.POSITIVE_INFINITY, true);
        case GT: return new CmpR(e, d, false, Double.POSITIVE_INFINITY, true);
        case LE: return new CmpR(e, Double.NEGATIVE_INFINITY, true, d, true);
        case LT: return new CmpR(e, Double.NEGATIVE_INFINITY, true, d, false);
        default: return null;
      }
    }
    return null;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    return Bln.get(eval(ctx)).iter();
  }

  /**
   * Evaluates the comparison operator.
   * @param ctx query context
   * @return result of check
   * @throws XQException evaluation exception
   */
  private boolean eval(final XQContext ctx) throws XQException {
    final Iter ir = ctx.iter(expr);

    // evaluate iterator
    boolean mn = false;
    boolean mx = false;
    Item it;
    while((it = ir.next()) != null) {
      final double d = it.dbl();
      mn |= mni ? d >= min : d > min;
      mx |= mxi ? d <= max : d < max;
      if(mn && mx) return true;
    }
    return false;
  }

  /**
   * Creates an intersection of the existing and the specified expressions.
   * @param c range comparison
   * @return resulting expression or null
   */
  public Expr intersect(final CmpR c) {
    if(c == null || !c.expr.sameAs(expr)) return null;
    return get(c, Math.max(min, c.min), mni && c.mni, Math.min(max, c.max),
        mxi && c.mxi);
  }

  @Override
  public void indexAccessible(final XQContext ctx, final IndexContext ic) {
    ic.iu = false;
    
    // accept only location path, string and equality expressions
    final Step s = CmpG.indexStep(expr);
    if(s == null) return;

    final boolean text = ic.data.meta.txtindex && s.test.type == Type.TXT;
    final boolean attr = !text && ic.data.meta.atvindex &&
      s.simpleName(Axis.ATTR);
    
    // no text or attribute index applicable, min/max not included in range
    if(!text && !attr || !mni | !mxi || min == Double.NEGATIVE_INFINITY ||
        max == Double.POSITIVE_INFINITY) return;

    final StatsKey key = getKey(ic, text);
    if(key == null) return;

    rt = new RangeToken(text, Math.max(min, key.min), Math.min(max, key.max));
    ic.iu = true;

    // estimate costs for range access; all values out of range: no results
    ic.is = rt.min > rt.max || rt.max < key.min || rt.min > key.max ? 0 :
      ic.data.meta.size / 5;
  }
  
  @Override
  public AxisPath indexEquivalent(final XQContext ctx, final IndexContext ic) {
    final Expr root = new IndexAccess(ic.data, rt);
    
    final AxisPath orig = (AxisPath) expr;
    final AxisPath path = orig.invertPath(root, ic.step);

    ctx.compInfo(OPTRNGINDEX);
    if(rt.type == IndexToken.Type.ATV) {
      // add attribute step
      final Step step = orig.step[0];
      Step[] steps = { Step.get(Axis.SELF, step.test) };
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
      return ic.data.tags.stat(ic.data.tagID(nm));
    }

    final Step step = path.step[st - 1];
    return !step.simpleName(Axis.ATTR) ? null : 
      ic.data.atts.stat(ic.data.attNameID(((NameTest) step.test).ln));
  }
  
  @Override
  public Return returned(final XQContext ctx) {
    return Return.BLN;
  }

  @Override
  public String toString() {
    return min + (mni ? " <= " : " < ") + expr + (mxi ? " <= " : " < ") + max;
  }
}
