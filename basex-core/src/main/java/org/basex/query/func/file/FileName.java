package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileName extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Path path = toPath(arg(0), qc).getFileName();
    return path == null ? Str.EMPTY : Str.get(path.toString());
  }
}
