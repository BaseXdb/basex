package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.io.*;
import org.basex.io.random.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FileReadBinary extends FileFn {
  @Override
  public Value eval(final QueryContext qc) throws QueryException, IOException {
    final Path path = toPath(arg(0), qc);
    final Long offset = toLongOrNull(arg(1), qc);
    final Long length = toLongOrNull(arg(2), qc);
    if(!Files.exists(path)) throw FILE_NOT_FOUND_X.get(info, path.toAbsolutePath());
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path.toAbsolutePath());

    // read full file
    final long off = offset != null ? offset : 0;
    long len = length != null ? length : Long.MAX_VALUE;
    if(off == 0 && len == Long.MAX_VALUE) {
      return new B64Lazy(new IOFile(path), FILE_IO_ERROR_X);
    }

    // read chunk
    try(DataAccess da = new DataAccess(new IOFile(path))) {
      final long dlen = da.length();
      if(len == Long.MAX_VALUE) len = dlen - off;
      if(off < 0 || off > dlen || len < 0 || off + len > dlen) {
        throw FILE_OUT_OF_RANGE_X_X.get(info, off, off + len);
      }
      da.cursor(off);
      return B64.get(da.readBytes((int) len));
    }
  }
}
