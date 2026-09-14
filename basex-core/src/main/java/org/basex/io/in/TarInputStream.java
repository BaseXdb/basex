package org.basex.io.in;

import static org.basex.io.in.TarEntry.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.util.*;

/**
 * Input stream filter for reading files in the TAR file format.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TarInputStream extends FilterInputStream {
  /** Single byte buffer. */
  private final byte[] buf = new byte[1];
  /** Current entry (can be {@code null}). */
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
    final byte[] header = readNBytes(BLOCK);
    if(header.length < BLOCK || isEmpty(header)) return null;

    // create entry
    entry = new TarEntry(header);
    if(entry.isLongName()) {
      final String name = longName();
      entry = getNextEntry();
      if(entry != null) entry.setName(name);
    } else if(entry.isPax()) {
      final byte[] body = readAllBytes();
      entry = getNextEntry();
      if(entry != null) pax(body);
    } else if(entry.isGlobalPax()) {
      entry = getNextEntry();
    }
    return entry;
  }

  /**
   * Reads a long file name.
   * @return name
   * @throws IOException I/O exception
   */
  private String longName() throws IOException {
    // remove trailing zero byte
    final byte[] body = readAllBytes();
    int size = body.length;
    if(size > 0 && body[size - 1] == 0) size--;
    return string(body, 0, size);
  }

  /**
   * Applies the records of a pax extended header to the current entry.
   * @param body header body
   * @throws IOException I/O exception
   */
  private void pax(final byte[] body) throws IOException {
    final int bl = body.length;
    for(int i = 0; i < bl;) {
      // record: "length key=value\n" (length includes the length field itself)
      int len = 0, j = i;
      while(j < bl && body[j] >= '0' && body[j] <= '9') len = len * 10 + body[j++] - '0';
      final int end = i + len;
      if(len == 0 || end > bl || j >= end || body[j] != ' ' || body[end - 1] != '\n') {
        throw new IOException("Invalid pax header.");
      }
      final String record = string(body, j + 1, end - j - 2);
      final int eq = record.indexOf('=');
      if(eq == -1) throw new IOException("Invalid pax record: " + record);

      final String key = record.substring(0, eq), value = record.substring(eq + 1);
      switch(key) {
        case "path" -> entry.setName(value);
        case "size" -> {
          final long size = Strings.toLong(value);
          if(size < 0) throw new IOException("Invalid pax record: " + record);
          entry.setSize(size);
        }
        case "mtime" -> {
          final double time = toDouble(token(value));
          if(Double.isNaN(time)) throw new IOException("Invalid pax record: " + record);
          entry.setTime((long) (time * 1000));
        }
        default -> { }
      }
      i = end;
    }
  }
}
