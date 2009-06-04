package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.IndexToken;
import org.basex.index.ValuesToken;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.FNSimple;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.path.Axis;
import org.basex.query.path.AxisPath;
import org.basex.query.path.Step;
import org.basex.query.util.Err;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * General comparison.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CmpG extends Arr {
  /** Comparators. */
  public enum Comp {
    /** General Comparison: less or equal. */
    LE("<=", CmpV.Comp.LE) {
      @Override
      public Comp invert() { return Comp.GE; }
    },
    /** General Comparison: less. */
    LT("<", CmpV.Comp.LT) {
      @Override
      public Comp invert() { return Comp.GT; }
    },
    /** General Comparison: greater of equal. */
    GE(">=", CmpV.Comp.GE) {
      @Override
      public Comp invert() { return Comp.LE; }
    },
    /** General Comparison: greater. */
    GT(">", CmpV.Comp.GT) {
      @Override
      public Comp invert() { return Comp.LT; }
    },
    /** General Comparison: equal. */
    EQ("=", CmpV.Comp.EQ) {
      @Override
      public Comp invert() { return Comp.EQ; }
    },
    /** General Comparison: not equal. */
    NE("!=", CmpV.Comp.NE) {
      @Override
      public Comp invert() { return Comp.NE; }
    };

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
    
    /**
     * Inverts the comparator.
     * @return inverted comparator
     */
    public abstract Comp invert();
    
    @Override
    public String toString() { return name; }
  };

  /** Comparator. */
  public Comp cmp;
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
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].addText(ctx);

    if(expr[0].i() && !expr[1].i()) {
      final Expr tmp = expr[0];
      expr[0] = expr[1];
      expr[1] = tmp;
      cmp = cmp.invert();
    }
    final Expr e1 = expr[0];
    final Expr e2 = expr[1];

    Expr e = this;
    if(e1.i() && e2.i()) {
      e = Bln.get(eval((Item) e1, (Item) e2));
    } else if(e1.e() || e2.e()) {
      e = Bln.FALSE;
    }
    if(e != this) {
      ctx.compInfo(OPTPRE, this);
    } else if(e1 instanceof Fun) {
      final Fun fun = (Fun) expr[0];
      if(fun.func == FunDef.POS) {
        if(e2 instanceof Range) {
          // position() CMP range
          final long[] rng = ((Range) e2).range(ctx);
          if(rng != null) e = Pos.get(rng[0], rng[1]);
        } else {
          // position() CMP number
          e = Pos.get(this, cmp.cmp, e2);
        }
        if(e != this) ctx.compInfo(OPTWRITE, this);
      } else if(fun.func == FunDef.COUNT) {
        if(e2.i() && ((Item) e2).n() && ((Item) e2).dbl() == 0) {
          // count(...) CMP 0
          if(cmp == Comp.LT || cmp == Comp.GE) {
            // < 0: always false, >= 0: always true
            ctx.compInfo(OPTPRE, this);
            e = Bln.get(cmp == Comp.GE);
          } else {
            // <=/= 0: empty(), >/!= 0: exist()
            final Fun f = new FNSimple();
            f.expr = fun.expr;
            f.func = cmp == Comp.EQ || cmp == Comp.LE ?
                FunDef.EMPTY : FunDef.EXISTS;
            ctx.compInfo(OPTWRITE, this);
            e = f;
          }
        }
      }
    } else if(standard(true)) {
      // rewrite path CMP number
      e = CmpR.get(this);
      if(e == null) e = this;
      else ctx.compInfo(OPTWRITE, this);
    }
    return e;
  }

  @Override
  public Bln atomic(final QueryContext ctx) throws QueryException {
    final Iter ir1 = ctx.iter(expr[0]);
    final int is1 = ir1.size();

    // skip empty result
    if(is1 == 0) return Bln.FALSE;
    final boolean s1 = is1 == 1;
    
    // evaluate single items
    if(s1 && expr[1].i()) return Bln.get(eval(ir1.next(), (Item) expr[1]));

    Iter ir2 = ctx.iter(expr[1]);
    final int is2 = ir2.size();

    // skip empty result
    if(is2 == 0) return Bln.FALSE;
    final boolean s2 = is2 == 1;
    
    // evaluate single items
    if(s1 && s2) return Bln.get(eval(ir1.next(), ir2.next()));

    // evaluate iterator and single item
    Item it1, it2;
    if(s2) {
      it2 = ir2.next();
      while((it1 = ir1.next()) != null) if(eval(it1, it2)) return Bln.TRUE;
      return Bln.FALSE;
    }
    
    // evaluate two iterators
    if(!ir2.reset()) {
      // cache items for next comparisons
      final SeqIter seq = new SeqIter();
      if((it1 = ir1.next()) != null) {
        while((it2 = ir2.next()) != null) {
          if(eval(it1, it2)) return Bln.TRUE;
          seq.add(it2);
        }
      }
      ir2 = seq;
    }

    while((it1 = ir1.next()) != null) {
      ir2.reset();
      while((it2 = ir2.next()) != null) if(eval(it1, it2)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Compares a single item.
   * @param a first item to be compared
   * @param b second item to be compared
   * @return result of check
   * @throws QueryException thrown if the items can't be compared
   */
  boolean eval(final Item a, final Item b) throws QueryException {
    if(a.type != b.type && !a.u() && !b.u() && !(a.s() && b.s()) && 
        !(a.n() && b.n())) Err.cmp(a, b);
    return cmp.cmp.e(a, b);
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    // accept only location path, string and equality expressions
    final Step s = indexStep(expr[0]);
    if(s == null || cmp != Comp.EQ || !(expr[1].i() ||
        expr[1] instanceof Seq)) return false;

    final boolean text = ic.data.meta.txtindex && s.test.type == Type.TXT;
    final boolean attr = !text && ic.data.meta.atvindex &&
      s.simple(Axis.ATTR, true);

    // no text or attribute index applicable
    if(!text && !attr) return false;

    // loop through all strings
    final Iter ir = expr[1].iter(ic.ctx);
    Item i;
    while((i = ir.next()) != null) {
      if(!(i instanceof Str)) return false;
      final ValuesToken vt = new ValuesToken(text, i.str());
      ic.is = Math.min(ic.data.nrIDs(vt), ic.is);
      index = Array.add(index, vt);
    }
    return true;
  }
  
  @Override
  public Expr indexEquivalent(final IndexContext ic) {
    // create index access expressions
    final int il = index.length;
    final Expr[] ia = new IndexAccess[il];
    for(int i = 0; i < il; i++) ia[i] = new IndexAccess(index[i], ic);
    
    // more than one string - merge index results
    final Expr root = il == 1 ? ia[0] : new Union(ia);
    
    final AxisPath orig = (AxisPath) expr[0];
    final AxisPath path = orig.invertPath(root, ic.step);

    if(index[0].type == org.basex.data.Data.Type.TXT) {
      ic.ctx.compInfo(OPTTXTINDEX);
    } else {
      ic.ctx.compInfo(OPTATVINDEX);
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
  public static Step indexStep(final Expr expr) {
    // check if index can be applied
    if(!(expr instanceof AxisPath)) return null;
    
    // accept only single axis steps as first expression
    final AxisPath path = (AxisPath) expr;
    if(path.root != null) return null;

    // step must not contain predicates
    return path.step[path.step.length - 1];
  }
  
  @Override
  public Return returned(final QueryContext ctx) {
    return Return.BLN;
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
