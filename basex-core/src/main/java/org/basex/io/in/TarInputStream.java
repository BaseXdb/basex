package org.basex.io.in;

import java.io.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Input stream filter for reading files in the TAR file format.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class TarInputStream extends FilterInputStream {
  /** Block size. */
  private static final int BLOCK = 512;

  /** Single byte buffer. */
  private final byte[] buf = new byte[1];
  /** Current entry. */
  private TarEntry entry;
  /** Number of read bytes. */
  private long offset;

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
    // no entry (header)
    if(entry == null) return super.read(bytes, off, len);

    // tar entry: bytes to read
    final long remain = entry.getSize() - offset;
    // stop if all bytes have been reached
    if(remain == 0) return -1;
    // stop reading at end of entry
    final int read = super.read(bytes, off, remain < len ? (int) remain : len);
    if(read != -1) offset += read;
    return read;
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
   * @return entry or {@code null}
   * @throws IOException I/O exception
   */
  public TarEntry getNextEntry() throws IOException {
    // close entry
    if(entry != null) {
      // skip bytes: count number of entry blocks, subtract number of read bytes
      long skip = (entry.getSize() + BLOCK - 1) / BLOCK * BLOCK - offset;
      offset = 0;
      entry = null;
      while(skip > 0) skip -= skip(skip);
    }
    // read header
    final byte[] header = new byte[BLOCK];
    int read = 0;
    while(read < BLOCK) {
      final int res = read(header, read, BLOCK - read);
      if(res < 0) break;
      read += res;
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
    for(final byte b : header) {
      if(b != 0) return false;
    }
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
    final int size = result.size() - 1;
    if(size >= 0 && result.get(size) == 0) result.size(size);
    return TarEntry.name(result);
  }
}
