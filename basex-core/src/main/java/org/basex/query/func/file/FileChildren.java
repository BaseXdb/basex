package org.basex.query.func.file;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FileChildren extends FileList {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return paths(false, qc);
  }
}
