package org.basex.query.util.archive;

import java.io.*;
import java.util.zip.*;

import org.basex.util.*;

/**
 * ZIP output.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ZIPOut extends ArchiveOut {
  /** ZIP output stream. */
  private final ZipOutputStream zos;

  /**
   * Writing constructor.
   */
  public ZIPOut() {
    zos = new ZipOutputStream(ao);
  }

  @Override
  public void level(final int l) {
    zos.setLevel(l);
  }

  @Override
  public void write(final ArchiveIn in) throws IOException {
    final ZipEntry zi = in.entry();
    final ZipEntry zo = new ZipEntry(zi.getName());
    zo.setTime(zi.getTime());
    zo.setComment(zi.getComment());
    zos.putNextEntry(zo);
    for(int c; (c = in.read(data)) != -1;) zos.write(data, 0, c);
    zos.closeEntry();
  }

  @Override
  public void write(final ZipEntry entry, final byte[] value) throws IOException {
    zos.putNextEntry(entry);
    zos.write(value);
    zos.closeEntry();
  }

  @Override
  public void close() {
    try { zos.close(); } catch(final IOException ex) { Util.debug(ex); }
  }
}
