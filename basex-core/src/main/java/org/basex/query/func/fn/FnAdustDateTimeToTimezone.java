package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnAdustDateTimeToTimezone extends DateTimeFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    return adjust(BasicType.DATE_TIME, qc);
  }
}
