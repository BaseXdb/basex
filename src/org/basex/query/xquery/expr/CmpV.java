package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
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
      public boolean e(final Item a, final Item b) throws XQException {
        final int v = a.diff(b);
        return v != UNDEF && v <= 0;
      }
    },

    /** Item Comparison:less. */
    LT("lt") {
      @Override
      public boolean e(final Item a, final Item b) throws XQException {
        final int v = a.diff(b);
        return v != UNDEF && v < 0;
      }
    },

    /** Item Comparison:greater of equal. */
    GE("ge") {
      @Override
      public boolean e(final Item a, final Item b) throws XQException {
        final int v = a.diff(b);
        return v != UNDEF && v >= 0;
      }
    },

    /** Item Comparison:greater. */
    GT("gt") {
      @Override
      public boolean e(final Item a, final Item b) throws XQException {
        final int v = a.diff(b);
        return v != UNDEF && v > 0;
      }
    },

    /** Item Comparison:equal. */
    EQ("eq") {
      @Override
      public boolean e(final Item a, final Item b) throws XQException {
        return a.eq(b);
      }
    },

    /** Item Comparison:not equal. */
    NE("ne") {
      @Override
      public boolean e(final Item a, final Item b) throws XQException {
        return !a.eq(b);
      }
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
     * @throws XQException evaluation exception
     */
    public abstract boolean e(Item a, Item b) throws XQException;

    @Override
    public String toString() { return name; }
  }
  
  /** Comparator. */
  private final Comp cmp;

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
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);

    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    
    Expr e = this;
    if(e1.i() && e2.i()) e = ev((Item) expr[0], (Item) expr[1]);
    else if(e1.e() || e2.e()) e = Seq.EMPTY;
    if(e != this) ctx.compInfo(OPTSIMPLE, this, e);
    return e;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item a = ctx.atomic(expr[0], this, true);
    if(a == null) return Iter.EMPTY;
    final Item b = ctx.atomic(expr[1], this, true);
    if(b == null) return Iter.EMPTY;
    return ev(a, b).iter();
  }

  /**
   * Performs the comparison.
   * @param a first item
   * @param b second item
   * @return result of check
   * @throws XQException evaluation exception
   */
  private Bln ev(final Item a, final Item b) throws XQException {
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
  public Type returned() {
    return Type.BLN;
  }

  @Override
  public String color() {
    return "FF9966";
  }

  @Override
  public String toString() {
    return toString(" " + cmp + " ");
  }

  @Override
  public String info() {
    return "'" + cmp + "' expression";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(cmp.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }
}
