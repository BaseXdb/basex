package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FtNormalize extends FtTokenize {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    // overwrite implementation of superclass
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] token : tokens(qc, true)) tb.add(token);
    return Str.get(tb.finish());
  }
}
