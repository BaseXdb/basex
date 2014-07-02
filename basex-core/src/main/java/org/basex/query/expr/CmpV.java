package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

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
 * @author BaseX Team 2005-14, BSD License
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
  public OpV op;

  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param coll collation
   * @param info input info
   */
  public CmpV(final Expr expr1, final Expr expr2, final OpV op, final Collation coll,
      final InputInfo info) {
    super(info, expr1, expr2, coll);
    this.op = op;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    // swap expressions
    if(swap()) {
      op = op.swap();
      ctx.compInfo(OPTSWAP, this);
    }

    final Expr e1 = exprs[0];
    final Expr e2 = exprs[1];
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
      e = Function.NOT.get(null, info, e1);
    }
    return e;
  }

  @Override
  public Expr compEbv(final QueryContext ctx) {
    // e.g.: if($x eq true()) -> if($x)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpV.EQ && exprs[1] == Bln.TRUE ||
            op == OpV.NE && exprs[1] == Bln.FALSE) &&
      exprs[0].type().eq(SeqType.BLN) ? exprs[0] : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item a = exprs[0].item(ctx, info);
    if(a == null) return null;
    final Item b = exprs[1].item(ctx, info);
    if(b == null) return null;
    if(a.comparable(b)) return Bln.get(op.eval(a, b, coll, info));

    if(a instanceof FItem) throw FIEQ.get(info, a.type);
    if(b instanceof FItem) throw FIEQ.get(info, b.type);
    throw INVTYPECMP.get(info, a.type, b.type);
  }

  @Override
  public CmpV invert() {
    return exprs[0].size() != 1 || exprs[1].size() != 1 ? this :
      new CmpV(exprs[0], exprs[1], op.invert(), coll, info);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr a = exprs[0].copy(ctx, scp, vs), b = exprs[1].copy(ctx, scp, vs);
    return new CmpV(a, b, op, coll, info);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(OP, op.name), exprs);
  }

  @Override
  public String description() {
    return "'" + op + "' operator";
  }

  @Override
  public String toString() {
    return toString(" " + op + ' ');
  }
}
