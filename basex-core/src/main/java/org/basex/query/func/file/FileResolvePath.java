package org.basex.query.func.file;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FileResolvePath extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws QueryException {
    final Path path = toPath(toToken(exprs[0], qc)), abs;
    if(exprs.length < 2) {
      abs = absolute(path);
    } else {
      final byte[] file = toToken(exprs[1], qc);
      Path base = toPath(file);
      if(!base.isAbsolute()) throw FILE_IS_RELATIVE_X.get(info, base);
      if(!endsWith(file, '/') && !endsWith(file, '\\')) base = base.getParent();
      abs = base.resolve(path);
    }
    return get(abs, Files.isDirectory(abs));
  }
}
