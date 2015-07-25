package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FileResolvePath extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws QueryException {
    final Path path = toPath(0, qc);
    final Path abs;
    if(exprs.length < 2) {
      abs = absolute(path);
    } else {
      final Path base = toPath(1, qc);
      if(!base.isAbsolute()) throw FILE_IS_RELATIVE_X.get(info, base);
      abs = base.resolve(path);
    }
    return get(abs, Files.isDirectory(abs));
  }
}
