package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
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
public final class FileReadText extends FileReadFn {
  @Override
  public Value eval(final QueryContext qc) throws QueryException, IOException {
    final Path path = toPath(arg(0), qc);

    final ReadTextOptions options = options(path, qc);
    final String encoding = options.get(ReadTextOptions.ENCODING);
    final boolean fallback = options.get(ReadTextOptions.FALLBACK) || toBooleanOrFalse(arg(2), qc);
    final boolean normalize = options.get(ReadTextOptions.NORMALIZE_NEWLINES);
    return new StrLazy(new IOFile(path), encoding, FILE_IO_ERROR_X, fallback, normalize, null);
  }
}
