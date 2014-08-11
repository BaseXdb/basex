package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Pos expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Pos extends Simple {
  /** Minimum position. */
  final long min;
  /** Maximum position. */
  final long max;

  /**
   * Constructor.
   * @param min minimum value
   * @param max minimum value
   * @param info input info
   */
  private Pos(final long min, final long max, final InputInfo info) {
    super(info);
    this.min = min;
    this.max = max;
    seqType = SeqType.BLN;
  }

  /**
   * Returns a position expression, or an optimized boolean item.
   * @param mn minimum value
   * @param mx minimum value
   * @param ii input info
   * @return expression
   */
  public static Expr get(final long mn, final long mx, final InputInfo ii) {
    // suppose that positions always fit in long values..
    return mn > mx || mx < 1 ? Bln.FALSE : mn <= 1 && mx == Long.MAX_VALUE ?
      Bln.TRUE : new Pos(mn, mx, ii);
  }

  /**
   * Returns an instance of this class, if possible, and the input expression
   * otherwise.
   * @param cmp comparator
   * @param a argument
   * @param o original expression
   * @param ii input info
   * @return resulting expression, or {@code null}
   */
  public static Expr get(final OpV cmp, final Expr a, final Expr o, final InputInfo ii) {
    if(a instanceof ANum) {
      final ANum it = (ANum) a;
      final long p = it.itr();
      final boolean ex = p == it.dbl();
      switch(cmp) {
        case EQ: return ex ? get(p, p, ii) : Bln.FALSE;
        case GE: return get(ex ? p : p + 1, Long.MAX_VALUE, ii);
        case GT: return get(p + 1, Long.MAX_VALUE, ii);
        case LE: return get(1, p, ii);
        case LT: return get(1, ex ? p - 1 : p, ii);
        default:
      }
    }
    return o;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);
    return Bln.get(qc.pos >= min && qc.pos <= max);
  }

  @Override
  public Pos copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Pos(min, max, info);
  }

  /**
   * Returns false if no more results can be expected.
   * @param qc query context
   * @return result of check
   */
  public boolean skip(final QueryContext qc) {
    return qc.pos >= max;
  }

  /**
   * Creates an intersection of the existing and the specified position
   * expressions.
   * @param pos second position expression
   * @param ii input info
   * @return resulting expression
   */
  Expr intersect(final Pos pos, final InputInfo ii) {
    return get(Math.max(min, pos.min), Math.min(max, pos.max), ii);
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.FCS;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Pos)) return false;
    final Pos p = (Pos) cmp;
    return min == p.min && max == p.max;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(MIN, min, MAX, max == Long.MAX_VALUE ? INF : max));
  }

  @Override
  public String toString() {
    if(min == max) return Long.toString(min);
    final StringBuilder sb = new StringBuilder("position() ");
    if(max == Long.MAX_VALUE) sb.append('>');
    sb.append("= ").append(min);
    if(max != Long.MAX_VALUE) sb.append(" to ").append(max);
    return sb.toString();
  }
}
