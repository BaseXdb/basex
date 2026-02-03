package org.basex.query.func.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
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
      final Iterator<Value> values = array.iterable().iterator();

      @Override
      public XQMap next() {
        return values.hasNext() ? record(values.next()) : null;
      }
      @Override
      public Item get(final long i) {
        return record(array.valueAt(i));
      }
    };
  }

  /**
   * Creates a value record.
   * @param value value of the record
   * @return map
   */
  private static XQMap record(final Value value) {
    final XQMap map = XQMap.get(Str.VALUE, value);
    map.type = Records.MEMBER.get();
    return map;
  }
}
