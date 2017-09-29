package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
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
    super(info, expr1, expr2, coll, sc);
    this.op = op;
    seqType = SeqType.BLN_ZO;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // swap expressions
    if(swap()) {
      cc.info(OPTSWAP_X, this);
      op = op.swap();
    }

    final Expr e1 = exprs[0], e2 = exprs[1];
    final SeqType st1 = e1.seqType(), st2 = e2.seqType();
    seqType = st1.oneNoArray() && st2.oneNoArray() ? SeqType.BLN : SeqType.BLN_ZO;

    Expr e = this;
    if(oneIsEmpty()) return cc.emptySeq(this);
    if(allAreValues()) return cc.preEval(this);

    if(e1.isFunction(Function.COUNT)) {
      // rewrite count() function
      e = compCount(op, cc);
    } else if(e1.isFunction(Function.STRING_LENGTH)) {
      // rewrite string-length() function
      e = compStringLength(op, cc);
    } else if(e1.isFunction(Function.POSITION) && st2.oneNoArray()) {
      // position() CMP number
      e = ItrPos.get(op, e2, this, info);
      if(e == this) e = Pos.get(op, e2, this, info, cc);
    } else if(st1.eq(SeqType.BLN) && (op == OpV.EQ && e2 == Bln.FALSE ||
        op == OpV.NE && e2 == Bln.TRUE)) {
      // (A eq false()) -> not(A)
      e = cc.function(Function.NOT, info, e1);
    }
    if(e != this) return cc.replaceWith(this, e);

    /* pre-evaluate equality test if:
     * - equality operator is specified,
     * - operands are equal,
     * - operands are deterministic, non-updating,
     * - operands do not depend on context, or if context value exists
     */
    if((op == OpV.EQ || op == OpV.NE) && e1.equals(e2) && !e1.has(Flag.NDT) && !e1.has(Flag.UPD) &&
        (!e1.has(Flag.CTX) || cc.qc.focus.value != null)) {
      // currently limited to strings, integers and booleans
      final Type t1 = st1.type;
      if(st1.one() && (t1.isStringOrUntyped() || t1.instanceOf(AtomType.ITR) || t1 == AtomType.BLN))
        return cc.replaceWith(this, Bln.get(op == OpV.EQ));
    }
    return this;
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

    if(it1 instanceof FItem) throw FIEQ_X.get(info, it1.type);
    if(it2 instanceof FItem) throw FIEQ_X.get(info, it2.type);
    throw diffError(it1, it2, info);
  }

  @Override
  public CmpV invert() {
    final Expr e1 = exprs[0], e2 = exprs[1];
    final SeqType st1 = e1.seqType(), st2 = e2.seqType();
    return st1.oneNoArray() && st2.oneNoArray() ? new CmpV(e1, e2, op.invert(), coll, sc, info) :
      this;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new CmpV(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, coll, sc, info);
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
