package org.basex.query.func.fn;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
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

    final long
        y  = value.yea(),
        mo = value.mon(),
        d  = value.day(),
        h  = value.hour(),
        mi = value.minute();
    final BigDecimal s = value.sec();
    final Value[] values = {
      y  != Long.MAX_VALUE ? Itr.get(y)  : Empty.VALUE,
      mo >  0              ? Itr.get(mo) : Empty.VALUE,
      d  >  0              ? Itr.get(d)  : Empty.VALUE,
      h  >= 0              ? Itr.get(h)  : Empty.VALUE,
      mi >= 0              ? Itr.get(mi) : Empty.VALUE,
      s  != null           ? Dec.get(s)  : Empty.VALUE,
      value.hasTz()        ? zon(value)   : Empty.VALUE
    };
    return new XQRecordMap(Records.DATETIME.get(), values);
  }
}
