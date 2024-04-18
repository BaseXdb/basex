package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Map expression for iterating single items.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class SingleValueMap extends ValueMap {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  SingleValueMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final QueryFocus qf = qc.focus;
    final Value qv = qf.value;
    try {
      Item item = exprs[0].item(qc, info);
      final int el = exprs.length;
      for(int e = 1; e < el; e++) {
        qf.value = item;
        item = exprs[e].item(qc, info);
      }
      return item;
    } finally {
      qf.value = qv;
    }
  }

  @Override
  public SingleValueMap copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new SingleValueMap(info, Arr.copyAll(cc, vm, exprs)));
  }
}
