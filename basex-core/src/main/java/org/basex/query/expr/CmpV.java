package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Value comparison.
 *
 * @author BaseX Team 2005-18, BSD License
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
        final int v = item1.diff(item2, coll, info);
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
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        final int v = item1.diff(item2, coll, info);
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
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        final int v = item1.diff(item2, coll, info);
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
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        final int v = item1.diff(item2, coll, info);
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
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        return item1.eq(item2, coll, sc, info);
      }
      @Override
      public OpV swap() { return EQ; }
      @Override
      public OpV invert() { return NE; }
    },

    /** Item comparison: not equal. */
    NE("ne") {
      @Override
      public boolean eval(final Item item1, final Item item2, final Collation coll,
          final StaticContext sc, final InputInfo info) throws QueryException {
        return !item1.eq(item2, coll, sc, info);
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
     * @param item1 first item
     * @param item2 second item
     * @param coll collation (can be {@code null})
     * @param sc static context
     * @param info input info
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

    // try to skip type checking at runtime
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    final Type type1 = st1.type, type2 = st2.type;
    check = !(type1 == type2 && !AtomType.AAT.instanceOf(type1) &&
        (type1.isSortable() || op != OpV.EQ && op != OpV.NE) ||
        type1.isStringOrUntyped() && type2.isStringOrUntyped() ||
        type1.instanceOf(AtomType.NUM) && type2.instanceOf(AtomType.NUM) ||
        type1.instanceOf(AtomType.DUR) && type2.instanceOf(AtomType.DUR));

    // try to rewrite to general expression (faster evaluation)
    Expr expr = check || !st1.oneNoArray() || !st2.oneNoArray() ? this :
      new CmpG(expr1, expr2, OpG.get(op), coll, sc, info).optimize(cc);

    if(expr == this) expr = opt(op, cc);
    // pre-evaluate values or return expression
    return allAreValues(false) ? cc.preEval(expr) : cc.replaceWith(this, expr);
  }

  @Override
  public Expr optimizeEbv(final CompileContext cc) {
    // e.g.: if($x eq true()) -> if($x)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpV.EQ && exprs[1] == Bln.TRUE || op == OpV.NE && exprs[1] == Bln.FALSE) &&
      exprs[0].seqType().eq(SeqType.BLN_O) ? cc.replaceEbv(this, exprs[0]) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1 == null) return null;
    final Item item2 = exprs[1].atomItem(qc, info);
    if(item2 == null) return null;
    if(item1.comparable(item2)) return Bln.get(op.eval(item1, item2, coll, sc, info));
    throw diffError(item1, item2, info);
  }

  @Override
  public Expr invert(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    return st1.oneNoArray() && st2.oneNoArray() ?
      new CmpV(expr1, expr2, op.invert(), coll, sc, info).optimize(cc) : this;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Cmp cmp = new CmpV(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, coll, sc, info);
    cmp.check = check;
    return copyType(cmp);
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
