package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Value comparison.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class CmpV extends Cmp {
  /** Comparators. */
  public enum OpV {
    /** Item comparison: less or equal. */
    LE("le") {
      @Override
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        final int v = item1.compare(item2, coll, info);
        return v != Item.UNDEF && v <= 0;
      }
      @Override
      public OpV swap() { return GE; }
      @Override
      public OpV invert() { return GT; }
      @Override
      public OpG general() { return OpG.LE; }
    },

    /** Item comparison: less. */
    LT("lt") {
      @Override
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        final int v = item1.compare(item2, coll, info);
        return v != Item.UNDEF && v < 0;
      }
      @Override
      public OpV swap() { return GT; }
      @Override
      public OpV invert() { return GE; }
      @Override
      public OpG general() { return OpG.LT; }
    },

    /** Item comparison: greater of equal. */
    GE("ge") {
      @Override
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        // includes UNDEF check
        return item1.compare(item2, coll, info) >= 0;
      }
      @Override
      public OpV swap() { return LE; }
      @Override
      public OpV invert() { return LT; }
      @Override
      public OpG general() { return OpG.GE; }
    },

    /** Item comparison: greater. */
    GT("gt") {
      @Override
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        // includes UNDEF check
        return item1.compare(item2, coll, info) > 0;
      }
      @Override
      public OpV swap() { return LT; }
      @Override
      public OpV invert() { return LE; }
      @Override
      public OpG general() { return OpG.GT; }
    },

    /** Item comparison: equal. */
    EQ("eq") {
      @Override
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        return item1.equal(item2, coll, sc, info);
      }
      @Override
      public OpV swap() { return EQ; }
      @Override
      public OpV invert() { return NE; }
      @Override
      public OpG general() { return OpG.EQ; }
    },

    /** Item comparison: not equal. */
    NE("ne") {
      @Override
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        return !item1.equal(item2, coll, sc, info);
      }
      @Override
      public OpV swap() { return NE; }
      @Override
      public OpV invert() { return EQ; }
      @Override
      public OpG general() { return OpG.NE; }
    };

    /** Cached enums (faster). */
    public static final OpV[] VALUES = values();
    /** String representation. */
    public final String name;

    /**
     * Constructor.
     * @param name string representation
     */
    OpV(final String name) {
      this.name = name;
    }

    /**
     * Evaluates the expression.
     * @param item1 first item
     * @param item2 second item
     * @param coll collation (can be {@code null})
     * @param sc static context
     * @param info input info (can be {@code null})
     * @return result
     * @throws QueryException query exception
     */
    public abstract boolean eval(Item item1, Item item2, Collation coll, StaticContext sc,
        InputInfo info) throws QueryException;

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

    /**
     * Returns the general comparator.
     * @return comparator
     */
    public abstract OpG general();

    @Override
    public String toString() {
      return name;
    }
  }

  /** Operator. */
  private OpV opV;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr1 first expression
   * @param expr2 second expression
   * @param opV operator
   * @param coll collation (can be {@code null})
   * @param sc static context
   */
  public CmpV(final InputInfo info, final Expr expr1, final Expr expr2, final OpV opV,
      final Collation coll, final StaticContext sc) {
    super(info, expr1, expr2, coll, SeqType.BOOLEAN_ZO, sc);
    this.opV = opV;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs = simplifyAll(Simplify.STRING, cc);
    if(allAreValues(false)) return cc.preEval(this);

    // swap operands
    if(swap()) {
      cc.info(OPTSWAP_X, this);
      opV = opV.swap();
    }

    Expr expr = emptyExpr();
    if(expr == this) expr = toGeneral(cc, true);
    if(expr == this) expr = opt(cc);
    if(expr == this) {
      // restrict type
      final SeqType st1 = exprs[0].seqType(), st2 = exprs[1].seqType();
      if(st1.oneOrMore() && !st1.mayBeArray() && st2.oneOrMore() && !st2.mayBeArray()) {
        exprType.assign(Occ.EXACTLY_ONE);
      }
    }
    return cc.replaceWith(this, expr);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // E[@x eq 'x']  ->  E[@x = 'x']  (triggers further optimizations)
    return cc.simplify(this, mode.oneOf(Simplify.EBV, Simplify.PREDICATE) ? toGeneral(cc, false) :
      this, mode);
  }

  /**
   * Tries to rewrite this expression to a general comparison.
   * @param cc compilation context
   * @param single operands must yield single values
   * @return general comparison or original expression
   * @throws QueryException query exception
   */
  private Expr toGeneral(final CompileContext cc, final boolean single) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    final Predicate<SeqType> p = st -> single ? st.one() : st.zeroOrOne();
    if(p.test(st1) && p.test(st2) && CmpG.compatible(st1, st2, opV == OpV.EQ || opV == OpV.NE)) {
      return new CmpG(info, expr1, expr2, OpG.get(opV), coll, sc).optimize(cc);
    }
    return this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1.isEmpty()) return Empty.VALUE;
    final Item item2 = exprs[1].atomItem(qc, info);
    if(item2.isEmpty()) return Empty.VALUE;
    if(item1.comparable(item2)) return Bln.get(opV.eval(item1, item2, coll, sc, info));
    throw compareError(item1, item2, info);
  }

  @Override
  public Expr invert() {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    return st1.one() && !st1.mayBeArray() && st2.one() && !st2.mayBeArray() ?
      new CmpV(info, expr1, expr2, opV.invert(), coll, sc) : null;
  }

  @Override
  public OpV opV() {
    return opV;
  }

  @Override
  public OpG opG() {
    return opV.general();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CmpV(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), opV, coll, sc));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CmpV && opV == ((CmpV) obj).opV && super.equals(obj);
  }

  @Override
  public String description() {
    return "'" + opV + "' comparison";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, OP, opV.name), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, " " + opV + ' ', true);
  }
}
