package org.basex.io.in;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.basex.util.*;

/**
 * Input stream filter for reading files in the TAR file format.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class TarInputStream extends FilterInputStream {
  /** Block size. */
  private static final int BLOCK = 512;

  /** Single byte buffer. */
  private final byte[] buf = new byte[1];
  /** Current entry. */
  private TarEntry entry;
  /** File size. */
  private long size;

  /**
   * Constructor.
   * @param is input stream
   */
  public TarInputStream(final InputStream is) {
    super(is);
  }

  @Override
  public int read() throws IOException {
    final int res = read(buf, 0, 1);
    return res == -1 ? -1 : buf[0] & 0xFF;
  }

  @Override
  public int read(final byte[] bytes, final int off, final int len) throws IOException {
    int l = len;
    if(entry != null) {
      final long ln = entry.getSize() - size;
      if(ln == 0) return -1;
      if(ln < len) l = (int) ln;
    }

    final int br = super.read(bytes, off, l);
    if(br != -1 && entry != null) size += br;
    return br;
  }

  @Override
  public boolean markSupported() {
    return false;
  }

  @Override
  public synchronized void mark(final int limit) {
    Util.notImplemented();
  }

  @Override
  public synchronized void reset() {
    Util.notImplemented();
  }

  /**
   * Returns the next entry.
   * @return entry
   * @throws IOException I/O exception
   */
  public TarEntry getNextEntry() throws IOException {
    // close entry
    if(entry != null) {
      long ln = entry.getSize() - size + BLOCK - (size & BLOCK - 1);
      while(ln != BLOCK && ln > 0) ln -= skip(ln);
      entry = null;
      size = 0;
    }
    // read header
    final byte[] header = new byte[BLOCK];
    int tr = 0;
    while(tr < BLOCK) {
      final int res = read(header, tr, BLOCK - tr);
      if(res < 0) break;
      tr += res;
    }
    for(final byte b : header) {
      if(b != 0) {
        entry = new TarEntry(header);
        break;
      }
    }
    return entry;
  }
}
