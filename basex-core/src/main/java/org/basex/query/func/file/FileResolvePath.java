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
    final String string = toString(arg(0), qc), base = toStringOrNull(arg(1), qc);
    final Path path = toPath(string, qc), abs;
    if(base != null) {
      Path bs = toPath(base, qc);
      if(!bs.isAbsolute()) throw FILE_IS_RELATIVE_X.get(info, bs);
      if(!separator(base)) bs = bs.getParent();
      abs = bs.resolve(path).normalize();
    } else {
      abs = absolute(path);
    }
    return get(abs, separator(string) || Files.isDirectory(abs));
  }

  /**
   * Checks if a path ends with a directory separator.
   * @param path path
   * @return result of check
   */
  private static boolean separator(final String path) {
    return endsWith(path, '/') || endsWith(path, '\\');
  }
}
