package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
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
    /** Comparator. */
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
    super(info, expr1, expr2, coll, SeqType.BLN, sc);
    this.op = op;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // swap expressions; add text() to location paths to simplify optimizations
    if(swap()) {
      cc.info(OPTSWAP_X, this);
      op = op.swap();
    }

    // check if both arguments will always yield one result
    final Expr e1 = exprs[0], e2 = exprs[1];
    final SeqType st1 = e1.seqType(), st2 = e2.seqType();

    // one value is empty (e.g.: () = local:expensive() )
    if(oneIsEmpty()) return cc.replaceWith(this, Bln.FALSE);

    Expr ex = this;
    if(e1.isFunction(Function.COUNT)) {
      // rewrite count() function
      ex = compCount(op.op, cc);
    } else if(e1.isFunction(Function.STRING_LENGTH)) {
      // rewrite string-length() function
      ex = compStringLength(op.op, cc);
    } else if(e1.isFunction(Function.POSITION)) {
      // position() CMP number(s)
      ex = ItrPos.get(op.op, e2, this, info);
      if(ex == this) ex = Pos.get(op.op, e2, this, info, cc);
    } else if(st1.eq(SeqType.BLN) && (op == OpG.EQ && e2 == Bln.FALSE ||
        op == OpG.NE && e2 == Bln.TRUE)) {
      // (A = false()) -> not(A)
      ex = cc.function(Function.NOT, info, e1);
    }
    if(ex != this) return cc.replaceWith(this, ex);

    // rewrite equality comparisons (range expression or number)
    ex = CmpR.get(this, cc);
    if(ex == this) ex = CmpSR.get(this, cc);
    if(ex != this) return cc.replaceWith(this, ex);

    if(op == OpG.EQ) {
      /* pre-evaluate equality test if:
       * - equality operator is specified,
       * - operands are equal,
       * - operands are deterministic, non-updating,
       * - operands do not depend on context (unless context value exists) */
      final Type t1 = st1.type, t2 = st2.type;
      if(e1.equals(e2) && !e1.has(Flag.NDT) &&
          (!e1.has(Flag.CTX) || cc.qc.focus.value != null)) {
        /* consider query flags. do not rewrite:
         * random:integer() = random:integer() */
        if(st1.oneOrMore() && (t1.isStringOrUntyped() || t1.instanceOf(AtomType.ITR) ||
            t1 == AtomType.BLN)) {
          /* currently limited to strings, integers and booleans. do not rewrite:
           * xs:double('NaN') = xs:double('NaN') */
          return cc.replaceWith(this, Bln.TRUE);
        }
      }

      // use hash
      if(coll == null && e2 instanceof Value && e2.size() > 1 &&
          (t1.isNumber() && t2.isNumber() || t1.isStringOrUntyped() && t2.isStringOrUntyped())) {
        return cc.replaceWith(this, new CmpHashG(e1, e2, op, coll, sc, info).optimize(cc));
      }
    }

    // choose evaluation strategy
    if(st1.zeroOrOne() && !st1.mayBeArray() && st2.zeroOrOne() && !st2.mayBeArray()) {
      return cc.replaceWith(this, new CmpSimpleG(e1, e2, op, coll, sc, info).optimize(cc));
    }

    // pre-evaluate values
    return allAreValues() ? cc.preEval(this) : this;
  }

  @Override
  public Expr optimizeEbv(final CompileContext cc) {
    // e.g.: exists(...) = true() -> exists(...)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpG.EQ && exprs[1] == Bln.TRUE || op == OpG.NE && exprs[1] == Bln.FALSE) &&
      exprs[0].seqType().eq(SeqType.BLN) ? cc.replaceEbv(this, exprs[0]) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // retrieve iterators
    Iter iter1 = exprs[0].atomIter(qc, info);
    final long is1 = iter1.size();
    if(is1 == 0) return Bln.FALSE;
    final boolean s1 = is1 == 1;

    Iter iter2 = exprs[1].atomIter(qc, info);
    final long is2 = iter2.size();
    if(is2 == 0) return Bln.FALSE;

    // evaluate single items
    final boolean s2 = is2 == 1;
    if(s1 && s2) return Bln.get(eval(iter1.next(), iter2.next()));

    if(s1) {
      // first iterator yields single result
      final Item it1 = iter1.next();
      for(Item it2; (it2 = iter2.next()) != null;) {
        qc.checkStop();
        if(eval(it1, it2)) return Bln.TRUE;
      }
      return Bln.FALSE;
    }

    if(s2) {
      // second iterator yields single result
      final Item it2 = iter2.next();
      for(Item it1; (it1 = iter1.next()) != null;) {
        qc.checkStop();
        if(eval(it1, it2)) return Bln.TRUE;
      }
      return Bln.FALSE;
    }

    // swap iterators if first iterator returns more results than second
    final boolean swap = is1 > is2;
    if(swap) {
      final Iter iter = iter1;
      iter1 = iter2;
      iter2 = iter;
    }

    // loop through all items of first and second iterator
    for(Item it1; (it1 = iter1.next()) != null;) {
      if(iter2 == null) iter2 = exprs[swap ? 0 : 1].atomIter(qc, info);
      for(Item it2; (it2 = iter2.next()) != null;) {
        qc.checkStop();
        if(swap ? eval(it2, it1) : eval(it1, it2)) return Bln.TRUE;
      }
      iter2 = null;
    }
    return Bln.FALSE;
  }

  /**
   * Compares a single item.
   * @param it1 first item to be compared
   * @param it2 second item to be compared
   * @return result of check
   * @throws QueryException query exception
   */
  final boolean eval(final Item it1, final Item it2) throws QueryException {
    final Type t1 = it1.type, t2 = it2.type;
    if(t1 == t2 || t1.isUntyped() || t2.isUntyped() ||
        it1 instanceof ANum && it2 instanceof ANum ||
        it1 instanceof AStr && it2 instanceof AStr) return op.op.eval(it1, it2, coll, sc, info);
    throw diffError(it1, it2, info);
  }

  @Override
  public final Expr invert(final CompileContext cc) throws QueryException {
    final Expr e1 = exprs[0], e2 = exprs[1];
    final SeqType st1 = e1.seqType(), st2 = e2.seqType();
    return st1.oneNoArray() && st2.oneNoArray() ?
      new CmpG(e1, e2, op.invert(), coll, sc, info).optimize(cc) : this;
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
    return new CmpG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, coll, sc, info);
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
