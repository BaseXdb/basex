package org.basex.query.func.db;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbSystem extends StandardFunc {
  /** Resource element name. */
  private static final String SYSTEM = "system";

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    return DbInfo.toNode(Info.info(qc.context), SYSTEM);
  }
}
