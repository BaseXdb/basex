package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.Function;
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.SeqType.Occ;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Value comparison.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CmpV extends Cmp {
  /** Comparators. */
  public enum Op {
    /** Item comparison:less or equal. */
    LE("le") {
      @Override
      public boolean e(final InputInfo ii, final Item a, final Item b)
          throws QueryException {
        final int v = a.diff(ii, b);
        return v != Item.UNDEF && v <= 0;
      }
      @Override
      public Op swap() { return GE; }
      @Override
      public Op invert() { return GT; }
    },

    /** Item comparison:less. */
    LT("lt") {
      @Override
      public boolean e(final InputInfo ii, final Item a, final Item b)
          throws QueryException {
        final int v = a.diff(ii, b);
        return v != Item.UNDEF && v < 0;
      }
      @Override
      public Op swap() { return GT; }
      @Override
      public Op invert() { return GE; }
    },

    /** Item comparison:greater of equal. */
    GE("ge") {
      @Override
      public boolean e(final InputInfo ii, final Item a, final Item b)
          throws QueryException {
        final int v = a.diff(ii, b);
        return v != Item.UNDEF && v >= 0;
      }
      @Override
      public Op swap() { return LE; }
      @Override
      public Op invert() { return LT; }
    },

    /** Item comparison:greater. */
    GT("gt") {
      @Override
      public boolean e(final InputInfo ii, final Item a, final Item b)
          throws QueryException {
        final int v = a.diff(ii, b);
        return v != Item.UNDEF && v > 0;
      }
      @Override
      public Op swap() { return LT; }
      @Override
      public Op invert() { return LE; }
    },

    /** Item comparison:equal. */
    EQ("eq") {
      @Override
      public boolean e(final InputInfo ii, final Item a, final Item b)
          throws QueryException {
        return a.eq(ii, b);
      }
      @Override
      public Op swap() { return EQ; }
      @Override
      public Op invert() { return NE; }
    },

    /** Item comparison:not equal. */
    NE("ne") {
      @Override
      public boolean e(final InputInfo ii, final Item a, final Item b)
          throws QueryException {
        return !a.eq(ii, b);
      }
      @Override
      public Op swap() { return NE; }
      @Override
      public Op invert() { return EQ; }
    };

    /** String representation. */
    public final String name;

    /**
     * Constructor.
     * @param n string representation
     */
    private Op(final String n) { name = n; }

    /**
     * Evaluates the expression.
     * @param ii input info
     * @param a first item
     * @param b second item
     * @return result
     * @throws QueryException query exception
     */
    public abstract boolean e(final InputInfo ii, final Item a, final Item b)
        throws QueryException;

    /**
     * Swaps the comparator.
     * @return swapped comparator
     */
    public abstract Op swap();

    /**
     * Inverts the comparator.
     * @return inverted comparator
     */
    public abstract Op invert();

    @Override
    public String toString() { return name; }
  }

  /** Comparator. */
  Op op;

  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   * @param o operator
   */
  public CmpV(final InputInfo ii, final Expr e1, final Expr e2, final Op o) {
    super(ii, e1, e2);
    op = o;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    // swap expressions; add text() to location paths to simplify optimizations
    if(swap()) {
      op = op.swap();
      ctx.compInfo(OPTSWAP, this);
    }
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].addText(ctx);

    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    type = SeqType.get(AtomType.BLN, e1.size() == 1 && e2.size() == 1 ?
        Occ.O : Occ.ZO);

    Expr e = this;
    if(oneEmpty()) {
      e = optPre(null, ctx);
    } else if(values()) {
      e = preEval(ctx);
    } else if(e1.isFun(Function.COUNT)) {
      e = compCount(op);
      if(e != this) ctx.compInfo(e instanceof Bln ? OPTPRE : OPTWRITE, this);
    } else if(e1.isFun(Function.POSITION)) {
      // position() CMP number
      e = Pos.get(op, e2, e, input);
      if(e != this) ctx.compInfo(OPTWRITE, this);
    } else if(e1.type().eq(SeqType.BLN) && (op == Op.EQ && e2 == Bln.FALSE ||
        op == Op.NE && e2 == Bln.TRUE)) {
      // (A eq false()) -> not(A)
      e = Function.NOT.get(input, e1);
    }
    return e;
  }

  @Override
  public Expr compEbv(final QueryContext ctx) {
    // e.g.: if($x eq true()) -> if($x)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == Op.EQ && expr[1] == Bln.TRUE ||
            op == Op.NE && expr[1] == Bln.FALSE) &&
      expr[0].type().eq(SeqType.BLN) ? expr[0] : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item a = expr[0].item(ctx, input);
    if(a == null) return null;
    final Item b = expr[1].item(ctx, input);
    if(b == null) return null;

    if(!a.comparable(b)) XPTYPECMP.thrw(input, a.type, b.type);
    return Bln.get(op.e(input, a, b));
  }

  @Override
  public CmpV invert() {
    return expr[0].size() != 1 || expr[1].size() != 1 ? this :
      new CmpV(input, expr[0], expr[1], op.invert());
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, OP, Token.token(op.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String desc() {
    return "'" + op + "' expression";
  }

  @Override
  public String toString() {
    return toString(" " + op + " ");
  }
}
