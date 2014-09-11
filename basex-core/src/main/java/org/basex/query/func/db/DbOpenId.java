package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class DbOpenId extends DbOpenPre {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return open(qc, true);
  }
}
