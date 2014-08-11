package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Context functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNContext extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case CURRENT_DATE:      return qc.initDateTime().date;
      case CURRENT_DATETIME:  return qc.initDateTime().dtm;
      case CURRENT_TIME:      return qc.initDateTime().time;
      case IMPLICIT_TIMEZONE: return qc.initDateTime().zone;
      case DEFAULT_COLLATION:
        final Collation coll = sc.collation;
        return Uri.uri(coll == null ? QueryText.COLLATIONURI : coll.uri());
      case STATIC_BASE_URI:
        final Uri uri = sc.baseURI();
        return uri == Uri.EMPTY ? null : uri;
      default: return super.item(qc, ii);
    }
  }
}
