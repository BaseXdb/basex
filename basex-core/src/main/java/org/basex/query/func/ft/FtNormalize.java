package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class FtNormalize extends FtTokenize {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] token : tokens(qc, true)) tb.add(token);
    return Str.get(tb.finish());
  }
}
