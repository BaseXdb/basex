package org.basex.io.out;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;

/**
 * This class allows a blockwise output of the database table.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableOutput extends OutputStream {
  /** Buffer. */
  private final byte[] buffer = new byte[IO.BLOCKSIZE];

  /** The underlying output stream. */
  private final OutputStream os;
  /** Meta data. */
  private final MetaData meta;
  /** Current filename. */
  private final String file;

  /** Current buffer position. */
  private int pos;
  /** Number of pages. */
  private int pages;

  /**
   * Initializes the output.
   * The database suffix will be added to all filenames.
   * @param md meta data
   * @param fn the file to be written to
   * @throws IOException I/O exception
   */
  public TableOutput(final MetaData md, final String fn) throws IOException {
    os = md.dbFile(fn).outputStream();
    meta = md;
    file = fn;
  }

  @Override
  public void write(final int b) throws IOException {
    if(pos == IO.BLOCKSIZE) writeBuffer();
    buffer[pos++] = (byte) b;
  }

  /**
   * Writes a page to disk.
   * @throws IOException I/O exception
   */
  private void writeBuffer() throws IOException {
    os.write(buffer);
    pages++;
    pos = 0;
  }

  @Override
  public void close() throws IOException {
    // write last entries, or single empty page, to disk
    final boolean empty = pages == 0 && pos == 0;
    if(pos > 0 || empty) writeBuffer();
    os.close();

    // create table info file
    try(DataOutput out = new DataOutput(meta.dbFile(file + 'i'))) {
      // total number of pages
      out.writeNum(pages);
      // number of used pages (0: empty table; MAX: no mapping)
      out.writeNum(empty ? 0 : Integer.MAX_VALUE);
    }
  }
}
