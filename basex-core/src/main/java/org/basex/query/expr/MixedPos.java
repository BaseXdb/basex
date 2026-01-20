package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Mixed position checks.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class MixedPos extends Single implements CmpPos {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr positions; if a value is supplied, the entries are sorted
   */
  MixedPos(final InputInfo info, final Expr expr) {
    super(info, expr, Types.BOOLEAN_O);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.NUMBER, cc).simplifyFor(Simplify.DISTINCT, cc);

    final Expr ex = Pos.get(expr, CmpOp.EQ, info, cc, this);
    return ex != null ? cc.replaceWith(this, ex) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    ctxValue(qc);
    return expr.value(qc).test(qc, info, qc.focus.pos);
  }

  @Override
  public Value positions(final QueryContext qc) throws QueryException {
    // Value instances are known to be sorted and duplicate-free (see Pos#get)
    return expr instanceof final Value value ? value : Pos.ddo(expr.value(qc));
  }

  @Override
  public boolean exact() {
    return false;
  }

  @Override
  public Expr invert(final CompileContext cc) {
    return null;
  }

  @Override
  public MixedPos copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new MixedPos(info, expr.copy(cc, vm)));
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.POS.oneOf(flags) || Flag.CTX.oneOf(flags) || super.has(flags);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof MixedPos && super.equals(obj);
  }

  @Override
  public String description() {
    return "mixed positional access";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.function(Function.POSITION).token("=").paren(expr);
  }
}
