package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.nio.file.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileReadText extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws QueryException {
    final Path path = toPath(arg(0), qc);
    final String encoding = toEncodingOrNull(arg(1), FILE_UNKNOWN_ENCODING_X, qc);
    final boolean fallback = toBooleanOrFalse(arg(2), qc);

    if(!Files.exists(path)) throw FILE_NOT_FOUND_X.get(info, path.toAbsolutePath());
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path.toAbsolutePath());

    return new StrLazy(new IOFile(path), encoding, FILE_IO_ERROR_X, !fallback);
  }
}
