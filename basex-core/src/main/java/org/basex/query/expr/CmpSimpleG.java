package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General comparison of two items.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CmpSimpleG extends CmpG {
  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param coll collation (can be {@code null})
   * @param sc static context
   * @param info input info
   */
  CmpSimpleG(final Expr expr1, final Expr expr2, final OpG op, final Collation coll,
      final StaticContext sc, final InputInfo info) {
    super(expr1, expr2, op, coll, sc, info);
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item1 = exprs[0].item(qc, info);
    if(item1 == Empty.VALUE) return Bln.FALSE;
    final Item item2 = exprs[1].item(qc, info);
    return item2 == Empty.VALUE ? Bln.FALSE : Bln.get(eval(item1, item2));
  }

  @Override
  public CmpG copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CmpSimpleG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, coll, sc,
        info));
  }

  @Override
  public String description() {
    return "simplified " + super.description();
  }
}
