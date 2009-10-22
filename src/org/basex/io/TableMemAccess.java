package org.basex.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import org.basex.core.Prop;

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
  /** Number of entries. */
  private int size;

  /**
   * Stores the file content in a long array.
   * @param nm name of the database
   * @param fn the file to be read
   * @param sz table size
   * @param pr database properties
   * @throws IOException IO Exception
   */
  public TableMemAccess(final String nm, final String fn, final int sz,
      final Prop pr) throws IOException {

    super(nm, fn, pr);
    buf1 = new long[sz];
    buf2 = new long[sz];
    size = sz;

    // read index info
    final DataInput in = new DataInput(pr.dbfile(nm, fn + 'i'));
    in.readNum(); in.readNum(); in.readNum();
    final int[] firstPres = in.readNums();
    final int[] blocks = in.readNums();
    in.close();

    // read blocks
    final RandomAccessFile f = new RandomAccessFile(pr.dbfile(nm, fn), "r");
    final byte[] array = new byte[IO.BLOCKSIZE];
    int np = 0;
    for(int c = 0, i = 0, l = 0; i != sz; i++) {
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

  @Override
  public synchronized void flush() throws IOException {
    if(dirty) {
      DataOutput out = new DataOutput(prop.dbfile(db, pref + 'i'));

      final int ent = TableDiskAccess.ENTRIES;
      final int blocks = (size + ent - 1) / ent;

      out.writeNum(blocks);
      out.writeNum(blocks);
      out.writeNum(size);

      final int[] array = new int[blocks];
      for(int b = 0, c = 0; b < blocks; b++, c += ent) array[b] = c;
      out.writeNums(array);
      for(int b = 0; b < blocks; b++) array[b] = b;
      out.writeNums(array);
      out.close();
      dirty = false;
      out = new DataOutput(prop.dbfile(db, pref));
      final byte[] data = new byte[IO.BLOCKSIZE];
      int a = 0;
      for(int b = 0; b < blocks; b++) {
        for(int p = 0; p < IO.BLOCKSIZE && a < size; p += 16, a++) {
          copy(buf1[a], data, p);
          copy(buf2[a], data, p + 8);
        }
        out.write(data);
      }
      out.close();
    }
  }

  @Override
  public synchronized void close() throws IOException {
    flush();
  }

  @Override
  public int read1(final int p, final int o) {
    return (int) ((o < 8 ? buf1 : buf2)[p] >>
      ((o < 8 ? 7 : 15) - o << 3) & 0xFF);
  }

  @Override
  public int read2(final int p, final int o) {
    return (int) ((o < 8 ? buf1 : buf2)[p] >>
      ((o < 8 ? 6 : 14) - o << 3) & 0xFFFF);
  }

  @Override
  public int read4(final int p, final int o) {
    return (int) ((o < 8 ? buf1 : buf2)[p] >>
      ((o < 8 ? 4 : 12) - o << 3));
  }

  @Override
  public long read5(final int p, final int o) {
    return (o < 8 ? buf1 : buf2)[p] >>
      ((o < 8 ? 3 : 11) - o << 3) & 0xFFFFFFFFFFL;
  }

  @Override
  public void write1(final int p, final int o, final int v) {
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = ((o < 8 ? 7 : 15) - o) << 3;
    buf[p] = buf[p] & ~(0xFFL << d) | (long) v << d;
  }

  @Override
  public void write2(final int p, final int o, final int v) {
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = ((o < 8 ? 6 : 14) - o) << 3;
    buf[p] = buf[p] & ~(0xFFFFL << d) | (long) v << d;
  }

  @Override
  public void write4(final int p, final int o, final int v) {
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = ((o < 8 ? 4 : 12) - o) << 3;
    buf[p] = buf[p] & ~(0xFFFFFFFFL << d) | (long) v << d;
  }

  @Override
  public void write5(final int p, final int o, final long v) {
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = ((o < 8 ? 3 : 11) - o) << 3;
    buf[p] = buf[p] & ~(0xFFFFFFFFFFL << d) | v << d;
  }

  @Override
  public void delete(final int pre, final int nr) {
    dirty = true;
    move(pre + nr, pre);
    size -= nr;
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    dirty = true;
    move(pre, pre + 1);
    //buf1[pre] = getLong(entries, 0);
    //buf2[pre] = getLong(entries, 8);

    buf1[pre] = (entries[0] & 0xFFL) << 56 | (entries[1] & 0xFFL) << 48 |
      (entries[2] & 0xFFL) << 40 | (entries[3] & 0xFFL) << 32 |
      (entries[4] & 0xFFL) << 24 | (entries[5] & 0xFFL) << 16 |
      (entries[6] & 0xFFL) <<  8 | (entries[7] & 0xFFL);
    buf2[pre] = (entries[8] & 0xFFL) << 56 | (entries[9] & 0xFFL) << 48 |
      (entries[10] & 0xFFL) << 40 | (entries[11] & 0xFFL) << 32 |
      (entries[12] & 0xFFL) << 24 | (entries[13] & 0xFFL) << 16 |
      (entries[14] & 0xFFL) <<  8 | (entries[15] & 0xFFL);
    size++;
  }

  /**
   * Moves data inside the value arrays.
   * @param sp source position
   * @param dp destination position
   */
  private void move(final int sp, final int dp) {
    final int l = size - sp;
    while(dp > sp && l + dp >= buf1.length) {
      buf1 = Arrays.copyOf(buf1, buf1.length << 1);
      buf2 = Arrays.copyOf(buf2, buf2.length << 1);
    }
    System.arraycopy(buf1, sp, buf1, dp, l);
    System.arraycopy(buf2, sp, buf2, dp, l);
  }

  /**
   * Returns a long value from the specified array.
   * @param v array input
   * @param i index
   * @return long value
   */
  private long getLong(final byte[] v, final int i) {
    final int j = i << 3;
    return ((long) (v[j] & 0xFF) << 56) + ((long) (v[j + 1] & 0xFF) << 48) +
       ((long) (v[j + 2] & 0xFF) << 40) + ((long) (v[j + 3] & 0xFF) << 32) +
       ((long) (v[j + 4] & 0xFF) << 24) + ((v[j + 5] & 0xFF) << 16) +
       ((v[j + 6] & 0xFF) <<  8) + (v[j + 7] & 0xFF);
  }

  /**
   * Copies a long value to the specified byte array.
   * @param v long value
   * @param a array
   * @param p position
   */
  private void copy(final long v, final byte[] a, final int p) {
    a[p    ] = (byte) (v >>> 56);
    a[p + 1] = (byte) (v >>> 48);
    a[p + 2] = (byte) (v >>> 40);
    a[p + 3] = (byte) (v >>> 32);
    a[p + 4] = (byte) (v >>> 24);
    a[p + 5] = (byte) (v >>> 16);
    a[p + 6] = (byte) (v >>>  8);
    a[p + 7] = (byte) v;
  }
}
