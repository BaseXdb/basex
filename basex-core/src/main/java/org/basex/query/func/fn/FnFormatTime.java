package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFormatTime extends FormatFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return formatDate(BasicType.TIME, qc);
  }
}
