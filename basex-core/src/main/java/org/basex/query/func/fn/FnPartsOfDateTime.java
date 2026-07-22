package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnPartsOfDateTime extends DateTimeFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ADate value = toGregorianOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    return new XQRecordMap(Records.DATETIME.get(),
      value.hasYear()    ? Itr.get(value.yea())     : Empty.VALUE,
      value.hasMonth()   ? Itr.get(value.mon())     : Empty.VALUE,
      value.hasDay()     ? Itr.get(value.day())     : Empty.VALUE,
      value.hasHours()   ? Itr.get(value.hour())    : Empty.VALUE,
      value.hasMinutes() ? Itr.get(value.minute())  : Empty.VALUE,
      value.hasSeconds() ? Dec.get(value.seconds()) : Empty.VALUE,
      value.hasTz()      ? zon(value)               : Empty.VALUE
    );
  }
}
