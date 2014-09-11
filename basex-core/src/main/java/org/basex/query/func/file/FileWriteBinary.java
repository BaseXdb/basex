package org.basex.query.func.file;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.nio.file.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class FileWriteBinary extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    return write(false, qc);
  }

  /**
   * Writes items to a file.
   * @param append append flag
   * @param qc query context
   * @return true if file was successfully written
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final synchronized Item write(final boolean append, final QueryContext qc)
      throws QueryException, IOException {

    final Path path = checkParentDir(toPath(0, qc));
    final Bin bin = toBin(exprs[1], qc);
    final long off = exprs.length > 2 ? toLong(exprs[2], qc) : 0;

    // write full file
    if(exprs.length == 2) {
      try(final BufferOutput out = new BufferOutput(new FileOutputStream(path.toFile(), append));
          final InputStream is = bin.input(info)) {
        for(int i; (i = is.read()) != -1;)  out.write(i);
      }
    } else {
      // write file chunk
      try(final RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
        final long dlen = raf.length();
        if(off < 0 || off > dlen) throw FILE_OUT_OF_RANGE_X_X.get(info, off, dlen);
        raf.seek(off);
        raf.write(bin.binary(info));
      }
    }
    return null;
  }
}
