package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Value comparison.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CmpV extends Cmp {
  /** Comparators. */
  public enum OpV {
    /** Item comparison:less or equal. */
    LE("le") {
      @Override
      public boolean eval(final Item a, final Item b, final Collation coll,
          final InputInfo ii) throws QueryException {
        final int v = a.diff(b, coll, ii);
        return v != Item.UNDEF && v <= 0;
      }
      @Override
      public OpV swap() { return GE; }
      @Override
      public OpV invert() { return GT; }
    },

    /** Item comparison:less. */
    LT("lt") {
      @Override
      public boolean eval(final Item a, final Item b, final Collation coll,
          final InputInfo ii) throws QueryException {
        final int v = a.diff(b, coll, ii);
        return v != Item.UNDEF && v < 0;
      }
      @Override
      public OpV swap() { return GT; }
      @Override
      public OpV invert() { return GE; }
    },

    /** Item comparison:greater of equal. */
    GE("ge") {
      @Override
      public boolean eval(final Item a, final Item b, final Collation coll,
          final InputInfo ii) throws QueryException {
        final int v = a.diff(b, coll, ii);
        return v != Item.UNDEF && v >= 0;
      }
      @Override
      public OpV swap() { return LE; }
      @Override
      public OpV invert() { return LT; }
    },

    /** Item comparison:greater. */
    GT("gt") {
      @Override
      public boolean eval(final Item a, final Item b, final Collation coll,
          final InputInfo ii) throws QueryException {
        final int v = a.diff(b, coll, ii);
        return v != Item.UNDEF && v > 0;
      }
      @Override
      public OpV swap() { return LT; }
      @Override
      public OpV invert() { return LE; }
    },

    /** Item comparison:equal. */
    EQ("eq") {
      @Override
      public boolean eval(final Item a, final Item b, final Collation coll,
          final InputInfo ii) throws QueryException {
        return a.eq(b, coll, ii);
      }
      @Override
      public OpV swap() { return EQ; }
      @Override
      public OpV invert() { return NE; }
    },

    /** Item comparison:not equal. */
    NE("ne") {
      @Override
      public boolean eval(final Item a, final Item b, final Collation coll,
          final InputInfo ii) throws QueryException {
        return !a.eq(b, coll, ii);
      }
      @Override
      public OpV swap() { return NE; }
      @Override
      public OpV invert() { return EQ; }
    };

    /** Cached enums (faster). */
    public static final OpV[] VALUES = values();
    /** String representation. */
    public final String name;

    /**
     * Constructor.
     * @param n string representation
     */
    OpV(final String n) { name = n; }

    /**
     * Evaluates the expression.
     * @param a first item
     * @param b second item
     * @param coll query context
     * @param ii input info
     * @return result
     * @throws QueryException query exception
     */
    public abstract boolean eval(final Item a, final Item b, final Collation coll,
        final InputInfo ii) throws QueryException;

    /**
     * Swaps the comparator.
     * @return swapped comparator
     */
    public abstract OpV swap();

    /**
     * Inverts the comparator.
     * @return inverted comparator
     */
    public abstract OpV invert();

    @Override
    public String toString() { return name; }
  }

  /** Comparator. */
  OpV op;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @param o operator
   * @param ii input info
   */
  public CmpV(final Expr e1, final Expr e2, final OpV o, final InputInfo ii) {
    super(ii, e1, e2);
    op = o;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);

    // swap expressions
    if(swap()) {
      op = op.swap();
      ctx.compInfo(OPTSWAP, this);
    }

    final Expr e1 = expr[0];
    final Expr e2 = expr[1];
    type = SeqType.get(AtomType.BLN, e1.size() == 1 && e2.size() == 1 ?
        Occ.ONE : Occ.ZERO_ONE);

    Expr e = this;
    if(oneIsEmpty()) {
      e = optPre(null, ctx);
    } else if(allAreValues()) {
      e = preEval(ctx);
    } else if(e1.isFunction(Function.COUNT)) {
      e = compCount(op);
      if(e != this) ctx.compInfo(e instanceof Bln ? OPTPRE : OPTWRITE, this);
    } else if(e1.isFunction(Function.POSITION)) {
      // position() CMP number
      e = Pos.get(op, e2, e, info);
      if(e != this) ctx.compInfo(OPTWRITE, this);
    } else if(e1.type().eq(SeqType.BLN) && (op == OpV.EQ && e2 == Bln.FALSE ||
        op == OpV.NE && e2 == Bln.TRUE)) {
      // (A eq false()) -> not(A)
      e = Function.NOT.get(info, e1);
    }
    return e;
  }

  @Override
  public Expr compEbv(final QueryContext ctx) {
    // e.g.: if($x eq true()) -> if($x)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpV.EQ && expr[1] == Bln.TRUE ||
            op == OpV.NE && expr[1] == Bln.FALSE) &&
      expr[0].type().eq(SeqType.BLN) ? expr[0] : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item a = expr[0].item(ctx, info);
    if(a == null) return null;
    final Item b = expr[1].item(ctx, info);
    if(b == null) return null;
    if(a.comparable(b)) return Bln.get(op.eval(a, b, ctx.sc.collation, info));

    if(a instanceof FItem) Err.FIEQ.thrw(info, a);
    if(b instanceof FItem) Err.FIEQ.thrw(info, b);
    throw Err.INVTYPECMP.thrw(info, a.type, b.type);
  }

  @Override
  public CmpV invert() {
    return expr[0].size() != 1 || expr[1].size() != 1 ? this :
      new CmpV(expr[0], expr[1], op.invert(), info);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new CmpV(expr[0].copy(ctx, scp, vs), expr[1].copy(ctx, scp, vs), op, info);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(OP, op.name), expr);
  }

  @Override
  public String description() {
    return "'" + op + "' expression";
  }

  @Override
  public String toString() {
    return toString(" " + op + ' ');
  }
}
