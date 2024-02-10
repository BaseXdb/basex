package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ConvertDayTimeToInteger extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final DTDur value = (DTDur) checkType(arg(0), AtomType.DAY_TIME_DURATION, qc);
    return Int.get(value.ms(info));
  }
}
