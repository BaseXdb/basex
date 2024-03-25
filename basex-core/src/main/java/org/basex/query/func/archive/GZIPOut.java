package org.basex.query.func.archive;

import java.io.*;
import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * GZIP output.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class GZIPOut extends ArchiveOut {
  /** ZIP output stream. */
  private final GZIPOutputStream zos;

  /**
   * Writing constructor.
   * @param os output stream
   * @throws IOException I/O exception
   */
  GZIPOut(final OutputStream os) throws IOException {
    zos = new GZIPOutputStream(os);
  }

  @Override
  public void level(final int level) {
    // ignore compression level
  }

  @Override
  public void write(final ArchiveIn in) throws IOException {
    write(in, zos);
  }

  @Override
  public void write(final ZipEntry entry, final byte[] value) throws IOException {
    zos.write(value);
  }

  @Override
  public void write(final ZipEntry entry, final Bin bin, final InputInfo info)
      throws IOException, QueryException {
    writeBin(bin, zos, info);
  }

  @Override
  public void close() {
    try {
      zos.close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }
}
