package org.basex.io.random;

import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class allows main memory access to the database table representation.
 * All table entries are stored in arrays
 *
 * NOTE: this class is not thread-safe.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class TableMemAccess extends TableAccess {
  /** Table data (first half). */
  private long[] data1 = new long[Array.INITIAL_CAPACITY];
  /** Table data (second half). */
  private long[] data2 = new long[Array.INITIAL_CAPACITY];

  /**
   * Constructor.
   * @param meta meta data
   */
  public TableMemAccess(final MetaData meta) {
    super(meta);
  }

  @Override
  public void flush(final boolean all) { }

  @Override
  public void close() { }

  @Override
  public boolean lock(final boolean lock) {
    return true;
  }

  @Override
  public int read1(final int pre, final int offset) {
    return (int) ((offset < 8 ? data1 : data2)[pre] >>
      ((offset < 8 ? 7 : 15) - offset << 3) & 0xFF);
  }

  @Override
  public int read2(final int pre, final int offset) {
    return (int) ((offset < 8 ? data1 : data2)[pre] >>
      ((offset < 8 ? 6 : 14) - offset << 3) & 0xFFFF);
  }

  @Override
  public int read4(final int pre, final int offset) {
    return (int) ((offset < 8 ? data1 : data2)[pre] >>
      ((offset < 8 ? 4 : 12) - offset << 3));
  }

  @Override
  public long read5(final int pre, final int offset) {
    return (offset < 8 ? data1 : data2)[pre] >>
      ((offset < 8 ? 3 : 11) - offset << 3) & 0xFFFFFFFFFFL;
  }

  @Override
  public void write1(final int pre, final int offset, final int value) {
    dirty();
    final long[] buf = offset < 8 ? data1 : data2;
    final long d = (offset < 8 ? 7 : 15) - offset << 3;
    buf[pre] = buf[pre] & ~(0xFFL << d) | (long) value << d;
  }

  @Override
  public void write2(final int pre, final int offset, final int value) {
    dirty();
    final long[] buf = offset < 8 ? data1 : data2;
    final long d = (offset < 8 ? 6 : 14) - offset << 3;
    buf[pre] = buf[pre] & ~(0xFFFFL << d) | (long) value << d;
  }

  @Override
  public void write4(final int pre, final int offset, final int value) {
    dirty();
    final long[] buf = offset < 8 ? data1 : data2;
    final long d = (offset < 8 ? 4 : 12) - offset << 3;
    buf[pre] = buf[pre] & ~(0xFFFFFFFFL << d) | (long) value << d;
  }

  @Override
  public void write5(final int pre, final int offset, final long value) {
    dirty();
    final long[] buf = offset < 8 ? data1 : data2;
    final long d = (offset < 8 ? 3 : 11) - offset << 3;
    buf[pre] = buf[pre] & ~(0xFFFFFFFFFFL << d) | value << d;
  }

  @Override
  protected void copy(final byte[] entries, final int pre, final int last) {
    dirty();
    for(int o = 0, i = pre; i < last; ++i, o += IO.NODESIZE) {
      data1[i] = getLong(entries, o);
      data2[i] = getLong(entries, o + 8);
    }
  }

  @Override
  public void delete(final int pre, final int count) {
    if(count == 0) return;
    move(pre + count, pre);
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

  // PRIVATE METHODS ==============================================================================

  /**
   * Moves data inside the value arrays.
   * @param source source position
   * @param target target position
   */
  private void move(final int source, final int target) {
    dirty();
    final int l = meta.size - source;
    while(l + target >= data1.length) {
      final int s = Array.newCapacity(data1.length);
      data1 = Arrays.copyOf(data1, s);
      data2 = Arrays.copyOf(data2, s);
    }
    Array.copy(data1, source, l, data1, target);
    Array.copy(data2, source, l, data2, target);
    meta.size += target - source;
  }

  /**
   * Returns a long value from the specified array.
   * @param entry array input
   * @param index index
   * @return long value
   */
  private static long getLong(final byte[] entry, final int index) {
    return (entry[index] & 0xFFL) << 56 | (entry[index + 1] & 0xFFL) << 48 |
       (entry[index + 2] & 0xFFL) << 40 | (entry[index + 3] & 0xFFL) << 32 |
       (entry[index + 4] & 0xFFL) << 24 | (entry[index + 5] & 0xFFL) << 16 |
       (entry[index + 6] & 0xFFL) <<  8 | entry[index + 7] & 0xFFL;
  }
}
