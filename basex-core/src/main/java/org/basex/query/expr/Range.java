package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.Function;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Range expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Range extends Arr {
  /** Dummy value for representing the last item in a sequence. */
  private static final long LAST = 1L << 52;
  /** Expressions yield integers. */
  boolean ints;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param range (min/max) expressions
   */
  public Range(final InputInfo info, final Expr... range) {
    super(info, SeqType.INTEGER_ZM, range);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs = simplifyAll(Simplify.NUMBER, cc);

    Expr expr = emptyExpr();
    if(expr == this) {
      if(allAreValues(false)) return cc.preEval(this);

      final Expr min = exprs[0], max = exprs[1];
      final SeqType st1 = min.seqType(), st2 = max.seqType();
      ints = st1.instanceOf(SeqType.INTEGER_O) && st2.instanceOf(SeqType.INTEGER_O);
      if(min.equals(max)) {
        exprType.assign(Occ.EXACTLY_ONE);
        if(ints && !min.has(Flag.NDT)) expr = min;
      }
    }
    return cc.replaceWith(this, expr);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item min = exprs[0].atomItem(qc, info);
    if(min.isEmpty()) return Empty.VALUE;
    final Item max = exprs[1].atomItem(qc, info);
    if(max.isEmpty()) return Empty.VALUE;
    final long mn = toLong(min), mx = toLong(max);
    // min smaller than max: empty sequence
    if(mn > mx) return Empty.VALUE;
    // max smaller than min: create range
    final long size = mx - mn + 1;
    // too large range: assign maximum
    return RangeSeq.get(mn, size < 0 ? Long.MAX_VALUE : size, true);
  }

  @Override
  public Range copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Range r = copyType(new Range(info, copyAll(cc, vm, exprs)));
    r.ints = ints;
    return r;
  }

  @Override
  public Expr optimizePos(final OpV op, final CompileContext cc) throws QueryException {
    final Predicate<Expr> type = e -> {
      final SeqType st = e.seqType();
      return st.one() && (st.type.instanceOf(AtomType.INTEGER) || st.type.isUntyped());
    };
    if(!type.test(exprs[0]) || !type.test(exprs[1])) return this;

    final Expr[] minMax = exprs.clone();
    final double mn = pos(minMax[0]), mx = pos(minMax[1]);
    if(mx < mn) return Bln.FALSE;

    final boolean results = mn <= mx;
    switch(op) {
      case EQ:
        if(mn <= 1 && mx >= LAST) return Bln.TRUE;
        if(mn > LAST || mx < 1) return Bln.FALSE;
        if(mn < 1) minMax[0] = Int.ONE;
        if(mn == LAST && mx > mn) minMax[1] = cc.function(Function.LAST, info);
        if(mn < LAST && mx >= LAST) minMax[1] = Int.MAX;
        break;
      case NE:
        if(mn <= 1 && mx >= LAST) return Bln.FALSE;
        if(results && (mn > LAST || mx < 1)) return Bln.TRUE;
        if(mn < 1) minMax[0] = Int.ONE;
        if(mn == LAST && mx > mn) minMax[1] = cc.function(Function.LAST, info);
        if(mn < LAST && mx >= LAST) minMax[1] = Int.MAX;
        break;
      case LE:
        if(mx < 1) return Bln.FALSE;
        if(results && mx >= LAST) return Bln.TRUE;
        if(mn < 1) minMax[0] = Int.ONE;
        break;
      case LT:
        if(mx <= 1) return Bln.FALSE;
        if(results && mx > LAST) return Bln.TRUE;
        break;
      case GE:
        if(mn > LAST) return Bln.FALSE;
        if(results && mx <= 1) return Bln.TRUE;
        break;
      case GT:
        if(mn >= LAST) return Bln.FALSE;
        if(results && mx < 1) return Bln.TRUE;
        break;
    }
    if(Arrays.equals(exprs, minMax)) return this;
    final Expr ex = new Range(info, minMax).optimize(cc);
    return ex == Empty.VALUE ? Bln.FALSE : ex;
  }

  /**
   * Returns a static positional value for the specified expression.
   * @param expr expression
   * @return positional value or {@code Double#NaN}
   */
  private static double pos(final Expr expr) {
    if(expr instanceof Int) return ((Int) expr).itr();
    if(Function.LAST.is(expr)) return LAST;
    if(expr instanceof Arith && Function.LAST.is(expr.arg(0))) {
      final double l = expr.arg(1) instanceof Int ? ((Int) expr.arg(1)).itr() : 0;
      if(l != 0) {
        switch(((Arith) expr).calc) {
          case ADD : return LAST + l;
          case SUBTRACT: return LAST - l;
          case MULTIPLY : return LAST * l;
          case DIVIDE  : return LAST / l;
          default: break;
        }
      }
    }
    return Double.NaN;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Range && super.equals(obj);
  }

  @Override
  public String description() {
    return "range expression";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, ' ' + TO + ' ', true);
  }
}
