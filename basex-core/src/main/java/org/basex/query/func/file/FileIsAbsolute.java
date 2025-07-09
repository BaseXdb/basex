package org.basex.query.func.file;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileIsAbsolute extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws QueryException {
    return Bln.get(toPath(arg(0), qc).isAbsolute());
  }
}
