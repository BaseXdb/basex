package org.basex.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.basex.data.MetaData;
import org.basex.util.Array;
import org.basex.util.BitArray;
import org.basex.util.Util;

/**
 * This class stores the table on disk and reads it block-wise.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableDiskAccess extends TableAccess {
  /** Max entries per block. */
  static final int ENTRIES = IO.BLOCKSIZE >>> IO.NODEPOWER;

  /** Buffer manager. */
  private final Buffers bm = new Buffers();
  /** Current buffer. */
  private Buffer bf;

  /** File storing all blocks. */
  private final RandomAccessFile data;

  /** FirstPre values (sorted ascending; length={@link #allBlocks}). */
  private int[] fpres;
  /** Index array storing BlockNumbers (length={@link #allBlocks}). */
  private int[] pages;
  /** Bit map storing free (=0) and occupied (=1) pages. */
  private BitArray pagemap;

  /** Pre value of the first entry in the current block. */
  private int fpre = -1;
  /** First pre value of the next block. */
  private int npre = -1;

  /** Number of blocks in the data file (including unused). */
  private int allBlocks;
  /** Number of entries in the index (used blocks). */
  private int blocks;
  /** Index of the current block number in the {@link #pages} array. */
  private int index = -1;

  /**
   * Constructor.
   * @param md meta data
   * @param pf file prefix
   * @throws IOException I/O exception
   */
  public TableDiskAccess(final MetaData md, final String pf)
      throws IOException {

    super(md, pf);

    // read meta and index data
    final DataInput in = new DataInput(meta.file(pf + 'i'));
    allBlocks  = in.readNum();
    blocks     = in.readNum();
    fpres      = in.readNums();
    pages      = in.readNums();

    final int psize = in.readNum();
    // check if the page map has been stored:
    if(psize == 0) {
      // init the map with empty pages:
      pagemap = new BitArray(allBlocks);
      for(final int p : pages) pagemap.set(p);
      dirty = true;
    } else {
      pagemap = new BitArray(in.readLongs(psize), allBlocks);
    }
    in.close();

    // initialize data file
    data = new RandomAccessFile(meta.file(pf), "rw");
    readBlock(0);
  }

  @Override
  public synchronized void flush() throws IOException {
    for(final Buffer b : bm.all())
      if(b.dirty) writeBlock(b);

    if(!dirty) return;
    final DataOutput out = new DataOutput(meta.file(pref + 'i'));
    out.writeNum(allBlocks);
    out.writeNum(blocks);
    out.writeNums(fpres);
    out.writeNums(pages);
    out.writeLongs(pagemap.toArray());
    out.close();
    dirty = false;
  }

  @Override
  public synchronized void close() throws IOException {
    flush();
    data.close();
  }

  @Override
  public synchronized int read1(final int pre, final int off) {
    final int o = off + cursor(pre);
    final byte[] b = bf.data;
    return b[o] & 0xFF;
  }

  @Override
  public synchronized int read2(final int pre, final int off) {
    final int o = off + cursor(pre);
    final byte[] b = bf.data;
    return ((b[o] & 0xFF) << 8) + (b[o + 1] & 0xFF);
  }

  @Override
  public synchronized int read4(final int pre, final int off) {
    final int o = off + cursor(pre);
    final byte[] b = bf.data;
    return ((b[o] & 0xFF) << 24) + ((b[o + 1] & 0xFF) << 16) +
      ((b[o + 2] & 0xFF) << 8) + (b[o + 3] & 0xFF);
  }

  @Override
  public synchronized long read5(final int pre, final int off) {
    final int o = off + cursor(pre);
    final byte[] b = bf.data;
    return ((long) (b[o] & 0xFF) << 32) + ((long) (b[o + 1] & 0xFF) << 24) +
      ((b[o + 2] & 0xFF) << 16) + ((b[o + 3] & 0xFF) << 8) + (b[o + 4] & 0xFF);
  }

  @Override
  public void write1(final int pre, final int off, final int v) {
    final int o = off + cursor(pre);
    final byte[] b = bf.data;
    b[o] = (byte) v;
    bf.dirty = true;
  }

  @Override
  public void write2(final int pre, final int off, final int v) {
    final int o = off + cursor(pre);
    final byte[] b = bf.data;
    b[o] = (byte) (v >>> 8);
    b[o + 1] = (byte) v;
    bf.dirty = true;
  }

  @Override
  public void write4(final int pre, final int off, final int v) {
    final int o = off + cursor(pre);
    final byte[] b = bf.data;
    b[o]     = (byte) (v >>> 24);
    b[o + 1] = (byte) (v >>> 16);
    b[o + 2] = (byte) (v >>> 8);
    b[o + 3] = (byte) v;
    bf.dirty = true;
  }

  @Override
  public void write5(final int pre, final int off, final long v) {
    final int o = off + cursor(pre);
    final byte[] b = bf.data;
    b[o]     = (byte) (v >>> 32);
    b[o + 1] = (byte) (v >>> 24);
    b[o + 2] = (byte) (v >>> 16);
    b[o + 3] = (byte) (v >>> 8);
    b[o + 4] = (byte) v;
    bf.dirty = true;
  }

  /* Note to delete method: Freed blocks are currently ignored. */
  @Override
  public void delete(final int first, final int nr) {
    // mark index as dirty and get first block
    dirty = true;
    cursor(first);

    // some useful variables to make code more readable
    int from = first - fpre;
    final int last = first + nr;

    // check if all entries are in current block => handle and return
    if(last - 1 < npre) {
      copy(bf.data, from + nr, bf.data, from, npre - last);
      updatePre(nr);

      // if whole block was deleted, remove it from the index
      if(npre == fpre) {
        // mark the block as empty:
        pagemap.clear(pages[index]);

        Array.move(fpres, index + 1, -1, blocks - index - 1);
        Array.move(pages, index + 1, -1, blocks - index - 1);

        --blocks;
        readBlock(index);
      }
      return;
    }

    // handle blocks whose entries are to be deleted entirely

    // first count them
    int unused = 0;
    while(npre < last) {
      if(from == 0) {
        ++unused;
        // mark the blocks as empty; range clear cannot be used because the
        // blocks may not be consecutive:
        pagemap.clear(pages[index]);
      }
      readBlock(index + 1);
      from = 0;
    }

    // if the last block is empty, clear the corresponding bit:
    if(npre == last) {
      pagemap.clear((int) bf.pos);
      ++unused;
      if(index < blocks - 1) readBlock(index + 1);
      else ++index;
    } else {
      // delete entries at beginning of current (last) block
      copy(bf.data, last - fpre, bf.data, 0, npre - last);
    }

    // now remove them from the index
    if(unused > 0) {
      Array.move(fpres, index, -unused, blocks - index);
      Array.move(pages, index, -unused, blocks - index);
      blocks -= unused;
      index -= unused;
    }

    // update index entry for this block
    fpres[index] = first;
    fpre = first;
    updatePre(nr);
  }

  @Override
  public void replace(final int pre, final byte[] entries, final int sub) {
    final int nsize = entries.length >>> IO.NODEPOWER;
    final int rpre = pre + nsize;
    int off = 0;
    final int diff = sub - nsize;
    final int max = rpre - Math.abs(diff);
    for(int i = pre; i < max; i++) {
      final int o = cursor(i);
      final byte[] b = bf.data;
      for(int j = 0; j < 16; j++) b[o + j] = entries[off++];
      bf.dirty = true;
    }

    // handle the remaining entries if the two subtrees are of different size
    // case1: new subtree bigger than old one, insert remaining new nodes
    if(diff < 0) {
      final byte[] tmp = new byte[entries.length - off];
      System.arraycopy(entries, off, tmp, 0, tmp.length);
      insert(max, tmp);
    } else if(diff > 0) {
      // case2: old subtree bigger than new one, delete remaining old nodes
      delete(max, diff);
    }
    bf.dirty = true;
    dirty = true;
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    if(entries.length == 0) return;

    // go to the block and find the offset within the block where the new
    // records will be inserted:
    final int split = cursor(pre - 1) + (1 << IO.NODEPOWER);

    // number of records to be inserted:
    final int nr = entries.length >>> IO.NODEPOWER;

    // number of bytes occupied by old records in the current block:
    final int nold = npre - fpre << IO.NODEPOWER;
    // number of bytes occupied by old records which will be moved at the end:
    final int moved = nold - split;

    // special case: all entries fit in the current block:
    if(nold + entries.length <= IO.BLOCKSIZE) {
      System.arraycopy(bf.data, split, bf.data, split + entries.length, moved);
      System.arraycopy(entries, 0, bf.data, split, entries.length);
      bf.dirty = true;

      // increment first pre-values of blocks after the last modified block:
      for(int i = index + 1; i < blocks; ++i) fpres[i] += nr;
      // update cached variables (fpre is not changed):
      npre += nr;
      meta.size += nr;
      dirty = true;
      return;
    }

    // append old entries at the end of the new entries:
    // [DP] the following can be optimized to avoid copying arrays:
    final byte[] all = new byte[entries.length + moved];
    System.arraycopy(entries, 0, all, 0, entries.length);
    System.arraycopy(bf.data, split, all, entries.length, moved);

    // fill in the current block with new entries:
    // number of bytes which can fit in the first block:
    int n = bf.data.length - split;
    if(n > 0) {
      System.arraycopy(all, 0, bf.data, split, n);
      bf.dirty = true;
    }

    int neededBlocks = (all.length - n) / IO.BLOCKSIZE;
    // number of bytes which don't fill one block completely:
    final int remain = (all.length - n) % IO.BLOCKSIZE;

    if(remain > 0) {
      // check if the last entries can fit in the block after the current one:
      if(index + 1 < blocks) {
        final int o = occupiedSpace(index + 1) << IO.NODEPOWER;
        if(remain <= IO.BLOCKSIZE - o) {
          // copy the last records:
          readBlock(index + 1);
          System.arraycopy(bf.data, 0, bf.data, remain, o);
          System.arraycopy(all, all.length - remain, bf.data, 0, remain);
          bf.dirty = true;
          // reduce the pre value, since it will be later incremented with nr:
          fpres[index] -= remain >>> IO.NODEPOWER;
          // go back to the previous block
          readBlock(index - 1);
        } else {
          // there is not enough space in the block - allocate a new one:
          ++neededBlocks;
        }
      } else {
        // this is the last block - allocate a new one:
        ++neededBlocks;
      }
    }

    // number of new blocks (number of needed block - number of empty blocks):
    final int newBlocks = neededBlocks - (allBlocks - blocks);

    // extend fpres and pages, if new blocks will be allocated:
    if(newBlocks > 0) {
      fpres = Arrays.copyOf(fpres, fpres.length + newBlocks);
      pages = Arrays.copyOf(pages, pages.length + newBlocks);
    }

    // make place for the blocks where the new entries will be written:
    Array.move(fpres, index + 1, neededBlocks, blocks - index - 1);
    Array.move(pages, index + 1, neededBlocks, blocks - index - 1);

    // write the all remaining entries:
    while(neededBlocks-- > 0) {
      getFreeBlock();
      n += write(all, n);
      fpres[index] = fpres[index - 1] + ENTRIES;
      pages[index] = (int) bf.pos;
    }

    // increment first pre-values of blocks after the last modified block:
    for(int i = index + 1; i < blocks; ++i) fpres[i] += nr;

    meta.size += nr;
    dirty = true;

    // update cached variables:
    fpre = fpres[index];
    npre = index + 1 < blocks && fpres[index + 1] < meta.size ? fpres[index + 1]
        : meta.size;
  }

  @Override
  public void set(final int pre, final byte[] entries) {
    dirty = true;
    final int nr = entries.length >>> IO.NODEPOWER;
    for(int l = 0, i = pre; i < pre + nr; ++i, l += 1 << IO.NODEPOWER) {
      final int o = cursor(pre);
      System.arraycopy(entries, l, bf.data, o, 1 << IO.NODEPOWER);
    }
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Searches for the block containing the entry for that pre. then it
   * reads the block and returns it's offset inside the block.
   * @param pre pre of the entry to search for
   * @return offset of the entry in currentBlock
   */
  private int cursor(final int pre) {
    int fp = fpre;
    int np = npre;

    if(pre < fp || pre >= np) {
      final int last = blocks - 1;
      int l = 0;
      int h = last;
      int m = index;
      while(l <= h) {
        if(pre < fp) h = m - 1;
        else if(pre >= np) l = m + 1;
        else break;
        m = h + l >>> 1;
        fp = fpres[m];
        np = m == last ? meta.size : fpres[m + 1];
      }
      if(l > h) Util.notexpected("Data Access out of bounds [pre:" + pre +
          ", indexSize:" + blocks + ", access:" + l + " > " + h + "]");

      readBlock(m);
    }
    return pre - fpre << IO.NODEPOWER;
  }

  /**
   * Updates the pre pointers and fetches the requested block.
   * @param i index number of the block to fetch
   */
  private void readBlock(final int i) {
    index = i;
    fpre = fpres[i];
    npre = i + 1 >= blocks ? meta.size : fpres[i + 1];

    final int b = pages[i];
    final boolean ch = bm.cursor(b);
    bf = bm.current();
    if(ch) {
      try {
        if(bf.dirty) writeBlock(bf);
        bf.pos = b;
        data.seek(bf.pos * IO.BLOCKSIZE);
        data.readFully(bf.data);
      } catch(final IOException ex) {
        Util.stack(ex);
      }
    }
  }

  /**
   * Writes the specified block to disk and resets the dirty flag.
   * @param buf buffer to write
   * @throws IOException I/O exception
   */
  private void writeBlock(final Buffer buf) throws IOException {
    data.seek(buf.pos * IO.BLOCKSIZE);
    data.write(buf.data);
    buf.dirty = false;
  }

  /**
   * Updates the firstPre index entries.
   * @param nr number of entries to move
   */
  private void updatePre(final int nr) {
    // update index entries for all following blocks and reduce counter
    for(int i = index + 1; i < blocks; ++i) fpres[i] -= nr;
    meta.size -= nr;
    npre = index + 1 < blocks && fpres[index + 1] < meta.size ? fpres[index + 1]
        : meta.size;
  }

  /**
   * Convenience method for copying blocks.
   * @param s source array
   * @param sp source position
   * @param d destination array
   * @param dp destination position
   * @param l source length
   */
  private void copy(final byte[] s, final int sp, final byte[] d,
      final int dp, final int l) {
    System.arraycopy(s, sp << IO.NODEPOWER, d, dp << IO.NODEPOWER,
        l << IO.NODEPOWER);
    bf.dirty = true;
  }

  /**
   * Fill the current buffer with bytes from the specified array from the
   * specified offset.
   * @param s source array
   * @param o offset from the beginning of the array
   * @return number of written bytes
   */
  private int write(final byte[] s, final int o) {
    final int len = Math.min(bf.data.length, s.length - o);
    System.arraycopy(s, o, bf.data, 0, len);
    bf.dirty = true;
    return len;
  }

  /** Free the current buffer. */
  private void flushCurrentBuffer() {
    try {
      if(bf.dirty) writeBlock(bf);
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /** Move the cursor to a free block (either new or existing empty one). */
  private void getFreeBlock() {
    flushCurrentBuffer();

    // find an empty block:
    bf.pos = pagemap.nextClearBit(0);

    // if block number is bigger than the total number of blocks, it's a new:
    if(bf.pos >= allBlocks) allBlocks = (int) bf.pos + 1;

    bf.dirty = true;
    pagemap.set(bf.pos);
    ++blocks;
    ++index;
  }

  /**
   * Calculate the occupied space in a block.
   * @param i index of the block
   * @return occupied space in number of records
   */
  private int occupiedSpace(final int i) {
    return (i + 1 < blocks ? fpres[i + 1] : meta.size) - fpres[i];
  }

  // TEST METHODS =============================================================

  /**
   * Returns the number of entries; needed for JUnit tests.
   * @return number of entries
   */
  public int size() {
    return meta.size;
  }

  /**
   * Returns the number of used blocks; needed for JUnit tests.
   * @return number of used blocks
   */
  public int blocks() {
    return blocks;
  }
}
