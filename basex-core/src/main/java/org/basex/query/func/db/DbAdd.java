package org.basex.query.func.db;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class DbAdd extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final String path = toStringOrNull(arg(2), qc);

    final NewInput input = toNewInput(toNodeOrAtomItem(arg(1), qc),
        path != null ? toDbPath(path) : "");
    final HashMap<String, String> options = toOptions(arg(3), qc);

    qc.updates().add(new DBAdd(data, input, options, false, qc, info), qc);
    return Empty.VALUE;
  }
}
