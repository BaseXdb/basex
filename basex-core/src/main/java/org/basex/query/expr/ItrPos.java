package org.basex.query.expr;

import static java.lang.Long.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
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
 * Simple position check expression.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class ItrPos extends Simple {
  /** Minimum position (1 or larger). */
  final long min;
  /** Maximum position (inclusive, 1 or larger, never smaller than {@link #min}). */
  final long max;

  /**
   * Constructor.
   * @param min minimum value (1 or larger)
   * @param max minimum value (inclusive, 1 or larger)
   * @param info input info
   */
  private ItrPos(final long min, final long max, final InputInfo info) {
    super(info, SeqType.BLN_O);
    this.min = min;
    this.max = max;
  }

  /**
   * Returns a position expression for the specified position, or an optimized boolean item.
   * @param pos position
   * @param ii input info
   * @return expression
   */
  public static Expr get(final double pos, final InputInfo ii) {
    final long p = (long) pos;
    return p != pos || p < 1 ? Bln.FALSE : get(p, p, ii);
  }

  /**
   * Returns a position expression for the specified range, or an optimized boolean item.
   * @param min minimum position
   * @param max minimum position (inclusive)
   * @param ii input info
   * @return expression
   */
  public static Expr get(final long min, final long max, final InputInfo ii) {
    // assumption: positions do not exceed bounds of long values
    return min > max || max < 1 ? Bln.FALSE : min <= 1 && max == MAX_VALUE ? Bln.TRUE :
      new ItrPos(Math.max(1, min), Math.max(1, max), ii);
  }

  /**
   * Tries to rewrite {@code fn:position() CMP number(s)} to this expression.
   * Returns an instance of this class, an optimized expression, or {@code null}
   * @param expr expression to be checked
   * @param op comparator
   * @param ii input info
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  static Expr get(final Expr expr, final OpV op, final InputInfo ii) throws QueryException {
    if(expr instanceof Value) {
      final Value value = (Value) expr;
      if(value instanceof RangeSeq && op == OpV.EQ) {
        final long[] range = ((RangeSeq) value).range(false);
        return get(range[0], range[1], ii);
      } else if(value.isItem()) {
        final Item item = (Item) value;
        final long pos = item.itr(ii);
        final boolean exact = pos == item.dbl(ii);
        switch(op) {
          case EQ: return exact ? get(pos, pos, ii) : Bln.FALSE;
          case GE: return get(exact ? pos : pos + 1, MAX_VALUE, ii);
          case GT: return get(pos + 1, MAX_VALUE, ii);
          case LE: return get(1, pos, ii);
          case LT: return get(1, exact ? pos - 1 : pos, ii);
          case NE: return exact ? pos < 2 ? get(pos + 1, MAX_VALUE, ii) : null : Bln.TRUE;
          default:
        }
      } else if(value.isEmpty()) {
        return Bln.FALSE;
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
  public Expr inline(final InlineContext ic) {
    return null;
  };

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

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc) {
    if(!(ex instanceof ItrPos)) return null;

    // find range with smaller minimum
    ItrPos pos1 = this, pos2 = (ItrPos) ex;
    if(pos2.min < pos1.min) {
      pos1 = pos2;
      pos2 = this;
    }
    // create intersection: position() = 1 to 2 and position() = 2 to 3  ->  position() = 2
    return !or ? get(pos2.min, Math.min(pos1.max, pos2.max), info) :
      // create union: position() = 1 to 2 or position() = 2 to 3  ->  position() = 1 to 3
      pos1.max + 1 >= pos2.min ? get(pos1.min, Math.max(pos1.max, pos2.max), info) :
      // disjoint ranges: position() = 1 or position() = 3   ->  no rewrite
      null;
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.POS.in(flags) || Flag.CTX.in(flags);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof ItrPos)) return false;
    final ItrPos p = (ItrPos) obj;
    return min == p.min && max == p.max;
  }

  @Override
  public String description() {
    return "positional access";
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, MIN, min, MAX, max == MAX_VALUE ? INF : max));
  }

  @Override
  public void plan(final QueryString qs) {
    if(min == max) {
      qs.token(min);
    } else {
      qs.token(Function.POSITION);
      if(max == MAX_VALUE) {
        qs.token(">=").token(min);
      } else {
        qs.token("=").token(min).token(TO).token(max);
      }
    }
  }
}
