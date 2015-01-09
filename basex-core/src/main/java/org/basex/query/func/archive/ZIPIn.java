package org.basex.query.func.archive;

import static org.basex.query.func.archive.ArchiveText.*;
import java.io.*;
import java.util.zip.*;

import org.basex.util.*;

/**
 * ZIP input.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class ZIPIn extends ArchiveIn {
  /** ZIP input stream. */
  private final ZipInputStream zis;
  /** Current entry. */
  private ZipEntry ze;

  /**
   * Constructor.
   * @param is input stream
   */
  ZIPIn(final InputStream is) {
    zis = new ZipInputStream(is);
  }

  @Override
  public boolean more() throws IOException {
    ze = zis.getNextEntry();
    return ze != null;
  }

  @Override
  public ZipEntry entry() {
    return ze;
  }

  @Override
  public int read(final byte[] d) throws IOException {
    return zis.read(d);
  }

  @Override
  public String format() {
    return ZIP;
  }

  @Override
  public void close() {
    try { zis.close(); } catch(final IOException ex) { Util.debug(ex); }
  }
}
