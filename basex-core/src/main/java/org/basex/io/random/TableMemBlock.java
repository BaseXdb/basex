package org.basex.io.random;

import java.util.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Organizes a single main memory table block.
 * All table entries are stored in arrays
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
final class TableMemBlock {
  /** Table data, with two values for one XML node. */
  private long[] data;
  /** First pre value. */
  int firstPre;

  /**
   * Constructor with initial capacity.
   * @param compact compact block size
   */
  private TableMemBlock(final boolean compact) {
    data = new long[(compact ? 1 : IO.BLOCKSIZE) << 1];
  }

  /**
   * Creates new blocks.
   * @param count number of entries to add
   * @param compact compact block size
   * @return new blocks
   */
  private static ArrayList<TableMemBlock> get(final int count, final boolean compact) {
    final int bs = IO.BLOCKSIZE + count - 1 >>> IO.BLOCKPOWER;
    final ArrayList<TableMemBlock> list = new ArrayList<>(bs);
    for(int b = 0; b < bs; b++) list.add(new TableMemBlock(compact));
    return list;
  }

  /**
   * Creates new blocks with computed pre values.
   * @param count number of entries to add
   * @param pre pre value of first block (will be incremented for subsequent blocks)
   * @return new blocks
   */
  static ArrayList<TableMemBlock> get(final int count, final int pre) {
    final ArrayList<TableMemBlock> blocks = get(count, true);
    int fp = pre;
    for(final TableMemBlock block : blocks) {
      block.firstPre = fp;
      fp += IO.BLOCKSIZE;
    }
    return blocks;
  }

  /**
   * Returns the number of remaining entries in this block.
   * @param nextPre pre value of next block
   * @return remaining entries
   */
  int remaining(final int nextPre) {
    return IO.BLOCKSIZE - nextPre + firstPre;
  }

  /**
   * Returns the value at the specified position.
   * @param pre pre value
   * @param offset offset
   * @return value
   */
  long value(final int pre, final int offset) {
    return data[index(pre, offset)];
  }

  /**
   * Assigns a value at the specified position.
   * @param pre pre value
   * @param offset offset
   * @param value value
   */
  void value(final int pre, final int offset, final long value) {
    final int i = index(pre, offset);
    resize(i + 1);
    data[i] = value;
  }

  /**
   * Deletes the specified number of entries.
   * @param pre pre value of first entry to delete
   * @param count number of entries to delete
   * @param nextPre pre value of next block
   * @return number of deleted entries
   */
  int delete(final int pre, final int count, final int nextPre) {
    final int first = pre - firstPre, last = first + count, filled = nextPre - firstPre;
    if(last >= filled) return filled - first;
    System.arraycopy(data, last << 1, data, first << 1, filled - last << 1);
    return count;
  }

  /**
   * Inserts the specified number of entries.
   * @param pre pre value of first entry to insert (may point to the end of the block)
   * @param count number of entries to insert
   * @param nextPre pre value of next block
   * @return new blocks or {@code null}
   */
  ArrayList<TableMemBlock> insert(final int pre, final int count, final int nextPre) {
    final int first = pre - firstPre, last = first + count, filled = nextPre - firstPre;
    final int remaining = IO.BLOCKSIZE - filled, copy = nextPre - pre;

    // check if entries can be inserted into existing block
    if(count <= remaining) {
      resize((last + copy) << 1);
      System.arraycopy(data, first << 1, data, last << 1, copy << 1);
      return null;
    }

    // otherwise, create new blocks
    resize(IO.BLOCKSIZE << 1);
    final ArrayList<TableMemBlock> blocks = get(count - remaining, false);
    // create temporary array with final entries
    final int total = filled + count;
    final long[] longs = new long[total << 1];
    System.arraycopy(data, 0, longs, 0, first << 1);
    System.arraycopy(data, first << 1, longs, last << 1, copy << 1);

    /* redistribute entries evenly:
     * 300 entries: 2 blocks with 150 entries each
     * 301 entries: 2 blocks with 151 and 150 entries
     * 514 entries: 3 blocks with 172, 172 and 170 entries */
    final int bs = blocks.size(), fill = (total + bs) / (bs + 1);
    final int total2 = total << 1, fill2 = fill << 1;
    // populate original block
    System.arraycopy(longs, 0, data, 0, fill2);
    // populate new blocks
    int copied = fill, copied2 = fill2;
    for(final TableMemBlock block : blocks) {
      block.firstPre = firstPre + copied;
      System.arraycopy(longs, copied2, block.data, 0, Math.min(fill2, total2 - copied2));
      copied += fill;
      copied2 += fill2;
    }
    return blocks;
  }

  /**
   * Resizes the table block.
   * @param size minimum size
   */
  private void resize(final int size) {
    final long[] dt = data;
    final int dl = dt.length;
    if(dl < size) data = Arrays.copyOf(dt, Math.min(Math.max(size, dl << 1), IO.BLOCKSIZE << 1));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this) + '[' + firstPre + ": ");
    final IntList ints = new IntList();
    int first = -1, last = 0;
    final int dl = data.length;
    for(int d = 0; d < dl; d++) {
      if(data[d] != 0) {
        if(first == -1) first = d;
        last = d;
      } else if(first != -1) {
        ints.add(first).add(last);
        first = -1;
      }
    }
    if(first != -1) ints.add(first).add(last);
    for(int i = 0; i < ints.size(); i += 2) {
      if(i != 0) sb.append(',');
      sb.append(ints.get(i)).append('-').append(ints.get(i + 1));
    }
    return sb.append(']').toString();
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns the index to the current table segment.
   * @param pre pre value
   * @param offset byte offset (0-16)
   * @return table index
   */
  private int index(final int pre, final int offset) {
    final int p = pre - firstPre << 1;
    return offset < 8 ? p : p + 1;
  }
}
