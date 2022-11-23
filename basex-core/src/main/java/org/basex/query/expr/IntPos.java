package org.basex.query.expr;

import static java.lang.Long.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Integer position range check.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class IntPos extends Simple implements CmpPos {
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
  private IntPos(final long min, final long max, final InputInfo info) {
    super(info, SeqType.BOOLEAN_O);
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
      new IntPos(Math.max(1, min), Math.max(1, max), ii);
  }

  /**
   * Tries to rewrite {@code fn:position() CMP number(s)} to this expression.
   * Returns an instance of this class, an optimized expression, or {@code null}
   * @param pos positions to be matched
   * @param op comparison operator
   * @param ii input info
   * @return optimized expression or {@code null}
   */
  static Expr get(final Expr pos, final OpV op, final InputInfo ii) {
    if(pos == Empty.VALUE) return Bln.FALSE;
    if(pos instanceof RangeSeq && op == OpV.EQ) {
      final long[] range = ((RangeSeq) pos).range(false);
      return get(range[0], range[1], ii);
    }
    if(pos instanceof ANum) {
      final ANum num = (ANum) pos;
      final long p = num.itr();
      final boolean exact = p == num.dbl();
      switch(op) {
        case EQ: return exact ? get(p, p, ii) : Bln.FALSE;
        case GE: return get(exact ? p : p + 1, MAX_VALUE, ii);
        case GT: return get(p + 1, MAX_VALUE, ii);
        case LE: return get(1, p, ii);
        case LT: return get(1, exact ? p - 1 : p, ii);
        case NE: return exact ? p < 2 ? get(p + 1, MAX_VALUE, ii) : null : Bln.TRUE;
        default:
      }
    }
    return null;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);
    return Bln.get(test(qc.focus.pos, qc) != 0);
  }

  @Override
  public boolean exact() {
    return min == max;
  }

  @Override
  public int test(final long pos, final QueryContext qc) {
    return pos == max ? 2 : pos >= min && pos <= max ? 1 : 0;
  }

  @Override
  public Expr inline(final InlineContext ic) {
    return null;
  }

  @Override
  public IntPos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new IntPos(min, max, info));
  }

  @Override
  public Expr invert(final CompileContext cc) throws QueryException {
    if(exact()) {
      final Expr pos = cc.function(Function.POSITION, info);
      return new CmpG(info, pos, Int.get(min), OpG.NE, null, cc.sc()).optimize(cc);
    }
    return min == 1 ? get(max + 1, MAX_VALUE, info) :
      max == MAX_VALUE ? get(1, min - 1, info) : null;
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc) {
    if(!(ex instanceof IntPos)) return null;

    // find range with smaller minimum
    IntPos pos1 = this, pos2 = (IntPos) ex;
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
    return Flag.POS.in(flags) || Flag.CTX.in(flags) || super.has(flags);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // E[position() = 3]  ->  E[3]
    return cc.simplify(this, mode.oneOf(Simplify.PREDICATE) && exact() ?
      Int.get(min) : this, mode);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof IntPos)) return false;
    final IntPos p = (IntPos) obj;
    return min == p.min && max == p.max;
  }

  @Override
  public String description() {
    return "positional access";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, MIN, min, MAX, max == MAX_VALUE ? INF : max));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.function(Function.POSITION);
    if(exact()) {
      qs.token("=").token(min);
    } else if(max == MAX_VALUE) {
      qs.token(">=").token(min);
    } else if(min == 1) {
      qs.token("<=").token(max);
    } else {
      qs.token("=").token(min).token(TO).token(max);
    }
  }
}
