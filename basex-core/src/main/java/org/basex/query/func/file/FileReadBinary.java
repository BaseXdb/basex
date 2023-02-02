package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.io.*;
import org.basex.io.random.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FileReadBinary extends FileFn {
  @Override
  public B64 item(final QueryContext qc) throws QueryException, IOException {
    final int el = exprs.length;
    final Path path = toPath(0, qc);
    final Item offset = el > 1 ? exprs[1].atomItem(qc, info) : Empty.VALUE;
    final Item length = el > 2 ? exprs[2].atomItem(qc, info) : Empty.VALUE;
    final long off = offset != Empty.VALUE ? toLong(offset) : 0;
    long len = length != Empty.VALUE ? toLong(length) : Long.MAX_VALUE;

    if(!Files.exists(path)) throw FILE_NOT_FOUND_X.get(info, path.toAbsolutePath());
    if(Files.isDirectory(path)) throw FILE_IS_DIR_X.get(info, path.toAbsolutePath());

    // read full file
    if(off == 0 && len == Long.MAX_VALUE) {
      return new B64Lazy(new IOFile(path.toFile()), FILE_IO_ERROR_X);
    }

    // read chunk
    try(DataAccess da = new DataAccess(new IOFile(path.toFile()))) {
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
