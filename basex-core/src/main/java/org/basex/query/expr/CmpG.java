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
public final class CmpG extends Cmp {
  /** Comparators. */
  public enum OpG {
    /** General comparison: less or equal. */
    LE("<=", OpV.LE) {
      @Override
      public OpG swap() { return OpG.GE; }
      @Override
      public OpG invert() { return OpG.LT; }
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

  /** Static context. */
  final StaticContext sc;
  /** Comparator. */
  OpG op;
  /** Flag for atomic evaluation. */
  private boolean atomic;

  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param coll collation
   * @param sc static context
   * @param info input info
   */
  public CmpG(final Expr expr1, final Expr expr2, final OpG op, final Collation coll,
      final StaticContext sc, final InputInfo info) {
    super(info, expr1, expr2, coll);
    this.op = op;
    this.sc = sc;
    seqType = SeqType.BLN;
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
    final SeqType st1 = e1.seqType();

    // one value is empty (e.g.: () = local:expensive() )
    if(oneIsEmpty()) return optPre(Bln.FALSE, cc);

    // rewrite count() function
    if(e1.isFunction(Function.COUNT)) {
      final Expr e = compCount(op.op, cc);
      if(e != this) {
        cc.info(e instanceof Bln ? OPTPRE_X : OPTREWRITE_X, this);
        return e;
      }
    }

    // rewrite string-length() function
    if(e1.isFunction(Function.STRING_LENGTH)) {
      final Expr e = compStringLength(op.op, cc);
      if(e != this) {
        cc.info(e instanceof Bln ? OPTPRE_X : OPTREWRITE_X, this);
        return e;
      }
    }

    // position() CMP expr
    if(e1.isFunction(Function.POSITION)) {
      final Expr e = Pos.get(op.op, e2, this, info);
      if(e != this) {
        cc.info(OPTREWRITE_X, this);
        return e;
      }
    }

    // (A = false()) -> not(A)
    if(st1.eq(SeqType.BLN) && (op == OpG.EQ && e2 == Bln.FALSE || op == OpG.NE && e2 == Bln.TRUE)) {
      cc.info(OPTREWRITE_X, this);
      return cc.function(Function.NOT, info, e1);
    }

    // rewrite expr CMP (range expression or number)
    ParseExpr e = CmpR.get(this);
    // rewrite expr CMP string)
    if(e == this) e = CmpSR.get(this);
    if(e != this) {
      // pre-evaluate optimized expression
      cc.info(OPTREWRITE_X, this);
      return allAreValues() ? e.preEval(cc) : e;
    }

    // choose evaluation strategy
    final SeqType st2 = e2.seqType();
    if(st1.zeroOrOne() && !st1.mayBeArray() && st2.zeroOrOne() && !st2.mayBeArray()) {
      atomic = true;
      cc.info(OPTATOMIC_X, this);
    }

    // pre-evaluate values
    if(allAreValues()) return preEval(cc);

    // pre-evaluate equality test if operands are equal, deterministic, and can be compared
    if(op == OpG.EQ && e1.sameAs(e2) && !e1.has(Flag.NDT) && !e1.has(Flag.UPD) &&
        (!e1.has(Flag.CTX) || cc.qc.focus.value != null)) {
      // currently limited to strings (function items are invalid, numbers may be NaN)
      final SeqType st = e1.seqType();
      if(st.oneOrMore() && st.type.isStringOrUntyped()) return optPre(Bln.TRUE, cc);
    }
    return this;
  }

  @Override
  public Expr optimizeEbv(final CompileContext cc) {
    // e.g.: exists(...) = true() -> exists(...)
    // checking one direction is sufficient, as operators may have been swapped
    return (op == OpG.EQ && exprs[1] == Bln.TRUE || op == OpG.NE && exprs[1] == Bln.FALSE) &&
      exprs[0].seqType().eq(SeqType.BLN) ? exprs[0] : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(atomic) {
      final Item it1 = exprs[0].item(qc, info);
      if(it1 == null) return Bln.FALSE;
      final Item it2 = exprs[1].item(qc, info);
      if(it2 == null) return Bln.FALSE;
      return Bln.get(eval(it1, it2));
    }

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
  private boolean eval(final Item it1, final Item it2) throws QueryException {
    final Type t1 = it1.type, t2 = it2.type;
    if(!(it1 instanceof FItem || it2 instanceof FItem) &&
        (t1 == t2 || t1.isUntyped() || t2.isUntyped() ||
        it1 instanceof ANum && it2 instanceof ANum ||
        it1 instanceof AStr && it2 instanceof AStr)) return op.op.eval(it1, it2, coll, sc, info);
    throw diffError(it1, it2, info);
  }

  @Override
  public CmpG invert() {
    final Expr e1 = exprs[0], e2 = exprs[1];
    return e1.size() != 1 || e1.seqType().mayBeArray() || e2.size() != 1 ||
        e2.seqType().mayBeArray() ? this : new CmpG(e1, e2, op.invert(), coll, sc, info);
  }

  /**
   * Creates a union of the existing and the specified expressions.
   * @param g general comparison
   * @param cc compilation context
   * @return resulting expression or {@code null}
   * @throws QueryException query exception
   */
  CmpG union(final CmpG g, final CompileContext cc) throws QueryException {
    if(op != g.op || coll != g.coll || !exprs[0].sameAs(g.exprs[0])) return null;

    final Expr list = new List(info, exprs[1], g.exprs[1]).optimize(cc);
    final CmpG cmp = new CmpG(exprs[0], list, op, coll, sc, info);
    final SeqType st = list.seqType();
    cmp.atomic = atomic && st.zeroOrOne() && !st.mayBeArray();
    return cmp;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
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
