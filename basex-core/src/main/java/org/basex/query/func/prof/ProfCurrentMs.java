package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProfCurrentMs extends StandardFunc {
  @Override
  protected Itr item(final QueryContext qc) {
    return Itr.get(System.currentTimeMillis());
  }
}
