package org.basex.io.out;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * This class allows a blockwise output of the database table.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableOutput extends OutputStream {
  /** Buffer. */
  private final byte[] buffer = new byte[IO.BLOCKSIZE];
  /** Index entries. */
  private final IntList fpres = new IntList();
  /** Index entries. */
  private final IntList pages = new IntList();

  /** The underlying output stream. */
  private final OutputStream os;
  /** Meta data. */
  private final MetaData meta;
  /** Current filename. */
  private final String file;

  /** Position inside buffer. */
  private int pos;
  /** First pre value of current block. */
  private int fpre;

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
    fpres.add(fpre);
    pages.add(pages.size());
    fpre += pos >>> IO.NODEPOWER;
    pos = 0;
  }

  @Override
  public void close() throws IOException {
    final boolean empty = fpre + pos == 0;
    if(empty) pos++;
    flush();
    os.close();

    final DataOutput out = new DataOutput(meta.dbfile(file + 'i'));
    try {
      out.writeNum(pages.size());
      out.writeNum(empty ? 0 : pages.size());
      out.writeNums(fpres.toArray());
      out.writeNums(pages.toArray());
    } finally {
      out.close();
    }
  }
}
