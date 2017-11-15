package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Value comparison.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class CmpV extends Cmp {
  /** Comparators. */
  public enum OpV {
    /** Item comparison: less or equal. */
    LE("le") {
      @Override
      public boolean eval(final Item it1, final Item it2, final Collation coll,
          final StaticContext sc, final InputInfo ii) throws QueryException {
        final int v = it1.diff(it2, coll, ii);
        return v != Item.UNDEF && v <= 0;
      }
      @Override
      public OpV swap() { return GE; }
      @Override
      public OpV invert() { return GT; }
    },

    /** Item comparison: less. */
    LT("lt") {
      @Override
      public boolean eval(final Item it1, final Item it2, final Collation coll,
          final StaticContext sc, final InputInfo ii) throws QueryException {
        final int v = it1.diff(it2, coll, ii);
        return v != Item.UNDEF && v < 0;
      }
      @Override
      public OpV swap() { return GT; }
      @Override
      public OpV invert() { return GE; }
    },

    /** Item comparison: greater of equal. */
    GE("ge") {
      @Override
      public boolean eval(final Item it1, final Item it2, final Collation coll,
          final StaticContext sc, final InputInfo ii) throws QueryException {
        final int v = it1.diff(it2, coll, ii);
        return v != Item.UNDEF && v >= 0;
      }
      @Override
      public OpV swap() { return LE; }
      @Override
      public OpV invert() { return LT; }
    },

    /** Item comparison: greater. */
    GT("gt") {
      @Override
      public boolean eval(final Item it1, final Item it2, final Collation coll,
          final StaticContext sc, final InputInfo ii) throws QueryException {
        final int v = it1.diff(it2, coll, ii);
        return v != Item.UNDEF && v > 0;
      }
      @Override
      public OpV swap() { return LT; }
      @Override
      public OpV invert() { return LE; }
    },

    /** Item comparison: equal. */
    EQ("eq") {
      @Override
      public boolean eval(final Item it1, final Item it2, final Collation coll,
          final StaticContext sc, final InputInfo ii) throws QueryException {
        return it1.eq(it2, coll, sc, ii);
      }
      @Override
      public OpV swap() { return EQ; }
      @Override
      public OpV invert() { return NE; }
    },

    /** Item comparison: not equal. */
    NE("ne") {
      @Override
      public boolean eval(final Item it1, final Item it2, final Collation coll,
          final StaticContext sc, final InputInfo ii) throws QueryException {
        return !it1.eq(it2, coll, sc, ii);
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
     * @param name string representation
     */
    OpV(final String name) {
      this.name = name;
    }

    /**
     * Evaluates the expression.
     * @param it1 first item
     * @param it2 second item
     * @param coll collation (can be {@code null})
     * @param sc static context
     * @param ii input info
     * @return result
     * @throws QueryException query exception
     */
    public abstract boolean eval(Item it1, Item it2, Collation coll, StaticContext sc,
        InputInfo ii) throws QueryException;

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
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param coll collation (can be {@code null})
   * @param sc static context
   * @param info input info
   */
  public CmpV(final Expr expr1, final Expr expr2, final OpV op, final Collation coll,
      final StaticContext sc, final InputInfo info) {
    super(info, expr1, expr2, coll, SeqType.BLN_ZO, sc);
    this.op = op;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // pre-evaluate if one value is empty (e.g.: () = local:expensive() )
    if(oneIsEmpty()) return cc.emptySeq(this);

    // operands will always yield a single item
    if(exprs[1].seqType().oneNoArray() && exprs[0].seqType().oneNoArray())
      exprType.assign(Occ.ONE);

    // swap operands
    if(swap()) {
      cc.info(OPTSWAP_X, this);
      op = op.swap();
    }

    // optimize expression. pre-evaluate values or return expression
    final Expr ex = opt(op, cc);
    return allAreValues() ? cc.preEval(ex) : cc.replaceWith(this, ex);
  }

  @Override
  public Expr optimizeEbv(final CompileContext cc) {
    // e.g.: if($x eq true()) -> if($x)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpV.EQ && exprs[1] == Bln.TRUE || op == OpV.NE && exprs[1] == Bln.FALSE) &&
      exprs[0].seqType().eq(SeqType.BLN) ? cc.replaceEbv(this, exprs[0]) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it1 = exprs[0].atomItem(qc, info);
    if(it1 == null) return null;
    final Item it2 = exprs[1].atomItem(qc, info);
    if(it2 == null) return null;
    if(it1.comparable(it2)) return Bln.get(op.eval(it1, it2, coll, sc, info));
    throw diffError(it1, it2, info);
  }

  @Override
  public Expr invert(final CompileContext cc) throws QueryException {
    final Expr e1 = exprs[0], e2 = exprs[1];
    final SeqType st1 = e1.seqType(), st2 = e2.seqType();
    return st1.oneNoArray() && st2.oneNoArray() ?
      new CmpV(e1, e2, op.invert(), coll, sc, info).optimize(cc) : this;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CmpV(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, coll, sc, info));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CmpV && op == ((CmpV) obj).op && super.equals(obj);
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
