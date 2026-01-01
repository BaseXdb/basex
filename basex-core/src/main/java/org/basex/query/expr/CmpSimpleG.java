package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General comparison of two items.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CmpSimpleG extends CmpG {
  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param info input info (can be {@code null})
   */
  CmpSimpleG(final Expr expr1, final Expr expr2, final OpG op, final InputInfo info) {
    super(info, expr1, expr2, op);
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final Item item1 = exprs[0].item(qc, info);
    if(item1.isEmpty()) return false;
    final Item item2 = exprs[1].item(qc, info);
    if(item2.isEmpty()) return false;
    return eval(item1, item2);
  }

  @Override
  public CmpG copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CmpSimpleG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, info));
  }

  @Override
  public String description() {
    return "simplified " + super.description();
  }
}
