package org.basex.query.func.file;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileExists extends FileFn {
  @Override
  public Bln item(final QueryContext qc) throws QueryException {
    return Bln.get(Files.exists(toPath(arg(0), qc)));
  }
}
