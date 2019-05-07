package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract comparison.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class Cmp extends Arr {
  /** Collation (can be {@code null}). */
  final Collation coll;
  /** Static context. */
  final StaticContext sc;

  /**
   * Constructor.
   * @param info input info
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

    final boolean swap =
      // move static value to the right: $words = 'words'
      expr1 instanceof Value && !(expr2 instanceof Value) ||
      // move larger sequences to the right: $small = $large
      expr1.size() > 1 && expr1.size() > expr2.size() ||
      // swap for index rewritings: word/text() = $word
      !(expr1 instanceof Path && ((Path) expr1).root == null) &&
        expr2 instanceof Path && ((Path) expr2).root == null ||
      // swap for hashed comparisons: . = $words
      expr1 instanceof VarRef && expr1.seqType().occ.max > 1 &&
        !(expr2 instanceof VarRef && expr2.seqType().occ.max > 1) && !(expr2 instanceof Value) ||
      // swap for positional checks: position() > 1
      Function.POSITION.is(expr2);

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
  public abstract Expr invert(CompileContext cc) throws QueryException;

  /**
   * Returns the value operator of the expression.
   * @return operator, or {@code null} if not defined
   */
  public abstract OpV opV();

  /**
   * Performs various optimizations.
   * @param cc compilation context
   * @return resulting expression
   * @throws QueryException query exception
   */
  final Expr opt(final CompileContext cc) throws QueryException {
    final OpV op = opV();
    Expr expr = optEqual(op, this instanceof CmpV, cc);
    if(expr == this) expr = optFalse(op, cc);
    if(expr == this) expr = optCount(op, cc);
    if(expr == this) expr = optStringLength(op, cc);
    if(expr == this) expr = optPos(op, cc);
    return expr;
  }

  /**
   * Tries to simplify an expression with equal operands. Rewriting is possible if:
   * <ul>
   *   <li> the equality operator is specified</li>
   *   <li> operands are equal</li>
   *   <li> operands are deterministic, non-updating</li>
   *   <li> operands do not depend on context, or if context value exists</li>
   * </ul>
   * @param op operator
   * @param cc compilation context
   * @param single single arguments
   * @return resulting expression
   */
  final Expr optEqual(final OpV op, final boolean single, final CompileContext cc) {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if((op == OpV.EQ || single && op == OpV.NE) && expr1.equals(expr2) && !expr1.has(Flag.NDT) &&
        (!expr1.has(Flag.CTX) || cc.qc.focus.value != null)) {
      /* consider query flags
       * illegal: random:integer() eq random:integer() */
      final SeqType st1 = expr1.seqType();
      final Type type1 = st1.type;
      final boolean one = single ? st1.one() : st1.oneOrMore();
      /* limited to strings, integers and booleans.
       * illegal rewriting: xs:double('NaN') eq xs:double('NaN') */
      if(one && (type1.isStringOrUntyped() || type1.instanceOf(AtomType.ITR) ||
          type1 == AtomType.BLN)) return Bln.get(op == OpV.EQ);
    }
    return this;
  }

  /**
   * Tries to rewrite {@code A = false()} to {@code not(A)}.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  final Expr optFalse(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    return expr1.seqType().eq(SeqType.BLN_O) && (op == OpV.EQ && expr2 == Bln.FALSE ||
        op == OpV.NE && expr2 == Bln.TRUE) ?  cc.function(Function.NOT, info, expr1) : this;
  }

  /**
   * Tries to rewrite {@code fn:count}.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  final Expr optCount(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(!(Function.COUNT.is(expr1) && expr2 instanceof ANum)) return this;

    // TRUE: c > (v<0), c != (v<0), c >= (v<=0), c != not-int(v)
    final int type = check(op, (ANum) expr2);
    if(type >= 2) {
      final Function func = type == 2 ? Function.EXISTS : Function.EMPTY;
      return cc.function(func, info, ((Arr) expr1).exprs);
    }
    if(type >= 0) {
      return Bln.get(type == 0);
    }
    return this;
  }

  /**
   * Tries to rewrite {@code fn:string-length}.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  final Expr optStringLength(final OpV op, final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(!(Function.STRING_LENGTH.is(expr1) && expr2 instanceof ANum)) return this;

    // TRUE: c > (v<0), c != (v<0), c >= (v<=0), c != not-int(v)
    final Expr[] args = ((Arr) expr1).exprs;
    final int type = check(op, (ANum) expr2);
    if(type >= 2) {
      final Function func = type == 2 ? Function.BOOLEAN : Function.NOT;
      return cc.function(func, info, cc.function(Function.STRING, info, args));
    }
    if(type >= 0) {
      final Expr arg1 = args.length > 0 ? args[0] : cc.qc.focus.value;
      if(arg1 != null) {
        final SeqType st1 = arg1.seqType();
        if(st1.zero() || st1.one() && st1.type.isStringOrUntyped()) return Bln.get(type == 0);
      }
    }
    return this;
  }

  /**
   * Positional optimizations.
   * @param op operator
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr optPos(final OpV op, final CompileContext cc) throws QueryException {
    if(!positional()) return this;

    Expr expr = ItrPos.get(exprs[1], op, info);
    if(expr == null) expr = Pos.get(exprs[1], op, info, cc);
    return expr != null ? expr : this;
  }

  /**
   * Indicates if this is a positional comparison.
   * @return result of check
   */
  public boolean positional() {
    return Function.POSITION.is(exprs[0]);
  }

  /**
   * Analyzes the comparison and returns its optimization type. Possible types are:
   * <ul>
   *   <li>0: always true</li>
   *   <li>1: always false</li>
   *   <li>2: positive, non-zero check</li>
   *   <li>3: zero check</li>
   *   <li>-1: none of them</li>
   * </ul>
   * @param op operator
   * @param num input number
   * @return comparison type
   */
  private static int check(final OpV op, final ANum num) {
    final double v = num.dbl();
    // > (v<0), != (v<0), >= (v<=0), != integer(v)
    if((op == OpV.GT || op == OpV.NE) && v < 0 || op == OpV.GE && v <= 0 ||
       op == OpV.NE && v != (long) v) return 0;
    // < (v<=0), <= (v<0), = (v<0), != integer(v)
    if(op == OpV.LT && v <= 0 || (op == OpV.LE || op == OpV.EQ) && v < 0 ||
       op == OpV.EQ && v != (long) v) return 1;
    // > (v<1), >= (v<=1), != (v=0)
    if(op == OpV.GT && v < 1 || op == OpV.GE && v <= 1 || op == OpV.NE && v == 0) return 2;
    // < (v<=1), <= (v<1), = (v=0)
    if(op == OpV.LT && v <= 1 || op == OpV.LE && v < 1 || op == OpV.EQ && v == 0) return 3;
    return -1;
  }
}
