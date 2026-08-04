package org.basex.query.func.db;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbOptimize extends DbNew {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final boolean all = toBooleanOrFalse(arg(1), qc);
    final HashMap<String, String> options = toOptions(arg(2), qc);

    qc.updates().add(new DBOptimize(data, all, options, qc, info), qc);
    return Empty.VALUE;
  }
}
