package org.basex.query.func.archive;

import java.io.*;
import java.util.zip.*;

import org.basex.io.out.*;
import org.basex.util.*;

/**
 * ZIP output.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ZIPOut extends ArchiveOut {
  /** ZIP output stream. */
  private final ZipOutputStream zos;
  /** Stored flag. */
  private boolean stored;

  /**
   * Writing constructor.
   */
  ZIPOut() {
    zos = new ZipOutputStream(ao);
  }

  @Override
  public void level(final int level) {
    stored = level == 0;
    zos.setMethod(stored ? ZipEntry.STORED : ZipEntry.DEFLATED);
    zos.setLevel(level);
  }

  @Override
  public void write(final ArchiveIn in) throws IOException {
    final ZipEntry zi = in.entry(), zo = new ZipEntry(zi.getName());
    zo.setTime(zi.getTime());
    zo.setComment(zi.getComment());

    if(zi.getMethod() == ZipEntry.STORED) {
      stored = true;
      final ArrayOutput out = new ArrayOutput();
      write(in, out);
      write(zo, out.finish());
    } else {
      level(-1);
      zos.putNextEntry(zo);
      write(in, zos);
      zos.closeEntry();
    }
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
  public void close() {
    try { zos.close(); } catch(final IOException ex) { Util.debug(ex); }
  }
}
