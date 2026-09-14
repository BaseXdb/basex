package org.basex.query.func.archive;

import static org.basex.query.func.archive.ArchiveText.*;

import java.io.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.util.*;

/**
 * TAR input.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TarIn extends ArchiveIn {
  /** TAR input stream. */
  private final TarInputStream tis;
  /** Compression method of the archive. */
  private final int method;
  /** Current entry (can be {@code null}). */
  private ZipEntry ze;

  /**
   * Constructor.
   * @param is input stream (decompressed)
   * @param method compression method of the archive
   */
  TarIn(final InputStream is, final int method) {
    tis = new TarInputStream(is);
    this.method = method;
  }

  @Override
  public boolean more() throws IOException {
    // skip links, devices and other special entries
    TarEntry te;
    do te = tis.getNextEntry(); while(te != null && !te.isFile() && !te.isDirectory());
    if(te == null) {
      ze = null;
      return false;
    }
    ze = new ZipEntry(te.getName());
    ze.setSize(te.getSize());
    final long time = te.getTime();
    if(time != -1) ze.setTime(time);
    return true;
  }

  @Override
  public ZipEntry entry() {
    return ze;
  }

  @Override
  public int read() throws IOException {
    return tis.read();
  }

  @Override
  public int read(final byte[] d, final int off, final int len) throws IOException {
    return tis.read(d, off, len);
  }

  @Override
  public String format() {
    return TAR;
  }

  @Override
  public int method() {
    return method;
  }

  @Override
  public void close() {
    try {
      tis.close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }
}
