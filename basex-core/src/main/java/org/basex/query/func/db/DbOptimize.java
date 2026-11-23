package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbOptimize extends DbNew {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final boolean all = toBooleanOrFalse(arg(1), qc);
    final DbOptions options = toOptions(qc);
    if(!all && options.contains(MainOptions.UPDINDEX)) {
      throw BASEX_OPTIONS_X.get(info, Options.unknown(
          MainOptions.UPDINDEX.name().toLowerCase(Locale.ENGLISH)));
    }

    qc.updates().add(new DBOptimize(data, all, options, qc, info), qc);
    return Empty.VALUE;
  }
}
