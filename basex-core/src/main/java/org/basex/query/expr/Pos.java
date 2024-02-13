package org.basex.query.expr;

import static java.lang.Long.*;

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
import org.basex.util.list.*;

/**
 * Position range check.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Pos extends Single {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression yielding an empty sequence, an integer or an integer range
   */
  private Pos(final InputInfo info, final Expr expr) {
    super(info, expr, SeqType.BOOLEAN_O);
  }

  /**
   * Tries to rewrite {@code fn:position() CMP number(s)} to a positional expression.
   * @param positions positions to be matched
   * @param op comparison operator
   * @param info input info (can be {@code null})
   * @param cc compilation context
   * @param ref calling expression
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  public static Expr get(final Expr positions, final OpV op, final InputInfo info,
      final CompileContext cc, final Expr ref) throws QueryException {

    // static result. example: position() > 0  ->  true
    Expr pos = positions.optimizePos(op, cc);
    if(pos instanceof Bln) return pos;

    if(op == OpV.EQ) {
      // normalize positions (sort, remove duplicates and illegal positions)
      if(pos instanceof Value && pos.size() <= CompileContext.MAX_PREEVAL) pos = ddo((Value) pos);
      if(pos == Empty.VALUE) return Bln.FALSE;

      // range sequence. example: position() = 5 to 10
      if(pos instanceof RangeSeq) {
        final RangeSeq rs = (RangeSeq) pos;
        return IntPos.get(rs.min(), rs.max(), info);
      }
      // range. example: position() = 3 to $max
      if(pos instanceof Range && ((Range) pos).ints) {
        if(pos.isSimple()) return new SimplePos(info, pos.args());
        return ref instanceof Pos ? null : new Pos(info, pos);
      }
    }

    // integer tests. example: position() > 5
    if(pos instanceof ANum) {
      final ANum num = (ANum) pos;
      final long p = num.itr();
      final boolean exact = p == num.dbl();
      switch(op) {
        case EQ: return exact ? IntPos.get(p, p, info) : Bln.FALSE;
        case GE: return IntPos.get(exact ? p : p + 1, MAX_VALUE, info);
        case GT: return IntPos.get(p + 1, MAX_VALUE, info);
        case LE: return IntPos.get(1, p, info);
        case LT: return IntPos.get(1, exact ? p - 1 : p, info);
        case NE: return exact ? p < 2 ? IntPos.get(p + 1, MAX_VALUE, info) : null : Bln.TRUE;
        default:
      }
    }

    // numeric tests
    final SeqType st = pos.seqType();
    final Type type = st.type;
    final boolean integer = type.instanceOf(AtomType.INTEGER);
    if(st.zeroOrOne() && type.isNumberOrUntyped()) {
      Expr min = null, max = null;
      switch(op) {
        case EQ:
          min = pos;
          break;
        case GE:
          min = pos;
          max = Int.MAX;
          break;
        case GT:
          min = new Arith(info, integer ? pos :
            cc.function(Function.FLOOR, info, pos), Int.ONE, Calc.ADD).optimize(cc);
          max = Int.MAX;
          break;
        case LE:
          min = Int.ONE;
          max = pos;
          break;
        case LT:
          min = Int.ONE;
          max = new Arith(info, integer ?
            pos : cc.function(Function.CEILING, info, pos), Int.ONE, Calc.SUBTRACT).optimize(cc);
          break;
        default:
      }
      if(min != null) {
        // position() <= $pos  ->  pos: 1, $pos
        if(pos.isSimple()) return SimplePos.get(min, max, info);
        // position() = last()  ->  pos: last()
        if(max == null) return ref instanceof Pos ? null : new Pos(info, min);
        // position() < last()  ->  position() = 1 to last() - 1
        if(integer) return get(new Range(info, min, max).optimize(cc), OpV.EQ, info, cc, ref);
      }
    }

    // position() = (1, 2, 4)
    if(op == OpV.EQ && pos.isSimple()) {
      return ref instanceof MixedPos ? null : new MixedPos(info, pos);
    }

    return null;
  }

  /**
   * Returns positions sorted and without duplicates.
   * @param value positions
   * @return sorted positions
   * @throws QueryException query exception
   */
  public static Value ddo(final Value value) throws QueryException {
    if(value instanceof RangeSeq) return value;
    final LongList list = new LongList();
    for(final Item item : value) {
      final double d = item.dbl(null);
      long l = (long) d;
      if(l > 0 && d == l) list.add(l);
    }
    return IntSeq.get(list.ddo().finish());
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.NUMBER, cc).simplifyFor(Simplify.DISTINCT, cc);

    final Expr ex = get(expr, OpV.EQ, info, cc, this);
    return ex != null ? cc.replaceWith(this, ex) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);

    final Value value = expr.value(qc);
    if(value.isEmpty()) return Bln.FALSE;

    final long pos = qc.focus.pos, vs = value.size();
    final double min = toDouble(value.itemAt(0));
    final double max = vs == 1 ? min : toDouble(value.itemAt(vs - 1));
    return Bln.get(pos >= min && pos <= max);
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
    if(mode == Simplify.PREDICATE) {
      // pos: last() + 1  ->  false()
      final Expr ex = expr.simplifyFor(mode, cc);
      if(ex != expr) return ex;
    }
    return simplify(mode, cc);
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
