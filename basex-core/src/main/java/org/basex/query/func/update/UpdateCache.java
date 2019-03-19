package org.basex.query.func.update;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public class UpdateCache extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) {
    return qc.updates().items.value();
  }
}
