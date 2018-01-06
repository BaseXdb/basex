package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnDeepEqual extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter1 = exprs[0].iter(qc), iter2 = exprs[1].iter(qc);
    final Collation coll = toCollation(2, qc);
    return Bln.get(new DeepEqual(info).collation(coll).equal(iter1, iter2, qc));
  }
}
