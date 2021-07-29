package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.func.archive.*;
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
    final boolean full = exprs.length == 2, archive = full && exprs[1] instanceof ArchiveCreate;
    final Bin bin = archive ? null : toBin(exprs[1], qc);
    final long off = full ? 0 : toLong(exprs[2], qc);

    if(full) {
      // write full file
      try(BufferOutput out = new BufferOutput(new FileOutputStream(path.toFile(), append))) {
        if(archive) {
          // optimization: stream created archives
          ((ArchiveCreate) exprs[1]).create(out, qc);
        } else {
          try(BufferInput in = bin.input(info)) {
            for(int b; (b = in.read()) != -1;) out.write(b);
          }
        }
      }
    } else {
      // write file chunk
      try(RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
        final long dlen = raf.length();
        if(off < 0 || off > dlen) throw FILE_OUT_OF_RANGE_X_X.get(info, off, dlen);
        raf.seek(off);
        raf.write(bin.binary(info));
      }
    }
  }
}
