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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbAdd extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final NewInput[] inputs = toInputs(qc);
    final HashMap<String, String> options = toOptions(arg(3), qc);
    if(inputs.length != 0) {
      qc.updates().add(new DBAdd(data, options, false, qc, info, inputs), qc);
    }
    return Empty.VALUE;
  }
}
