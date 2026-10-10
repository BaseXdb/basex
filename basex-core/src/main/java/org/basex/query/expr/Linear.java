package org.basex.query.expr;

import static org.basex.query.value.type.BasicType.*;

import java.math.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Linear combination of integer and decimal terms.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Linear {
  /** Maximum number of visited nodes (limits compile time of long chains). */
  private static final int MAX = 64;

  /** Terms. */
  private final ExprList terms = new ExprList();
  /** Factors of the terms. */
  private final ArrayList<BigDecimal> factors = new ArrayList<>();
  /** Constant summand. */
  private BigDecimal constant = BigDecimal.ZERO;
  /** Number of collected terms and constants. */
  private int operands;
  /** Number of visited nodes. */
  private int nodes;

  /** Private constructor. */
  private Linear() { }

  /**
   * Rewrites an addition or subtraction to its normalized linear combination.
   * @param arith arithmetic expression
   * @param cc compilation context
   * @return normalized expression, or {@code null} if it is not simpler
   * @throws QueryException query exception
   */
  static Expr normalize(final Arith arith, final CompileContext cc) throws QueryException {
    // skip simple operations: $x + 1
    if(!arith.calc.oneOf(Calc.ADD, Calc.SUBTRACT) || !arith.seqType().one() ||
        !composite(arith) || !composite(arith.arg(0)) && !composite(arith.arg(1))) return null;
    // only rewrite if the number of operands decreases (prevents rewrite loops)
    final Linear linear = collect(arith, null);
    if(linear == null || linear.terms.size() + (linear.constant.signum() != 0 ? 1 : 0) >=
        linear.operands) return null;

    final ANum num = num(linear.terms.isEmpty() ? linear.constant : linear.constant.abs());
    if(num == null) return null;
    final InputInfo info = arith.info();
    Expr expr = null;
    for(int i = 0; i < linear.terms.size(); i++) {
      final BigDecimal factor = linear.factors.get(i);
      final Expr term = term(linear.terms.get(i), factor.abs(), info, cc);
      if(term == null) return null;
      // -$x + $y → $y - $x (see Calc.ADD)
      expr = expr != null ? new Arith(info, expr, term, calc(factor)).optimize(cc) :
        factor.signum() > 0 ? term : new Unary(info, term, true).optimize(cc);
    }
    if(expr == null) {
      expr = num;
    } else if(linear.constant.signum() != 0) {
      expr = new Arith(info, expr, num, calc(linear.constant)).optimize(cc);
    }
    // preserve result type: $x + 1.0 - 1.0 → xs:decimal($x)
    final Type type = arith.seqType().type;
    return expr.seqType().type == type ? expr : new Cast(info, expr, type.seqType()).optimize(cc);
  }

  /**
   * Rewrites a general comparison of two linear combinations.
   * @param cmp comparison
   * @param cc compilation context
   * @return rewritten or original expression
   * @throws QueryException query exception
   */
  static Expr compare(final CmpG cmp, final CompileContext cc) throws QueryException {
    // skip normalized comparisons: $x = 3
    final Expr expr1 = cmp.arg(0), expr2 = cmp.arg(1);
    if(!expr1.seqType().instanceOf(Types.DECIMAL_ZO) ||
        !expr2.seqType().instanceOf(Types.DECIMAL_ZO) ||
        !composite(expr1) && !composite(expr2) && !expr1.equals(expr2) ||
        cmp.has(Flag.NDT)) return cmp;

    // $o * 2 = 6 → $o = 3 (empty operands: false either way)
    final Linear linear = collect(expr1, expr2);
    if(linear == null || linear.terms.size() > 2) return cmp;
    // $x + 1 = $x + 2 → false()
    final CmpOp op = cmp.op;
    if(linear.terms.isEmpty()) return Bln.get(op.eval(linear.constant.signum()));

    final BigDecimal factor = linear.factors.get(0);
    final CmpOp nop = factor.signum() < 0 ? op.swap() : op;
    if(linear.terms.size() == 2) {
      // $x + 1 = $y + 1 → $x = $y, -$x < -$y → $x > $y
      return linear.constant.signum() != 0 ||
        factor.add(linear.factors.get(1)).signum() != 0 ? cmp :
        new CmpG(cmp.info(), linear.terms.get(0), linear.terms.get(1), nop).optimize(cc);
    }

    // 2 * $x + 3 = 5 → $x = 1, -$x < 3 → $x > -3
    BigDecimal value;
    try {
      value = linear.constant.negate().divide(factor);
    } catch(final ArithmeticException ex) {
      Util.debug(ex);
      return cmp;
    }
    final Expr expr = linear.terms.get(0);
    final SeqType st = expr.seqType();
    CmpOp cop = nop;
    if(st.type.instanceOf(INTEGER) && value.stripTrailingZeros().scale() > 0) {
      // $int * 2 = 3 → false()
      if(nop.oneOf(CmpOp.EQ, CmpOp.NE) && st.one()) return Bln.get(nop == CmpOp.NE);
      // $int * 2 < 3 → $int <= 1, $int * 2 > 3 → $int >= 2
      if(nop.oneOf(CmpOp.LT, CmpOp.LE)) {
        value = value.setScale(0, RoundingMode.FLOOR);
        cop = CmpOp.LE;
      } else if(nop.oneOf(CmpOp.GT, CmpOp.GE)) {
        value = value.setScale(0, RoundingMode.CEILING);
        cop = CmpOp.GE;
      }
    }
    final ANum num = num(value);
    return num == null ? cmp : new CmpG(cmp.info(), expr, num, cop).optimize(cc);
  }

  /**
   * Returns the constant difference between a sum and a subtracted expression.
   * @param expr expression to be subtracted from the sum
   * @param exprs summed expressions
   * @return difference, or {@code null} if it is not constant
   * @throws QueryException query exception
   */
  public static BigDecimal difference(final Expr expr, final Expr... exprs)
      throws QueryException {
    // $x + 10 - ($x - 1) → 11
    final Linear linear = new Linear();
    for(final Expr ex : exprs) linear.add(ex, BigDecimal.ONE);
    linear.add(expr, BigDecimal.ONE.negate());
    return linear.prune() && linear.terms.isEmpty() ? linear.constant : null;
  }

  /**
   * Collects the linear combination of one or two expressions.
   * @param expr1 first expression
   * @param expr2 expression to be subtracted (can be {@code null})
   * @return linear combination, or {@code null} if it cannot be collected
   * @throws QueryException query exception
   */
  private static Linear collect(final Expr expr1, final Expr expr2) throws QueryException {
    final Linear linear = new Linear();
    linear.add(expr1, BigDecimal.ONE);
    if(expr2 != null) linear.add(expr2, BigDecimal.ONE.negate());
    return linear.prune() ? linear : null;
  }

  /**
   * Removes cancelled terms.
   * @return {@code false} if the combination cannot be collected
   */
  private boolean prune() {
    if(nodes > MAX) return false;
    // empty operands must not be dropped: $o - $o + 1
    for(int i = terms.size() - 1; i >= 0; i--) {
      if(factors.get(i).signum() == 0) {
        if(!terms.get(i).seqType().one()) return false;
        terms.remove(i);
        factors.remove(i);
      }
    }
    return true;
  }

  /**
   * Checks if an expression will be decomposed.
   * @param expr expression
   * @return result of check
   */
  private static boolean composite(final Expr expr) {
    if(expr instanceof final Unary unary) {
      return unary.minus && unary.seqType().type.instanceOf(DECIMAL) &&
        unary.expr.seqType().zeroOrOne();
    }
    return expr instanceof final Arith arith && (arith.calc.oneOf(Calc.ADD, Calc.SUBTRACT) ||
      arith.calc == Calc.MULTIPLY && arith.arg(1) instanceof ANum) &&
      arith.seqType().type.instanceOf(DECIMAL) && arith.arg(0).seqType().zeroOrOne() &&
      arith.arg(1).seqType().zeroOrOne();
  }

  /**
   * Adds the terms of an expression.
   * @param expr expression
   * @param factor factor
   * @throws QueryException query exception
   */
  private void add(final Expr expr, final BigDecimal factor) throws QueryException {
    if(++nodes > MAX) return;
    if(expr instanceof final ANum num && num.type.instanceOf(DECIMAL)) {
      constant = constant.add(factor.multiply(num.dec(null)));
      operands++;
    } else if(!composite(expr)) {
      final int t = terms.size();
      int i = 0;
      while(i < t && !terms.get(i).equals(expr)) i++;
      if(i == t) {
        terms.add(expr);
        factors.add(factor);
      } else {
        factors.set(i, factors.get(i).add(factor));
      }
      operands++;
    } else if(expr instanceof final Unary unary) {
      add(unary.expr, factor.negate());
    } else {
      final Arith arith = (Arith) expr;
      final Expr expr1 = arith.arg(0), expr2 = arith.arg(1);
      switch(arith.calc) {
        case ADD -> {
          add(expr1, factor);
          add(expr2, factor);
        }
        case SUBTRACT -> {
          add(expr1, factor);
          add(expr2, factor.negate());
        }
        default -> add(expr1, factor.multiply(((ANum) expr2).dec(null)));
      }
    }
  }

  /**
   * Returns the operator for adding a signed number.
   * @param number number
   * @return operator
   */
  private static Calc calc(final BigDecimal number) {
    return number.signum() > 0 ? Calc.ADD : Calc.SUBTRACT;
  }

  /**
   * Creates a term.
   * @param expr expression
   * @param factor positive factor
   * @param info input info (can be {@code null})
   * @param cc compilation context
   * @return term, or {@code null} if the factor exceeds the integer range
   * @throws QueryException query exception
   */
  private static Expr term(final Expr expr, final BigDecimal factor, final InputInfo info,
      final CompileContext cc) throws QueryException {
    if(factor.compareTo(BigDecimal.ONE) == 0) return expr;
    final ANum num = num(factor);
    return num == null ? null : new Arith(info, expr, num, Calc.MULTIPLY).optimize(cc);
  }

  /**
   * Creates a number.
   * @param value value
   * @return number, or {@code null} if an integral value exceeds the integer range
   */
  private static ANum num(final BigDecimal value) {
    if(value.scale() > 0 && value.stripTrailingZeros().scale() > 0) return Dec.get(value);
    final BigInteger bi = value.toBigIntegerExact();
    return bi.bitLength() < 64 ? Itr.get(bi.longValue()) : null;
  }
}
