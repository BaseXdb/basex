package org.basex.query.func.convert;

import static org.basex.query.util.Err.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ConvertDayTimeToInteger extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final DTDur dur = (DTDur) checkAtomic(exprs[0], qc, AtomType.DTD);
    final BigDecimal ms = dur.sec.multiply(BigDecimal.valueOf(1000));
    if(ms.compareTo(ADateDur.BDMAXLONG) > 0) throw INTRANGE_X.get(info, ms);
    return Int.get(ms.longValue());
  }
}
