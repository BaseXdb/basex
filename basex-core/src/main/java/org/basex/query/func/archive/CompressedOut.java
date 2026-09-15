package org.basex.query.func.archive;

import java.io.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Writer for compressed single files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class CompressedOut extends ArchiveOut {
  /** Compressing output stream. */
  private final OutputStream zos;

  /**
   * Writing constructor.
   * @param os output stream
   * @param compr compression
   * @throws IOException I/O exception
   */
  CompressedOut(final OutputStream os, final Compression compr) throws IOException {
    zos = compr.output(os);
  }

  @Override
  public void level(final int level) {
    // ignore compression level
  }

  @Override
  public void write(final ArchiveIn in) throws IOException {
    in.transferTo(zos);
  }

  @Override
  public void write(final ZipEntry entry, final byte[] value) throws IOException {
    zos.write(value);
  }

  @Override
  public void write(final ZipEntry entry, final InputStream is) throws IOException {
    is.transferTo(zos);
  }

  @Override
  public void write(final ZipEntry entry, final Bin bin, final InputInfo info,
      final QueryContext qc) throws IOException, QueryException {
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
