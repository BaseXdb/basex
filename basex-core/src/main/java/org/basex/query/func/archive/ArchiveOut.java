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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class ArchiveOut implements AutoCloseable {
  /** Output. */
  final ArrayOutput ao = new ArrayOutput();
  /** Buffer. */
  final byte[] data = new byte[IO.BLOCKSIZE];

  /**
   * Returns a new instance of an archive writer.
   * @param format archive format
   * @param info input info
   * @return writer
   * @throws QueryException query exception
   */
  public static ArchiveOut get(final String format, final InputInfo info) throws QueryException {
    try {
      if(format.equals(ZIP)) return new ZIPOut();
      if(format.equals(GZIP)) return new GZIPOut();
    } catch(final IOException ex) {
      throw ARCH_FAIL_X.get(info, ex);
    }
    throw ARCH_UNKNOWN.get(info);
  }

  /**
   * Sets the compression level.
   * @param l level
   */
  public abstract void level(final int l);

  /**
   * Writes data from the specified archive.
   * @param in input archive
   * @throws IOException I/O exception
   */
  public abstract void write(final ArchiveIn in) throws IOException;

  /**
   * Writes the specified entry.
   * @param entry zip entry
   * @param value value to be written
   * @throws IOException I/O exception
   */
  public abstract void write(final ZipEntry entry, final byte[] value) throws IOException;

  @Override
  public abstract void close();

  /**
   * Returns the output as byte array.
   * @return byte array
   */
  public final byte[] toArray() {
    return ao.toArray();
  }
}
