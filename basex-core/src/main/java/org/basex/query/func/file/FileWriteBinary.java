package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.func.archive.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
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
  final void write(final boolean append, final QueryContext qc) throws QueryException, IOException {
    final Path path = toParent(toPath(arg(0), qc));
    if(defined(2)) {
      // write file chunk
      final Bin binary = toBin(arg(1), qc);
      final long offset = toLong(arg(2), qc);

      try(RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
        final long length = raf.length();
        if(offset < 0 || offset > length) throw FILE_OUT_OF_RANGE_X_X.get(info, offset, length);
        raf.seek(offset);
        raf.write(binary.binary(info));
      }
    } else {
      // write full file
      try(BufferOutput out = BufferOutput.get(new FileOutputStream(path.toFile(), append))) {
        if(arg(1).getClass() == ArchiveCreate.class) {
          // optimization: stream archive to disk (no support for ArchiveCreateFrom)
          ((ArchiveCreate) arg(1)).create(out, qc);
        } else {
          IO.write(toBin(arg(1), qc).input(info), out);
        }
      }
    }
  }
}
