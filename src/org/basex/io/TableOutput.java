package org.basex.io;

import java.io.FileOutputStream;
import java.io.IOException;
import org.basex.util.IntList;

/**
 * This class allows a blockwise output of the database table.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Tim Petrowsky
 */
public final class TableOutput extends FileOutputStream {
  /** Buffer Threshold. */
  private static final int THRESHOLD = (int) Math.floor(IO.BLOCKFILL
      * (IO.BLOCKSIZE >>> IO.NODEPOWER)) << IO.NODEPOWER;
  /** Buffer. */
  private final byte[] buffer = new byte[THRESHOLD];
  /** Index Entries. */
  private IntList firstPres = new IntList();
  /** Index Entries. */
  private IntList blocks = new IntList();
  /** Position inside buffer. */
  private int pos;
  /** Block Count. */
  private int blockCount;
  /** First pre value of current block. */
  private int firstPre;
  /** Name of the database. */
  private String database;
  /** Current Filename. */
  private String filename;

  /**
   * Initializes the output.
   * The database suffix will be added to all filenames.
   * @param db name of the database
   * @param fn the file to be written to
   * @throws IOException IO Exception
   */
  public TableOutput(final String db, final String fn) throws IOException {
    super(IO.dbfile(db, fn));
    database = db;
    filename = fn;
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
    firstPres.add(firstPre);
    blocks.add(blockCount++);
    firstPre += pos >>> IO.NODEPOWER;
    pos = 0;
  }

  @Override
  public void close() throws IOException {
    flush();
    super.close();

    final DataOutput info = new DataOutput(database, filename + 'i');
    info.writeNum(blockCount);
    info.writeNum(blockCount);
    info.writeNum(firstPre);
    info.writeNum(blockCount);
    for(int i = 0; i < blockCount; i++) info.writeNum(firstPres.list[i]);
    info.writeNum(blockCount);
    for(int i = 0; i < blockCount; i++) info.writeNum(blocks.list[i]);
    info.close();
  }
}
