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
    /** Item comparison: less or equal. */
    LE("le") {
      @Override
      public boolean eval(final Item it1, final Item it2, final Collation coll, final InputInfo ii)
          throws QueryException {
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
      public boolean eval(final Item it1, final Item it2, final Collation coll, final InputInfo ii)
          throws QueryException {
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
      public boolean eval(final Item it1, final Item it2, final Collation coll, final InputInfo ii)
          throws QueryException {
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
      public boolean eval(final Item it1, final Item it2, final Collation coll, final InputInfo ii)
          throws QueryException {
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
      public boolean eval(final Item it1, final Item it2, final Collation coll, final InputInfo ii)
          throws QueryException {
        return it1.eq(it2, coll, ii);
      }
      @Override
      public OpV swap() { return EQ; }
      @Override
      public OpV invert() { return NE; }
    },

    /** Item comparison: not equal. */
    NE("ne") {
      @Override
      public boolean eval(final Item it1, final Item it2, final Collation coll, final InputInfo ii)
          throws QueryException {
        return !it1.eq(it2, coll, ii);
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
     * @param coll query context
     * @param ii input info
     * @return result
     * @throws QueryException query exception
     */
    public abstract boolean eval(final Item it1, final Item it2, final Collation coll,
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
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // swap expressions
    if(swap()) {
      op = op.swap();
      qc.compInfo(OPTSWAP, this);
    }

    final Expr e1 = exprs[0], e2 = exprs[1];
    seqType = SeqType.get(AtomType.BLN, e1.size() == 1 && e2.size() == 1 ? Occ.ONE : Occ.ZERO_ONE);

    Expr e = this;
    if(oneIsEmpty()) {
      e = optPre(qc);
    } else if(allAreValues()) {
      e = preEval(qc);
    } else if(e1.isFunction(Function.COUNT)) {
      e = compCount(op);
      if(e != this) qc.compInfo(e instanceof Bln ? OPTPRE : OPTWRITE, this);
    } else if(e1.isFunction(Function.POSITION)) {
      // position() CMP number
      e = Pos.get(op, e2, e, info);
      if(e != this) qc.compInfo(OPTWRITE, this);
    } else if(e1.seqType().eq(SeqType.BLN) && (op == OpV.EQ && e2 == Bln.FALSE ||
        op == OpV.NE && e2 == Bln.TRUE)) {
      // (A eq false()) -> not(A)
      e = Function.NOT.get(null, info, e1);
    }
    return e;
  }

  @Override
  public Expr compEbv(final QueryContext qc) {
    // e.g.: if($x eq true()) -> if($x)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpV.EQ && exprs[1] == Bln.TRUE || op == OpV.NE && exprs[1] == Bln.FALSE) &&
      exprs[0].seqType().eq(SeqType.BLN) ? exprs[0] : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it1 = exprs[0].item(qc, info);
    if(it1 == null) return null;
    final Item it2 = exprs[1].item(qc, info);
    if(it2 == null) return null;
    if(it1.comparable(it2)) return Bln.get(op.eval(it1, it2, coll, info));

    if(it1 instanceof FItem) throw FIEQ.get(info, it1.type);
    if(it2 instanceof FItem) throw FIEQ.get(info, it2.type);
    throw diffError(info, it1, it2);
  }

  @Override
  public CmpV invert() {
    return exprs[0].size() != 1 || exprs[1].size() != 1 ? this :
      new CmpV(exprs[0], exprs[1], op.invert(), coll, info);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CmpV(exprs[0].copy(qc, scp, vs), exprs[1].copy(qc, scp, vs), op, coll, info);
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
