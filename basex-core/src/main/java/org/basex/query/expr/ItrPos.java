package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple position check expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ItrPos extends Simple {
  /** Minimum position (1 or larger). */
  final long min;
  /** Maximum position (1 or larger, never smaller than {@link #min}). */
  final long max;

  /**
   * Constructor.
   * @param min minimum value (1 or larger)
   * @param max minimum value (1 or larger)
   * @param info input info
   */
  private ItrPos(final long min, final long max, final InputInfo info) {
    super(info, SeqType.BLN_O);
    this.min = min;
    this.max = max;
  }

  /**
   * Returns a position expression for the specified index, or an optimized boolean item.
   * @param index index position
   * @param info input info
   * @return expression
   */
  public static Expr get(final long index, final InputInfo info) {
    return get(index, index, info);
  }

  /**
   * Returns a position expression for the specified range, or an optimized boolean item.
   * @param min minimum value
   * @param max minimum value
   * @param info input info
   * @return expression
   */
  private static Expr get(final long min, final long max, final InputInfo info) {
    // suppose that positions always fit in long values..
    return min > max || max < 1 ? Bln.FALSE : min <= 1 && max == Long.MAX_VALUE ? Bln.TRUE :
      new ItrPos(Math.max(1, min), Math.max(1, max), info);
  }

  /**
   * Returns a position expression for the specified range comparison.
   * @param expr range comparison
   * @return expression
   */
  public static Expr get(final CmpR expr) {
    final double min = expr.min, max = expr.max;
    final long mn = (long) (expr.mni ? (long) Math.ceil(min) : Math.floor(min + 1));
    final long mx = (long) (expr.mxi ? (long) Math.floor(max) : Math.ceil(max - 1));
    return get(mn, mx, expr.info);
  }

  /**
   * Tries to rewrite {@code fn:position() CMP number(s)} to this expression.
   * Returns an instance of this class, the original expression, or an optimized expression.
   * @param cmp comparison expression
   * @param op comparator
   * @param info input info
   * @return resulting or original expression
   */
  public static Expr get(final Cmp cmp, final OpV op, final InputInfo info) {
    final Expr cmp1 = cmp.exprs[0], cmp2 = cmp.exprs[1];
    if(!cmp1.isFunction(Function.POSITION)) return cmp;

    if(cmp2 instanceof RangeSeq && op == OpV.EQ) {
      final long[] range = ((RangeSeq) cmp2).range(false);
      return get(range[0], range[1], info);
    } else if(cmp2 instanceof ANum) {
      final ANum item2 = (ANum) cmp2;
      final long pos = item2.itr();
      final boolean exact = pos == item2.dbl();
      switch(op) {
        case EQ: return exact ? get(pos, info) : Bln.FALSE;
        case GE: return get(exact ? pos : pos + 1, Long.MAX_VALUE, info);
        case GT: return get(pos + 1, Long.MAX_VALUE, info);
        case LE: return get(1, pos, info);
        case LT: return get(1, exact ? pos - 1 : pos, info);
        default:
      }
    }
    return cmp;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);
    return Bln.get(matches(qc.focus.pos));
  }

  @Override
  public ItrPos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new ItrPos(min, max, info);
  }

  /**
   * Returns false if no more results can be expected.
   * @param pos current position
   * @return result of check
   */
  public boolean skip(final long pos) {
    return pos >= max;
  }

  /**
   * Checks if the current position lies within the given position.
   * @param pos current position
   * @return result of check
   */
  public boolean matches(final long pos) {
    return pos >= min && pos <= max;
  }

  /**
   * Creates an intersection of the existing and the specified position expressions.
   * @param pos second position expression
   * @param ii input info
   * @return resulting expression
   */
  Expr intersect(final ItrPos pos, final InputInfo ii) {
    return get(Math.max(min, pos.min), Math.min(max, pos.max), ii);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.POS.in(flags);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof ItrPos)) return false;
    final ItrPos p = (ItrPos) obj;
    return min == p.min && max == p.max;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(MIN, min, MAX, max == Long.MAX_VALUE ? INF : max));
  }

  @Override
  public String description() {
    return "positional access";
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("position() ");
    if(min == max) {
      sb.append("= ").append(min);
    } else {
      if(max == Long.MAX_VALUE) sb.append('>');
      sb.append("= ").append(min);
      if(max != Long.MAX_VALUE) sb.append(" to ").append(max);
    }
    return sb.toString();
  }
}
