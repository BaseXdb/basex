package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General comparison.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class CmpG extends Cmp {
  /** Comparators. */
  public enum OpG {
    /** General comparison: less or equal. */
    LE("<=", OpV.LE) {
      @Override
      public OpG swap() { return OpG.GE; }
      @Override
      public OpG invert() { return OpG.GT; }
    },

    /** General comparison: less. */
    LT("<", OpV.LT) {
      @Override
      public OpG swap() { return OpG.GT; }
      @Override
      public OpG invert() { return OpG.GE; }
    },

    /** General comparison: greater of equal. */
    GE(">=", OpV.GE) {
      @Override
      public OpG swap() { return LE; }
      @Override
      public OpG invert() { return LT; }
    },

    /** General comparison: greater. */
    GT(">", OpV.GT) {
      @Override
      public OpG swap() { return LT; }
      @Override
      public OpG invert() { return LE; }
    },

    /** General comparison: equal. */
    EQ("=", OpV.EQ) {
      @Override
      public OpG swap() { return OpG.EQ; }
      @Override
      public OpG invert() { return OpG.NE; }
    },

    /** General comparison: not equal. */
    NE("!=", OpV.NE) {
      @Override
      public OpG swap() { return OpG.NE; }
      @Override
      public OpG invert() { return EQ; }
    };

    /** Cached enums (faster). */
    public static final OpG[] VALUES = values();
    /** String representation. */
    public final String name;
    /** Value comparison operator. */
    public final OpV op;

    /**
     * Constructor.
     * @param name string representation
     * @param op comparator
     */
    OpG(final String name, final OpV op) {
      this.name = name;
      this.op = op;
    }

    /**
     * Swaps the comparator.
     * @return swapped comparator
     */
    public abstract OpG swap();

    /**
     * Inverts the comparator.
     * @return inverted comparator
     */
    public abstract OpG invert();

    @Override
    public String toString() { return name; }

    /**
     * Returns the comparator for the specified value comparison operator.
     * @param cmp operator to be found
     * @return comparator or {@code null}
     */
    static OpG get(final OpV cmp) {
      for(final OpG value : VALUES) {
        if(value.op == cmp) return value;
      }
      return null;
    }
  }

  /** Comparator. */
  OpG op;

  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param coll collation (can be {@code null})
   * @param sc static context
   * @param info input info
   */
  public CmpG(final Expr expr1, final Expr expr2, final OpG op, final Collation coll,
      final StaticContext sc, final InputInfo info) {
    super(info, expr1, expr2, coll, SeqType.BLN_O, sc);
    this.op = op;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // pre-evaluate if one value is empty (e.g.: () eq local:expensive() )
    if(oneIsEmpty()) return cc.replaceWith(this, Bln.FALSE);

    // swap operands
    if(swap()) {
      cc.info(OPTSWAP_X, this);
      op = op.swap();
    }

    // optimize expression
    Expr expr = opt(op.op, cc);
    // range comparisons
    if(expr == this) expr = CmpR.get(this, cc);
    if(expr == this) expr = CmpSR.get(this, cc);

    // try to skip type checking at runtime
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    final Type t1 = st1.type, t2 = st2.type;
    // skip type check if types are identical (and a child instance of of any atomic type)
    check = !(t1 == t2 && !AtomType.AAT.instanceOf(t1) &&
        (t1.isSortable() || op != OpG.EQ && op != OpG.NE) ||
        t1.isUntyped() || t2.isUntyped() ||
        t1.instanceOf(AtomType.STR) && t2.instanceOf(AtomType.STR) ||
        t1.instanceOf(AtomType.NUM) && t2.instanceOf(AtomType.NUM) ||
        t1.instanceOf(AtomType.DUR) && t2.instanceOf(AtomType.DUR));

    // simple comparisons
    if(expr == this && st1.zeroOrOne() && !st1.mayBeArray() && st2.zeroOrOne() && !st2.mayBeArray())
      expr = new CmpSimpleG(expr1, expr2, op, coll, sc, info);

    // hash-based comparisons
    if(expr == this && op == OpG.EQ && coll == null && (t1.isNumber() && t2.isNumber() ||
        (t1.isStringOrUntyped() && t2.isStringOrUntyped())) && !st2.zeroOrOne())
      expr = new CmpHashG(expr1, expr2, op, coll, sc, info);

    // pre-evaluate values or return expression
    return allAreValues(false) ? cc.preEval(expr) : cc.replaceWith(this, expr);
  }

  @Override
  public Expr optimizeEbv(final CompileContext cc) {
    // e.g.: exists(...) = true() -> exists(...)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpG.EQ && exprs[1] == Bln.TRUE || op == OpG.NE && exprs[1] == Bln.FALSE) &&
      exprs[0].seqType().eq(SeqType.BLN_O) ? cc.replaceEbv(this, exprs[0]) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter1 = exprs[0].atomIter(qc, info);
    final long size1 = iter1.size();
    if(size1 == 0) return Bln.FALSE;
    final Iter iter2 = exprs[1].atomIter(qc, info);
    final long size2 = iter2.size();
    return size2 == 0 ? Bln.FALSE : compare(iter1, iter2, size1, size2, qc);
  }

  /**
   * Compares all values of the first and second iterators.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @param size1 size of first iterator
   * @param size2 size of second iterator
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  Bln compare(final Iter iter1, final Iter iter2, final long size1, final long size2,
      final QueryContext qc) throws QueryException {

    // evaluate single items
    Iter ir1 = iter1, ir2 = iter2;
    final boolean single1 = size1 == 1, single2 = size2 == 1;
    if(single1 && single2) return Bln.get(eval(ir1.next(), ir2.next()));

    if(single1) {
      // first iterator yields single result
      final Item item1 = ir1.next();
      for(Item item2; (item2 = qc.next(ir2)) != null;) {
        if(eval(item1, item2)) return Bln.TRUE;
      }
      return Bln.FALSE;
    }

    if(single2) {
      // second iterator yields single result
      final Item item2 = ir2.next();
      for(Item item1; (item1 = qc.next(ir1)) != null;) {
        if(eval(item1, item2)) return Bln.TRUE;
      }
      return Bln.FALSE;
    }

    // swap iterators if first iterator returns more results than second
    final boolean swap = size1 > size2;
    if(swap) {
      final Iter iter = ir1;
      ir1 = ir2;
      ir2 = iter;
    }

    // loop through all items of first and second iterator
    for(Item item1; (item1 = ir1.next()) != null;) {
      if(ir2 == null) ir2 = exprs[swap ? 0 : 1].atomIter(qc, info);
      for(Item item2; (item2 = qc.next(ir2)) != null;) {
        if(swap ? eval(item2, item1) : eval(item1, item2)) return Bln.TRUE;
      }
      ir2 = null;
    }
    return Bln.FALSE;

  }

  /**
   * Compares a single item.
   * @param item1 first item to be compared
   * @param item2 second item to be compared
   * @return result of check
   * @throws QueryException query exception
   */
  final boolean eval(final Item item1, final Item item2) throws QueryException {
    if(check) {
      final Type t1 = item1.type, t2 = item2.type;
      if(!(t1 == t2 || t1.isUntyped() || t2.isUntyped() ||
          item1 instanceof ANum && item2 instanceof ANum ||
          item1 instanceof AStr && item2 instanceof AStr ||
          item1 instanceof Dur && item2 instanceof Dur)) throw diffError(item1, item2, info);
    }
    return op.op.eval(item1, item2, coll, sc, info);
  }

  @Override
  public final Expr invert(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    return st1.oneNoArray() && st2.oneNoArray() ?
      new CmpG(expr1, expr2, op.invert(), coll, sc, info).optimize(cc) : this;
  }

  /**
   * Creates a union of the existing and the specified expressions.
   * @param g general comparison
   * @param cc compilation context
   * @return resulting expression or {@code null}
   * @throws QueryException query exception
   */
  final Expr union(final CmpG g, final CompileContext cc) throws QueryException {
    if(op != g.op || coll != g.coll || !exprs[0].equals(g.exprs[0])) return null;

    final Expr list = new List(info, exprs[1], g.exprs[1]).optimize(cc);
    return new CmpG(exprs[0], list, op, coll, sc, info).optimize(cc);
  }

  @Override
  public final boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // only equality expressions on default collation can be rewritten
    if(op != OpG.EQ || coll != null) return false;

    Expr expr1 = exprs[0];
    final boolean tokenize = expr1 instanceof FnTokenize;
    if(tokenize) expr1 = ((FnTokenize) expr1).input();
    return ii.create(exprs[1], ii.type(expr1, tokenize ? IndexType.TOKEN : null), info, false);
  }

  @Override
  public CmpG copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final CmpG cmp = new CmpG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, coll, sc, info);
    cmp.check = check;
    return cmp;
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof CmpG && op == ((CmpG) obj).op && super.equals(obj);
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(OP, op.name), exprs);
  }

  @Override
  public String description() {
    return op + " operator";
  }

  @Override
  public final String toString() {
    return toString(" " + op + ' ');
  }
}
