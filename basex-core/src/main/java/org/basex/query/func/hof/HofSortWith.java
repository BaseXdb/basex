package org.basex.query.func.hof;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class HofSortWith extends HofFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value v = exprs[0].value(qc);
    final Comparator<Item> cmp = getComp(1, qc);
    if(v.size() < 2) return v;
    final ValueBuilder vb = v.cache();
    try {
      Arrays.sort(vb.items(), 0, (int) vb.size(), cmp);
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
    return vb.value();
  }
}
