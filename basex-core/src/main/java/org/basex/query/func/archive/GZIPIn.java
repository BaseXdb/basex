package org.basex.query.func.archive;

import java.io.*;
import java.util.zip.*;

import org.basex.util.*;

/**
 * GZIP reader.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class GZIPIn extends ArchiveIn {
  /** GZIP input stream. */
  private final GZIPInputStream zis;
  /** Flag. */
  private boolean more;

  /**
   * Constructor.
   * @param is input stream
   * @throws IOException I/O exception
   */
  public GZIPIn(final InputStream is) throws IOException {
    zis = new GZIPInputStream(is);
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
  public int read(final byte[] d) throws IOException {
    return zis.read(d);
  }

  @Override
  public String format() {
    return FNArchive.GZIP;
  }

  @Override
  public void close() {
    try { zis.close(); } catch(final IOException ex) { Util.debug(ex); }
  }
}
