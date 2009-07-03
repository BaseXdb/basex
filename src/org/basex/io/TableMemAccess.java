package org.basex.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import org.basex.BaseX;

/**
 * This class allows main memory access to the database table representation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class TableMemAccess extends TableAccess {
  /** Long buffer array. */
  private long[] buf1;
  /** Long buffer array. */
  private long[] buf2;

  /**
   * Stores the file content in a long array.
   * @param db name of the database
   * @param fn the file to be read
   * @param size table size
   * @throws IOException IO Exception
   */
  public TableMemAccess(final String db, final String fn, final int size)
      throws IOException {

    buf1 = new long[size];
    buf2 = new long[size];

    // read index info
    final DataInput in = new DataInput(db, fn + 'i');
    in.readNum(); in.readNum(); in.readNum();
    final int[] firstPres = in.readNums();
    final int[] blocks = in.readNums();
    in.close();

    // read blocks
    final RandomAccessFile f = new RandomAccessFile(IO.dbfile(db, fn), "r");
    final byte[] array = new byte[IO.BLOCKSIZE];
    int np = 0;
    for(int c = 0, i = 0, l = 0; i != size; i++) {
      while(i == np) {
        f.seek((long) blocks[c++] * IO.BLOCKSIZE);
        f.read(array);
        np = c == firstPres.length ? Integer.MAX_VALUE : firstPres[c];
        l = 0;
      }
      buf1[i] = getLong(array, l++);
      buf2[i] = getLong(array, l++);
    }
    f.close();
  }

  /**
   * Returns a long value.
   * @param array byte array
   * @param i position
   * @return long value
   */
  private long getLong(final byte[] array, final int i) {
    final int j = i << 3;
    return ((long) (array[j] & 0xFF) << 56) +
       ((long) (array[j + 1] & 0xFF) << 48) +
       ((long) (array[j + 2] & 0xFF) << 40) +
       ((long) (array[j + 3] & 0xFF) << 32) +
       ((long) (array[j + 4] & 0xFF) << 24) +
       ((array[j + 5] & 0xFF) << 16) +
       ((array[j + 6] & 0xFF) <<  8) +
       ((array[j + 7] & 0xFF));
  }

  @Override
  public int read1(final int p, final int o) {
    return (int) (o >= 8 ? buf2[p] >> (15 - o << 3) :
      buf1[p] >> (7 - o << 3)) & 0xFF;
  }

  @Override
  public int read2(final int p, final int o) {
    return (int) (o >= 8 ? buf2[p] >> (14 - o << 3) :
      buf1[p] >> (6 - o << 3)) & 0xFFFF;
  }

  @Override
  public int read4(final int p, final int o) {
    return (int) (o >= 8 ? buf2[p] >> (12 - o << 3) :
      buf1[p] >> (4 - o << 3));
  }

  @Override
  public long read5(final int p, final int o) {
    return (o >= 8 ? buf2[p] >> (11 - o << 3) : buf1[p] >> (3 - o << 3)) &
      0xFFFFFFFFFFL;
  }

  @Override
  public void write1(final int p, final int o, final int v) {
    BaseX.notimplemented();
  }

  @Override
  public void write2(final int p, final int o, final int v) {
    BaseX.notimplemented();
  }

  @Override
  public void write4(final int p, final int o, final int v) {
    BaseX.notimplemented();
  }

  @Override
  public void write5(final int p, final int o, final long v) {
    BaseX.notimplemented();
  }

  @Override
  public void delete(final int pre, final int size) {
    BaseX.notimplemented();
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    BaseX.notimplemented();
  }

  @Override
  public void flush() { }

  @Override
  public void close() { }
}
