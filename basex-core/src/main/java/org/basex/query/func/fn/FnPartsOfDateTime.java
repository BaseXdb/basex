package org.basex.query.func.fn;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnPartsOfDateTime extends DateTimeFn {
  /** Return type. */
  private static final RecordType RETURN_TYPE = Records.DATETIME.get();

  static {
    if(RETURN_TYPE.fields().size() != 7) throw Util.notExpected();
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, ii);
    if(value.isEmpty()) return Empty.VALUE;
    final Value coerced = definition.types[0].coerce(value, definition.params[0], qc, null, ii);
    final ADate date = toDate(value, (BasicType) coerced.type, qc);
    final long
        y  = date.yea(),
        mo = date.mon(),
        d  = date.day(),
        h  = date.hour(),
        mi = date.minute();
    final BigDecimal s = date.sec();
    final Value[] values = {
      y  != Long.MAX_VALUE ? Itr.get(y)  : Empty.VALUE,
      mo >  0              ? Itr.get(mo) : Empty.VALUE,
      d  >  0              ? Itr.get(d)  : Empty.VALUE,
      h  >= 0              ? Itr.get(h)  : Empty.VALUE,
      mi >= 0              ? Itr.get(mi) : Empty.VALUE,
      s  != null           ? Dec.get(s)  : Empty.VALUE,
      date.hasTz()         ? zon(date)   : Empty.VALUE
    };
    return new XQRecordMap(values, RETURN_TYPE);
  }
}
