package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.query.*;

/**
 * Functions for reading files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class FileReadFn extends FileFn {
  /**
   * Returns the options and checks the file path.
   * @param path file path
   * @param qc query context
   * @return options
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final ReadTextOptions options(final Path path, final QueryContext qc)
      throws QueryException, IOException {
    final ReadTextOptions options = toOptions(qc);
    toEncodingOrNull(options.get(ReadTextOptions.ENCODING), FILE_UNKNOWN_ENCODING_X);
    checkReadable(path);
    return options;
  }
}
