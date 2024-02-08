package org.basex.query.expr;

import static java.lang.Long.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Integer range expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CmpIR extends Single {
  /** Minimum. */
  public final long min;
  /** Maximum. */
  public final long max;

  /** Evaluation flag: atomic evaluation. */
  private boolean single;

  /**
   * Constructor.
   * @param expr (compiled) expression
   * @param min minimum value
   * @param max maximum value
   * @param info input info (can be {@code null})
   */
  private CmpIR(final Expr expr, final long min, final long max, final InputInfo info) {
    super(info, expr, SeqType.BOOLEAN_O);
    this.min = min;
    this.max = max;
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @param expr expression to be compared
   * @param min minimum position
   * @param max minimum position (inclusive)
   * @return expression
   * @throws QueryException query exception
   */
  static Expr get(final CompileContext cc, final InputInfo info, final Expr expr,
      final long min, final long max) throws QueryException {
    return min > max ? Bln.FALSE : min == MIN_VALUE && max == MAX_VALUE ?
      cc.function(Function.EXISTS, info, expr) :
      new CmpIR(expr, min, max, info).optimize(cc);
  }

  /**
   * Tries to convert the specified expression into a range expression.
   * @param cc compilation context
   * @param cmp expression to be converted
   * @param eq also rewrite equality comparisons of single integers
   * @return new or original expression
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final CmpG cmp, final boolean eq)
      throws QueryException {

    // only rewrite deterministic expressions
    final Expr expr1 = cmp.exprs[0], expr2 = cmp.exprs[1];
    if(cmp.has(Flag.NDT)) return cmp;

    // only rewrite integer or equality comparisons
    // allowed: $integer > 2, $value = 10 to 20; rejected: $double  > 2
    final Type type1 = expr1.seqType().type;
    final boolean cmpEq = cmp.op == OpG.EQ;
    if(!(type1.instanceOf(AtomType.INTEGER) || cmpEq && type1.isUntyped())) return cmp;

    long mn, mx;
    if(expr2 instanceof RangeSeq) {
      final RangeSeq rs = (RangeSeq) expr2;
      mn = rs.min();
      mx = rs.max();
    } else if(expr2 instanceof Int && (eq || !cmpEq)) {
      mn = ((Int) expr2).itr();
      mx = mn;
    } else {
      return cmp;
    }

    switch(cmp.op) {
      case GE: mx = MAX_VALUE; break;
      case GT: mn++; mx = MAX_VALUE; break;
      case LE: mn = MIN_VALUE; break;
      case LT: mn = MIN_VALUE; mx--; break;
      case EQ: break;
      default: return cmp;
    }
    return get(cc, cmp.info, expr1, mn, mx);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.NUMBER, cc);

    final SeqType st = expr.seqType();
    single = st.zeroOrOne() && !st.mayBeArray();

    if(expr instanceof Value) return cc.preEval(this);

    if(Function.POSITION.is(expr)) {
      // E[let $p := position() return $p = 1 to 2]
      final long mn = Math.max(min, 1), size = max - mn + 1;
      final Expr pos = RangeSeq.get(mn, size, true).optimizePos(OpV.EQ, cc);
      return cc.replaceWith(this, pos instanceof Bln ? pos : IntPos.get(pos, OpV.EQ, info));
    }
    return this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // atomic evaluation of arguments (faster)
    if(single) {
      final Item item = expr.item(qc, info);
      return Bln.get(!item.isEmpty() && inRange(item));
    }

    // pre-evaluate ranges
    if(expr instanceof Range || expr instanceof RangeSeq) {
      final Value value = expr.value(qc);
      final long size = value.size();
      if(size == 0) return Bln.FALSE;
      if(size == 1) return Bln.get(inRange((Item) value));
      final RangeSeq rs = (RangeSeq) value;
      return Bln.get(rs.max() >= min && rs.min() <= max);
    }

    // iterative evaluation
    final Iter iter = expr.atomIter(qc, info);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(inRange(item)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  /**
   * Checks if the specified value is within the allowed range.
   * @param item value to check
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean inRange(final Item item) throws QueryException {
    final double value = item.dbl(info);
    return value >= min && value <= max && value == (long) value;
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {

    Long newMin = null, newMax = null;
    if(ex instanceof CmpIR) {
      final CmpIR cmp = (CmpIR) ex;
      newMin = cmp.min;
      newMax = cmp.max;
    } else if(ex instanceof CmpG && ((CmpG) ex).op == OpG.EQ && ex.arg(1) instanceof Int) {
      newMin = ((Int) ex.arg(1)).itr();
      newMax = newMin;
    }
    if(newMin == null || !expr.equals(ex.arg(0)) || or && (max < newMin || min > newMax))
      return null;

    // determine common minimum and maximum value
    newMin = or ? Math.min(min, newMin) : Math.max(min, newMin);
    newMax = or ? Math.max(max, newMax) : Math.min(max, newMax);
    return get(cc, info, expr, newMin, newMax);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final CmpIR cmp = new CmpIR(expr.copy(cc, vm), min, max, info);
    cmp.single = single;
    return copyType(cmp);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof CmpIR)) return false;
    final CmpIR c = (CmpIR) obj;
    return min == c.min && max == c.max && super.equals(obj);
  }

  @Override
  public String description() {
    return "integer range comparison";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, MIN, min, MAX, max, SINGLE, single), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(expr);
    if(min == max) {
      qs.token("=").token(min);
    } else if(min != MIN_VALUE && max != MAX_VALUE) {
      qs.token("=").token(min).token(TO).token(max);
    } else if(min != MIN_VALUE) {
      qs.token(">=").token(min);
    } else {
      qs.token("<=").token(max);
    }
  }
}
