package org.basex.query.func.archive;

import static org.basex.query.func.archive.ArchiveText.*;
import java.io.*;
import java.util.zip.*;

import org.basex.util.*;

/**
 * GZIP reader.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class GZIPIn extends ArchiveIn {
  /** Decompressed input stream. */
  private final InputStream zis;
  /** Flag. */
  private boolean more;

  /**
   * Constructor.
   * @param is decompressed input stream
   */
  GZIPIn(final InputStream is) {
    zis = is;
  }

  @Override
  public boolean more() {
    return more ^= true;
  }

  @Override
  public ZipEntry entry() {
    final ZipEntry ze = new ZipEntry("");
    ze.setMethod(ZipEntry.DEFLATED);
    return ze;
  }

  @Override
  public int read() throws IOException {
    return zis.read();
  }

  @Override
  public int read(final byte[] d, final int off, final int len) throws IOException {
    return zis.read(d, off, len);
  }

  @Override
  public String format() {
    return GZIP;
  }

  @Override
  public int method() {
    return ZipEntry.DEFLATED;
  }

  @Override
  public void close() {
    try {
      zis.close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }
}
