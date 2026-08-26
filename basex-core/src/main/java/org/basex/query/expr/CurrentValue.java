package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Expression that supplies the context value of the caller to fn:current.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CurrentValue extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param current context value of the caller
   * @param expr expression
   */
  private CurrentValue(final InputInfo info, final Expr current, final Expr expr) {
    super(info, Types.ITEM_ZM, current, expr);
  }

  /**
   * Binds the context value of the caller to the fn:current calls of a default value.
   * @param expr default value
   * @param info input info (can be {@code null})
   * @return expression
   */
  public static Expr get(final Expr expr, final InputInfo info) {
    return expr.has(Flag.CUR) ? new CurrentValue(info, new ContextValue(info), expr) : expr;
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    // current() → context value of the caller
    final Expr expr = exprs[1] instanceof final GlobalFocus gf ? gf.expr : exprs[1];
    if(expr instanceof FnCurrent) return cc.replaceWith(this, exprs[0]);
    // no remaining reference to the context value of the caller
    if(!exprs[1].has(Flag.CUR)) return cc.replaceWith(this, exprs[1]);
    return adoptType(exprs[1]);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc), current = qc.current;
    qc.current = value;
    try {
      return exprs[1].value(qc);
    } finally {
      qc.current = current;
    }
  }

  @Override
  public boolean has(final Flag... flags) {
    // fn:current calls of the second operand are resolved by this expression
    return exprs[0].has(flags) || exprs[1].has(Flag.remove(flags, Flag.CUR));
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return ic.var != null ? super.inlineable(ic) : exprs[0].inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return var != null ? super.count(var) : exprs[0].count(var);
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    if(ic.var != null) return super.inline(ic);
    final Expr inlined = exprs[0].inline(ic);
    if(inlined == null) return null;
    exprs[0] = inlined;
    return optimize(ic.cc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CurrentValue(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CurrentValue && super.equals(obj);
  }

  @Override
  public String description() {
    return "context value of the caller";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(exprs[1]);
  }
}
