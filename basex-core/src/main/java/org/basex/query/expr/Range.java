package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
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
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Range extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param range (min/max) expressions
   */
  public Range(final InputInfo info, final Expr... range) {
    super(info, SeqType.INTEGER_ZM, range);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    simplifyAll(Simplify.NUMBER, cc);

    Expr expr = emptyExpr();
    if(expr == this) {
      if(allAreValues(false)) return cc.preEval(this);

      final Expr min = exprs[0], max = exprs[1];
      if(min.equals(max)) {
        if(min.seqType().instanceOf(SeqType.INTEGER_O) && !min.has(Flag.NDT)) {
          expr = min;
        } else {
          exprType.assign(Occ.EXACTLY_ONE);
        }
      }
    }
    return cc.replaceWith(this, expr);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item min = exprs[0].atomItem(qc, info);
    if(min == Empty.VALUE) return Empty.VALUE;
    final Item max = exprs[1].atomItem(qc, info);
    if(max == Empty.VALUE) return Empty.VALUE;
    final long mn = toLong(min), mx = toLong(max);
    // min smaller than max: empty sequence
    if(mn > mx) return Empty.VALUE;
    // max smaller than min: create range
    final long size = mx - mn + 1;
    if(size > 0) return RangeSeq.get(mn, size, true);
    // overflow of long value
    throw RANGE_X.get(info, mx);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Range(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public Expr optimizePos(final OpV op, final CompileContext cc) throws QueryException {
    if(!exprs[0].seqType().instanceOf(SeqType.INTEGER_O) ||
      !exprs[1].seqType().instanceOf(SeqType.INTEGER_O)) return this;

    Expr[] minMax = exprs.clone();
    final double mn = pos(minMax[0]), mx = pos(minMax[1]);
    final boolean results = mn <= mx;
    switch(op) {
      case EQ:
        if(mn <= 1 && mx >= Integer.MAX_VALUE) return Bln.TRUE;
        if(mn > Integer.MAX_VALUE || mx < 1) return Bln.FALSE;
        if(mn < 1) minMax[0] = Int.ONE;
        if(mn == Integer.MAX_VALUE && mx > mn) minMax[1] = cc.function(Function.LAST, info);
        break;
      case NE:
        if(mn <= 1 && mx >= Integer.MAX_VALUE) return Bln.FALSE;
        if(results && (mn > Integer.MAX_VALUE || mx < 1)) return Bln.TRUE;
        if(mn < 1) minMax[0] = Int.ONE;
        if(mn == Integer.MAX_VALUE && mx > mn) minMax[1] = cc.function(Function.LAST, info);
        break;
      case LE:
        if(mx < 1) return Bln.FALSE;
        if(results && mx >= Integer.MAX_VALUE) return Bln.TRUE;
        if(mn < 1) minMax[0] = Int.ONE;
        break;
      case LT:
        if(mx <= 1) return Bln.FALSE;
        if(results && mx > Integer.MAX_VALUE) return Bln.TRUE;
        break;
      case GE:
        if(mn > Integer.MAX_VALUE) return Bln.FALSE;
        if(results && mx <= 1) return Bln.TRUE;
        break;
      case GT:
        if(mn >= Integer.MAX_VALUE) return Bln.FALSE;
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
   * @return positional value or {@code null}
   */
  private static double pos(final Expr expr) {
    if(expr instanceof Int) return ((Int) expr).itr();
    if(Function.LAST.is(expr)) return Integer.MAX_VALUE;
    if(expr instanceof Arith && Function.LAST.is(expr.arg(0))) {
      final long l = expr.arg(1) instanceof Int ? ((Int) expr.arg(1)).itr() : 0;
      if(l != 0) {
        switch(((Arith) expr).calc) {
          case PLUS : return Integer.MAX_VALUE + l;
          case MINUS: return Integer.MAX_VALUE - l;
          case MULT : return Integer.MAX_VALUE * l;
          case DIV  : return Integer.MAX_VALUE / l;
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
