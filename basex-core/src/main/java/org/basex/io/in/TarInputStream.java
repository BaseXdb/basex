package org.basex.io.in;

import java.io.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Input stream filter for reading files in the TAR file format.
 *
 * @author BaseX Team 2005-17, BSD License
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
    throw Util.notExpected();
  }

  @Override
  public synchronized void reset() {
    throw Util.notExpected();
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
    if(eof(header)) return null;

    // create entry
    entry = new TarEntry(header);
    if(entry.isLongName()) {
      final String name = longName();
      entry = getNextEntry();
      entry.setName(name);
    }
    return entry;
  }

  /**
   * Checks if end of data is reached.
   * @param header header data
   * @return result of check
   */
  private static boolean eof(final byte[] header) {
    for(final byte b : header) if(b != 0) return false;
    return true;
  }

  /**
   * Reads a long file name.
   * @return name
   * @throws IOException I/O exception
   */
  private String longName() throws IOException {
    // read name, remove trailing zero byte
    final ByteList result = new ByteList();
    for(int b; (b = read()) != -1;) result.add(b);
    final int sz = result.size() - 1;
    if(sz >= 0 && result.get(sz) == 0) result.size(sz);
    return TarEntry.name(result);
  }
}
