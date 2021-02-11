package org.basex.query.func.jobs;

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
public final class JobsCurrent extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) {
    return Str.get(qc.jc().id());
  }
}
