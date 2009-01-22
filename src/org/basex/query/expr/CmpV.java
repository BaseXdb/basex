package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.path.AxisPath;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * Value comparison.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CmpV extends Arr {
  /** Comparators. */
  public enum Comp {
    /** Item Comparison:less or equal. */
    LE("le") {
      @Override
      public boolean e(final Item a, final Item b) throws QueryException {
        final int v = a.diff(b);
        return v != UNDEF && v <= 0;
      }
      @Override
      public Comp invert() { return GE; }
    },

    /** Item Comparison:less. */
    LT("lt") {
      @Override
      public boolean e(final Item a, final Item b) throws QueryException {
        final int v = a.diff(b);
        return v != UNDEF && v < 0;
      }
      @Override
      public Comp invert() { return GT; }
    },

    /** Item Comparison:greater of equal. */
    GE("ge") {
      @Override
      public boolean e(final Item a, final Item b) throws QueryException {
        final int v = a.diff(b);
        return v != UNDEF && v >= 0;
      }
      @Override
      public Comp invert() { return LE; }
    },

    /** Item Comparison:greater. */
    GT("gt") {
      @Override
      public boolean e(final Item a, final Item b) throws QueryException {
        final int v = a.diff(b);
        return v != UNDEF && v > 0;
      }
      @Override
      public Comp invert() { return LT; }
    },

    /** Item Comparison:equal. */
    EQ("eq") {
      @Override
      public boolean e(final Item a, final Item b) throws QueryException {
        return a.eq(b);
      }
      @Override
      public Comp invert() { return EQ; }
    },

    /** Item Comparison:not equal. */
    NE("ne") {
      @Override
      public boolean e(final Item a, final Item b) throws QueryException {
        return !a.eq(b);
      }
      @Override
      public Comp invert() { return NE; }
    };

    /** String representation. */
    public final String name;

    /**
     * Constructor.
     * @param n string representation
     */
    Comp(final String n) { name = n; }

    /**
     * Evaluates the expression.
     * @param a first item
     * @param b second item
     * @return result
     * @throws QueryException evaluation exception
     */
    public abstract boolean e(Item a, Item b) throws QueryException;
    
    /**
     * Inverts the comparator.
     * @return inverted comparator
     */
    public abstract Comp invert();

    @Override
    public String toString() { return name; }
  }
  
  /** Comparator. */
  public Comp cmp;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @param c comparator
   */
  public CmpV(final Expr e1, final Expr e2, final Comp c) {
    super(e1, e2);
    cmp = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].addText(ctx);

    if(expr[0].i() && expr[1] instanceof AxisPath) {
      final Expr tmp = expr[0];
      expr[0] = expr[1];
      expr[1] = tmp;
      cmp = cmp.invert();
    }
    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    
    Expr e = this;
    if(e1.i() && e2.i()) {
      e = eval((Item) expr[0], (Item) expr[1]);
    } else if(e1.e() || e2.e()) {
      e = Seq.EMPTY;
    } else if(e1 instanceof Fun && ((Fun) e1).func == FunDef.POS) {
      e = Pos.get(this, cmp, e2);
    }
    if(e != this) ctx.compInfo(OPTPRE, this);
    return e;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item a = atomic(ctx, expr[0], true);
    if(a == null) return Iter.EMPTY;
    final Item b = atomic(ctx, expr[1], true);
    if(b == null) return Iter.EMPTY;
    return eval(a, b).iter();
  }

  /**
   * Performs the comparison.
   * @param a first item
   * @param b second item
   * @return result of check
   * @throws QueryException evaluation exception
   */
  private Bln eval(final Item a, final Item b) throws QueryException {
    if(!valCheck(a, b)) Err.cmp(a, b);
    return Bln.get(cmp.e(a, b));
  }

  /**
   * Checks if the specified items can be compared.
   * @param a first item
   * @param b second item
   * @return result of check
   */
  public static boolean valCheck(final Item a, final Item b) {
    return a.type == b.type || a.n() && b.n() || (a.u() || a.s()) &&
      (b.s() || b.u()) || a.d() && b.d();
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return Return.BLN;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(cmp.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String color() {
    return "FF9966";
  }

  @Override
  public String info() {
    return "'" + cmp + "' expression";
  }

  @Override
  public String toString() {
    return toString(" " + cmp + " ");
  }
}
