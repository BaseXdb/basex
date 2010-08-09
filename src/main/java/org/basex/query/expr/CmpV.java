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
public final class CmpV extends Arr {
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
  Op cmp;

  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   * @param c comparator
   */
  public CmpV(final InputInfo ii, final Expr e1, final Expr e2, final Op c) {
    super(ii, e1, e2);
    cmp = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].addText(ctx);

    if(expr[0].value() && !expr[1].value()) {
      final Expr tmp = expr[0];
      expr[0] = expr[1];
      expr[1] = tmp;
      cmp = cmp.invert();
    }
    final Expr e1 = expr[0];
    final Expr e2 = expr[1];

    Expr e = this;
    if(e1.empty() || e2.empty()) {
      e = optPre(Empty.SEQ, ctx);
    } else if(e1.value() && e2.value()) {
      e = preEval(ctx);
    } else if(e1 instanceof Fun) {
      final Fun fun = (Fun) expr[0];
      if(fun.func == FunDef.COUNT) {
        e = count(this, cmp);
        if(e != this) ctx.compInfo(e instanceof Bln ? OPTPRE : OPTWRITE, this);
      } else if(fun.func == FunDef.POS) {
        // position() CMP number
        e = Pos.get(cmp, e2, e, input);
        if(e != this) ctx.compInfo(OPTWRITE, this);
      }
    }
    return e;
  }

  /**
   * Optimizes a {@code count()} function.
   * @param e calling expression
   * @param op comparison operator
   * @return resulting expression
   * @throws QueryException query exception
   */
  static Expr count(final Arr e, final Op op) throws QueryException {
    // evaluate argument
    final Expr a = e.expr[1];
    if(!a.item()) return e;
    final Item it = (Item) a;
    if(!it.num() && !it.unt()) return e;

    final double d = it.dbl(e.input);
    // x > (d<0), x >= (d<=0), x != (d<=0), x != not-int(d)
    if(op == Op.GT && d < 0 || (op == Op.GE || op == Op.NE) && d <= 0 ||
       op == Op.NE && d != (int) d) return Bln.TRUE;
    // x < (d<=0), x <= (d<0), x = (d<0), x = not-int(d)
    if(op == Op.LT && d <= 0 || (op == Op.LE || op == Op.EQ) && d < 0 ||
       op == Op.EQ && d != (int) d) return Bln.FALSE;
    // x > (d<1), x >= (d<=1),  x != (d=0)
    if(op == Op.GT && d < 1 || op == Op.GE && d <= 1 || op == Op.NE && d == 0)
      return Fun.create(e.input, FunDef.EXISTS, ((Fun) e.expr[0]).expr);
    // x < (d<=1), x <= (d<1),  x = (d=0)
    if(op == Op.LT && d <= 1 || op == Op.LE && d < 1 || op == Op.EQ && d == 0)
      return Fun.create(e.input, FunDef.EMPTY, ((Fun) e.expr[0]).expr);

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
    return Bln.get(cmp.e(input, a, b));
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.BLN;
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
  public String desc() {
    return "'" + cmp + "' expression";
  }

  @Override
  public String toString() {
    return toString(" " + cmp + " ");
  }
}
