package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnStartsWith extends FnContains {
  @Override
  boolean test(final byte[] value, final byte[] substring, final Collation collation)
      throws QueryException {
    return collation == null ? Token.startsWith(value, substring) :
      collation.startsWith(value, substring, info);
  }
}
