package org.basex.io.random;

import java.io.IOException;
import java.util.Arrays;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.out.DataOutput;

/**
 * This class allows main memory access to the database table representation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class TableMemAccess extends TableAccess {
  /** Long buffer array. */
  private long[] buf1;
  /** Long buffer array. */
  private long[] buf2;

  /**
   * Stores the table in long arrays.
   * @param md meta data
   * @param pf file prefix
   * @param s array size
   */
  public TableMemAccess(final MetaData md, final String pf, final int s) {
    super(md, pf);
    buf1 = new long[s];
    buf2 = new long[s];
  }

  @Override
  public void flush() { }

  @Override
  public void close() throws IOException {
    if(dirty && pref != null) {
      DataOutput out = new DataOutput(meta.file(pref + 'i'));

      final int blocks = (meta.size + IO.ENTRIES - 1) / IO.ENTRIES;
      out.writeNum(blocks);
      out.writeNum(blocks);
      out.writeNum(meta.size);

      final int[] array = new int[blocks];
      for(int b = 0, c = 0; b < blocks; ++b, c += IO.ENTRIES) array[b] = c;
      out.writeNums(array);
      for(int b = 0; b < blocks; ++b) array[b] = b;
      out.writeNums(array);
      out.close();
      out = new DataOutput(meta.file(pref));
      final byte[] data = new byte[IO.BLOCKSIZE];
      int a = 0;
      for(int b = 0; b < blocks; ++b) {
        for(int p = 0; p < IO.BLOCKSIZE && a < meta.size; p += 16, ++a) {
          copy(buf1[a], data, p);
          copy(buf2[a], data, p + 8);
        }
        out.writeBytes(data);
      }
      out.close();
      dirty = false;
    }
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
    dirty = true;
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = (o < 8 ? 7 : 15) - o << 3;
    buf[p] = buf[p] & ~(0xFFL << d) | (long) v << d;
  }

  @Override
  public void write2(final int p, final int o, final int v) {
    dirty = true;
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = (o < 8 ? 6 : 14) - o << 3;
    buf[p] = buf[p] & ~(0xFFFFL << d) | (long) v << d;
  }

  @Override
  public void write4(final int p, final int o, final int v) {
    dirty = true;
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = (o < 8 ? 4 : 12) - o << 3;
    buf[p] = buf[p] & ~(0xFFFFFFFFL << d) | (long) v << d;
  }

  @Override
  public void write5(final int p, final int o, final long v) {
    dirty = true;
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = (o < 8 ? 3 : 11) - o << 3;
    buf[p] = buf[p] & ~(0xFFFFFFFFFFL << d) | v << d;
  }

  @Override
  protected void copy(final byte[] entries, final int pre, final int last) {
    for(int o = 0, i = pre; i < last; ++i, o += IO.NODESIZE) {
      buf1[i] = getLong(entries, o);
      buf2[i] = getLong(entries, o + 8);
    }
    dirty = true;
  }

  @Override
  public void delete(final int pre, final int nr) {
    if(nr == 0) return;
    move(pre + nr, pre);
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    if(entries.length == 0) return;
    move(pre, pre + (entries.length >>> IO.NODEPOWER));
    set(pre, entries);
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Moves data inside the value arrays.
   * @param op source position
   * @param np destination position
   */
  private void move(final int op, final int np) {
    dirty = true;
    final int l = meta.size - op;
    while(l + np >= buf1.length) {
      final int s = buf1.length << 1;
      buf1 = Arrays.copyOf(buf1, s);
      buf2 = Arrays.copyOf(buf2, s);
    }
    System.arraycopy(buf1, op, buf1, np, l);
    System.arraycopy(buf2, op, buf2, np, l);
    meta.size += np - op;
  }

  /**
   * Returns a long value from the specified array.
   * @param v array input
   * @param i index
   * @return long value
   */
  private long getLong(final byte[] v, final int i) {
    return (v[i] & 0xFFL) << 56 | (v[i + 1] & 0xFFL) << 48 |
       (v[i + 2] & 0xFFL) << 40 | (v[i + 3] & 0xFFL) << 32 |
       (v[i + 4] & 0xFFL) << 24 | (v[i + 5] & 0xFFL) << 16 |
       (v[i + 6] & 0xFFL) <<  8 | v[i + 7] & 0xFFL;
  }

  /**
   * Copies a long value to the specified byte array.
   * @param v long value
   * @param a array
   * @param p position
   */
  private void copy(final long v, final byte[] a, final int p) {
    a[p    ] = (byte) (v >> 56);
    a[p + 1] = (byte) (v >> 48);
    a[p + 2] = (byte) (v >> 40);
    a[p + 3] = (byte) (v >> 32);
    a[p + 4] = (byte) (v >> 24);
    a[p + 5] = (byte) (v >> 16);
    a[p + 6] = (byte) (v >>  8);
    a[p + 7] = (byte) v;
  }
}
