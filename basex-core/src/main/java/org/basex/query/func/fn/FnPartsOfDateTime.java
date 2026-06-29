package org.basex.query.func.fn;

import java.math.*;

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

    final long y = value.yea(), m = value.mon();
    final long d = value.day(), h = value.hour(), n = value.minute();
    final BigDecimal s = value.sec();
    return new XQRecordMap(Records.DATETIME.get(),
      y != Long.MAX_VALUE ? Itr.get(y) : Empty.VALUE,
      m >  0              ? Itr.get(m) : Empty.VALUE,
      d >  0              ? Itr.get(d) : Empty.VALUE,
      h >= 0              ? Itr.get(h) : Empty.VALUE,
      n >= 0              ? Itr.get(n) : Empty.VALUE,
      s != null           ? Dec.get(s) : Empty.VALUE,
      value.hasTz()       ? zon(value) : Empty.VALUE
    );
  }
}
