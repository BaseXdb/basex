package org.basex.query.func.json;

import org.basex.build.json.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class JsonParse extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    final JsonParserOptions opts = toOptions(1, new JsonParserOptions(), qc);
    if(item == Empty.VALUE) return Empty.VALUE;

    try {
      return JsonConverter.get(opts).convert(toToken(item), null);
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(true);
  }
}
