package org.basex.query.func.archive;

import java.io.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * Reader for compressed single files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class CompressedIn extends ArchiveIn {
  /** Decompressed input stream. */
  private final InputStream zis;
  /** Compression. */
  private final Compression compr;
  /** Flag. */
  private boolean more;

  /**
   * Constructor.
   * @param is decompressed input stream
   * @param compr compression
   */
  CompressedIn(final InputStream is, final Compression compr) {
    zis = is;
    this.compr = compr;
  }

  @Override
  public boolean more() {
    return more ^= true;
  }

  @Override
  public ZipEntry entry() {
    return new ZipEntry("");
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
    return compr.toString();
  }

  @Override
  public int method() {
    return compr.method;
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
