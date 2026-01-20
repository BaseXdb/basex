package org.basex.query.expr;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Cmp extends Arr {
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
   * @param seqType sequence type
   */
  Cmp(final InputInfo info, final Expr expr1, final Expr expr2, final SeqType seqType) {
    super(info, seqType, expr1, expr2);
  }

  /**
   * Checks if the operands of the expression can be swapped to improve performance.
   * @return result of check
   */
  final boolean swap() {
    final Expr expr1 = exprs[0], expr2 = exprs[1];

    // keep dedicated function calls as left-hand operand
    if(COUNT.is(expr1) || POSITION.is(expr1)) return false;

    // move position() and count() to the left: position() = 123
    boolean swap = COUNT.is(expr2) || POSITION.is(expr2);
    // right operand is a value, and left operand yields more results: (1, 2) = 3
    if(!swap && expr2 instanceof Value)
      return expr1 instanceof Value && expr1.size() > expr2.size();

    // move static value to the right: $words = 'words'
    if(!swap) swap = expr1 instanceof Value;
    // move larger input to the right: $small = $large
    if(!swap) swap = expr1.size() > 1 && expr1.size() > expr2.size();
    // move context item to the left: . = $input
    if(!swap) swap = expr2 instanceof ContextValue && expr2.size() == 1 &&
        !(expr1 instanceof ContextValue);
    // move path to the left: word/text() = $word
    if(!swap) swap = !(expr1 instanceof final Path pth1 && pth1.root == null) &&
      expr2 instanceof final Path pth2 && pth2.root == null;

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
   * Returns the general comparator of the expression.
   * @return operator, or {@code null} for node comparisons
   */
  public abstract CmpOp cmpOp();

  /**
   * Performs various optimizations.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  final Expr opt(final CompileContext cc) throws QueryException {
    final CmpOp op = cmpOp();
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
  private Expr optEqual(final CmpOp op, final CompileContext cc) {
    if(!(this instanceof CmpG)) return this;

    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType();
    final Type type1 = st1.type;
    if(expr1.equals(expr2) &&
      // keep: () = (), (1,2) != (1,2), (1,2) eq (1,2)
      (op != CmpOp.EQ ? st1.one() : st1.oneOrMore()) &&
      // keep: xs:double('NaN') = xs:double('NaN')
      (type1.isStringOrUntyped() || type1.instanceOf(AtomType.DECIMAL) ||
          type1 == AtomType.BOOLEAN) &&
      // keep: random:integer() = random:integer()
      // keep if no context is available: last() = last()
      !expr1.has(Flag.NDT) && (!expr1.has(Flag.CTX) || cc.qc.focus.value != null)
    ) {
      // 1 = 1 → true()
      // <x/> ne <x/> → false()
      // (1, 2) >= (1, 2) → true()
      // (1, 2, 3)[last() = last()] → (1, 2, 3)
      return Bln.get(op.oneOf(CmpOp.EQ, CmpOp.GE, CmpOp.LE));
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
  private Expr optBoolean(final CmpOp op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    if(st1.type == AtomType.BOOLEAN && st2.type == AtomType.BOOLEAN) {
      final boolean eq = op == CmpOp.EQ, ne = op == CmpOp.NE;
      if(expr2 instanceof Bln) {
        final boolean ok = expr2 == Bln.TRUE, success = ne ^ ok;

        // boolean(A) = true() → boolean(A)
        // boolean(A) <= true() → true()
        if(st1.zeroOrOne() && (success || st1.one())) {
          final Expr ex1 = st1.one() ? expr1 : cc.function(BOOLEAN, info, expr1);
          final QuerySupplier<Expr> not = () -> cc.function(NOT, info, ex1);
          return switch(op) {
            case EQ -> ok ? ex1       : not.get();
            case NE -> ok ? not.get() : ex1;
            case GE -> ok ? ex1       : Bln.TRUE;
            case LE -> ok ? Bln.TRUE  : not.get();
            case GT -> ok ? Bln.FALSE : ex1;
            default -> ok ? not.get() : Bln.FALSE;
          };
        }

        if(this instanceof CmpG) {
          // (A, B) = true() → A or B
          // (A, B) = false() → not(A and B)
          final Expr[] args = expr1.args();
          if((eq || ne) && expr1 instanceof List &&
              ((Checks<Expr>) expr -> expr.seqType().eq(Types.BOOLEAN_O)).all(args)) {
            return success ? new Or(info, args).optimize(cc) :
              cc.function(NOT, info, new And(info, args).optimize(cc));
          }

          if(expr1 instanceof final SimpleMap map) {
            final int al = args.length - 1;
            final Expr last = args[al];

            // expr ! true() = true() → exists(expr)
            if(last instanceof final Bln bln && (eq || ne)) {
              return bln.bool(info) != success ? Bln.FALSE :
                cc.function(EXISTS, info, map.remove(cc, al));
            }

            final Expr[] ops = last.args();
            if(ops != null && ops.length > 0 && ops[0] instanceof ContextValue) {
              if(last instanceof final CmpG cmp) {
                // (name ! (. = 'Ukraine')) = true() → name = 'Ukraine'
                // (code ! (. = 1)) = false() → code != 1
                final Expr op2 = ops[1];
                if(!op2.has(Flag.CTX) && (eq && ok || op2.seqType().one())) {
                  CmpOp cmpOp = cmp.op;
                  if(!success) cmpOp = cmpOp.invert();
                  return new CmpG(info, map.remove(cc, al), op2, cmpOp).optimize(cc);
                }
              } else if(success && last instanceof final CmpR cmp) {
                // (number ! (. >= 1e0) = true() → number >= 1e0
                return CmpR.get(cc, info, map.remove(cc, al), cmp.min, cmp.max);
              } else if(success && last instanceof final CmpIR cmp) {
                // (integer ! (. >= 1) != false() → integer >= 1
                return CmpIR.get(cc, info, map.remove(cc, al), cmp.min, cmp.max);
              } else if(success && last instanceof final CmpSR cmp) {
                // (string ! (. >= 'b') = true() → string >= 'b'
                return new CmpSR(map.remove(cc, al), cmp.min, cmp.mni, cmp.max, cmp.mxi, info).
                    optimize(cc);
              }
            }
          }
        }
      }
      // BOOL = not(BOOL) → false()
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
  private Expr optCount(final CmpOp op, final CompileContext cc) throws QueryException {
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
    if(DISTINCT_VALUES.is(arg) && count instanceof final Itr itr) {
      final long size1 = arg.arg(0).size(), size2 = itr.itr();
      if(size1 != -1 && size1 == size2) return ((FnDistinctValues) arg).duplicates(op.swap(), cc);
    }

    final ExprList args = new ExprList(3);
    if(count instanceof final ANum num) {
      final double cnt = num.dbl();
      if(arg.seqType().zeroOrOne()) {
        // count(ZeroOrOne) < 2 → true()
        if(cnt > 1) {
          return Bln.get(op.oneOf(CmpOp.LT, CmpOp.LE, CmpOp.NE));
        }
        // count(ZeroOrOne)  < 1 → empty(ZeroOrOne)
        // count(ZeroOrOne)  = 1 → exists(ZeroOrOne)
        // count(ZeroOrOne) <= 1 → true()
        if(cnt == 1) {
          return op.oneOf(CmpOp.NE, CmpOp.LT) ? cc.function(EMPTY, info, arg) :
                 op.oneOf(CmpOp.EQ, CmpOp.GE) ? cc.function(EXISTS, info, arg) :
                 Bln.get(op == CmpOp.LE);
        }
      }
      final long[] counts = countRange(op, cnt);
      // count(A) >= 0 → true()
      if(counts == COUNT_TRUE) return Bln.TRUE;
      if(counts == COUNT_FALSE) return Bln.FALSE;
      // count(A) > 0 → exists(A)
      if(counts == COUNT_EMPTY) return cc.function(EMPTY, info, arg);
      if(counts == COUNT_EXISTS) return cc.function(EXISTS, info, arg);
      // count(A) > 1 → util:within(A, 2)
      // count(A) < 5 → util:within(A, 0, 4)
      if(counts != null) {
        for(final long c : counts) args.add(Itr.get(c));
      }
    } else if(op.oneOf(CmpOp.EQ, CmpOp.GE, CmpOp.LE)) {
      final SeqType st2 = count.seqType();
      if(st2.type.instanceOf(AtomType.INTEGER)) {
        if(count instanceof final RangeSeq rs) {
          // count(A) = 3 to 5 → util:within(A, 3, 5)
          args.add(Itr.get(rs.min())).add(Itr.get(rs.max()));
        } else if(st2.one() && (count instanceof VarRef || count instanceof ContextValue)) {
          // count(A) = $c → util:within(A, $c)
          args.add(count).add(count);
        }
        if(!args.isEmpty()) {
          if(op == CmpOp.GE) args.remove(args.size() - 1);
          else if(op == CmpOp.LE) args.set(0, Itr.ONE);
        }
      }
    }
    if(args.isEmpty()) return this;

    // count(A) = 1 → util:within(A, 1, 1)
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
  private Expr optStringLength(final CmpOp op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(!(STRING_LENGTH.is(expr1) && expr2 instanceof final ANum num)) return this;

    final Expr[] args = expr1.args();
    final long[] counts = countRange(op, num.dbl());
    if(counts == COUNT_TRUE || counts == COUNT_FALSE) {
      // string-length(A) >= 0 → true()
      final Expr arg1 = args.length > 0 ? args[0] : cc.qc.focus.value;
      if(arg1 != null) {
        final SeqType st1 = arg1.seqType();
        if(st1.zero() || st1.one() && st1.type.isStringOrUntyped())
          return Bln.get(counts == COUNT_TRUE);
      }
    }
    if(counts == COUNT_EMPTY || counts == COUNT_EXISTS) {
      // string-length(A) > 0 → boolean(string(A))
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
  private Expr optEmptyString(final CmpOp op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType();
    if(st1.one() && st1.type.isStringOrUntyped() && expr2 == Str.EMPTY) {
      if(op == CmpOp.LT) return Bln.FALSE;
      if(op == CmpOp.GE) return Bln.TRUE;
      // do not rewrite GT, as it may be rewritten to a range expression later on
      if(op != CmpOp.GT) {
        // EQ and LE can be treated identically
        final Function func = op == CmpOp.NE ? BOOLEAN : NOT;
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
  private Expr optPos(final CmpOp op, final CompileContext cc) throws QueryException {
    if(POSITION.is(exprs[0]) && exprs[1].seqType().type.isNumberOrUntyped()) {
      final Expr expr = Pos.get(exprs[1], op, info, cc, null);
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
  private static long[] countRange(final CmpOp op, final double count) {
    // skip special cases
    if(!Double.isFinite(count)) return null;

    // > (v<0), != (v<0), >= (v<=0), != integer(v)
    final long cnt = (long) count;
    if((op.oneOf(CmpOp.GT, CmpOp.NE)) && count < 0 ||
        op == CmpOp.GE && count <= 0 ||
        op == CmpOp.NE && count != cnt) return COUNT_TRUE;
    // < (v<=0), <= (v<0), = (v<0), != integer(v)
    if(op == CmpOp.LT && count <= 0 ||
      (op.oneOf(CmpOp.LE, CmpOp.EQ)) && count < 0 ||
       op == CmpOp.EQ && count != cnt) return COUNT_FALSE;
    // < (v<=1), <= (v<1), = (v=0)
    if(op == CmpOp.LT && count <= 1 ||
       op == CmpOp.LE && count < 1 ||
       op == CmpOp.EQ && count == 0) return COUNT_EMPTY;
    // > (v<1), >= (v<=1), != (v=0)
    if(op == CmpOp.GT && count < 1 ||
       op == CmpOp.GE && count <= 1 ||
       op == CmpOp.NE && count == 0) return COUNT_EXISTS;
    // range queries
    if(op == CmpOp.GT) return new long[] { (long) Math.floor(count) + 1 };
    if(op == CmpOp.GE) return new long[] { (long) Math.ceil(count) };
    if(op == CmpOp.LT) return new long[] { 0, (long) Math.ceil(count) - 1 };
    if(op == CmpOp.LE) return new long[] { 0, (long) Math.floor(count) };
    if(op == CmpOp.EQ) return new long[] { cnt, cnt };
    return null;
  }
}
