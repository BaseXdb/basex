package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class FileWriteBinary extends FileFn {
  @Override
  public Item item(final QueryContext qc) throws IOException, QueryException {
    write(false, qc);
    return Empty.VALUE;
  }

  /**
   * Writes items to a file.
   * @param append append flag
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final synchronized void write(final boolean append, final QueryContext qc)
      throws QueryException, IOException {

    final Path path = checkParentDir(toPath(0, qc));
    final Bin bin = toBin(exprs[1], qc);

    // write full file
    if(exprs.length == 2) {
      try(BufferOutput out = new BufferOutput(new FileOutputStream(path.toFile(), append));
          BufferInput in = bin.input(info)) {
        for(int b; (b = in.read()) != -1;) out.write(b);
      }
    } else {
      // write file chunk
      final long off = toLong(exprs[2], qc);
      try(RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
        final long dlen = raf.length();
        if(off < 0 || off > dlen) throw FILE_OUT_OF_RANGE_X_X.get(info, off, dlen);
        raf.seek(off);
        raf.write(bin.binary(info));
      }
    }
  }
}
