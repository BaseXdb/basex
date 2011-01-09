package org.basex.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import org.basex.data.MetaData;
import org.basex.util.Array;
import org.basex.util.Util;

/**
 * This class stores the table on disk and reads it block-wise.
 *
 * @author BaseX Team 2005-11, ISC License
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

  /** Index array storing the FirstPre values. this one is sorted ascending. */
  private int[] fpres;
  /** Index array storing the BlockNumbers. */
  private int[] pages;

  /** Pre value of the first entry in the current block. */
  private int fpre = -1;
  /** First pre value of the next block. */
  private int npre = -1;

  /** Number of blocks in the data file (including unused). */
  private int allBlocks;
  /** Number of entries in the index (used blocks). */
  private int blocks;
  /** Index number of the current block, referencing indexBlockNumber array. */
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
    allBlocks = in.readNum();
    blocks    = in.readNum();
    fpres     = in.readNums();
    pages     = in.readNums();
    in.close();

    // initialize data file
    data = new RandomAccessFile(meta.file(pf), "rw");
    readBlock(0, 0, blocks > 1 ? fpres[1] : md.size);
  }

  @Override
  public void flush() throws IOException {
    for(final Buffer b : bm.all()) if(b.dirty) writeBlock(b);

    if(!dirty) return;
    final DataOutput out = new DataOutput(meta.file(pref + 'i'));
    out.writeNum(allBlocks);
    out.writeNum(blocks);
    out.writeNums(fpres);
    out.writeNums(pages);
    out.close();
    dirty = false;
  }

  @Override
  public void close() throws IOException {
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
        Array.move(fpres, index + 1, -1, blocks - index - 1);
        Array.move(pages, index + 1, -1, blocks - index - 1);
        readBlock(index, fpre, index + 2 > --blocks ? meta.size :
          fpres[index + 1]);
      }
      return;
    }

    // handle blocks whose entries are to be deleted entirely

    // first count them
    int unused = 0;
    while(npre < last) {
      if(from == 0) ++unused;
      nextBlock();
      from = 0;
    }

    // now remove them from the index
    if(unused > 0) {
      Array.move(fpres, index, -unused, blocks - index);
      Array.move(pages, index, -unused, blocks - index);
      blocks -= unused;
      index -= unused;
    }

    // delete entries at beginning of current (last) block
    copy(bf.data, last - fpre, bf.data, 0, npre - last);

    // update index entry for this block
    fpres[index] = first;
    fpre = first;
    updatePre(nr);
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    dirty = true;
    final int nr = entries.length >>> IO.NODEPOWER;
    meta.size += nr;
    cursor(pre - 1);

    final int ins = pre - fpre;

    // all entries fit in current block
    if(nr < ENTRIES - npre + fpre) {
      // shift following entries forward and insert next entries
      copy(bf.data, ins, bf.data, ins + nr, npre - pre + 1);
      copy(entries, 0, bf.data, ins, nr);

      // update index entries
      for(int i = index + 1; i < blocks; ++i) fpres[i] += nr;
      npre += nr;
      return;
    }

    // we need to reorganize entries

    // save entries to be added into new block after inserted blocks
    final int move = npre - pre;
    final byte[] rest = new byte[move << IO.NODEPOWER];
    copy(bf.data, ins, rest, 0, move);

    // make room in index for new blocks
    int newBlocks = (int) Math.ceil((double) nr / ENTRIES) + 1;
    // in case we insert at block boundary
    if(pre == npre) --newBlocks;

    // resize the index
    final int s = allBlocks + newBlocks;
    fpres = Arrays.copyOf(fpres, s);
    pages = Arrays.copyOf(pages, s);

    Array.move(fpres, index + 1, newBlocks, blocks - index - 1);
    Array.move(pages, index + 1, newBlocks, blocks - index - 1);

    // add blocks for new entries
    int remain = nr;
    int pos = 0;
    while(remain > 0) {
      newBlock();
      copy(entries, pos, bf.data, 0, Math.min(remain, ENTRIES));

      fpres[++index] = nr - remain + pre;
      pages[index] = (int) bf.pos;
      ++blocks;
      remain -= ENTRIES;
      pos += ENTRIES;
    }

    // add remaining part of split block
    if(rest.length > 0) {
      newBlock();
      copy(rest, 0, bf.data, 0, move);

      fpres[++index] = pre + nr;
      pages[index] = (int) bf.pos;
      ++blocks;
    }

    // update index entries
    for(int i = index + 1; i < blocks; ++i) fpres[i] += nr;

    // update cached variables
    fpre = pre;
    if(rest.length > 0) fpre += nr;
    npre = index + 1 >= blocks ? meta.size : fpres[index + 1];
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
        np = m == last ? fp + ENTRIES : fpres[m + 1];
      }
      if(l > h) Util.notexpected("Data Access out of bounds [pre:" + pre +
          ", indexSize:" + blocks + ", access:" + l + " > " + h + "]");

      readBlock(m, fp, np);
    }
    return pre - fpre << IO.NODEPOWER;
  }

  /**
   * Fetches the requested block and update pointers.
   * @param i index number of the block to fetch
   * @param f first entry in that block
   * @param n first entry in the next block
   */
  private void readBlock(final int i, final int f, final int n) {
    index = i;
    fpre = f;
    npre = n;

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
   * Checks whether the current block needs to be written and write it.
   * @param buf buffer to write
   * @throws IOException I/O exception
   */
  private void writeBlock(final Buffer buf) throws IOException {
    data.seek(buf.pos * IO.BLOCKSIZE);
    data.write(buf.data);
    buf.dirty = false;
  }

  /**
   * Fetches next block.
   */
  private void nextBlock() {
    readBlock(index + 1, npre, index + 2 >= blocks ? meta.size :
      fpres[index + 2]);
  }

  /**
   * Updates the firstPre index entries.
   * @param nr number of entries to move
   */
  private void updatePre(final int nr) {
    // update index entries for all following blocks and reduce counter
    for(int i = index + 1; i < blocks; ++i) fpres[i] -= nr;
    meta.size -= nr;
    npre = index + 1 >= blocks ? meta.size : fpres[index + 1];
  }

  /**
   * Creates a new, empty block.
   */
  private void newBlock() {
    try {
      if(bf.dirty) writeBlock(bf);
    } catch(final IOException ex) {
      Util.stack(ex);
    }
    bf.pos = allBlocks++;
    bf.dirty = true;
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

  // TEST METHODS =============================================================

  /**
   * Returns the number of entries; needed for JUnit tests.
   * @return number of used blocks
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
