package org.basex.query.func.db;

import java.util.*;

import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.name.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbCreate extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String database = toName(arg(0), qc);
    checkCreate(database, qc);
    final NewInput[] inputs = toInputs(qc);
    final HashMap<String, String> options = toOptions(arg(3), qc);
    qc.updates().add(new DBCreate(database, inputs, options, qc, info), qc);
    return Empty.VALUE;
  }
}
