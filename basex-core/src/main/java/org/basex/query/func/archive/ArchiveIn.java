package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Archive reader. Behaves as an {@link InputStream} over the body of the current entry —
 * reads return {@code -1} at the end of an entry; calling {@link #more()} advances to the
 * next entry (skipping any remaining body bytes).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class ArchiveIn extends InputStream {
  /**
   * Returns a new instance of an archive reader.
   * @param bi buffer input
   * @param info input info (can be {@code null})
   * @return reader
   * @throws QueryException query exception
   */
  static ArchiveIn get(final BufferInput bi, final InputInfo info) throws QueryException {
    try {
      final LookupInput li = new LookupInput(bi);
      if(li.lookup() == 0x50) return new ZIPIn(li);
      if(li.lookup() == 0x1f) return new GZIPIn(li);
    } catch(final IOException ex) {
      throw ARCHIVE_ERROR_X.get(info, ex);
    }
    throw ARCHIVE_FORMAT.get(info);
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

  @Override
  public abstract void close();
}
