package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Archive writer.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class ArchiveOut implements Closeable {
  /** Output. */
  final ArrayOutput ao = new ArrayOutput();
  /** Buffer. */
  final byte[] data = new byte[IO.BLOCKSIZE];

  /**
   * Returns a new instance of an archive writer.
   * @param format archive format
   * @param ii input info
   * @return writer
   * @throws QueryException query exception
   */
  static ArchiveOut get(final String format, final InputInfo ii) throws QueryException {
    try {
      if(format.equals(ZIP)) return new ZIPOut();
      if(format.equals(GZIP)) return new GZIPOut();
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(ii, ex);
    }
    throw ARCHIVE_FORMAT.get(ii);
  }

  /**
   * Sets the compression level.
   * @param level level
   */
  public abstract void level(int level);

  /**
   * Writes data from the specified archive.
   * @param in input archive
   * @throws IOException I/O exception
   */
  public abstract void write(ArchiveIn in) throws IOException;

  /**
   * Writes the specified entry.
   * @param entry zip entry
   * @param value value to be written
   * @throws IOException I/O exception
   */
  public abstract void write(ZipEntry entry, byte[] value) throws IOException;

  @Override
  public abstract void close();

  /**
   * Returns the output as byte array and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  final byte[] finish() {
    close();
    return ao.finish();
  }

  /**
   * Writes data from the specified archive to the specified output stream.
   * @param in input archive
   * @param out output stream
   * @throws IOException I/O exception
   */
  public final void write(final ArchiveIn in, final OutputStream out) throws IOException {
    for(int c; (c = in.read(data)) != -1;) out.write(data, 0, c);
  }
}
