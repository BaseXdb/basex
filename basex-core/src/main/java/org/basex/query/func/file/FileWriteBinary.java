package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.file.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.func.archive.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FileWriteBinary extends FileWriteFn {
  @Override
  public Value eval(final QueryContext qc) throws IOException, QueryException {
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
    final Path path = toTarget(arg(0), qc);
    final Long offset = toLongOrNull(arg(2), qc);
    if(offset != null) {
      final byte[] content = toBin(arg(1), qc).binary(info);
      // write file chunk
      try(RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
        final long length = raf.length();
        if(offset < 0 || offset > length) throw FILE_OUT_OF_RANGE_X_X.get(info, offset, length);
        raf.seek(offset);
        raf.write(content);
      }
    } else if(arg(1) instanceof final ArchiveCreate ac) {
      // optimization: stream archive to disk
      try(BufferOutput out = BufferOutput.get(new FileOutputStream(path.toFile(), append))) {
        ac.create(out, qc);
      }
    } else {
      final Bin content = toBin(arg(1), qc);
      try(BufferOutput out = BufferOutput.get(new FileOutputStream(path.toFile(), append))) {
        IO.write(content.input(info), out);
      }
    }
  }
}
