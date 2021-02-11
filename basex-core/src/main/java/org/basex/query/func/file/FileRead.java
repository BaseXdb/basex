package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.nio.file.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class FileRead extends FileFn {
  /**
   * Reads the contents of a file.
   * @param qc query context
   * @return string
   * @throws QueryException query exception
   */
  final StrLazy text(final QueryContext qc) throws QueryException {
    final Path path = toPath(0, qc);
    final String encoding = toEncodingOrNull(1, FILE_UNKNOWN_ENCODING_X, qc);
    final boolean validate = exprs.length < 3 || !toBoolean(exprs[2], qc);
    if(!Files.exists(path)) throw FILE_NOT_FOUND_X.get(info, path.toAbsolutePath());
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path.toAbsolutePath());

    return new StrLazy(new IOFile(path.toFile()), encoding, FILE_IO_ERROR_X, validate);
  }
}
