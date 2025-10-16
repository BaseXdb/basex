package org.basex.query.func.file;

import static org.basex.query.QueryError.*;
import static org.basex.util.Strings.*;

import java.nio.file.*;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileResolvePath extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws QueryException {
    final Path path = toPath(toString(arg(0), qc)), abs;
    final String base = toStringOrNull(arg(1), qc);
    if(base != null) {
      Path bs = toPath(base);
      if(!bs.isAbsolute()) throw FILE_IS_RELATIVE_X.get(info, bs);
      if(!endsWith(base, '/') && !endsWith(base, '\\')) bs = bs.getParent();
      abs = bs.resolve(path).normalize();
    } else {
      abs = absolute(path);
    }
    return get(abs, Files.isDirectory(abs));
  }
}
