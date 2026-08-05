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
public final class ConvertIntegerToDateTime extends StandardFunc {
  @Override
  protected Dtm item(final QueryContext qc) throws QueryException {
    return Dtm.get(toLong(arg(0), qc));
  }
}
