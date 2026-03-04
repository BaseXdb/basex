package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayMembers extends ArrayFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);

    return new BasicIter<>(array.structSize()) {
      @Override
      public Item get(final long i) {
        return new XQRecordMap(Records.MEMBER.get(), array.valueAt(i));
      }
    };
  }
}
