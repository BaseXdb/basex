package org.basex.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import javax.naming.OperationNotSupportedException;

import org.basex.data.MetaData;

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

  /**
   * Stores the table in long arrays.
   * @param md meta data
   * @param pf filename
   * @throws IOException IO Exception
   */
  public TableMemAccess(final MetaData md, final String pf) throws IOException {
    this(md, pf, md.size);

    // read index info
    final DataInput in = new DataInput(md.file(pf + 'i'));
    in.readNum(); in.readNum();
    final int[] firstPres = in.readNums();
    final int[] blocks = in.readNums();
    in.close();

    // read blocks
    final RandomAccessFile f = new RandomAccessFile(md.file(pf), "r");
    final byte[] array = new byte[IO.BLOCKSIZE];
    int np = 0, c = 0, l = 0;
    for(int i = 0; i != md.size; ++i) {
      while(i == np) {
        f.seek((long) blocks[c++] * IO.BLOCKSIZE);
        f.readFully(array);
        np = c == firstPres.length ? Integer.MAX_VALUE : firstPres[c];
        l = 0;
      }
      buf1[i] = getLong(array, l++ << 3);
      buf2[i] = getLong(array, l++ << 3);
    }
    f.close();
  }

  @Override
  public void flush() { }

  @Override
  public void close() throws IOException {
    if(dirty && pref != null) {
      DataOutput out = new DataOutput(meta.file(pref + 'i'));

      final int ent = TableDiskAccess.ENTRIES;
      final int blocks = (meta.size + ent - 1) / ent;

      out.writeNum(blocks);
      out.writeNum(blocks);
      out.writeNum(meta.size);

      final int[] array = new int[blocks];
      for(int b = 0, c = 0; b < blocks; ++b, c += ent) array[b] = c;
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
  public void replace(final int pre, final byte[] entries, final int sub) {
    final int nsize = entries.length >>> IO.NODEPOWER;
    final int rpre = pre + nsize;
    int off = 0;
    final int diff = sub - nsize;
    final int max = rpre - Math.abs(diff);
    for(int i = pre; i < max; ++i, off += 16) {
      buf1[i] = getLong(entries, off);
      buf2[i] = getLong(entries, off + 8);
    }
    dirty = true;

    // handle the remaining entries if the two subtrees are of different size
    // case1: new subtree bigger than old one, insert remaining new nodes
    if(diff < 0) {
      final byte[] tmp = new byte[entries.length - off];
      System.arraycopy(entries, off, tmp, 0, tmp.length);
      insert(rpre + diff, tmp);
    } else if(diff > 0) {
      // case2: old subtree bigger than new one, delete remaining old nodes
      delete(rpre - diff, diff);
    }
  }

  @Override
  public void delete(final int pre, final int nr) {
    move(pre + nr, pre);
  }

  @Override
  public void replace(final int pre, final byte[] entries) {
    try {
      throw new OperationNotSupportedException();
    } catch(OperationNotSupportedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void replace2(final int pre, final byte[] entries,
      final int replacedSubtreeSize) {
    // TODO Auto-generated method stub
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    final int nr = entries.length >>> IO.NODEPOWER;
    move(pre, pre + nr);
    for(int l = 0, i = pre; i < pre + nr; ++i, l += 16) {
      buf1[i] = getLong(entries, l);
      buf2[i] = getLong(entries, l + 8);
    }
  }

  @Override
  public void set(final int pre, final byte[] entries) {
    final int nr = entries.length >>> IO.NODEPOWER;
    for(int l = 0, i = pre; i < pre + nr; ++i, l += 1 << IO.NODEPOWER) {
      buf1[i] = getLong(entries, l);
      buf2[i] = getLong(entries, l + 8);
    }
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
