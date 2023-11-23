package org.basex.query.expr;

import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract comparison.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class Cmp extends Arr {
  /** Collation (can be {@code null}). */
  final Collation coll;
  /** Static context. */
  final StaticContext sc;

  /** Check: true. */
  private static final long[] COUNT_TRUE = { };
  /** Check: false. */
  private static final long[] COUNT_FALSE = { };
  /** Check: empty. */
  private static final long[] COUNT_EMPTY = { };
  /** Check: exists. */
  private static final long[] COUNT_EXISTS = { };

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr1 first expression
   * @param expr2 second expression
   * @param coll collation (can be {@code null})
   * @param seqType sequence type
   * @param sc static context
   */
  Cmp(final InputInfo info, final Expr expr1, final Expr expr2, final Collation coll,
      final SeqType seqType, final StaticContext sc) {
    super(info, seqType, expr1, expr2);
    this.coll = coll;
    this.sc = sc;
  }

  /**
   * Swaps the operands of the expression if this might improve performance.
   * The operator itself needs to be swapped by the calling expression.
   * @return resulting expression
   */
  final boolean swap() {
    // move value, or path without root, to second position
    final Expr expr1 = exprs[0], expr2 = exprs[1];

    final boolean swap = POSITION.is(expr2) || !(expr2 instanceof Value) && (
      // move static value to the right: $words = 'words'
      expr1 instanceof Value ||
      // hashed comparisons: move larger sequences to the right: $small = $large
      expr1.size() > 1 && expr1.size() > expr2.size() &&
      expr1.seqType().type.instanceOf(AtomType.ANY_ATOMIC_TYPE) ||
      // hashed comparisons: . = $words
      expr1 instanceof VarRef && expr1.seqType().occ.max > 1 &&
        !(expr2 instanceof VarRef && expr2.seqType().occ.max > 1) ||
      // context value: . = $item
      expr1 instanceof VarRef && expr2 instanceof ContextValue ||
      // index rewritings: move path to the left: word/text() = $word
      !(expr1 instanceof Path && ((Path) expr1).root == null) &&
        expr2 instanceof Path && ((Path) expr2).root == null
    );

    if(swap) {
      exprs[0] = expr2;
      exprs[1] = expr1;
    }
    return swap;
  }

  /**
   * If possible, returns an optimized expression with inverted operands.
   * @param cc compilation context
   * @return original or modified expression
   * @throws QueryException query exception
   */
  public final Expr invert(final CompileContext cc) throws QueryException {
    final Expr expr = invert();
    return expr != null ? expr.optimize(cc) : this;
  }

  /**
   * If possible, returns an expression with inverted operands.
   * @return original or {@code null}
   */
  public abstract Expr invert();

  /**
   * Returns the value comparator of the expression.
   * @return operator, or {@code null} for node comparisons
   */
  public abstract OpV opV();

  /**
   * Returns the general comparator of the expression.
   * @return operator, or {@code null} for node comparisons
   */
  public abstract OpG opG();

  /**
   * Performs various optimizations.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  final Expr opt(final CompileContext cc) throws QueryException {
    final OpV op = opV();
    Expr expr = optPos(op, cc);
    if(expr == this) expr = optEqual(op, cc);
    if(expr == this) expr = optCount(op, cc);
    if(expr == this) expr = optBoolean(op, cc);
    if(expr == this) expr = optEmptyString(op, cc);
    if(expr == this) expr = optStringLength(op, cc);
    return expr;
  }

  /**
   * Tries to simplify an expression with equal operands.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   */
  private Expr optEqual(final OpV op, final CompileContext cc) {
    if(!(this instanceof CmpG)) return this;

    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType();
    final Type type1 = st1.type;
    if(expr1.equals(expr2) &&
      // keep: () = (), (1,2) != (1,2), (1,2) eq (1,2)
      (op != OpV.EQ || this instanceof CmpV ? st1.one() : st1.oneOrMore()) &&
      // keep: xs:double('NaN') = xs:double('NaN')
      (type1.isStringOrUntyped() || type1.instanceOf(AtomType.DECIMAL) ||
          type1 == AtomType.BOOLEAN) &&
      // keep: random:integer() = random:integer()
      // keep if no context is available: last() = last()
      !expr1.has(Flag.NDT) && (!expr1.has(Flag.CTX) || cc.qc.focus.value != null)
    ) {
      // 1 = 1  ->  true()
      // <x/> ne <x/>  ->  false()
      // (1, 2) >= (1, 2)  ->  true()
      // (1, 2, 3)[last() = last()]  ->  (1, 2, 3)
      return Bln.get(op == OpV.EQ || op == OpV.GE || op == OpV.LE);
    }
    return this;
  }

  /**
   * Tries to rewrite boolean comparisons.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optBoolean(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    if(st1.type == AtomType.BOOLEAN && st2.type == AtomType.BOOLEAN) {
      final boolean eq = op == OpV.EQ, ne = op == OpV.NE;
      if(expr2 instanceof Bln) {
        // boolean(A) = true()  ->  boolean(A)
        // boolean(A) <= true()  ->  true()
        final boolean ok = expr2 == Bln.TRUE, success = ne ^ ok;
        final Expr ex1 = st1.one() ? expr1 : st1.zeroOrOne() ? cc.function(BOOLEAN, info, expr1) :
          null;
        if(ex1 != null && (success || st1.one())) {
          final QuerySupplier<Expr> not = () -> cc.function(NOT, info, ex1);
          switch(op) {
            case EQ: return ok ? ex1       : not.get();
            case NE: return ok ? not.get() : ex1;
            case GE: return ok ? ex1       : Bln.TRUE;
            case LE: return ok ? Bln.TRUE  : not.get();
            case GT: return ok ? Bln.FALSE : ex1;
            default: return ok ? not.get() : Bln.FALSE;
          }
        }

        if(this instanceof CmpG) {
          // (A, B) = true()  ->  A or B
          // (A, B) = false()  ->  not(A and B)
          final Checks<Expr> booleans = expr -> expr.seqType().eq(SeqType.BOOLEAN_O);
          final Expr[] args = expr1.args();
          if((eq || ne) && expr1 instanceof List && booleans.all(args)) {
            if(success) return new Or(info, args).optimize(cc);
            return cc.function(NOT, info, new And(info, args).optimize(cc));
          }

          if(expr1 instanceof SimpleMap) {
            final int al = args.length;
            final Expr last = args[al - 1];
            final Expr[] ops = last.args();
            if(ops != null && ops.length > 0 && ops[0] instanceof ContextValue) {
              final QuerySupplier<Expr> op1 = () ->
                SimpleMap.get(cc, info, Arrays.copyOf(args, al - 1));
              if(last instanceof CmpG) {
                // (name ! (. = 'Ukraine')) = true()  ->  name = 'Ukraine'
                // (code ! (. = 1)) = false()  ->  code != 1
                final Expr op2 = ops[1];
                if(!op2.has(Flag.CTX) && (eq && ok || op2.seqType().one())) {
                  OpG opG = ((CmpG) last).op;
                  if(!success) opG = opG.invert();
                  return new CmpG(info, op1.get(), op2, opG, coll, sc).optimize(cc);
                }
              } else if(success && last instanceof CmpR) {
                // (number ! (. >= 1e0) = true()  ->  number >= 1e0
                final CmpR cmp = (CmpR) last;
                return CmpR.get(cc, info, op1.get(), cmp.min, cmp.max);
              } else if(success && last instanceof CmpIR) {
                // (integer ! (. >= 1) != false()  ->  integer >= 1
                final CmpIR cmp = (CmpIR) last;
                return CmpIR.get(cc, info, op1.get(), cmp.min, cmp.max);
              } else if(success && last instanceof CmpSR) {
                // (string ! (. >= 'b') = true()  ->  string >= 'b'
                final CmpSR cmp = (CmpSR) last;
                return new CmpSR(op1.get(), cmp.min, cmp.mni, cmp.max, cmp.mxi, cmp.coll,
                    info).optimize(cc);
              }
            }
          }
        }
      }
      // BOOL = not(BOOL)  ->  false()
      if((eq || ne) && st1.one() && st2.one() && (NOT.is(expr2) && expr1.equals(expr2.arg(0)) ||
          NOT.is(expr1) && expr2.equals(expr1.arg(0)))) return Bln.get(ne);
    }
    return this;
  }

  /**
   * Tries to rewrite {@code fn:count}.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optCount(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0];
    if(!COUNT.is(expr1)) return this;

    // distinct values checks
    final Expr arg = expr1.arg(0), count = exprs[1];
    if(COUNT.is(count)) {
      final Expr carg = count.arg(0);
      // count(E) = count(distinct-values(E))
      if(DISTINCT_VALUES.is(carg) && arg.equals(carg.arg(0)))
        return ((FnDistinctValues) carg).duplicates(op, cc);
      // count(distinct-values(E)) = count(E)
      if(DISTINCT_VALUES.is(arg) && arg.arg(0).equals(carg))
        return ((FnDistinctValues) arg).duplicates(op.swap(), cc);
    }
    // count(distinct-values(E)) = int
    if(DISTINCT_VALUES.is(arg) && count instanceof Int) {
      final long size1 = arg.arg(0).size(), size2 = ((Int) count).itr();
      if(size1 != -1 && size1 == size2) return ((FnDistinctValues) arg).duplicates(op.swap(), cc);
    }

    final ExprList args = new ExprList(3);
    if(count instanceof ANum) {
      final double cnt = ((ANum) count).dbl();
      if(arg.seqType().zeroOrOne()) {
        // count(ZeroOrOne)
        if(cnt > 1) {
          return Bln.get(op == OpV.LT || op == OpV.LE || op == OpV.NE);
        }
        if(cnt == 1) {
          return op == OpV.NE || op == OpV.LT ? cc.function(EMPTY, info, arg) :
                 op == OpV.EQ || op == OpV.GE ? cc.function(EXISTS, info, arg) :
                 Bln.get(op == OpV.LE);
        }
      }
      final long[] counts = countRange(op, cnt);
      // count(A) >= 0  ->  true()
      if(counts == COUNT_TRUE || counts == COUNT_FALSE) {
        return Bln.get(counts == COUNT_TRUE);
      }
      // count(A) > 0  ->  exists(A)
      if(counts == COUNT_EMPTY || counts == COUNT_EXISTS) {
        return cc.function(counts == COUNT_EMPTY ? EMPTY : EXISTS, info, arg);
      }
      // count(A) > 1  ->  util:within(A, 2)
      if(counts != null) {
        for(final long c : counts) args.add(Int.get(c));
      }
    } else if(op == OpV.EQ || op == OpV.GE || op == OpV.LE) {
      final SeqType st2 = count.seqType();
      if(st2.type.instanceOf(AtomType.INTEGER)) {
        if(count instanceof RangeSeq) {
          final long[] range = ((RangeSeq) count).range(false);
          args.add(Int.get(range[0])).add(Int.get(range[1]));
        } else if(st2.one() && (count instanceof VarRef || count instanceof ContextValue)) {
          args.add(count).add(count);
        }
        if(!args.isEmpty()) {
          if(op == OpV.GE) args.remove(args.size() - 1);
          else if(op == OpV.LE) args.set(0, Int.ONE);
        }
      }
    }
    if(args.isEmpty()) return this;

    // count(A) = 1  ->  util:within(A, 1, 1)
    args.insert(0, arg);
    return cc.function(_UTIL_COUNT_WITHIN, info, args.finish());
  }

  /**
   * Tries to rewrite {@code fn:string-length}.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optStringLength(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(!(STRING_LENGTH.is(expr1) && expr2 instanceof ANum)) return this;

    final Expr[] args = expr1.args();
    final long[] counts = countRange(op, ((ANum) expr2).dbl());
    if(counts == COUNT_TRUE || counts == COUNT_FALSE) {
      // string-length(A) >= 0  ->  true()
      final Expr arg1 = args.length > 0 ? args[0] : cc.qc.focus.value;
      if(arg1 != null) {
        final SeqType st1 = arg1.seqType();
        if(st1.zero() || st1.one() && st1.type.isStringOrUntyped())
          return Bln.get(counts == COUNT_TRUE);
      }
    }
    if(counts == COUNT_EMPTY || counts == COUNT_EXISTS) {
      // string-length(A) > 0  ->  boolean(string(A))
      final Function func = counts == COUNT_EMPTY ? NOT : BOOLEAN;
      return cc.function(func, info, cc.function(STRING, info, args));
    }
    return this;
  }

  /**
   * Tries to rewrite comparisons with empty strings.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optEmptyString(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType();
    if(st1.one() && st1.type.isStringOrUntyped() && expr2 == Str.EMPTY) {
      if(op == OpV.LT) return Bln.FALSE;
      if(op == OpV.GE) return Bln.TRUE;
      // do not rewrite GT, as it may be rewritten to a range expression later on
      if(op != OpV.GT) {
        // EQ and LE can be treated identically
        final Function func = op == OpV.NE ? BOOLEAN : NOT;
        return cc.function(func, info, cc.function(DATA, info, exprs[0]));
      }
    }
    return this;
  }

  /**
   * Positional optimizations.
   * @param op comparison operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optPos(final OpV op, final CompileContext cc) throws QueryException {
    if(POSITION.is(exprs[0])) {
      final Expr expr = Pos.get(exprs[1], op, info, cc, true);
      if(expr != null) return expr;
    }
    return this;
  }

  /**
   * Analyzes the comparison and returns its optimization type.
   * @param op operator
   * @param count count to compare against
   * @return comparison type, min/max range or {@code null}
   */
  private static long[] countRange(final OpV op, final double count) {
    // skip special cases
    if(!Double.isFinite(count)) return null;

    // > (v<0), != (v<0), >= (v<=0), != integer(v)
    final long cnt = (long) count;
    if((op == OpV.GT || op == OpV.NE) && count < 0 ||
        op == OpV.GE && count <= 0 ||
        op == OpV.NE && count != cnt) return COUNT_TRUE;
    // < (v<=0), <= (v<0), = (v<0), != integer(v)
    if(op == OpV.LT && count <= 0 ||
      (op == OpV.LE || op == OpV.EQ) && count < 0 ||
       op == OpV.EQ && count != cnt) return COUNT_FALSE;
    // < (v<=1), <= (v<1), = (v=0)
    if(op == OpV.LT && count <= 1 ||
       op == OpV.LE && count < 1 ||
       op == OpV.EQ && count == 0) return COUNT_EMPTY;
    // > (v<1), >= (v<=1), != (v=0)
    if(op == OpV.GT && count < 1 ||
       op == OpV.GE && count <= 1 ||
       op == OpV.NE && count == 0) return COUNT_EXISTS;
    // range queries
    if(op == OpV.GT) return new long[] { (long) Math.floor(count) + 1 };
    if(op == OpV.GE) return new long[] { (long) Math.ceil(count) };
    if(op == OpV.LT) return new long[] { 0, (long) Math.ceil(count) - 1 };
    if(op == OpV.LE) return new long[] { 0, (long) Math.floor(count) };
    if(op == OpV.EQ) return new long[] { cnt, cnt };
    return null;
  }
}
