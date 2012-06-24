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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableDiskAccess extends TableAccess {
  /** Buffer manager. */
  private final Buffers bm = new Buffers();
  /** File storing all blocks. */
  private final RandomAccessFile file;
  /** File lock. */
  private FileLock fl;

  /** FirstPre values (sorted ascending; length: {@link #blocks}). */
  private int[] fpres;
  /** Page index (length: {@link #blocks}). */
  private int[] pages;
  /** Bitmap storing free (=0) and occupied (=1) pages. */
  private final BitArray pagemap;

  /** Pre value of the first entry in the current block. */
  private int fpre = -1;
  /** First pre value of the next block. */
  private int npre = -1;

  /** Total number of blocks. */
  private int blocks;
  /** Number of used blocks. */
  private int used;
  /** Index of the current block number in the {@link #pages} array. */
  private int index = -1;

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
    blocks = in.readNum();
    used   = in.readNum();
    fpres  = in.readNums();
    pages  = in.readNums();

    final int psize = in.readNum();
    // check if the page map has been stored
    if(psize == 0) {
      // init the map with empty pages
      pagemap = new BitArray(blocks);
      for(final int p : pages) pagemap.set(p);
      dirty = true;
    } else {
      pagemap = new BitArray(in.readLongs(psize), blocks);
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
    final IOFile table = MetaData.file(ctx.mprop.dbpath(db), DATATBL);
    if(!table.exists()) return false;

    final RandomAccessFile file;
    try {
      file = new RandomAccessFile(table.file(), "rw");
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
      return true; // [CG] why do we return true when IO error occurs?
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

    out.writeLongs(pagemap.toArray());
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
    dirty = true;

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
        pagemap.clear(pages[index]);

        Array.move(fpres, index + 1, -1, used - index - 1);
        Array.move(pages, index + 1, -1, used - index - 1);

        --used;
        readIndex(index);
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
        pagemap.clear(pages[index]);
      }
      setIndex(index + 1);
      from = 0;
    }

    // if the last block is empty, clear the corresponding bit
    readBlock(pages[index]);
    final Buffer bf = bm.current();
    if(npre == last) {
      pagemap.clear((int) bf.pos);
      ++unused;
      if(index < used - 1) readIndex(index + 1);
      else ++index;
    } else {
      // delete entries at beginning of current (last) block
      copy(bf.data, last - fpre, bf.data, 0, npre - last);
    }

    // now remove them from the index
    if(unused > 0) {
      Array.move(fpres, index, -unused, used - index);
      Array.move(pages, index, -unused, used - index);
      used -= unused;
      index -= unused;
    }

    // update index entry for this block
    fpres[index] = pre;
    fpre = pre;
    updatePre(nr);
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    final int nnew = entries.length;
    if(nnew == 0) return;
    dirty = true;

    // number of records to be inserted
    final int nr = nnew >>> IO.NODEPOWER;

    int split = 0;
    if(pre == 0) {
      // empty database: insert new data into first block
      readIndex(0);
      pagemap.set(0);
      ++used;
    } else {
      // find the offset within the block where the new records will be inserted
      split = cursor(pre - 1) + IO.NODESIZE;
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
      for(int i = index + 1; i < used; ++i) fpres[i] += nr;
      // update cached variables (fpre is not changed)
      npre += nr;
      meta.size += nr;
      return;
    }

    // append old entries at the end of the new entries
    // [DP] Storage: the following can be optimized to avoid copying arrays
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
      if(index + 1 < used) {
        final int o = occSpace(index + 1) << IO.NODEPOWER;
        if(remain <= IO.BLOCKSIZE - o) {
          // copy the last records
          readIndex(index + 1);
          bf = bm.current();
          System.arraycopy(bf.data, 0, bf.data, remain, o);
          System.arraycopy(all, all.length - remain, bf.data, 0, remain);
          bf.dirty = true;
          // reduce the pre value, since it will be later incremented with nr
          fpres[index] -= remain >>> IO.NODEPOWER;
          // go back to the previous block
          readIndex(index - 1);
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
    Array.move(fpres, index + 1, needed, used - index - 1);
    Array.move(pages, index + 1, needed, used - index - 1);

    // write the all remaining entries
    while(needed-- > 0) {
      freeBlock();
      nrem += write(all, nrem);
      fpres[index] = fpres[index - 1] + IO.ENTRIES;
      pages[index] = (int) bm.current().pos;
    }

    // increment all fpre values after the last modified block
    for(int i = index + 1; i < used; ++i) fpres[i] += nr;

    meta.size += nr;

    // update cached variables
    fpre = fpres[index];
    npre = index + 1 < used && fpres[index + 1] < meta.size ?
        fpres[index + 1] : meta.size;
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
      int m = index;
      while(l <= h) {
        if(pre < fp) h = m - 1;
        else if(pre >= np) l = m + 1;
        else break;
        m = h + l >>> 1;
        fp = fpres[m];
        np = m == last ? meta.size : fpres[m + 1];
      }
      if(l > h) Util.notexpected(
          "Data Access out of bounds:" +
          "\n- pre value: " + pre +
          "\n- #used blocks: " + used +
          "\n- #total locks: " + blocks +
          "\n- access: " + m + " (" + l + " > " + h + ']');
      readIndex(m);
    }
    return pre - fpre << IO.NODEPOWER;
  }

  /**
   * Update the index pointers.
   * @param i new index
   */
  private void setIndex(final int i) {
    index = i;
    fpre = fpres[i];
    npre = i + 1 >= used ? meta.size : fpres[i + 1];
  }

  /**
   * Updates the index pointers and fetches the requested block.
   * @param i index of the block to fetch
   */
  private void readIndex(final int i) {
    setIndex(i);
    readBlock(pages[i]);
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
    final int b = pagemap.nextFree(0);
    pagemap.set(b);
    readBlock(b);
    ++used;
    ++index;
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
    for(int i = index + 1; i < used; ++i) fpres[i] -= nr;
    meta.size -= nr;
    npre = index + 1 < used && fpres[index + 1] < meta.size ? fpres[index + 1] :
      meta.size;
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
