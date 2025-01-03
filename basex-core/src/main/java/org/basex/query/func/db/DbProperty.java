package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbProperty extends DbPropertyMap {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final String name = toString(arg(1), qc);

    for(final MetaProp prop : MetaProp.ENUMS) {
      if(prop.name().equalsIgnoreCase(name)) return item(prop.value(data.meta));
    }
    throw DB_PROPERTY_X.get(info, name);
  }
}
