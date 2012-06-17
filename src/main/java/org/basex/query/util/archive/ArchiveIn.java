package org.basex.query.util.archive;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Archive reader.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class ArchiveIn {
  /** Buffer. */
  private final byte[] data = new byte[IO.BLOCKSIZE];

  /**
   * Returns a new instance of an archive reader.
   * @param bi buffer input
   * @param info input info
   * @return reader
   * @throws QueryException query exception
   */
  public static ArchiveIn get(final BufferInput bi, final InputInfo info)
      throws QueryException {

    try {
      final LookupInput li = new LookupInput(bi);
      if(li.lookup() == 0x50) return new ZIPIn(li);
      if(li.lookup() == 0x1f) return new GZIPIn(li);
    } catch(final IOException ex) {
      try { bi.close(); } catch(final IOException e) { }
      throw ARCH_FAIL.thrw(info, ex);
    }
    throw ARCH_UNKNOWN.thrw(info);
  }

  /**
   * Indicates if the archive contains more entries.
   * @return result of check
   * @throws IOException I/O exception
   */
  public abstract boolean more() throws IOException;

  /**
   * Returns the current entry.
   * @return entry
   */
  public abstract ZipEntry entry();

  /**
   * Returns the name of the archive format.
   * @return name
   */
  public abstract String format();

  /**
   * Reads data from the archive.
   * @param d data buffer
   * @return number of read bytes
   * @throws IOException I/O exception
   */
  public abstract int read(final byte[] d) throws IOException;

  /**
   * Reads the next entry.
   * @return entry
   * @throws IOException I/O exception
   */
  public byte[] read() throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    for(int c; (c = read(data)) != -1;) ao.write(data, 0, c);
    return ao.toArray();
  }

  /**
   * Closes the stream.
   */
  public abstract void close();
}
