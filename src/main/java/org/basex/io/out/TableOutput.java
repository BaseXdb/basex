package org.basex.io.out;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.util.list.IntList;

/**
 * This class allows a blockwise output of the database table.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableOutput extends OutputStream {
  /** Buffer. */
  private final byte[] buffer = new byte[IO.BLOCKSIZE];
  /** Index entries. */
  private final IntList firstPres = new IntList();
  /** Index entries. */
  private final IntList blocks = new IntList();

  /** The underlying output stream. */
  private final OutputStream os;
  /** Meta data. */
  private final MetaData meta;
  /** Current filename. */
  private final String file;

  /** Position inside buffer. */
  private int pos;
  /** Block count. */
  private int bcount;
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
    os = new FileOutputStream(md.dbfile(fn));
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
    firstPres.add(fpre);
    blocks.add(bcount++);
    fpre += pos >>> IO.NODEPOWER;
    pos = 0;
  }

  @Override
  public void close() throws IOException {
    flush();
    os.close();

    DataOutput dt = null;
    try {
      dt = new DataOutput(meta.dbfile(file + 'i'));
      dt.writeNum(bcount);
      dt.writeNum(bcount);
      dt.writeNums(firstPres.toArray());
      dt.writeNums(blocks.toArray());
    } finally {
      if(dt != null) try { dt.close(); } catch(final IOException ex) { }
    }
  }
}
