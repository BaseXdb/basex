package org.basex.query.func.json;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonSerialize extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final JsonSerialOptions options = toOptions(arg(1), new JsonSerialOptions(), qc);
    return Str.get(serialize(input, FnXmlToJson.options(options), INVALIDOPTION_X, qc));
  }
}
