package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FileParent extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws QueryException {
    final Path parent = absolute(toPath(0, qc)).getParent();
    return parent == null ? Empty.VALUE : get(parent, true);
  }
}
