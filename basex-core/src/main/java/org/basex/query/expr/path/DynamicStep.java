package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Non-navigational step in a path expression: XNodes are navigated, JNodes are selected by key
 * from the child axis.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DynamicStep extends Single {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr step expression
   */
  public DynamicStep(final InputInfo info, final Expr expr) {
    super(info, expr, Types.ITEM_ZM);
  }

  @Override
  public void checkUp() throws QueryException {
    expr.checkUp();
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // updating expressions are no JNode selectors
    if(expr.has(Flag.UPD)) return expr;

    // build only the interpretations that can apply to the static type of the input
    final Expr ctx = new ContextValue(info).optimize(cc);
    final Expr cond = new Instance(info, ctx, Types.XNODE_O).optimize(cc);
    if(cond == Bln.TRUE) return expr;

    final boolean both = cond != Bln.FALSE;
    final Expr selector = new SelectorStep(info, Axis.CHILD,
        both ? expr.copy(cc, new IntObjectMap<>()) : expr).optimize(cc);
    return both ? new If(info, cond, expr, selector).optimize(cc) : selector;
  }

  @Override
  public Value value(final QueryContext qc) {
    throw Util.notExpected(this);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new DynamicStep(info, expr.copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DynamicStep && super.equals(obj);
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
