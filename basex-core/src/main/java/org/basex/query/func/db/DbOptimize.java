package org.basex.query.func.db;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbOptimize extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final boolean all = toBooleanOrFalse(arg(1), qc);
    final HashMap<String, String> options = toOptions(arg(2), qc);

    qc.updates().add(new DBOptimize(data, all, options, qc, info), qc);
    return Empty.VALUE;
  }
}
