package org.basex.query.func.file;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileCurrentDir extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws QueryException {
    return get(absolute(toPath(".", qc)), true);
  }
}
