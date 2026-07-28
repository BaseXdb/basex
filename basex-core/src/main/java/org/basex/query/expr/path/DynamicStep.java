package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Non-navigational step in a path expression (XQuery 4.0, qtspecs #2734). Before evaluation it is
 * rewritten to {@code if(. instance of node()) then E else child::{ E }}, so that XNodes are
 * navigated while JNodes are selected by key from the child axis.
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

    // if(. instance of node()) then E else child::{ E }
    final Expr ctx = new ContextValue(info).optimize(cc);
    final Expr cond = new Instance(info, ctx, Types.NODE_O).optimize(cc);
    final Expr selector = new SelectorStep(info, Axis.CHILD,
        expr.copy(cc, new IntObjectMap<>())).optimize(cc);
    return new If(info, cond, expr, selector).optimize(cc);
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
