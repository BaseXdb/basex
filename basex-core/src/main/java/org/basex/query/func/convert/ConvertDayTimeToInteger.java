package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ConvertDayTimeToInteger extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final DTDur value = (DTDur) checkType(arg(0), BasicType.DAY_TIME_DURATION, qc);
    return Itr.get(value.ms(info));
  }
}
