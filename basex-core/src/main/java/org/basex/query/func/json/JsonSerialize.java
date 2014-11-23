package org.basex.query.func.json;

import static org.basex.query.QueryError.*;

import org.basex.build.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions for parsing and serializing JSON objects.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class JsonSerialize extends JsonFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    final JsonSerialOptions jopts = toOptions(1, Q_OPTIONS, new JsonSerialOptions(), qc);

    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.JSON);
    sopts.set(SerializerOptions.JSON, jopts);
    return Str.get(serialize(iter, sopts, INVALIDOPT_X));
  }
}
