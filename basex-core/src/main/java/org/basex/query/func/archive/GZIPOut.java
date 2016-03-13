package org.basex.query.func.archive;

import java.io.*;
import java.util.zip.*;

import org.basex.util.*;

/**
 * GZIP output.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class GZIPOut extends ArchiveOut {
  /** ZIP output stream. */
  private final GZIPOutputStream zos;

  /**
   * Writing constructor.
   * @throws IOException I/O exception
   */
  GZIPOut() throws IOException {
    zos = new GZIPOutputStream(ao);
  }

  @Override
  public void level(final int l) {
    // ignore compression level
  }

  @Override
  public void write(final ArchiveIn in) throws IOException {
    for(int c; (c = in.read(data)) != -1;) zos.write(data, 0, c);
  }

  @Override
  public void write(final ZipEntry entry, final byte[] value) throws IOException {
    zos.write(value);
  }

  @Override
  public void close() {
    try { zos.close(); } catch(final IOException ex) { Util.debug(ex); }
  }
}
