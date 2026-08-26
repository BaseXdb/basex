package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Expression that is evaluated with the focus of the query prolog.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GlobalFocus extends Single {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression
   */
  public GlobalFocus(final InputInfo info, final Expr expr) {
    super(info, expr, Types.ITEM_ZM);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final QueryFocus focus = cc.qc.focus;
    cc.qc.focus = cc.qc.globalFocus();
    try {
      expr = expr.compile(cc);
    } finally {
      cc.qc.focus = focus;
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    // focus-independent expressions can be evaluated in place
    return expr.has(Flag.CTX, Flag.POS) ? adoptType(expr) : expr;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final QueryFocus focus = qc.focus;
    qc.focus = qc.globalFocus();
    try {
      return expr.value(qc);
    } finally {
      qc.focus = focus;
    }
  }

  @Override
  public boolean has(final Flag... flags) {
    return expr.has(Flag.remove(flags, Flag.CTX, Flag.POS));
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    // the focus of the caller is not accessed
    if(ic.var == null) return true;
    // do not replace $v with .:  ($v := .) in a global focus
    return !(ic.expr.has(Flag.CTX) && expr.uses(ic.var)) && expr.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return var == null ? VarUsage.NEVER : expr.count(var);
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    return ic.var != null ? super.inline(ic) : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new GlobalFocus(info, expr.copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof GlobalFocus && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(expr);
  }
}
