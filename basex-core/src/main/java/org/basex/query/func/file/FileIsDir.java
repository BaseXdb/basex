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
public final class FileIsDir extends FileFn {
  @Override
  public Bln item(final QueryContext qc) throws QueryException {
    return Bln.get(Files.isDirectory(toPath(arg(0), qc)));
  }
}
