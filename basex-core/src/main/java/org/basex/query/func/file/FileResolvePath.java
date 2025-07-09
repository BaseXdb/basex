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
    final String file = toStringOrNull(arg(1), qc);
    if(file != null) {
      Path base = toPath(file);
      if(!base.isAbsolute()) throw FILE_IS_RELATIVE_X.get(info, base);
      if(!endsWith(file, '/') && !endsWith(file, '\\')) base = base.getParent();
      abs = base.resolve(path).normalize();
    } else {
      abs = absolute(path);
    }
    return get(abs, Files.isDirectory(abs));
  }
}
