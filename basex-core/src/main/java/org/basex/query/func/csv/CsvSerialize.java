package org.basex.query.func.csv;

import static org.basex.query.QueryError.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CsvSerialize extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final Item options = arg(1).item(qc, ii);
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.CSV);
    sopts.set(SerializerOptions.CSV, options.isEmpty() ? XQMap.empty() : toMap(options));
    return Str.get(serialize(input, sopts, INVALIDOPT_X, qc));
  }
}
