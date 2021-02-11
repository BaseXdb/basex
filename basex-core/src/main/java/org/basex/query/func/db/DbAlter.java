package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbAlter extends DbCopy {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    copy(qc, false);
    return Empty.VALUE;
  }
}
