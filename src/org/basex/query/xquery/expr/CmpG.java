package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.IndexToken;
import org.basex.index.ValuesToken;
import org.basex.query.xquery.FTIndexAcsbl;
import org.basex.query.xquery.FTIndexEq;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.ResetIter;
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

  /** Comparator. */
  public final Comp cmp;
  /** Index type. */
  private IndexToken[] index = {};

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
    if(e1 instanceof AxisPath) ((AxisPath) e1).addText(ctx);

    Expr e = this;
    if(e1.i() && e2.i()) e = Bln.get(eval((Item) e1, (Item) e2, cmp.cmp));
    else if(e1.e() || e2.e()) e = Bln.FALSE;
    if(e != this) ctx.compInfo(OPTSIMPLE, this, e);
    return e;
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
    final Iter ir1 = ctx.iter(expr[0]);
    // skip empty result
    if(ir1.size() == 0) return false;
    final boolean s1 = ir1.size() == 1;
    
    // evaluate single items
    if(s1 && expr[1].i()) return eval(ir1.next(), (Item) expr[1], cmp.cmp);
    final Iter ir2 = ctx.iter(expr[1]);
    // skip empty result
    if(ir2.size() == 0) return false;
    final boolean s2 = ir2.size() == 1;
    
    // evaluate single items
    if(s1 && s2) return eval(ir1.next(), ir2.next(), cmp.cmp);

    // evaluate iterator and single item
    Item it1, it2;
    if(s2) {
      it2 = ir2.next();
      while((it1 = ir1.next()) != null) if(eval(it1, it2, cmp.cmp)) return true;
      return false;
    }
    
    // evaluate two iterators
    final ResetIter ir;
    if(ir2 instanceof ResetIter) {
      ir = (ResetIter) ir2;
    } else {
      // cache items for next comparisons
      final SeqIter seq = new SeqIter();
      if((it1 = ir1.next()) != null) {
        while((it2 = ir2.next()) != null) {
          if(eval(it1, it2, cmp.cmp)) return true;
          seq.add(it2);
        }
      }
      ir = seq;
    }
    while((it1 = ir1.next()) != null) {
      ir.reset();
      while((it2 = ir.next()) != null) if(eval(it1, it2, cmp.cmp)) return true;
    }
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
  static boolean eval(final Item a, final Item b, final CmpV.Comp c)
      throws XQException {

    if(a.type != b.type && !a.u() && !b.u() && !(a.s() && b.s()) && 
        !(a.n() && b.n())) Err.cmp(a, b);
    return c.e(a, b);
  }

  @Override
  public void indexAccessible(final XQContext ctx, final FTIndexAcsbl ia)
      throws XQException {

    // accept only location path, string and equality expressions
    final Step s = indexStep(expr);
    if(s == null || cmp != Comp.EQ || !(expr[1] instanceof Str) &&
        !(expr[1] instanceof Seq)) return;

    final boolean text = ia.data.meta.txtindex && s.test.type == Type.TXT;
    final boolean attr = !text && ia.data.meta.atvindex &&
      s.simpleName(Axis.ATTR);
    
    // no text or attribute index applicable
    if(!text && !attr) return;

    // loop through all strings
    final Iter ir = expr[1].iter(ctx);
    Item i;
    while((i = ir.next()) != null) {
      if(!(i instanceof Str)) return;
      final ValuesToken vt = new ValuesToken(text, i.str());
      ia.is = Math.max(ia.data.nrIDs(vt), ia.is);
      index = Array.add(index, vt);
    }
    ia.iu = true;
  }
  
  @Override
  public Expr indexEquivalent(final XQContext ctx, final FTIndexEq ieq) {
    // create index access expressions
    final int il = index.length;
    final Expr[] ia = new IndexAccess[il];
    for(int i = 0; i < il; i++) ia[i] = new IndexAccess(ieq.data, index[i]);
    
    // more than one string - merge index results
    final Expr root = il == 1 ? ia[0] : new Union(ia);
    
    final AxisPath orig = (AxisPath) expr[0];
    final AxisPath path = orig.invertPath(root, ieq.curr);

    if(index[0].type == IndexToken.Type.TXT) {
      ctx.compInfo(OPTTXTINDEX);
    } else {
      ctx.compInfo(OPTATVINDEX);
      // add attribute step
      final Step step = orig.step[0];
      Step[] steps = { Step.get(Axis.SELF, step.test) };
      for(final Step s : path.step) steps = Array.add(steps, s);
      path.step = steps;
    }
    return path;
  }

  /**
   * Collect all VarCall expressions.
   * @return VarCall[]
   */
  public VarCall[] getVarCalls() {
    VarCall[] v = new VarCall[0];
    for (int i = 0; i < expr.length; i++) {
      if (expr[i] instanceof AxisPath) {
        v = Array.add(v, ((AxisPath) expr[i]).getVarCalls());
      }
    }
    return v;
  }
  
  /**
   * Remove all varcall expressions vc.
   */
  public void removeVarCall() {
    for (int i = 0; i < expr.length; i++) {
      if (expr[i] instanceof AxisPath) {
        ((AxisPath) expr[i]).removeVarCall();
      }
    }
    
  }
  
  /**
   * Returns the indexable index step or null.
   * @param expr expression arguments
   * @return result of check
   */
  static Step indexStep(final Expr[] expr) {
    // check if index can be applied
    if(!(expr[0] instanceof AxisPath)) return null;
    
    // accept only single axis steps as first expression
    final AxisPath path = (AxisPath) expr[0];
    if(path.root != null) return null;
    //if(path.root != null || path.step.length != 1) return null;

    // step must not contain predicates
    return path.step[path.step.length - 1];
  }
  
  /**
   * Checks if this expression has a path and a string as arguments.
   * @return result of check
   */
  boolean standard() {
    return expr[0] instanceof AxisPath && expr[1] instanceof Str;
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
