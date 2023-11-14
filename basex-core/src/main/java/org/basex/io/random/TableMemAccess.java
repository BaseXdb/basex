package org.basex.io.random;

import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class allows main memory access to the database table representation.
 * All table entries are stored in arrays
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class TableMemAccess extends TableAccess {
  /** Table blocks. */
  private final ArrayList<TableMemBlock> blocks = new ArrayList<>();
  /** Dense flag (blocks contain no gaps). */
  private boolean dense = true;
  /** Current block index. */
  private int current;

  /**
   * Constructor.
   * @param meta meta data
   */
  public TableMemAccess(final MetaData meta) {
    super(meta);
  }

  @Override
  public void flush(final boolean all) {
  }

  @Override
  public void close() {
  }

  @Override
  public boolean lock(final boolean lock) {
    return true;
  }

  @Override
  public int read1(final int pre, final int offset) {
    final TableMemBlock block = block(pre);
    return (int) (block.value(pre, offset) >> ((offset < 8 ? 7 : 15) - offset << 3) & 0xFF);
  }

  @Override
  public int read2(final int pre, final int offset) {
    final TableMemBlock block = block(pre);
    return (int) (block.value(pre, offset) >> ((offset < 8 ? 6 : 14) - offset << 3) & 0xFFFF);
  }

  @Override
  public int read4(final int pre, final int offset) {
    final TableMemBlock block = block(pre);
    return (int) (block.value(pre, offset) >> ((offset < 8 ? 4 : 12) - offset << 3));
  }

  @Override
  public long read5(final int pre, final int offset) {
    final TableMemBlock block = block(pre);
    return block.value(pre, offset) >> ((offset < 8 ? 3 : 11) - offset << 3) & 0xFFFFFFFFFFL;
  }

  @Override
  public void write1(final int pre, final int offset, final int value) {
    final TableMemBlock block = block(pre);
    final long d = (long) (offset < 8 ? 7 : 15) - offset << 3;
    block.value(pre, offset, block.value(pre, offset) & ~(0xFFL << d) | (long) value << d);
  }

  @Override
  public void write2(final int pre, final int offset, final int value) {
    final TableMemBlock block = block(pre);
    final long d = (long) (offset < 8 ? 6 : 14) - offset << 3;
    block.value(pre, offset, block.value(pre, offset) & ~(0xFFFFL << d) | (long) value << d);
  }

  @Override
  public void write4(final int pre, final int offset, final int value) {
    final TableMemBlock block = block(pre);
    final long d = (long) (offset < 8 ? 4 : 12) - offset << 3;
    block.value(pre, offset, block.value(pre, offset) & ~(0xFFFFFFFFL << d) | (long) value << d);
  }

  @Override
  public void write5(final int pre, final int offset, final long value) {
    final TableMemBlock block = block(pre);
    final long d = (long) (offset < 8 ? 3 : 11) - offset << 3;
    block.value(pre, offset, block.value(pre, offset) & ~(0xFFFFFFFFFFL << d) | value << d);
  }

  @Override
  protected void copy(final byte[] entries, final int first, final int last) {
    for(int o = 0, pre = first; pre < last; pre++, o += IO.NODESIZE) {
      final TableMemBlock block = block(pre);
      block.value(pre, 0, toLong(entries, o));
      block.value(pre, 8, toLong(entries, o + 8));
    }
  }

  @Override
  public void delete(final int pre, final int count) {
    TableMemBlock block = block(pre);
    int c = current;
    for(int deleted = 0; deleted < count;) {
      // delete entries inside block
      deleted += block.delete(pre, count - deleted, firstPre(c + 1) - deleted);
      // adjust firstPre value of next block
      if(c + 1 < blocks.size()) {
        block = blocks.get(c + 1);
        block.firstPre -= deleted;
      }
      // delete emptied block
      if(firstPre(c) == firstPre(c + 1)) {
        blocks.remove(c);
      } else {
        ++c;
      }
    }
    updateFirstPre(c + 1, -count);

    // decrease table size; update dense flag
    final int size = meta.size - count;
    if(pre <= size) dense = false;
    else if(size == 0) dense = true;
    meta.size = size;
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    final int count = entries.length >>> IO.NODEPOWER;
    int size = meta.size;
    if(pre == size) {
      // append entries. if no space is left, append new blocks
      final int bs = blocks.size();
      final int remaining = bs == 0 ? 0 : blocks.get(bs - 1).remaining(size);
      if(remaining < count) {
        blocks.addAll(bs, TableMemBlock.get(count - remaining, pre + remaining));
      }
    } else {
      // insert entries. if no space is left, insert new blocks
      final TableMemBlock block = block(pre);
      int c = current + 1;
      final Collection<TableMemBlock> list = block.insert(pre, count, firstPre(c));
      if(list != null) {
        blocks.addAll(c, list);
        c += list.size();
      }
      updateFirstPre(c, count);
      dense = false;
    }
    // increase table size, populate table with actual entries
    size += count;
    meta.size = size;
    copy(entries, pre, pre + count);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this));
    sb.append("[size: ").append(meta.size).append("; current: ").append(current).append("; ");
    sb.append(blocks.size()).append(" blocks: ");
    for(final TableMemBlock block : blocks) {
      sb.append(block.firstPre).append(' ');
    }
    return sb.append(']').toString();
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns the current block and assigns the {@link #current} block index.
   * @param pre pre value
   * @return block
   */
  private TableMemBlock block(final int pre) {
    int c;
    if(dense) {
      c = pre >>> IO.BLOCKPOWER;
    } else {
      c = current;
      int fp = firstPre(c), np = firstPre(c + 1);
      if(pre >= np) {
        // choose next block
        fp = np;
        np = firstPre(++c + 1);
      } else if(pre < fp) {
        // choose previous block
        np = fp;
        fp = firstPre(--c);
      } else {
        return blocks.get(c);
      }
      if(pre >= np || pre < fp) {
        // binary search
        int l = 0, h = blocks.size() - 1;
        while(l <= h) {
          if(pre >= np) l = c + 1;
          else if(pre < fp) h = c - 1;
          else break;
          c = h + l >>> 1;
          fp = firstPre(c);
          np = firstPre(c + 1);
        }
      }
    }
    current = c;
    return blocks.get(c);
  }

  /**
   * Returns the first pre value of the specified block.
   * @param index block index
   * @return first pre value
   */
  private int firstPre(final int index) {
    return index < blocks.size() ? blocks.get(index).firstPre : meta.size;
  }

  /**
   * Updates the firstPre values of the specified and all subsequent blocks.
   * @param index index of the first block
   * @param count number of insertions (can be negative)
   */
  private void updateFirstPre(final int index, final int count) {
    final int bs = blocks.size();
    for(int b = index; b < bs; b++) blocks.get(b).firstPre += count;
  }

  /**
   * Converts values from the specified array to a long value.
   * @param data data
   * @param offset offset (multiple of 8)
   * @return long value
   */
  private static long toLong(final byte[] data, final int offset) {
    return (data[offset] & 0xFFL) << 56 | (data[offset + 1] & 0xFFL) << 48
        | (data[offset + 2] & 0xFFL) << 40 | (data[offset + 3] & 0xFFL) << 32
        | (data[offset + 4] & 0xFFL) << 24 | (data[offset + 5] & 0xFFL) << 16
        | (data[offset + 6] & 0xFFL) << 8 | data[offset + 7] & 0xFFL;
  }
}
