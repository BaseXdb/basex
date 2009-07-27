package org.basex.io;

import java.io.FileOutputStream;
import java.io.IOException;

import org.basex.core.Prop;
import org.basex.util.IntList;

/**
 * This class allows a blockwise output of the database table.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableOutput extends FileOutputStream {
  /** Buffer Threshold. */
  private static final int THRESHOLD = (int) Math.floor(IO.BLOCKFILL
      * (IO.BLOCKSIZE >>> IO.NODEPOWER)) << IO.NODEPOWER;

  /** Buffer. */
  private final byte[] buffer = new byte[THRESHOLD];
  /** Index Entries. */
  private final IntList firstPres = new IntList();
  /** Index Entries. */
  private final IntList blocks = new IntList();

  /** Name of the database. */
  private final String name;
  /** Current Filename. */
  private final String file;
  /** Database properties. */
  private final Prop prop;

  /** Position inside buffer. */
  private int pos;
  /** Block Count. */
  private int bcount;
  /** First pre value of current block. */
  private int fpre;

  /**
   * Initializes the output.
   * The database suffix will be added to all filenames.
   * @param db name of the database
   * @param fn the file to be written to
   * @param pr database properties
   * @throws IOException IO Exception
   */
  public TableOutput(final String db, final String fn, final Prop pr)
      throws IOException {
    super(pr.dbfile(db, fn));
    name = db;
    file = fn;
    prop = pr;
  }

  @Override
  public void write(final int b) throws IOException {
    if(pos == THRESHOLD) flush();
    buffer[pos++] = (byte) b;
  }

  @Override
  public void flush() throws IOException {
    if(pos == 0) return;
    super.write(buffer);
    firstPres.add(fpre);
    blocks.add(bcount++);
    fpre += pos >>> IO.NODEPOWER;
    pos = 0;
  }

  @Override
  public void close() throws IOException {
    flush();
    super.close();

    final DataOutput info = new DataOutput(prop.dbfile(name, file + 'i'));
    info.writeNum(bcount);
    info.writeNum(bcount);
    info.writeNum(fpre);
    info.writeNum(bcount);
    for(int i = 0; i < bcount; i++) info.writeNum(firstPres.get(i));
    info.writeNum(bcount);
    for(int i = 0; i < bcount; i++) info.writeNum(blocks.get(i));
    info.close();
  }
}
