package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class FtExtract extends FtMark {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return mark(qc, true);
  }
}
