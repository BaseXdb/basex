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
 * @author BaseX Team 2005-23, BSD License
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

    final Path path = toParent(toPath(0, qc));
    if(exprs.length < 3) {
      // write full file
      try(BufferOutput out = new BufferOutput(new FileOutputStream(path.toFile(), append))) {
        if(exprs[1].getClass() == ArchiveCreate.class) {
          // optimization: stream archive to disk (no support for ArchiveCreateFrom)
          ((ArchiveCreate) exprs[1]).create(out, qc);
        } else {
          final Bin value = toBin(exprs[1], qc);
          try(BufferInput bi = value.input(info)) {
            for(int b; (b = bi.read()) != -1;) out.write(b);
          }
        }
      }
    } else {
      // write file chunk
      final Bin binary = toBin(exprs[1], qc);
      final long offset = toLong(exprs[2], qc);

      try(RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
        final long length = raf.length();
        if(offset < 0 || offset > length) throw FILE_OUT_OF_RANGE_X_X.get(info, offset, length);
        raf.seek(offset);
        raf.write(binary.binary(info));
      }
    }
  }
}
