package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.IndexToken;
import org.basex.index.ValuesToken;
import org.basex.query.xquery.FTIndexAcsbl;
import org.basex.query.xquery.FTIndexEq;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQOptimizer;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.path.Axis;
import org.basex.query.xquery.path.AxisPath;
import org.basex.query.xquery.path.Step;
import org.basex.query.xquery.util.Err;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * General comparison.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CmpG extends Arr {
  /** Comparators. */
  public enum Comp {
    /** General Comparison: less or equal. */
    LE("<=", CmpV.Comp.LE),
    /** General Comparison: less. */
    LT("<", CmpV.Comp.LT),
    /** General Comparison: greater of equal. */
    GE(">=", CmpV.Comp.GE),
    /** General Comparison: greater. */
    GT(">", CmpV.Comp.GT),
    /** General Comparison: equal. */
    EQ("=", CmpV.Comp.EQ),
    /** General Comparison: not equal. */
    NE("!=", CmpV.Comp.NE);

    /** String representation. */
    public final String name;
    /** Comparator. */
    public final CmpV.Comp cmp;

    /**
     * Constructor.
     * @param n string representation
     * @param c comparator
     */
    Comp(final String n, final CmpV.Comp c) {
      name = n;
      cmp = c;
    }
    
    @Override
    public String toString() { return name; }
  };

  /** Index type. */
  private IndexToken index;
  /** Comparator. */
  final Comp cmp;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @param c comparator
   */
  public CmpG(final Expr e1, final Expr e2, final Comp c) {
    super(e1, e2);
    cmp = c;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);

    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    if(e1.e() || e2.e()) return Bln.FALSE;
    expr[0] = XQOptimizer.addText(expr[0], ctx);
    if(!e1.i() || !e2.i()) return this;
    return Bln.get(ev((Item) expr[0], (Item) expr[1], cmp.cmp));
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {
      /** Iterator flag. */
      private boolean more;
      
      @Override
      public Item next() throws XQException {
        if(!(more ^= true)) return null;
        final Expr e1 = expr[0];
        final Expr e2 = expr[1];
        final boolean i1 = e1.i();
        final boolean i2 = e2.i();
        return Bln.get(i1 && i2 ? ev((Item) e1, (Item) e2, cmp.cmp) :
          i2 ? ev(ctx.iter(e1), (Item) e2) : ev(ctx.iter(e1), ctx.iter(e2)));
      }
    };
  }

  /**
   * Performs a general comparison on the specified iterators and comparator.
   * @param ir1 first iterator
   * @param ir2 second iterator
   * @return result of check
   * @throws XQException evaluation exception
   */
  boolean ev(final Iter ir1, final Iter ir2) throws XQException {
    if(ir1.size() == 0 || ir2.size() == 0) return false;
    
    Item it1, it2;
    final SeqIter seq = new SeqIter();
    if((it1 = ir1.next()) != null) {
      while((it2 = ir2.next()) != null) {
        if(ev(it1, it2, cmp.cmp)) return true;
        seq.add(it2);
      }
    }
    while((it1 = ir1.next()) != null) {
      seq.reset();
      while((it2 = seq.next()) != null) if(ev(it1, it2, cmp.cmp)) return true;
    }
    return false;
  }

  /**
   * Performs a general comparison on the specified iterator and item.
   * @param ir iterator
   * @param it item
   * @return result of check
   * @throws XQException evaluation exception
   */
  boolean ev(final Iter ir, final Item it) throws XQException {
    Item i;
    while((i = ir.next()) != null) if(ev(i, it, cmp.cmp)) return true;
    return false;
  }

  /**
   * Compares a single item.
   * @param c comparator
   * @param a first item to be compared
   * @param b second item to be compared
   * @return result of check
   * @throws XQException thrown if the items can't be compared
   */
  static boolean ev(final Item a, final Item b, final CmpV.Comp c)
      throws XQException {

    if(a.type != b.type && !a.u() && !b.u() && !(a.s() && b.s()) && 
        !(a.n() && b.n())) Err.cmp(a, b);
    return c.e(a, b);
  }

  @Override
  public void indexAccessible(final XQContext ctx, final FTIndexAcsbl ia) {
    // accept only location path, string and equality expressions
    final Step s = indexStep(expr);
    if(s == null || !(expr[1] instanceof Str) || cmp != CmpG.Comp.EQ) return;

    final boolean text = ia.data.meta.txtindex && s.test.type == Type.TXT;
    final boolean attr = !text && ia.data.meta.atvindex &&
      s.simpleName(Axis.ATTR);
    
    // no text or attribute index applicable
    if(!text && !attr) return;

    index = new ValuesToken(text, ((Str) expr[1]).str());
    ia.iu = true;
    ia.is = ia.data.nrIDs(index);
  }
  
  @Override
  public Expr indexEquivalent(final XQContext ctx, final FTIndexEq ieq) {
    final boolean txt = index.type == IndexToken.Type.TXT;

    // standard index evaluation; first expression will always be an axis path
    final Expr ex = new IndexAccess(ieq.data, index);
    final AxisPath orig = (AxisPath) expr[0];
    final AxisPath path = orig.invertPath(ex, ieq.curr);

    if(!txt) {
      // add attribute step
      final Step step = orig.step[0];
      Step[] steps = { Step.get(Axis.SELF, step.test) };
      for(final Step s : path.step) steps = Array.add(steps, s);
      path.step = steps;
    }
    return path;
  }

  /**
   * Returns the indexable index step or null.
   * @param expr expression arguments
   * @return result of check
   */
  public static Step indexStep(final Expr[] expr) {
    // check if index can be applied
    if(!(expr[0] instanceof AxisPath)) return null;
    
    // accept only single axis steps as first expression
    final AxisPath path = (AxisPath) expr[0];
    if(path.root != null || path.step.length != 1) return null;

    // step must not contain predicates
    final Step s = path.step[0];
    return s.pred.length != 0 ? null : s;
  }
  
  @Override
  public Type returned() {
    return Type.BLN;
  }

  @Override
  public String info() {
    return "'" + cmp + "' expression";
  }

  @Override
  public String color() {
    return "FF9966";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(cmp.name), EVAL, ITER);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return toString(" " + cmp + " ");
  }
}
