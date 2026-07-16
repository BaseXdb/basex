package org.basex.query.func.archive;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * ZIP output.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ZIPOut extends ArchiveOut {
  /** ZIP output stream. */
  private final ZipOutputStream zos;
  /** Stored flag. */
  private boolean stored;

  /**
   * Writing constructor.
   * @param os output stream
   */
  ZIPOut(final OutputStream os) {
    zos = new ZipOutputStream(os);
  }

  @Override
  public void level(final int level) {
    stored = level == 0;
    zos.setMethod(stored ? ZipEntry.STORED : ZipEntry.DEFLATED);
    zos.setLevel(level);
  }

  @Override
  public void write(final ArchiveIn in) throws IOException {
    write(in.entry(), in);
  }

  @Override
  public void write(final ZipEntry entry, final InputStream is) throws IOException {
    final ZipEntry zo = new ZipEntry(entry.getName());
    zo.setTime(entry.getTime());
    zo.setComment(entry.getComment());

    if(entry.getMethod() == ZipEntry.STORED && entry.getSize() != -1 && entry.getCrc() != -1) {
      // size and checksum are known in advance: copy stored entry without buffering it
      zo.setMethod(ZipEntry.STORED);
      zo.setSize(entry.getSize());
      zo.setCrc(entry.getCrc());
    } else {
      level(-1);
    }
    zos.putNextEntry(zo);
    is.transferTo(zos);
    zos.closeEntry();
  }

  @Override
  public void write(final ZipEntry entry, final byte[] value) throws IOException {
    if(stored) {
      final CRC32 crc = new CRC32();
      crc.update(value);
      entry.setCrc(crc.getValue());
      entry.setSize(value.length);
    }
    zos.putNextEntry(entry);
    zos.write(value);
    zos.closeEntry();
  }

  @Override
  public void write(final ZipEntry entry, final Bin bin, final InputInfo info,
      final QueryContext qc) throws IOException, QueryException {

    if(stored) {
      // spool the contents to compute checksum and size: avoids reading lazy input twice
      try(SpillOutput so = new SpillOutput(qc)) {
        final CRC32 crc = new CRC32();
        final long size;
        try(BufferInput bi = bin.input(info)) {
          size = bi.transferTo(new CheckedOutputStream(so, crc));
        }
        entry.setCrc(crc.getValue());
        entry.setSize(size);
        zos.putNextEntry(entry);
        writeBin(so.finish(ARCHIVE_ERROR_X), zos, info);
      }
    } else {
      zos.putNextEntry(entry);
      writeBin(bin, zos, info);
    }
    zos.closeEntry();
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
