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
 * @author BaseX Team 2005-18, BSD License
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
    return get((long) Math.ceil(min), (long) Math.floor(max), expr.info);
  }

  /**
   * Tries to rewrite {@code fn:position() CMP number(s)} to this expression.
   * Returns an instance of this class, the original expression, or an optimized expression.
   * @param pos position to be compared
   * @param op comparator
   * @param info input info
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  public static Expr get(final Expr pos, final OpV op, final InputInfo info) throws QueryException {
    if(pos instanceof RangeSeq && op == OpV.EQ) {
      final long[] range = ((RangeSeq) pos).range(false);
      return get(range[0], range[1], info);
    } else if(pos instanceof Item) {
      final Item item2 = (Item) pos;
      final long p = item2.itr(info);
      final boolean exact = p == item2.dbl(info);
      switch(op) {
        case EQ: return exact ? get(p, info) : Bln.FALSE;
        case GE: return get(exact ? p : p + 1, Long.MAX_VALUE, info);
        case GT: return get(p + 1, Long.MAX_VALUE, info);
        case LE: return get(1, p, info);
        case LT: return get(1, exact ? p - 1 : p, info);
        case NE: return exact ? p < 2 ? get(p + 1, Long.MAX_VALUE, info) : null : Bln.TRUE;
        default:
      }
    }
    return null;
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
    final StringBuilder sb = new StringBuilder().append(Function.POSITION).append(' ');
    if(max == Long.MAX_VALUE) {
      sb.append(">= ").append(min);
    } else {
      sb.append("= ").append(min);
      if(min != max) sb.append(' ').append(TO).append(' ').append(max);
    }
    return sb.toString();
  }
}
