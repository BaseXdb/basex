package org.basex.query.expr;

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
 * Position range check.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
final class Pos extends Single {
  /**
   * Constructor.
   * @param info input info
   * @param expr expression yielding an empty sequence, an integer or an integer range
   */
  private Pos(final InputInfo info, final Expr expr) {
    super(info, expr, SeqType.BOOLEAN_O);
  }

  /**
   * Tries to rewrite {@code fn:position() CMP number(s)} to a positional expression.
   * @param pos positions to be matched
   * @param op comparison operator
   * @param ii input info
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  static Expr get(final Expr pos, final OpV op, final InputInfo ii, final CompileContext cc)
      throws QueryException {

    final Expr ex = IntPos.get(pos, op, ii);
    if(ex != null) return ex;
    if(pos.isSimple()) return SimplePos.get(pos, op, ii, cc);

    final Expr[] minMax = minMax(pos, op, cc, ii);
    if(minMax == null) return null;

    final Expr range;
    if(pos instanceof Range && Arrays.equals(pos.args(), minMax)) {
      range = pos;
    } else if(minMax[0] == minMax[1]) {
      range = minMax[0];
    } else {
      range = new Range(ii, minMax[0], minMax[1]).optimize(cc);
    }
    return new Pos(ii, range);
  }

  /**
   * Returns min/max positions for a positional query.
   * @param pos positions to be matched
   * @param op comparator
   * @param ii input info
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  static Expr[] minMax(final Expr pos, final OpV op, final CompileContext cc,
      final InputInfo ii) throws QueryException {
    final SeqType st2 = pos.seqType();
    final boolean range = op == OpV.EQ && pos instanceof Range;
    if(range) return pos.args();

    if(st2.one() && !st2.mayBeArray()) {
      switch(op) {
        case EQ: return new Expr[] { pos, pos };
        case GE: return new Expr[] { pos, Int.MAX };
        case GT: return new Expr[] { new Arith(ii, st2.type.instanceOf(AtomType.INTEGER) ? pos :
            cc.function(Function.FLOOR, ii, pos), Int.ONE, Calc.PLUS).optimize(cc), Int.MAX };
        case LE: return new Expr[] { Int.ONE, pos };
        case LT: return new Expr[] { Int.ONE, new Arith(ii, st2.type.instanceOf(AtomType.INTEGER) ?
            pos : cc.function(Function.CEILING, ii, pos), Int.ONE, Calc.MINUS).optimize(cc) };
        default:
      }
    }
    return null;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.NUMBER, cc);

    Expr ex = null;
    final Expr pos = expr.optimizePos(OpV.EQ, cc);
    if(pos instanceof Bln) {
      ex = pos;
    } else {
      ex = IntPos.get(pos, OpV.EQ, info);
      if(ex == null && pos.isSimple()) ex = SimplePos.get(pos, OpV.EQ, info, cc);
    }
    return ex != null ? cc.replaceWith(this, ex) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);

    final Value value = expr.value(qc);
    if(value == Empty.VALUE) return Bln.FALSE;

    final long pos = qc.focus.pos, min = toLong(value.itemAt(0)), size = value.size();
    return Bln.get(size == 1 ? pos == min : pos >= min && pos <= toLong(value.itemAt(size - 1)));
  }

  @Override
  public Pos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Pos(info, expr.copy(cc, vm)));
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.POS.in(flags) || Flag.CTX.in(flags) || super.has(flags);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    if(mode.oneOf(Simplify.PREDICATE)) {
      return expr.seqType().instanceOf(SeqType.NUMERIC_O) ? expr : this;
    }
    return super.simplifyFor(mode, cc);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Pos && super.equals(obj);
  }

  @Override
  public String description() {
    return "positional access";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.function(Function.POSITION).token("=").token(expr);
  }
}
