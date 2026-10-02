package org.basex.query.func.file;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
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
      // write file chunk
      final byte[] value = toBin(arg(1), qc).binary(info);
      try(FileChannel fc = FileChannel.open(path, StandardOpenOption.CREATE,
          StandardOpenOption.WRITE)) {
        final long length = fc.size();
        if(offset < 0 || offset > length) throw FILE_OUT_OF_RANGE_X_X.get(info, offset, length);
        final ByteBuffer buffer = ByteBuffer.wrap(value);
        for(long pos = offset; buffer.hasRemaining();) pos += fc.write(buffer, pos);
      }
    } else if(arg(1) instanceof final ArchiveCreate ac) {
      // optimization: stream archive to disk (archive:create, archive:create-from)
      try(BufferOutput out = BufferOutput.get(output(path, append))) {
        ac.create(out, new IOFile(path), qc);
      }
    } else {
      // default case: no archive, no offset
      final Bin value = toBin(arg(1), qc);
      cacheSource(value, path);
      try(OutputStream out = output(path, append)) {
        IO.write(value.input(info), out);
      }
    }
  }
}
