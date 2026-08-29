package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileName extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final Path path = toRawPath(arg(0), qc).getFileName();
    return path == null ? Str.EMPTY : Str.get(path.toString());
  }
}
