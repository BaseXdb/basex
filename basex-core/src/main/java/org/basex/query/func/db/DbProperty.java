package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbProperty extends DbPropertyMap {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final String key = toString(arg(1), qc);

    for(final MetaProp prop : MetaProp.values()) {
      if(prop.name().equalsIgnoreCase(key)) return DbOptionMap.toValue(prop.value(data.meta));
    }
    final MetaProp similar = Levenshtein.similar(Token.token(key), MetaProp.values(),
        MetaProp::name);
    throw DB_PROPERTY_X.get(info, QueryError.similar(key, similar,
        prop -> prop.name().toLowerCase(Locale.ENGLISH)));
  }
}
