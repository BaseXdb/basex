package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

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
   * Returns a position expression for the specified numeric expression.
   * @param expr numeric expression
   * @param info input info (can be {@code null})
   * @param qc query context
   * @return position expression, {@link Bln#FALSE}, or {@code null}
   * @throws QueryException query exception
   */
  public static Expr get(final Expr expr, final InputInfo info, final QueryContext qc)
      throws QueryException {

    if(expr instanceof CmpPos) return expr;
    if(!numeric(expr)) return null;

    final Value value = expr.value(qc);
    final Expr ex = IntPos.get(value, OpV.EQ, info);
    return ex != null ? ex : MixedPos.get(value, info);
  }

  /**
   * Checks if the specified expression returns numbers and is deterministic.
   * @param expr expression
   * @return result of check
   */
  public static boolean numeric(final Expr expr) {
    return expr.seqType().type.isNumber() && expr.isSimple();
  }

  /**
   * Tries to rewrite {@code fn:position() CMP number(s)} to a positional expression.
   * @param pos positions to be matched
   * @param op comparison operator
   * @param info input info (can be {@code null})
   * @param cc compilation context
   * @param create create create new instance of this class
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  static Expr get(final Expr pos, final OpV op, final InputInfo info,
      final CompileContext cc, final boolean create) throws QueryException {

    // static result. example: position() > 0  ->  true
    final Expr ps = pos.optimizePos(op, cc);
    if(ps instanceof Bln) return ps;

    // value range. example: position() = 5 to 10
    final Expr ex = IntPos.get(ps, op, info);
    if(ex != null) return ex;

    // equality check, range: position() = RANGE
    if(op == OpV.EQ && ps instanceof Range) {
      if(((Range) ps).ints) {
        if(ps.isSimple()) return new SimplePos(info, ps.args());
        if(create) return new Pos(info, ps);
      }
      return null;
    }

    // rewrite check of single values to equality check
    Expr[] minMax = null;
    final SeqType st = ps.seqType();
    final Type type = st.type;
    if(st.one() && type.isNumberOrUntyped()) {
      switch(op) {
        case EQ:
          minMax = new Expr[] { ps, ps };
          break;
        case GE:
          minMax = new Expr[] { ps, Int.MAX };
          break;
        case GT:
          minMax = new Expr[] { new Arith(info, type.instanceOf(AtomType.INTEGER) ? ps :
            cc.function(Function.FLOOR, info, ps), Int.ONE, Calc.ADD).optimize(cc), Int.MAX };
          break;
        case LE:
          minMax = new Expr[] { Int.ONE, ps };
          break;
        case LT:
          minMax = new Expr[] { Int.ONE, new Arith(info, type.instanceOf(AtomType.INTEGER) ?
            ps : cc.function(Function.CEILING, info, ps), Int.ONE, Calc.SUBTRACT).optimize(cc) };
          break;
        default:
      }
    }

    // position() = 'xyz', position() != 2
    if(minMax == null) return null;
    // position() <= $pos  ->  position() = 1 to $pos
    if(ps.isSimple()) return new SimplePos(info, minMax);
    // position() = last()
    if(minMax[0] == minMax[1]) return create ? new Pos(info, minMax[0]) : null;
    // rewritten to equality range: position() < last()  ->  position() = 1 to last() - 1
    return type.instanceOf(AtomType.INTEGER) ?
      get(new Range(info, minMax).optimize(cc), OpV.EQ, info, cc, create) : null;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.NUMBER, cc);

    final Expr ex = get(expr, OpV.EQ, info, cc, false);
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
    // E[position() = last()]  ->  E[last()]
    return cc.simplify(this, mode == Simplify.PREDICATE &&
        expr.seqType().instanceOf(SeqType.NUMERIC_O) ? expr : this, mode);
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
