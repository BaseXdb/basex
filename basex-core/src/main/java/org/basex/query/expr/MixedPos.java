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
 * Mixed position checks.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class MixedPos extends Single implements CmpPos {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr positions; if a value is supplied, the entries are sorted
   */
  MixedPos(final InputInfo info, final Expr expr) {
    super(info, expr, SeqType.BOOLEAN_O);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.NUMBER, cc).simplifyFor(Simplify.DISTINCT, cc);

    final Expr ex = Pos.get(expr, OpV.EQ, info, cc, this);
    return ex != null ? cc.replaceWith(this, ex) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);
    final long pos = qc.focus.pos;
    return Bln.get(expr.value(qc).test(qc, info, pos));
  }

  @Override
  public Value positions(final QueryContext qc) throws QueryException {
    return expr instanceof Value ? (Value) expr : Pos.ddo(expr.value(qc));
  }

  @Override
  public boolean exact() {
    return false;
  }

  @Override
  public Expr invert(final CompileContext cc) throws QueryException {
    return null;
  }

  @Override
  public MixedPos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new MixedPos(info, expr.copy(cc, vm)));
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.POS.in(flags) || Flag.CTX.in(flags) || super.has(flags);
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
