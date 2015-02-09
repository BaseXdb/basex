package org.basex.io.out;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;

/**
 * This class allows a blockwise output of the database table.
 *
 * @author BaseX Team 2005-15, BSD License
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

  /** Position inside buffer. */
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
    os = new FileOutputStream(md.dbfile(fn).file());
    meta = md;
    file = fn;
  }

  @Override
  public void write(final int b) throws IOException {
    if(pos == IO.BLOCKSIZE) flush();
    buffer[pos++] = (byte) b;
  }

  @Override
  public void flush() throws IOException {
    if(pos == 0) return;
    os.write(buffer);
    pages++;
    pos = 0;
  }

  @Override
  public void close() throws IOException {
    // store at least one page on disk
    final boolean empty = pages == 0 && pos == 0;
    if(empty) pos++;
    flush();
    os.close();

    // create table info file
    try(final DataOutput out = new DataOutput(meta.dbfile(file + 'i'))) {
      out.writeNum(pages);
      // max value indicates that regular page table is not stored on disk
      out.writeNum(empty ? 0 : Integer.MAX_VALUE);
    }
  }
}
