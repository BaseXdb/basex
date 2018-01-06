package org.basex.query.func.db;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class DbInfo extends DbAccess {
  /** Resource element name. */
  private static final String DATABASE = "database";

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    return toNode(InfoDB.db(data.meta, false, true), DATABASE);
  }
}
