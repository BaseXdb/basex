package org.basex.io;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class allows a blockwise output of the database table.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class TableOutput extends FileOutputStream {
  /** Buffer Threshold. */
  private static final int THRESHOLD = (int) Math.floor(IO.BLOCKFILL
      * ((1 << IO.BLOCKPOWER) >>> IO.NODEPOWER)) << IO.NODEPOWER;
  /** Buffer. **/
  private final byte[] buffer = new byte[THRESHOLD];
  /** Position inside buffer. **/
  private int pos;
  /** Block Filler. */
  private static final byte[] BLOCKFILLER = new byte[(1 << IO.BLOCKPOWER)
      - THRESHOLD];
  /** Block Count. */
  private int blockCount;
  /** First pre value of current block. */
  private int firstPre;
  /** Index File. */
  private DataOutput indexFile;
  /** Name of the database. */
  private String database;
  /** Current Filename. */
  private String filename;

  /**
   * Initializes the output.
   * DBSUFFIX will be added to all filenames.
   * @param db name of the database
   * @param fn the file to be written to
   * @throws IOException IO Exception
   */
  public TableOutput(final String db, final String fn) throws IOException {
    super(IO.dbfile(db, fn));
    indexFile = new DataOutput(db, fn + 'x');
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

    super.write(buffer, 0, pos);

    indexFile.writeInt(firstPre);
    indexFile.writeInt(blockCount++);
    firstPre += pos >>> IO.NODEPOWER;

    while(pos++ < THRESHOLD) super.write(0);
    super.write(BLOCKFILLER);
    pos = 0;
  }

  @Override
  public void close() throws IOException {
    flush();
    super.close();
    indexFile.close();

    final DataOutput info = new DataOutput(database, filename + 'i');
    info.writeInt(blockCount);
    info.writeInt(blockCount);
    info.writeInt(firstPre);
    info.close();
  }
}
