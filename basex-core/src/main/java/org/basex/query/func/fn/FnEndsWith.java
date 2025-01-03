package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnEndsWith extends FnContains {
  @Override
  boolean test(final byte[] value, final byte[] substring, final Collation collation)
      throws QueryException {
    return collation == null ? Token.endsWith(value, substring) :
      collation.endsWith(value, substring, info);
  }
}
