package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.index.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General comparison.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class CmpG extends Cmp {
  /** Comparators. */
  public enum OpG {
    /** General comparison: less or equal. */
    LE("<=") {
      @Override
      public OpG swap() { return OpG.GE; }
      @Override
      public OpG invert() { return OpG.GT; }
      @Override
      public OpV value() { return OpV.LE; }
    },

    /** General comparison: less. */
    LT("<") {
      @Override
      public OpG swap() { return OpG.GT; }
      @Override
      public OpG invert() { return OpG.GE; }
      @Override
      public OpV value() { return OpV.LT; }
    },

    /** General comparison: greater of equal. */
    GE(">=") {
      @Override
      public OpG swap() { return LE; }
      @Override
      public OpG invert() { return LT; }
      @Override
      public OpV value() { return OpV.GE; }
    },

    /** General comparison: greater. */
    GT(">") {
      @Override
      public OpG swap() { return LT; }
      @Override
      public OpG invert() { return LE; }
      @Override
      public OpV value() { return OpV.GT; }
    },

    /** General comparison: equal. */
    EQ("=") {
      @Override
      public OpG swap() { return OpG.EQ; }
      @Override
      public OpG invert() { return OpG.NE; }
      @Override
      public OpV value() { return OpV.EQ; }
    },

    /** General comparison: not equal. */
    NE("!=") {
      @Override
      public OpG swap() { return OpG.NE; }
      @Override
      public OpG invert() { return EQ; }
      @Override
      public OpV value() { return OpV.NE; }
    };

    /** Cached enums (faster). */
    public static final OpG[] VALUES = values();
    /** String representation. */
    public final String name;

    /**
     * Constructor.
     * @param name string representation
     */
    OpG(final String name) {
      this.name = name;
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

    /**
     * Returns the value comparator.
     * @return comparator
     */
    public abstract OpV value();

    @Override
    public String toString() {
      return name;
    }

    /**
     * Checks if this is one of the specified types.
     * @param ops types
     * @return result of check
     */
    public final boolean oneOf(final OpG... ops) {
      for(final OpG op : ops) {
        if(this == op) return true;
      }
      return false;
    }

    /**
     * Returns the comparator for the specified value comparison operator.
     * @param opV operator to be found
     * @return comparator or {@code null}
     */
    static OpG get(final OpV opV) {
      for(final OpG value : VALUES) {
        if(value.value() == opV) return value;
      }
      return null;
    }
  }

  /** Operator. */
  OpG op;
  /** Type check at runtime. */
  boolean check = true;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   */
  public CmpG(final InputInfo info, final Expr expr1, final Expr expr2, final OpG op) {
    super(info, expr1, expr2, SeqType.BOOLEAN_O);
    this.op = op;
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // pre-evaluate if one value is empty:
    // () eq local:expensive()  ->  ()
    // void(123) = 1  ->  boolean(void('123'))
    Expr expr = emptyExpr();
    if(expr != this) return cc.replaceWith(this, cc.function(Function.BOOLEAN, info, expr));

    // remove redundant type conversions
    final Type t1 = exprs[0].seqType().type.atomic(), t2 = exprs[1].seqType().type.atomic();
    if(t1 != null && t2 != null) {
      if(t1.isStringOrUntyped() && t2.isStringOrUntyped()) {
        exprs = simplifyAll(Simplify.STRING, cc);
      } else if(t1.isNumber() && t2.isNumber()) {
        exprs = simplifyAll(Simplify.NUMBER, cc);
      }
    }

    // simplify operands
    exprs = simplifyAll(Simplify.DISTINCT, cc);

    // swap operands
    if(swap()) {
      cc.info(QueryText.OPTSWAP_X, this);
      op = op.swap();
    }

    // optimize expression
    expr = opt(cc);

    // (if(A) then B else C) = X  ->  if(A) then B = X else C = X
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr == this && expr1 instanceof If && !expr1.has(Flag.NDT)) {
      final If iff = (If) expr1;
      final Expr thn = new CmpG(info, iff.arg(0), expr2, op);
      final Expr els = new CmpG(info, iff.arg(1), expr2.copy(cc, new IntObjMap<>()), op);
      return new If(info, iff.cond, thn.optimize(cc), els.optimize(cc)).optimize(cc);
    }

    if(expr == this) expr = optArith(cc);
    if(expr == this) expr = CmpIR.get(cc, this, false);
    if(expr == this) expr = CmpR.get(cc, this);
    if(expr == this) expr = CmpSR.get(cc, this);

    if(expr == this) {
      // determine types, choose best implementation
      final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
      final Type type1 = st1.type, type2 = st2.type;
      // skip type check if types are identical, have a specific atomic type and are comparable
      check = !(type1 == type2 && !type1.oneOf(AtomType.ANY_ATOMIC_TYPE, AtomType.ITEM) &&
          comparable(type1, type2, true));

      CmpHashG hash = null;
      if(st1.zeroOrOne() && !st1.mayBeArray() && st2.zeroOrOne() && !st2.mayBeArray()) {
        // simple comparisons
        if(!(this instanceof CmpSimpleG)) {
          expr = new CmpSimpleG(expr1, expr2, op, info, check);
        }
      } else if(op == OpG.EQ && sc().collation == null && (type1.isNumber() && type2.isNumber() ||
          type1.isStringOrUntyped() && type2.isStringOrUntyped()) && !st2.zeroOrOne()) {
        // hash-based comparisons
        hash = this instanceof CmpHashG ? (CmpHashG) this : new CmpHashG(expr1, expr2, op, info);
        expr = hash;
      }
      // pre-evaluate expression; discard hashed results
      if(allAreValues(false)) {
        expr = cc.preEval(expr);
        if(hash != null) cc.qc.threads.get(hash, info).remove();
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
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    Expr ex = null;

    if(expr1 instanceof Arith && expr2.seqType().instanceOf(SeqType.NUMERIC_O)) {
      final Expr op11 = expr1.arg(0), op12 = expr1.arg(1), op22 = expr2.arg(1);
      final double num12 = op12 instanceof ANum ? ((ANum) op12).dbl() : Double.NaN;
      if(op12.seqType().instanceOf(SeqType.NUMERIC_O)) {
        final Calc calc1 = ((Arith) expr1).calc;
        if(calc1 == Calc.SUBTRACT && expr2 == Int.ZERO) {
          // E - NUMERIC = 0  ->  E = NUMERIC
          ex = new CmpG(info, op11, op12, op);
        } else if((
          Function.POSITION.is(op11) ||
          !Double.isNaN(num12) &&
          (expr2 instanceof ANum || expr2 instanceof Arith && op22 instanceof ANum)
        ) && (
          calc1.oneOf(Calc.ADD, Calc.SUBTRACT) ||
          calc1.oneOf(Calc.MULTIPLY, Calc.DIVIDE) && num12 != 0 &&
            (op.oneOf(OpG.EQ, OpG.NE) || num12 > 0)
        )) {
          // position() + 1 < last()  ->  position() < last() - 1
          // count(E) div 2 = 1  ->  count(E) = 1 * 2
          // $a - 1 = $b + 1  ->  $a = $b + 2
          // $x * -1 = 1  ->  $x = 1 div -1  (no rewrite if RHS of */div (<,<=,>=,>) is negative)
          final Expr arg2 = new Arith(info, expr2, op12, calc1.invert()).optimize(cc);
          ex = new CmpG(info, op11, arg2, op);
        }
      }
    }
    return ex != null ? ex.optimize(cc) : this;
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
   * @param iter1 first atomic iterator
   * @param iter2 second atomic iterator
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
      if(!comparable(type1, type2, true)) throw compareError(item1, item2, info);
    }
    return op.value().eval(item1, item2, info);
  }

  /**
   * Checks if an expression with the specified types can be rewritten to a general comparison.
   * @param st1   first sequence type
   * @param st2   second sequence type
   * @param eqne  eq/ne comparison
   * @return result of check
   */
  public static boolean compatible(final SeqType st1, final SeqType st2, final boolean eqne) {
    final Type type1 = st1.type, type2 = st2.type;
    return type1 == type2 && !AtomType.ANY_ATOMIC_TYPE.instanceOf(type1) &&
        (type1.isSortable() || !eqne) ||
      type1.isStringOrUntyped() && type2.isStringOrUntyped() ||
      type1 == AtomType.QNAME && type2 == AtomType.QNAME ||
      type1.instanceOf(AtomType.NUMERIC) && type2.instanceOf(AtomType.NUMERIC) ||
      type1.instanceOf(AtomType.DURATION) && type2.instanceOf(AtomType.DURATION);
  }

  /**
   * Checks if types can be compared.
   * @param type1 first type to compare
   * @param type2 second type to compare
   * @param untyped allow untyped atomics
   * @return result of check
   */
  public static boolean comparable(final Type type1, final Type type2, final boolean untyped) {
    return type1 == type2 ||
      type1.isNumber() && type2.isNumber() ||
      type1.isStringOrUntyped() && type2.isStringOrUntyped() ||
      untyped && (type1.isUntyped() || type2.isUntyped()) ||
      type1.instanceOf(AtomType.DURATION) && type2.instanceOf(AtomType.DURATION) ||
      type1.instanceOf(AtomType.BINARY) && type2.instanceOf(AtomType.BINARY);
  }

  @Override
  public final CmpG invert() {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    return st1.one() && !st1.mayBeArray() && st2.one() && !st2.mayBeArray() ?
      new CmpG(info, expr1, expr2, op.invert()) : null;
  }

  @Override
  public final OpV opV() {
    return op.value();
  }

  @Override
  public final OpG opG() {
    return op;
  }

  @Override
  public Expr mergeEbv(final Expr expr, final boolean or, final CompileContext cc)
      throws QueryException {

    if(expr instanceof Single) return expr.mergeEbv(this, or, cc);

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
    if(op != cmpOp || sc().collation != cmp2.sc().collation || !exprs[0].equals(cmp2.exprs[0]))
      return null;

    // function for creating new comparison
    final Expr exprL = exprs[0], exprR1 = exprs[1], exprR2 = cmp2.exprs[1];
    final QueryFunction<OpG, Expr> newList = newOp -> {
      final Expr exprR = List.get(cc, info, exprR1, exprR2);
      return new CmpG(info, exprL, exprR, newOp).optimize(cc);
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
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // E[local-name() = 'a']  ->  E[self::*:a]
    return cc.simplify(this, mode.oneOf(Simplify.EBV, Simplify.PREDICATE) ? optPred(cc) : this,
      mode);
  }

  /**
   * Optimizes this expression as predicate.
   * @param cc compilation context
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Expr optPred(final CompileContext cc) throws QueryException {
    final Value val = cc.qc.focus.value;
    if(val == null) return this;

    final Type type = val.seqType().type;
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final OpV opV = opV();
    if(type instanceof NodeType && type != NodeType.NODE && expr1 instanceof ContextFn &&
        expr2 instanceof Value && opV == OpV.EQ) {
      // skip functions that do not refer to the current context item
      final ContextFn func = (ContextFn) expr1;
      final Value value = (Value) expr2;
      if(func.exprs.length > 0 && !(func.exprs[0] instanceof ContextValue)) return this;

      final ArrayList<QNm> qnames = new ArrayList<>();
      NamePart part = null;
      if(expr2.seqType().type.isStringOrUntyped()) {
        // local-name() eq 'a'  ->  self::*:a
        if(LOCAL_NAME.is(func)) {
          part = NamePart.LOCAL;
          for(final Item item : value) {
            final byte[] name = item.string(info);
            if(XMLToken.isNCName(name)) qnames.add(new QNm(name));
          }
        } else if(NAMESPACE_URI.is(func)) {
          // namespace-uri() = ('URI1', 'URI2')  ->  self::Q{URI1}* | self::Q{URI2}*
          for(final Item item : value) {
            final byte[] uri = item.string(info);
            if(Token.eq(Token.normalize(uri), uri)) qnames.add(new QNm(Token.COLON, uri));
          }
          if(qnames.size() == value.size()) part = NamePart.URI;
        } else if(NAME.is(func)) {
          // (db-without-ns)[name() = 'city']  ->  (db-without-ns)[self::city]
          final Data data = cc.qc.focus.value.data();
          final byte[] dataNs = data != null ? data.defaultNs() : null;
          if(dataNs != null && dataNs.length == 0) {
            part = NamePart.LOCAL;
            for(final Item item : value) {
              final byte[] name = item.string(info);
              if(XMLToken.isNCName(name)) qnames.add(new QNm(name));
            }
          }
        }
      } else if(NODE_NAME.is(func) && expr2.seqType().type == AtomType.QNAME) {
        // node-name() = xs:QName('prefix:local')  ->  self::prefix:local
        part = NamePart.FULL;
        for(final Item item : value) {
          qnames.add((QNm) item);
        }
      }

      if(part != null) {
        final ExprList paths = new ExprList(2);
        for(final QNm qname : qnames) {
          final Test test = new NameTest(qname, part, (NodeType) type, sc().elemNS);
          final Expr step = Step.get(cc, null, info, test);
          if(step != Empty.VALUE) paths.add(Path.get(cc, info, null, step));
        }
        return paths.isEmpty() ? Bln.FALSE : paths.size() == 1 ? paths.get(0) :
          new Union(info, paths.finish()).optimize(cc);
      }
    }
    return this;
  }

  @Override
  public final boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // only equality expressions on default collation can be rewritten
    if(op != OpG.EQ || sc().collation != null) return false;

    Expr expr1 = exprs[0];
    IndexType type = null;
    if(Function.TOKENIZE.is(expr1)) {
      if(!(expr1.arg(0).seqType().zeroOrOne() && ((FnTokenize) expr1).whitespace())) return false;
      expr1 = expr1.arg(0);
      type = IndexType.TOKEN;
    }
    return ii.create(exprs[1], ii.type(expr1, type), false, info);
  }

  @Override
  public CmpG copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final CmpG cmp = new CmpG(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op);
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
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, QueryText.OP, op.name), exprs);
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.tokens(exprs, " " + op + ' ', true);
  }
}
