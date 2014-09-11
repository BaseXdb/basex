package org.basex.query.func.file;

import static org.basex.query.util.Err.*;

import java.nio.file.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class FileRead extends FileFn {
  /**
   * Reads the contents of a file.
   * @param qc query context
   * @return string
   * @throws QueryException query exception
   */
  final StrStream text(final QueryContext qc) throws QueryException {
    final Path path = toPath(0, qc);
    final String enc = toEncoding(1, FILE_UNKNOWN_ENCODING_X, qc);
    if(!Files.exists(path)) throw FILE_NOT_FOUND_X.get(info, path);
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path);
    return new StrStream(new IOFile(path.toFile()), enc, FILE_IO_ERROR_X, qc);
  }
}
