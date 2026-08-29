package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ConvertIntegerToDayTime extends StandardFunc {
  @Override
  public DTDur value(final QueryContext qc) throws QueryException {
    return DTDur.get(toLong(arg(0), qc));
  }
}
