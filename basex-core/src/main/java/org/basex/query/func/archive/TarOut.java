package org.basex.query.func.archive;

import java.io.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * TAR output.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TarOut extends ArchiveOut {
  /** TAR output stream. */
  private final TarOutputStream tos;

  /**
   * Writing constructor.
   * @param os output stream
   * @param gzip compress the archive with GZIP
   * @throws IOException I/O exception
   */
  TarOut(final OutputStream os, final boolean gzip) throws IOException {
    tos = new TarOutputStream(gzip ? new GZIPOutputStream(os) : os);
  }

  @Override
  public void level(final int level) {
    // ignore compression level
  }

  @Override
  public void write(final ArchiveIn in) throws IOException {
    write(in.entry(), in);
  }

  @Override
  public void write(final ZipEntry entry, final InputStream is) throws IOException {
    write(entry, is, entry.getSize());
  }

  @Override
  public void write(final ZipEntry entry, final byte[] value) throws IOException {
    tos.putNextEntry(new TarEntry(entry.getName(), value.length, entry.getTime()));
    tos.write(value);
    tos.closeEntry();
  }

  @Override
  public void write(final ZipEntry entry, final Bin bin, final InputInfo info,
      final QueryContext qc) throws IOException, QueryException {
    try(BufferInput bi = bin.input(info)) {
      final long size = bi.length();
      if(size != -1) {
        write(entry, bi, size);
      } else {
        // the header requires the size in advance: spool contents of unknown size
        final IO io = SpillOutput.read(bi, qc);
        try(InputStream is = io.inputStream()) {
          write(entry, is, io.length());
        }
      }
    }
  }

  /**
   * Writes an entry.
   * @param entry entry
   * @param is input stream with the entry's body
   * @param size size
   * @throws IOException I/O exception
   */
  private void write(final ZipEntry entry, final InputStream is, final long size)
      throws IOException {
    tos.putNextEntry(new TarEntry(entry.getName(), size, entry.getTime()));
    is.transferTo(tos);
    tos.closeEntry();
  }

  @Override
  public void close() {
    try {
      tos.close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }
}
