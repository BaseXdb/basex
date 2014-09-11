package org.basex.query.func.db;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DbSystem extends DbFn {
  /** Resource element name. */
  private static final String SYSTEM = "system";

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return toNode(Info.info(qc.context), SYSTEM);
  }
}
