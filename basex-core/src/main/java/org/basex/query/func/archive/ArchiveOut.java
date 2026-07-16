package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.archive.ArchiveText.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Archive writer.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class ArchiveOut implements Closeable {
  /**
   * Returns a new instance of an archive writer.
   * @param format archive format
   * @param info input info (can be {@code null})
   * @param os output stream
   * @return writer
   * @throws QueryException query exception
   */
  static ArchiveOut get(final String format, final InputInfo info, final OutputStream os)
      throws QueryException {
    try {
      if(format.equals(ZIP)) return new ZIPOut(os);
      if(format.equals(GZIP)) return new GZIPOut(os);
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
    throw ARCHIVE_FORMAT.get(info);
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

  /**
   * Copies the specified entry, reading its body from the given input stream.
   * Entry attributes (name, time, comment, compression method) are preserved.
   * @param entry source entry
   * @param is input stream with the entry's body
   * @throws IOException I/O exception
   */
  public abstract void write(ZipEntry entry, InputStream is) throws IOException;

  /**
   * Writes the specified entry. The contents of lazy binaries are read in a single pass.
   * @param entry zip entry
   * @param bin binary data
   * @param info input info (can be {@code null})
   * @param qc query context
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public abstract void write(ZipEntry entry, Bin bin, InputInfo info, QueryContext qc)
    throws IOException, QueryException;

  @Override
  public abstract void close();

  /**
   * Writes data from the specified binary to the specified output stream.
   * @param bin binary item
   * @param out output stream
   * @param info input info (can be {@code null})
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  static void writeBin(final Bin bin, final OutputStream out, final InputInfo info)
      throws IOException, QueryException {
    // keep output stream open
    try(BufferInput bi = bin.input(info)) {
      bi.transferTo(out);
    }
  }
}
