package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.index.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General comparison.
 *
 * @author BaseX Team 2005-21, BSD License
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
    public final OpV opV;

    /**
     * Constructor.
     * @param name string representation
     * @param opV operator for value comparisons
     */
    OpG(final String name, final OpV opV) {
      this.name = name;
      this.opV = opV;
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
    public String toString() {
      return name;
    }

    /**
     * Returns the comparator for the specified value comparison operator.
     * @param opV operator to be found
     * @return comparator or {@code null}
     */
    static OpG get(final OpV opV) {
      for(final OpG value : VALUES) {
        if(value.opV == opV) return value;
      }
      return null;
    }
  }

  /** Operator. */
  OpG op;
  /** Type check at runtime. */
  private boolean check = true;

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
    super(info, expr1, expr2, coll, SeqType.BOOLEAN_O, sc);
    this.op = op;
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // pre-evaluate if one value is empty:
    // () eq local:expensive()  ->  ()
    // prof:void(123) = 1  ->  boolean(prof:void('123'))
    Expr expr = emptyExpr();
    if(expr != this) return cc.replaceWith(this, cc.function(Function.BOOLEAN, info, expr));

    // remove redundant type conversions
    final Type t1 = exprs[0].seqType().type, t2 = exprs[1].seqType().type;
    if(t1.isStringOrUntyped() && t2.isStringOrUntyped()) {
      simplifyAll(Simplify.STRING, cc);
    } else if(t1.isNumber() && t2.isNumber()) {
      simplifyAll(Simplify.NUMBER, cc);
    }

    // swap operands
    if(swap()) {
      cc.info(OPTSWAP_X, this);
      op = op.swap();
    }

    // simplify operands
    for(int e = 0; e < 2; e++) {
      exprs[e] = exprs[e].simplifyFor(Simplify.DISTINCT, cc);
    }

    // optimize expression
    expr = opt(cc);

    // range comparisons
    if(expr == this) expr = optArith(cc);
    if(expr == this) expr = CmpIR.get(this, false, cc);
    if(expr == this) expr = CmpR.get(this, cc);
    if(expr == this) expr = CmpSR.get(this, cc);

    if(expr == this) {
      // determine types, choose best implementation
      final Expr expr1 = exprs[0], expr2 = exprs[1];
      final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
      final Type type1 = st1.type, type2 = st2.type;
      // skip type check if types are identical (and a child instance of of any atomic type)
      check = !(type1 == type2 && !AtomType.ANY_ATOMIC_TYPE.instanceOf(type1) &&
          (type1.isSortable() || op != OpG.EQ && op != OpG.NE) ||
          type1.isUntyped() || type2.isUntyped() ||
          type1.instanceOf(AtomType.STRING) && type2.instanceOf(AtomType.STRING) ||
          type1.instanceOf(AtomType.NUMERIC) && type2.instanceOf(AtomType.NUMERIC) ||
          type1.instanceOf(AtomType.DURATION) && type2.instanceOf(AtomType.DURATION));

      CmpHashG hash = null;
      if(st1.zeroOrOne() && !st1.mayBeArray() && st2.zeroOrOne() && !st2.mayBeArray()) {
        // simple comparisons
        if(!(this instanceof CmpSimpleG)) expr = new CmpSimpleG(expr1, expr2, op, coll, sc, info);
      } else if(op == OpG.EQ && coll == null && (type1.isNumber() && type2.isNumber() ||
          (type1.isStringOrUntyped() && type2.isStringOrUntyped())) && !st2.zeroOrOne()) {
        // hash-based comparisons
        hash = this instanceof CmpHashG ? (CmpHashG) this :
          new CmpHashG(expr1, expr2, op, null, sc, info);
        expr = hash;
      }
      // pre-evaluate expression; discard hashed results
      if(allAreValues(false)) {
        expr = cc.preEval(expr);
        if(hash != null) cc.qc.threads.get(hash).remove();
        return expr;
      }
    }

    // return optimized, pre-evaluated or original expression
    return expr instanceof CmpG ? expr : cc.replaceWith(this, expr);
  }

  /**
   * Tries to rewrite arithmetic operations.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optArith(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], count = exprs[1];
    if(expr1 instanceof Arith && count instanceof ANum) {
      final Arith arith = (Arith) expr1;
      final Expr op1 = arith.arg(0), op2 = arith.arg(1);
      if(arith.calc == Calc.MINUS && op2.seqType().instanceOf(SeqType.NUMERIC_O) &&
          count == Int.ZERO) {
        // sum(A) - sum(B) = 0  ->  sum(A) = sum(B)
        return new CmpG(op1, op2, op, coll, sc, info).optimize(cc);
      } else if(arith.calc != Calc.MOD && arith.calc != Calc.IDIV && op2 instanceof ANum) {
        // count(E) div 2 = 1  ->  count(E) = 1 * 2
        final Expr arg2 = new Arith(info, count, op2, arith.calc.invert()).optimize(cc);
        return new CmpG(op1, arg2, op, coll, sc, info).optimize(cc);
      }
    }
    return this;
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
      final Type type1 = item1.type, type2 = item2.type;
      if(!(type1 == type2 || type1.isUntyped() || type2.isUntyped() ||
          item1 instanceof ANum && item2 instanceof ANum ||
          item1 instanceof AStr && item2 instanceof AStr ||
          item1 instanceof Dur && item2 instanceof Dur)) throw diffError(item1, item2, info);
    }
    return op.opV.eval(item1, item2, coll, sc, info);
  }

  @Override
  public final CmpG invert() {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    return st1.one() && !st1.mayBeArray() && st2.one() && !st2.mayBeArray() ?
      new CmpG(expr1, expr2, op.invert(), coll, sc, info) : null;
  }

  @Override
  public final OpV opV() {
    return op.opV;
  }

  @Override
  public Expr mergeEbv(final Expr expr, final boolean or, final CompileContext cc)
      throws QueryException {

    /* OR: merge comparisons
     * E = 'a' or E = 'b'  ->  E = ('a', 'b')
     * AND: invert operator, wrap with not()
     * E != 'a' and E != 'b'  ->  not(E = ('a', 'b'))
     * negation: invert operator
     * E != 'a' or not(E = 'b')  ->  E != ('a', 'b')  */

    // if required, invert second operator (first operator need never be inverted)
    final boolean not2 = Function.NOT.is(expr);
    Expr expr2 = not2 ? expr.arg(0) : expr;
    if(!(expr2 instanceof CmpG)) return null;

    // compare first and second comparison
    final CmpG cmp2 = (CmpG) expr2;
    final OpG cmpOp = not2 ? cmp2.op.invert() : cmp2.op;
    if(op != cmpOp || coll != cmp2.coll || !exprs[0].equals(cmp2.exprs[0])) return null;

    // function for creating new comparison
    final Expr exprL = exprs[0], exprR1 = exprs[1], exprR2 = cmp2.exprs[1];
    final QueryFunction<OpG, Expr> newList = newOp -> {
      final Expr exprR = List.get(cc, info, exprR1, exprR2);
      return new CmpG(exprL, exprR, newOp, coll, sc, info).optimize(cc);
    };

    // check if comparisons can be merged
    final boolean seqL = !exprL.seqType().one();
    final boolean seqR1 = !exprR1.seqType().one(), seqR2 = !exprR2.seqType().one();
    if(or) {
      /* do not merge if second comparison was inverted and left operand or
       * second right operand contain are not a single item. examples:
       * $number  = 2  or  not($number  = 4)
       * $numbers = 3  or  not($numbers = 4)  */
      if(not2 && (seqR2 || seqL)) return null;
      /* rewriting is possible in all other cases. examples:
       * $number != 1  or  not($number = 2)   ->  $number != (1, 2)
       * $numbers = 2  or  $numbers = (3, 4)  ->  $numbers = (2, 3, 4)  */
      expr2 = newList.apply(op);
    } else {
      /* do not merge if left operand or first right operand is not a single item, or if
       * second comparison was inverted and right operand is not a single item. examples:
       * $numbers = 2      and  $numbers = 2
       * $number = (1, 2)  and  $number  = 3
       * $number = 1       and  not($number = (2, 3)  */
      if(seqL || seqR1 || seqR2 && !not2) return null;
      /* rewriting is possible in all other cases. examples:
       * $number != 1  and  $number != 2             ->  not($number = (1, 2))
       * $numbers = 2  and  not($numbers != (3, 4))  ->  not($numbers != (2, 3, 4))  */
      expr2 = cc.function(Function.NOT, info, newList.apply(op.invert()));
    }

    // return merged expression
    return expr2;
  }

  @Override
  public final boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // only equality expressions on default collation can be rewritten
    if(op != OpG.EQ || coll != null) return false;

    Expr expr1 = exprs[0];
    IndexType type = null;
    if(Function.TOKENIZE.is(expr1)) {
      if(!(expr1.arg(0).seqType().zeroOrOne() && ((FnTokenize) expr1).whitespaces())) return false;
      expr1 = expr1.arg(0);
      type = IndexType.TOKEN;
    }
    return ii.create(exprs[1], ii.type(expr1, type), false, info);
  }

  @Override
  public CmpG copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final CmpG cmp = new CmpG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, coll, sc, info);
    cmp.check = check;
    return copyType(cmp);
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof CmpG && op == ((CmpG) obj).op && super.equals(obj);
  }

  @Override
  public String description() {
    return op + " comparison";
  }

  @Override
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this, OP, op.name), exprs);
  }

  @Override
  public final void plan(final QueryString qs) {
    qs.tokens(exprs, " " + op + ' ', true);
  }
}
