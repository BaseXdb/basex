package org.basex.query.func.unit;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UnitAssertEquals extends UnitFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter1 = exprs[0].iter(qc), iter2 = exprs[1].iter(qc);
    final DeepEqual comp = new DeepEqual(info);
    Item item1, item2;
    int c = 1;
    while(true) {
      item1 = qc.next(iter1);
      item2 = iter2.next();
      final boolean empty1 = item1 == null, empty2 = item2 == null;
      if(empty1 && empty2) return Empty.VALUE;
      if(empty1 || empty2 || !comp.equal(item1.iter(), item2.iter())) break;
      c++;
    }
    final Item item = exprs.length > 2 ? toNodeOrAtomItem(2, qc) : null;
    throw new UnitException(info, UNIT_FAIL_X_X_X, item1, item2, c).value(item);
  }
}
