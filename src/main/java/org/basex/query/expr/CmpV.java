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
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Value comparison.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
      public Op invert() { return GE; }
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
      public Op invert() { return GT; }
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
      public Op invert() { return LE; }
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
      public Op invert() { return LT; }
    },

    /** Item comparison:equal. */
    EQ("eq") {
      @Override
      public boolean e(final InputInfo ii, final Item a, final Item b)
          throws QueryException {
        return a.eq(ii, b);
      }
      @Override
      public Op invert() { return EQ; }
    },

    /** Item comparison:not equal. */
    NE("ne") {
      @Override
      public boolean e(final InputInfo ii, final Item a, final Item b)
          throws QueryException {
        return !a.eq(ii, b);
      }
      @Override
      public Op invert() { return NE; }
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
      op = op.invert();
      ctx.compInfo(OPTSWAP, this);
    }
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].addText(ctx);

    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    Expr e = this;
    if(oneEmpty()) {
      e = optPre(Empty.SEQ, ctx);
    } else if(values()) {
      e = preEval(ctx);
    } else if(e1 instanceof Fun) {
      final Fun fun = (Fun) e1;
      if(fun.def == FunDef.COUNT) {
        e = count(op);
        if(e != this) ctx.compInfo(e instanceof Bln ? OPTPRE : OPTWRITE, this);
      } else if(fun.def == FunDef.POS) {
        // position() CMP number
        e = Pos.get(op, e2, e, input);
        if(e != this) ctx.compInfo(OPTWRITE, this);
      }
    }
    type = SeqType.BLN;
    return e;
  }

  @Override
  public Bln atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item a = expr[0].atomic(ctx, input);
    if(a == null) return null;
    final Item b = expr[1].atomic(ctx, input);
    if(b == null) return null;

    if(!a.comparable(b)) Err.or(input, XPTYPECMP, a.type, b.type);
    return Bln.get(op.e(input, a, b));
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
