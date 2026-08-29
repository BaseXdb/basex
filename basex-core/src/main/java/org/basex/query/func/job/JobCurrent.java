package org.basex.query.func.job;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobCurrent extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) {
    return Str.get(qc.jc().id());
  }
}
