package org.basex.query.func.file;

import java.io.*;
import java.nio.file.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileDelete extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws QueryException, IOException {
    final Path path = toPath(arg(0), qc);
    final boolean recursive = toBooleanOrFalse(arg(1), qc);

    if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
      if(recursive) {
        IOFile.delete(path, qc);
      } else {
        IOFile.delete(path);
      }
    }
    return Empty.VALUE;
  }
}
