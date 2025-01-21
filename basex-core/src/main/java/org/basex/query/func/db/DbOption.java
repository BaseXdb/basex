package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbOption extends DbOptionMap {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] name = toToken(arg(0), qc);

    final Object value = qc.context.option(Token.string(name));
    if(value == null) throw DB_OPTION_X.get(info, name);
    return item(value);
  }
}
