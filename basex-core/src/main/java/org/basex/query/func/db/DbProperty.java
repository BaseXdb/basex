package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbProperty extends DbPropertyMap {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final String key = toString(arg(1), qc);

    for(final MetaProp prop : MetaProp.values()) {
      if(prop.name().equalsIgnoreCase(key)) return item(prop.value(data.meta));
    }
    throw DB_PROPERTY_X.get(info, key);
  }
}
