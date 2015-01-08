package org.basex.query.func.json;

import org.basex.build.json.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions for parsing and serializing JSON objects.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class JsonParse extends JsonFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] input = toToken(exprs[0], qc);
    final JsonParserOptions opts = toOptions(1, Q_OPTIONS, new JsonParserOptions(), qc);
    try {
      return JsonConverter.get(opts).convert(input, null);
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }
}
