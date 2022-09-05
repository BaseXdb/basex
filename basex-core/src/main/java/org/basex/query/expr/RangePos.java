package org.basex.query.expr;

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
 * Position range check.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
final class RangePos extends Single {
  /**
   * Constructor.
   * @param info input info
   * @param range range expression
   */
  private RangePos(final InputInfo info, final Expr range) {
    super(info, range, SeqType.BOOLEAN_O);
  }

  /**
   * Tries to rewrite {@code fn:position() CMP number(s)} to this expression.
   * Returns an instance of this class, an optimized expression, or {@code null}
   * @param expr positions to be matched
   * @param op comparator
   * @param ii input info
   * @return optimized expression or {@code null}
   */
  static Expr get(final Expr expr, final OpV op, final InputInfo ii) {
    return expr instanceof Range && op == OpV.EQ ? new RangePos(ii, expr) : null;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    Expr ex = ItrPos.get(expr, OpV.EQ, info);
    if(ex == null) ex = Pos.get(expr, OpV.EQ, info, cc);
    return ex != null ? cc.replaceWith(this, ex) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);

    final Value value = expr.value(qc);
    if(value == Empty.VALUE) return Bln.FALSE;

    final long pos = qc.focus.pos;
    final long min = toLong(value.itemAt(0)), max = toLong(value.itemAt(value.size() - 1));
    return Bln.get(pos >= min && pos <= max);
  }

  @Override
  public RangePos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new RangePos(info, expr.copy(cc, vm)));
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.POS.in(flags) || Flag.CTX.in(flags) || super.has(flags);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof RangePos && super.equals(obj);
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
