package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbAlter extends DbCopy {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    copy(qc, false);
    return Empty.VALUE;
  }
}
