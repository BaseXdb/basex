package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.item.Type;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * Node comparison.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CmpN extends Arr {
  /** Comparators. */
  public enum Comp {
    /** Node Comparison: same. */
    EQ("is") {
      @Override
      public boolean e(final Item a, final Item b) {
        return ((Nod) a).is((Nod) b);
      }
    },

    /** Node Comparison: before. */
    ET("<<") {
      @Override
      public boolean e(final Item a, final Item b) {
        return ((Nod) a).diff((Nod) b) < 0;
      }
    },

    /** Node Comparison: after. */
    GT(">>") {
      @Override
      public boolean e(final Item a, final Item b) {
        return ((Nod) a).diff((Nod) b) > 0;
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
     * @throws QueryException evaluation exception
     */
    public abstract boolean e(Item a, Item b) throws QueryException;

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
  public CmpN(final Expr e1, final Expr e2, final Comp c) {
    super(e1, e2);
    cmp = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(expr[0].e() || expr[1].e()) {
      ctx.compInfo(OPTSIMPLE, this, Seq.EMPTY);
      return Seq.EMPTY;
    }
    return this;
  }

  @Override
  public Bln atomic(final QueryContext ctx) throws QueryException {
    final Item a = expr[0].atomic(ctx);
    if(a == null) return null;
    final Item b = expr[1].atomic(ctx);
    if(b == null) return null;

    if(!a.node()) Err.type(info(), Type.NOD, a);
    if(!b.node()) Err.type(info(), Type.NOD, b);
    return Bln.get(cmp.e(a, b));
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return Return.BLN;
  }

  @Override
  public String color() {
    return "FF9966";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(cmp.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String info() {
    return "'" + cmp + "' operator";
  }

  @Override
  public String toString() {
    return toString(" " + cmp + " ");
  }
}
