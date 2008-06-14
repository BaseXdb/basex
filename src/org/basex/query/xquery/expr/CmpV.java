package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.query.xquery.XQText.*;
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
  public enum COMP {
    /** Item Comparison:less or equal. */
    LE("le") {
      @Override
      public boolean e(final Item a, final Item b) throws XQException {
        final int v = a.diff(b);
        return v != Integer.MIN_VALUE && v <= 0;
      }
    },

    /** Item Comparison:less. */
    LT("lt") {
      @Override
      public boolean e(final Item a, final Item b) throws XQException {
        final int v = a.diff(b);
        return v != Integer.MIN_VALUE && v < 0;
      }
    },

    /** Item Comparison:greater of equal. */
    GE("ge") {
      @Override
      public boolean e(final Item a, final Item b) throws XQException {
        final int v = a.diff(b);
        return v != Integer.MIN_VALUE && v >= 0;
      }
    },

    /** Item Comparison:greater. */
    GT("gt") {
      @Override
      public boolean e(final Item a, final Item b) throws XQException {
        final int v = a.diff(b);
        return v != Integer.MIN_VALUE && v > 0;
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
    COMP(final String n) { name = n; }

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
  private final COMP cmp;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @param c comparator
   */
  public CmpV(final Expr e1, final Expr e2, final COMP c) {
    super(e1, e2);
    cmp = c;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);

    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    final boolean empty = e1.e() || e2.e();
    if(!(empty || e1.i() && e2.i())) return this;
    
    ctx.compInfo(OPTPREEVAL, this);
    if(empty) return Seq.EMPTY;

    final Item i1 = (Item) expr[0];
    final Item i2 = (Item) expr[1];
    if(!valCheck(i1, i2)) Err.cmp(i1, i2);
    return Bln.get(cmp.e(i1, i2));
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item a = ctx.atomic(expr[0], this, true);
    if(a == null) return Iter.EMPTY;
    final Item b = ctx.atomic(expr[1], this, true);
    if(b == null) return Iter.EMPTY;

    if(!valCheck(a, b)) Err.cmp(a, b);
    return Bln.get(cmp.e(a, b)).iter();
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
  public String toString() {
    return toString(" " + cmp + " ");
  }

  @Override
  public String info() {
    return "'" + cmp + "' expression";
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, TYPE, Token.token(cmp.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "FF9999";
  }
}
