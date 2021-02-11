package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ConvertDayTimeToInteger extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final DTDur dur = (DTDur) checkType(exprs[0], qc, AtomType.DAY_TIME_DURATION);
    final BigDecimal ms = dur.sec.multiply(Dec.BD_1000);
    if(ms.compareTo(Dec.BD_MAXLONG) > 0) throw INTRANGE_X.get(info, ms);
    return Int.get(ms.longValue());
  }
}
