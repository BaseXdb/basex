package org.basex.io.out;

import static org.basex.io.in.TarEntry.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.in.*;

/**
 * Output stream filter for writing files in the TAR (ustar) file format.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TarOutputStream extends FilterOutputStream {
  /** Empty block. */
  private static final byte[] EMPTY = new byte[BLOCK];

  /** Current entry (can be {@code null}). */
  private TarEntry entry;
  /** Number of bytes written for the current entry. */
  private long written;

  /**
   * Constructor.
   * @param os output stream
   */
  public TarOutputStream(final OutputStream os) {
    super(os);
  }

  /**
   * Starts a new entry.
   * @param te entry
   * @throws IOException I/O exception
   */
  public void putNextEntry(final TarEntry te) throws IOException {
    closeEntry();
    final long time = te.getTime();
    final long secs = Math.max(0, time == -1 ? System.currentTimeMillis() : time) / 1000;
    final byte[] name = token(te.getName());
    final int nl = name.length;

    // split name into prefix and name if it does not fit into the name field
    int split = -1;
    if(nl > 100) {
      for(int s = Math.min(155, nl - 2); s >= nl - 101 && split == -1; s--) {
        if(name[s] == '/') split = s;
      }
      if(split == -1) {
        // GNU extension: long name is stored in the preceding entry
        out.write(header(token(LONGNAME), 0, nl + 1, secs, (byte) 'L'));
        out.write(name);
        out.write(0);
        pad(nl + 1);
      }
    }
    out.write(header(name, split + 1, te.getSize(), secs, (byte) (te.isDirectory() ? '5' : '0')));
    entry = te;
    written = 0;
  }

  /**
   * Finishes the current entry.
   * @throws IOException I/O exception
   */
  public void closeEntry() throws IOException {
    if(entry == null) return;
    if(written != entry.getSize()) throw new IOException("Size of entry " + entry.getName() +
      " differs: " + written + " vs. " + entry.getSize() + " bytes.");
    pad(written);
    entry = null;
  }

  @Override
  public void write(final int b) throws IOException {
    out.write(b);
    written++;
  }

  @Override
  public void write(final byte[] bytes, final int off, final int len) throws IOException {
    out.write(bytes, off, len);
    written += len;
  }

  @Override
  public void close() throws IOException {
    closeEntry();
    out.write(EMPTY);
    out.write(EMPTY);
    super.close();
  }

  /**
   * Writes a header block.
   * @param name name
   * @param off offset of the name (the part before the preceding slash is the prefix)
   * @param size entry size
   * @param secs modification time in seconds
   * @param type entry type
   * @return header
   * @throws IOException I/O exception
   */
  private static byte[] header(final byte[] name, final int off, final long size, final long secs,
      final byte type) throws IOException {
    final byte[] header = new byte[BLOCK];
    System.arraycopy(name, off, header, 0, Math.min(name.length - off, 100));
    if(off > 0) System.arraycopy(name, 0, header, 345, off - 1);
    octal(header, 100, 8, type == '5' ? 0755 : 0644);
    octal(header, 108, 8, 0);
    octal(header, 116, 8, 0);
    octal(header, 124, 12, size);
    octal(header, 136, 12, secs);
    header[156] = type;
    System.arraycopy(MAGIC, 0, header, 257, MAGIC.length);
    octal(header, 329, 8, 0);
    octal(header, 337, 8, 0);
    // checksum: computed with the checksum field filled with spaces
    for(int i = 148; i < 156; i++) header[i] = ' ';
    int sum = 0;
    for(final byte b : header) sum += b & 0xFF;
    octal(header, 148, 7, sum);
    return header;
  }

  /**
   * Writes an octal number to a header field, padded with leading zeros and terminated with NUL.
   * @param header header block
   * @param off field offset
   * @param len field length
   * @param value value
   * @throws IOException I/O exception
   */
  private static void octal(final byte[] header, final int off, final int len, final long value)
      throws IOException {
    if(value < 0 || value >= 1L << 3 * (len - 1)) {
      throw new IOException("Value too large: " + value);
    }
    long v = value;
    for(int i = off + len - 2; i >= off; i--) {
      header[i] = (byte) ('0' + (v & 7));
      v >>= 3;
    }
    header[off + len - 1] = 0;
  }

  /**
   * Pads the output to the block size.
   * @param size number of written bytes
   * @throws IOException I/O exception
   */
  private void pad(final long size) throws IOException {
    out.write(EMPTY, 0, (int) (-size & BLOCK - 1));
  }
}
