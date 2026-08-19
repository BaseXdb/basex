package org.basex.query.func.json;

import static org.basex.query.QueryError.*;

import org.basex.build.json.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonSerialize extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final JsonSerialOptions options = options(1, JsonSerialOptions::new, qc);
    return Str.get(serialize(input, FnXmlToJson.options(options), INVALIDOPTION_X, qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(1, JsonSerialOptions::new, cc);
    return this;
  }
}
