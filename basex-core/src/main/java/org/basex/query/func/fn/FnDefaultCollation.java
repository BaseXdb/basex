package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDefaultCollation extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) {
    final Collation coll = sc().collation;
    return Str.get(coll == null ? QueryText.COLLATION_URI : coll.uri());
  }
}
