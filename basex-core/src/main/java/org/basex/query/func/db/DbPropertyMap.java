package org.basex.query.func.db;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbPropertyMap extends DbAccessFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);

    final MapBuilder map = new MapBuilder();
    for(final MetaProp prop : MetaProp.values()) {
      map.put(prop.name().toLowerCase(Locale.ENGLISH), DbOptionMap.toValue(prop.value(data.meta)));
    }
    return map.map();
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    return cc.dynamicContext() && values(true, cc) ? value(cc.qc) : compileData(cc);
  }
}
