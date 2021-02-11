package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.io.*;
import org.basex.io.random.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FileReadBinary extends FileFn {
  @Override
  public B64 item(final QueryContext qc) throws QueryException, IOException {
    final Path path = toPath(0, qc);
    final long off = exprs.length > 1 ? toLong(exprs[1], qc) : 0;
    long len = exprs.length > 2 ? toLong(exprs[2], qc) : 0;

    if(!Files.exists(path)) throw FILE_NOT_FOUND_X.get(info, path.toAbsolutePath());
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path.toAbsolutePath());

    // read full file
    if(exprs.length == 1) return new B64Lazy(new IOFile(path.toFile()), FILE_IO_ERROR_X);

    // read file chunk
    try(DataAccess da = new DataAccess(new IOFile(path.toFile()))) {
      final long dlen = da.length();
      if(exprs.length == 2) len = dlen - off;
      if(off < 0 || off > dlen || len < 0 || off + len > dlen)
        throw FILE_OUT_OF_RANGE_X_X.get(info, off, off + len);

      da.cursor(off);
      return B64.get(da.readBytes((int) len));
    }
  }
}
