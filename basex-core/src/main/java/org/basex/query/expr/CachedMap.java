package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map expression.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class CachedMap extends SimpleMap {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  CachedMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value cv = qc.value;
    final long cp = qc.pos, cs = qc.size;
    try {
      ItemList result = qc.value(exprs[0]).cache();
      final int el = exprs.length;
      for(int e = 1; e < el; e++) {
        qc.pos = 0;
        qc.size = result.size();
        final ItemList vb = new ItemList(result.size());
        for(final Item it : result) {
          qc.pos++;
          qc.value = it;
          vb.add(qc.value(exprs[e]));
        }
        result = vb;
      }
      return result.value();
    } finally {
      qc.value = cv;
      qc.size = cs;
      qc.pos = cp;
    }
  }

  @Override
  public SimpleMap copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new CachedMap(info, Arr.copyAll(qc, scp, vs, exprs)));
  }
}
