package org.basex.query.util.archive;

import java.io.*;
import java.util.zip.*;

import org.basex.util.*;

/**
 * GZIP output.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class GZIPOut extends ArchiveOut {
  /** ZIP output stream. */
  private final GZIPOutputStream zos;

  /**
   * Writing constructor.
   * @throws IOException I/O exception
   */
  public GZIPOut() throws IOException {
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
  public void write(final ZipEntry entry, final byte[] val) throws IOException {
    zos.write(val);
  }

  @Override
  public void close() {
    try { zos.close(); } catch(final IOException ex) { Util.debug(ex); }
  }
}
