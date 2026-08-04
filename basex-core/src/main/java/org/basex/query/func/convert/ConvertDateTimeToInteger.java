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
public final class ConvertDateTimeToInteger extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    return Itr.get(toMs(arg(0), qc));
  }
}
