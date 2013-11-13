package org.basex.io.random;

import static org.basex.data.DataText.*;

import java.io.*;
import java.nio.channels.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;

/**
 * This class stores the table on disk and reads it block-wise.
 *
 * NOTE: this class is not thread-safe.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableDiskAccess extends TableAccess {
  /** Buffer manager. */
  private final Buffers bm = new Buffers();
  /** File storing all blocks. */
  private final RandomAccessFile file;
  /** Bitmap storing free (=0) and used (=1) pages. */
  private BitArray usedPages;
  /** File lock. */
  private FileLock fl;

  /** First pre values (ascending order); will be initialized with the first update. */
  private int[] fpres;
  /** Page index; will be initialized with the first update. */
  private int[] pages;

  /** Page index. */
  private int page = -1;
  /** Pre value of the first entry in the current block. */
  private int fpre = -1;
  /** First pre value of the next block. */
  private int npre = -1;

  /** Total number of blocks. */
  private int blocks;
  /** Number of used blocks. */
  private int used;

  /**
   * Constructor.
   * @param md meta data
   * @param lock exclusive access
   * @throws IOException I/O exception
   */
  public TableDiskAccess(final MetaData md, final boolean lock) throws IOException {
    super(md);

    // read meta and index data
    final DataInput in = new DataInput(meta.dbfile(DATATBL + 'i'));
    final int b = in.readNum();
    blocks = b;

    // check if page index is regular and can be calculated (0: no pages)
    final int u = in.readNum();
    final boolean regular = u == 0 || u == Integer.MAX_VALUE;
    if(regular) {
      used = u == 0 ? 0 : b;
    } else {
      // read page index and first pre values from disk
      used = u;
      fpres = in.readNums();
      pages = in.readNums();
    }

    // read block bitmap
    if(!regular) {
      final int psize = in.readNum();
      usedPages = new BitArray(in.readLongs(psize), used);
    }
    in.close();

    // initialize data file
    file = new RandomAccessFile(meta.dbfile(DATATBL).file(), "rw");
    if(lock) exclusiveLock();
    else sharedLock();
    if(fl == null) throw new BaseXException(Text.DB_PINNED_X, md.name);
  }

  /**
   * Checks if the table of the specified database is locked.
   * @param db name of database
   * @param ctx database context
   * @return result of check
   */
  public static boolean locked(final String db, final Context ctx) {
    final IOFile table = MetaData.file(ctx.globalopts.dbpath(db), DATATBL);
    if(!table.exists()) return false;

    try {
      final RandomAccessFile file = new RandomAccessFile(table.file(), "rw");
      try {
        return file.getChannel().tryLock() == null;
      } finally {
        file.close();
      }
    } catch(final OverlappingFileLockException ex) {
      return true;
    } catch(final ClosedChannelException ex) {
      return false;
    } catch(final IOException ex) {
      return true;
    }
  }

  @Override
  public synchronized void flush() throws IOException {
    for(final Buffer b : bm.all()) if(b.dirty) writeBlock(b);
    if(!dirty) return;

    final DataOutput out = new DataOutput(meta.dbfile(DATATBL + 'i'));
    out.writeNum(blocks);
    out.writeNum(used);

    // due to legacy issues, number of blocks is written several times
    out.writeNum(blocks);
    for(int a = 0; a < blocks; a++) out.writeNum(fpres[a]);
    out.writeNum(blocks);
    for(int a = 0; a < blocks; a++) out.writeNum(pages[a]);

    out.writeLongs(usedPages.toArray());
    out.close();
    dirty = false;
  }

  @Override
  public synchronized void close() throws IOException {
    flush();
    file.close();
  }

  @Override
  public boolean lock(final boolean lock) {
    try {
      if(lock) {
        if(exclusiveLock()) return true;
        if(sharedLock()) return false;
      } else {
        if(sharedLock()) return true;
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
    throw Util.notexpected((lock ? "Exclusive" : "Shared") +
        " lock could not be acquired.");
  }

  /**
   * Acquires an exclusive lock on the file.
   * @return success flag
   * @throws IOException I/O exception
   */
  private boolean exclusiveLock() throws IOException {
    return lck(false);
  }

  /**
   * Acquires a shared lock on the file.
   * @return success flag
   * @throws IOException I/O exception
   */
  private boolean sharedLock() throws IOException {
    return lck(true);
  }

  /**
   * Acquires a lock on the file. Does nothing if the correct lock has already been
   * acquired. Otherwise, releases an existing lock.
   * @param shared shared/exclusive lock
   * @return success flag
   * @throws IOException I/O exception
   */
  private boolean lck(final boolean shared) throws IOException {
    if(fl != null && shared == fl.isShared()) return true;
    if(fl != null) fl.release();
    fl = file.getChannel().tryLock(0, Long.MAX_VALUE, shared);
    return fl != null;
  }

  @Override
  public synchronized int read1(final int pre, final int off) {
    final int o = off + cursor(pre);
    final byte[] b = bm.current().data;
    return b[o] & 0xFF;
  }

  @Override
  public synchronized int read2(final int pre, final int off) {
    final int o = off + cursor(pre);
    final byte[] b = bm.current().data;
    return ((b[o] & 0xFF) << 8) + (b[o + 1] & 0xFF);
  }

  @Override
  public synchronized int read4(final int pre, final int off) {
    final int o = off + cursor(pre);
    final byte[] b = bm.current().data;
    return ((b[o] & 0xFF) << 24) + ((b[o + 1] & 0xFF) << 16) +
      ((b[o + 2] & 0xFF) << 8) + (b[o + 3] & 0xFF);
  }

  @Override
  public synchronized long read5(final int pre, final int off) {
    final int o = off + cursor(pre);
    final byte[] b = bm.current().data;
    return ((long) (b[o] & 0xFF) << 32) + ((long) (b[o + 1] & 0xFF) << 24) +
      ((b[o + 2] & 0xFF) << 16) + ((b[o + 3] & 0xFF) << 8) + (b[o + 4] & 0xFF);
  }

  @Override
  public void write1(final int pre, final int off, final int v) {
    final int o = off + cursor(pre);
    final Buffer bf = bm.current();
    final byte[] b = bf.data;
    b[o] = (byte) v;
    bf.dirty = true;
  }

  @Override
  public void write2(final int pre, final int off, final int v) {
    final int o = off + cursor(pre);
    final Buffer bf = bm.current();
    final byte[] b = bf.data;
    b[o] = (byte) (v >>> 8);
    b[o + 1] = (byte) v;
    bf.dirty = true;
  }

  @Override
  public void write4(final int pre, final int off, final int v) {
    final int o = off + cursor(pre);
    final Buffer bf = bm.current();
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
    final Buffer bf = bm.current();
    final byte[] b = bf.data;
    b[o]     = (byte) (v >>> 32);
    b[o + 1] = (byte) (v >>> 24);
    b[o + 2] = (byte) (v >>> 16);
    b[o + 3] = (byte) (v >>> 8);
    b[o + 4] = (byte) v;
    bf.dirty = true;
  }

  @Override
  protected void copy(final byte[] entries, final int pre, final int last) {
    for(int o = 0, i = pre; i < last; ++i, o += IO.NODESIZE) {
      final int off = cursor(i);
      final Buffer bf = bm.current();
      System.arraycopy(entries, o, bf.data, off, IO.NODESIZE);
      bf.dirty = true;
    }
  }

  @Override
  public void delete(final int pre, final int nr) {
    if(nr == 0) return;
    dirty();

    // get first block
    cursor(pre);

    // some useful variables to make code more readable
    int from = pre - fpre;
    final int last = pre + nr;

    // check if all entries are in current block: handle and return
    if(last - 1 < npre) {
      final Buffer bf = bm.current();
      copy(bf.data, from + nr, bf.data, from, npre - last);
      updatePre(nr);

      // if whole block was deleted, remove it from the index
      if(npre == fpre) {
        // mark the block as empty
        usedPages.clear(pages[page]);

        Array.move(fpres, page + 1, -1, used - page - 1);
        Array.move(pages, page + 1, -1, used - page - 1);

        --used;
        readPage(page);
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
        // blocks may not be consecutive
        usedPages.clear(pages[page]);
      }
      setPage(page + 1);
      from = 0;
    }

    // if the last block is empty, clear the corresponding bit
    readBlock(pages[page]);
    final Buffer bf = bm.current();
    if(npre == last) {
      usedPages.clear((int) bf.pos);
      ++unused;
      if(page < used - 1) readPage(page + 1);
      else ++page;
    } else {
      // delete entries at beginning of current (last) block
      copy(bf.data, last - fpre, bf.data, 0, npre - last);
    }

    // now remove them from the index
    if(unused > 0) {
      Array.move(fpres, page, -unused, used - page);
      Array.move(pages, page, -unused, used - page);
      used -= unused;
      page -= unused;
    }

    // update index entry for this block
    fpres[page] = pre;
    fpre = pre;
    updatePre(nr);
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    final int nnew = entries.length;
    if(nnew == 0) return;
    dirty();

    // number of records to be inserted
    final int nr = nnew >>> IO.NODEPOWER;

    int split = 0;
    if(used == 0) {
      // special case: insert new data into first block if database is empty
      readPage(0);
      usedPages.set(0);
      ++used;
    } else if(pre > 0) {
      // find the offset within the block where the new records will be inserted
      split = cursor(pre - 1) + IO.NODESIZE;
    } else {
      // all insert operations will add data after first node.
      // i.e., there is no "insert before first document" statement
      Util.notexpected("Insertion at beginning of populated table.");
    }

    // number of bytes occupied by old records in the current block
    final int nold = npre - fpre << IO.NODEPOWER;
    // number of bytes occupied by old records which will be moved at the end
    final int moved = nold - split;

    // special case: all entries fit in the current block
    Buffer bf = bm.current();
    if(nold + nnew <= IO.BLOCKSIZE) {
      Array.move(bf.data, split, nnew, moved);
      System.arraycopy(entries, 0, bf.data, split, nnew);
      bf.dirty = true;

      // increment first pre-values of blocks after the last modified block
      for(int i = page + 1; i < used; ++i) fpres[i] += nr;
      // update cached variables (fpre is not changed)
      npre += nr;
      meta.size += nr;
      return;
    }

    // append old entries at the end of the new entries
    final byte[] all = new byte[nnew + moved];
    System.arraycopy(entries, 0, all, 0, nnew);
    System.arraycopy(bf.data, split, all, nnew, moved);

    // fill in the current block with new entries
    // number of bytes which fit in the first block
    int nrem = IO.BLOCKSIZE - split;
    if(nrem > 0) {
      System.arraycopy(all, 0, bf.data, split, nrem);
      bf.dirty = true;
    }

    // number of new required blocks and remaining bytes
    final int req = all.length - nrem;
    int needed = req / IO.BLOCKSIZE;
    final int remain = req % IO.BLOCKSIZE;

    if(remain > 0) {
      // check if the last entries can fit in the block after the current one
      if(page + 1 < used) {
        final int o = occSpace(page + 1) << IO.NODEPOWER;
        if(remain <= IO.BLOCKSIZE - o) {
          // copy the last records
          readPage(page + 1);
          bf = bm.current();
          System.arraycopy(bf.data, 0, bf.data, remain, o);
          System.arraycopy(all, all.length - remain, bf.data, 0, remain);
          bf.dirty = true;
          // reduce the pre value, since it will be later incremented with nr
          fpres[page] -= remain >>> IO.NODEPOWER;
          // go back to the previous block
          readPage(page - 1);
        } else {
          // there is not enough space in the block - allocate a new one
          ++needed;
        }
      } else {
        // this is the last block - allocate a new one
        ++needed;
      }
    }

    // number of expected blocks: existing blocks + needed block - empty blocks
    final int exp = blocks + needed - (blocks - used);
    if(exp > fpres.length) {
      // resize directory arrays if existing ones are too small
      final int ns = Math.max(fpres.length << 1, exp);
      fpres = Arrays.copyOf(fpres, ns);
      pages = Arrays.copyOf(pages, ns);
    }

    // make place for the blocks where the new entries will be written
    Array.move(fpres, page + 1, needed, used - page - 1);
    Array.move(pages, page + 1, needed, used - page - 1);

    // write the all remaining entries
    while(needed-- > 0) {
      freeBlock();
      nrem += write(all, nrem);
      fpres[page] = fpres[page - 1] + IO.ENTRIES;
      pages[page] = (int) bm.current().pos;
    }

    // increment all fpre values after the last modified block
    for(int i = page + 1; i < used; ++i) fpres[i] += nr;

    meta.size += nr;

    // update cached variables
    fpre = fpres[page];
    npre = page + 1 < used && fpres[page + 1] < meta.size ? fpres[page + 1] : meta.size;
  }

  @Override
  protected void dirty() {
    // initialize data structures required for performing updates
    if(fpres == null) {
      final int b = blocks;
      fpres = new int[b];
      pages = new int[b];
      for(int i = 0; i < b; i++) {
        fpres[i] = i * IO.ENTRIES;
        pages[i] = i;
      }
      usedPages = new BitArray(used, true);
    }
    dirty = true;
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Searches for the block containing the entry for the specified pre value.
   * Reads the block and returns its offset inside the block.
   * @param pre pre of the entry to search for
   * @return offset of the entry in the block
   */
  private int cursor(final int pre) {
    int fp = fpre;
    int np = npre;
    if(pre < fp || pre >= np) {
      final int last = used - 1;
      int l = 0;
      int h = last;
      int m = page;
      while(l <= h) {
        if(pre < fp) h = m - 1;
        else if(pre >= np) l = m + 1;
        else break;
        m = h + l >>> 1;
        fp = fpre(m);
        np = m == last ? meta.size : fpre(m + 1);
      }
      if(l > h) Util.notexpected(
          "Data Access out of bounds:" +
          "\n- pre value: " + pre +
          "\n- #used blocks: " + used +
          "\n- #total locks: " + blocks +
          "\n- access: " + m + " (" + l + " > " + h + ']');
      readPage(m);
    }
    return pre - fpre << IO.NODEPOWER;
  }

  /**
   * Updates the page pointers.
   * @param p page index
   */
  private void setPage(final int p) {
    page = p;
    fpre = fpre(p);
    npre = p + 1 >= used ? meta.size : fpre(p + 1);
  }

  /**
   * Updates the index pointers and fetches the requested block.
   * @param p index of the block to fetch
   */
  private void readPage(final int p) {
    setPage(p);
    readBlock(page(p));
  }

  /**
   * Return the specified page index.
   * @param p index of the block to fetch
   * @return pre value
   */
  private int page(final int p) {
    return pages == null ? p : pages[p];
  }

  /**
   * Return the specified pre value.
   * @param p index of the block to fetch
   * @return pre value
   */
  private int fpre(final int p) {
    return fpres == null ? p * IO.ENTRIES : fpres[p];
  }

  /**
   * Reads a block from disk.
   * @param b block to fetch
   */
  private void readBlock(final int b) {
    if(!bm.cursor(b)) return;

    final Buffer bf = bm.current();
    try {
      if(bf.dirty) writeBlock(bf);
      bf.pos = b;
      if(b >= blocks) {
        blocks = b + 1;
      } else {
        file.seek(bf.pos * IO.BLOCKSIZE);
        file.readFully(bf.data);
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Moves the cursor to a free block (either new or existing empty one).
   */
  private void freeBlock() {
    final int b = usedPages.nextFree(0);
    usedPages.set(b);
    readBlock(b);
    ++used;
    ++page;
  }

  /**
   * Writes the specified block to disk and resets the dirty flag.
   * @param bf buffer to write
   * @throws IOException I/O exception
   */
  private void writeBlock(final Buffer bf) throws IOException {
    file.seek(bf.pos * IO.BLOCKSIZE);
    file.write(bf.data);
    bf.dirty = false;
  }

  /**
   * Updates the firstPre index entries.
   * @param nr number of entries to move
   */
  private void updatePre(final int nr) {
    // update index entries for all following blocks and reduce counter
    for(int i = page + 1; i < used; ++i) fpres[i] -= nr;
    meta.size -= nr;
    npre = page + 1 < used && fpres[page + 1] < meta.size ? fpres[page + 1] : meta.size;
  }

  /**
   * Convenience method for copying blocks.
   * @param s source array
   * @param sp source position
   * @param d destination array
   * @param dp destination position
   * @param l source length
   */
  private void copy(final byte[] s, final int sp, final byte[] d, final int dp, final int l) {
    System.arraycopy(s, sp << IO.NODEPOWER, d, dp << IO.NODEPOWER, l << IO.NODEPOWER);
    bm.current().dirty = true;
  }

  /**
   * Fill the current buffer with bytes from the specified array from the
   * specified offset.
   * @param s source array
   * @param o offset from the beginning of the array
   * @return number of written bytes
   */
  private int write(final byte[] s, final int o) {
    final Buffer bf = bm.current();
    final int len = Math.min(IO.BLOCKSIZE, s.length - o);
    System.arraycopy(s, o, bf.data, 0, len);
    bf.dirty = true;
    return len;
  }

  /**
   * Calculate the occupied space in a block.
   * @param i index of the block
   * @return occupied space in number of records
   */
  private int occSpace(final int i) {
    return (i + 1 < used ? fpres[i + 1] : meta.size) - fpres[i];
  }
}
