package org.basex.io.random;

import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class allows main memory access to the database table representation.
 *
 * NOTE: this class is not thread-safe.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class TableMemAccess extends TableAccess {
  /** Long buffer array. */
  private long[] buf1 = new long[Array.CAPACITY];
  /** Long buffer array. */
  private long[] buf2 = new long[Array.CAPACITY];

  /**
   * Stores the table in long arrays.
   * @param md meta data
   */
  public TableMemAccess(final MetaData md) {
    super(md);
  }

  @Override
  public void flush() { }

  @Override
  public void close() { }

  @Override
  public boolean lock(final boolean lock) {
    return true;
  }

  @Override
  public int read1(final int p, final int o) {
    return (int) ((o < 8 ? buf1 : buf2)[p] >> ((o < 8 ? 7 : 15) - o << 3) & 0xFF);
  }

  @Override
  public int read2(final int p, final int o) {
    return (int) ((o < 8 ? buf1 : buf2)[p] >> ((o < 8 ? 6 : 14) - o << 3) & 0xFFFF);
  }

  @Override
  public int read4(final int p, final int o) {
    return (int) ((o < 8 ? buf1 : buf2)[p] >> ((o < 8 ? 4 : 12) - o << 3));
  }

  @Override
  public long read5(final int p, final int o) {
    return (o < 8 ? buf1 : buf2)[p] >> ((o < 8 ? 3 : 11) - o << 3) & 0xFFFFFFFFFFL;
  }

  @Override
  public void write1(final int p, final int o, final int v) {
    dirty();
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = (o < 8 ? 7 : 15) - o << 3;
    buf[p] = buf[p] & ~(0xFFL << d) | (long) v << d;
  }

  @Override
  public void write2(final int p, final int o, final int v) {
    dirty();
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = (o < 8 ? 6 : 14) - o << 3;
    buf[p] = buf[p] & ~(0xFFFFL << d) | (long) v << d;
  }

  @Override
  public void write4(final int p, final int o, final int v) {
    dirty();
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = (o < 8 ? 4 : 12) - o << 3;
    buf[p] = buf[p] & ~(0xFFFFFFFFL << d) | (long) v << d;
  }

  @Override
  public void write5(final int p, final int o, final long v) {
    dirty();
    final long[] buf = o < 8 ? buf1 : buf2;
    final long d = (o < 8 ? 3 : 11) - o << 3;
    buf[p] = buf[p] & ~(0xFFFFFFFFFFL << d) | v << d;
  }

  @Override
  protected void copy(final byte[] entries, final int pre, final int last) {
    dirty();
    for(int o = 0, i = pre; i < last; ++i, o += IO.NODESIZE) {
      buf1[i] = getLong(entries, o);
      buf2[i] = getLong(entries, o + 8);
    }
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

  @Override
  protected void dirty() {
    dirty = true;
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Moves data inside the value arrays.
   * @param op source position
   * @param np destination position
   */
  private void move(final int op, final int np) {
    dirty();
    final int l = meta.size - op;
    while(l + np >= buf1.length) {
      final int s = Array.newSize(buf1.length);
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
  private static long getLong(final byte[] v, final int i) {
    return (v[i] & 0xFFL) << 56 | (v[i + 1] & 0xFFL) << 48 |
       (v[i + 2] & 0xFFL) << 40 | (v[i + 3] & 0xFFL) << 32 |
       (v[i + 4] & 0xFFL) << 24 | (v[i + 5] & 0xFFL) << 16 |
       (v[i + 6] & 0xFFL) <<  8 | v[i + 7] & 0xFFL;
  }
}
