package org.basex.query.func.fn;

import org.basex.build.json.JsonOptions.*;
import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnParseJson extends ParseJson {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return parse(qc);
  }

  @Override
  protected final JsonFormat format() {
    return JsonFormat.W3;
  }
}
