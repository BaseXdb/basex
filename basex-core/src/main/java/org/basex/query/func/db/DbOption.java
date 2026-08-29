package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbOption extends DbOptionMap {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] key = toToken(arg(0), qc);

    final Object value = qc.context.option(Token.string(key));
    if(value == null) throw DB_OPTION_X.get(info, key);
    return item(value);
  }
}
