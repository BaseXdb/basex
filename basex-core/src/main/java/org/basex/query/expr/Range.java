package org.basex.query.expr;

import static org.basex.query.QueryError.*;
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
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Range extends Arr {
  /** Dummy value for representing the last item in a sequence. */
  private static final long LAST = 1L << 52;

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
    final Predicate<Type> type = t -> t.instanceOf(AtomType.INTEGER) || t.isUntyped();
    if(!type.test(exprs[0].seqType().type) || !type.test(exprs[1].seqType().type)) return this;

    Expr[] minMax = exprs.clone();
    final double mn = pos(minMax[0]), mx = pos(minMax[1]);
    if(mx < mn) return Bln.FALSE;

    final boolean results = mn <= mx;
    switch(op) {
      case EQ:
        if(mn <= 1 && mx >= LAST) return Bln.TRUE;
        if(mn > LAST || mx < 1) return Bln.FALSE;
        if(mn < 1) minMax[0] = Int.ONE;
        if(mn == LAST && mx > mn) minMax[1] = cc.function(Function.LAST, info);
        break;
      case NE:
        if(mn <= 1 && mx >= LAST) return Bln.FALSE;
        if(results && (mn > LAST || mx < 1)) return Bln.TRUE;
        if(mn < 1) minMax[0] = Int.ONE;
        if(mn == LAST && mx > mn) minMax[1] = cc.function(Function.LAST, info);
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
      final long l = expr.arg(1) instanceof Int ? ((Int) expr.arg(1)).itr() : 0;
      if(l != 0) {
        switch(((Arith) expr).calc) {
          case PLUS : return LAST + l;
          case MINUS: return LAST - l;
          case MULT : return LAST * l;
          case DIV  : return LAST / l;
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
