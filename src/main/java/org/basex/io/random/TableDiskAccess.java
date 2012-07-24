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
import org.basex.io.random.Buffers.Selector;
import org.basex.util.*;

/**
 * This class stores the table on disk and reads it block-wise.
 *
 * NOTE: this class is not thread-safe.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableDiskAccess extends TableAccess {

  /**
   * The TableCursor class encapsulates information about the current position
   * in the TableDiskAccess data structure we are working with.
   * @author kgb
   *
   */
  private static final class TableCursor implements Buffers.Selector {
    /**
     * Sole constructor.
     * @param buffers the buffers manager
     */
    public TableCursor(final Buffers buffers) {
      bm = buffers;
    }
    /** Buffer manager. */
    private final Buffers bm;
    /** Page index. */
    public int page = -1;
    /** Pre value of the first entry in the current block. */
    public int fpre = -1;
    /** First pre value of the next block. */
    public int npre = -1;
    /** the Buffer, as long as acquired but not released. */
    public Buffer buffer;

    /** variable storing the Buffers.Selector index. */
    private int bufferIndex;

    @Override
    public void setSelectedBufferIndex(final int index) {
      bufferIndex = index;
    }
    @Override
    public int getSelectedBufferIndex() {
      return bufferIndex;
    }

    /**
     * Returns the bufffer containing the file contents starting at
     * blockNumber * IO.BLOCKSIZE.
     * NOTE: always call freeBuffer() when doen with the acquired buffer.
     * Use try...finally to do so. No write or disk operations will be
     * possible while any thread holds a buffer.
     * @param selector a Selector instance keeping index of the buffer
     *    previously requested by the caller before calling. On
     *    completion of the call, the index of the buffer returned
     *    by this call is stored in this instance.
     * @param blockNumber the part of the file we are interested in,
     *    starting at file position blockNumber * IO.BLOCKSIZE
     * @param write must be set to true if we intend to change the buffer
     */
    public void acquireBuffer(final Selector selector, final long blockNumber,
        final boolean write) {
      if (buffer != null) {
        bm.freeBuffer();
      }
      buffer = bm.acquireBuffer(selector, blockNumber, write);
    }

    /**
     * Frees a buffer previously qcquired with acquireBuffer().
     */
    public void freeBuffer() {
      if (buffer != null) {
        buffer = null;
        bm.freeBuffer();
      }
    }


  }

  /** each thread gets its own copy of the cursor varaible. */
  private final ThreadLocal<TableCursor> tableCursor
      = new ThreadLocal<TableCursor>(){
    @Override
    public TableCursor initialValue() {
      return new TableCursor(bm);
    }
  };

  /** Buffer manager. */
  final Buffers bm;
  /** File storing all blocks. */
  private final RandomAccessFile file;
  /** Bitmap storing free (=0) and occupied (=1) pages. */
  private final BitArray freePages;
  /** File lock. */
  private FileLock fl;

  /** FirstPre values (sorted ascending; length: {@link #blocks}). */
  private int[] fpres;
  /** Page index (length: {@link #blocks}). */
  private int[] pages;

  /** Access on the blocks variable must synchronizze on blocksLock.*/
  private final Object blocksLock = new Object();
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
    blocks = in.readNum();
    used   = in.readNum();
    fpres  = in.readNums();
    pages  = in.readNums();

    final int psize = in.readNum();
    // check if the page map has been stored
    if(psize == 0) {
      // init the map with empty pages
      freePages = new BitArray(blocks);
      for(final int p : pages) freePages.set(p);
      dirty = true;
    } else {
      freePages = new BitArray(in.readLongs(psize), blocks);
    }
    in.close();

    // initialize data file
    file = new RandomAccessFile(meta.dbfile(DATATBL).file(), "rw");
    bm = new Buffers(file);
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
      return true; // [CG] why do we return true when IO error occurs?
    }
  }

  @Override
  public void flush() throws IOException {
    bm.flush();
    if(!dirty) return;

    final DataOutput out = new DataOutput(meta.dbfile(DATATBL + 'i'));
    synchronized (blocksLock) {
      out.writeNum(blocks);
      out.writeNum(used);

      // due to legacy issues, number of blocks is written several times
      out.writeNum(blocks);
      for(int a = 0; a < blocks; a++) out.writeNum(fpres[a]);
      out.writeNum(blocks);
      for(int a = 0; a < blocks; a++) out.writeNum(pages[a]);
    }

    out.writeLongs(freePages.toArray());
    out.close();
    dirty = false;
  }

  @Override
  public void close() throws IOException {
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
  public int read1(final int pre, final int off) {
    TableCursor cursor = tableCursor.get();
    final int o = off + cursor(cursor, pre, false);
    try {
      final byte[] b = cursor.buffer.data;
      return b[o] & 0xFF;
    } finally {
      cursor.freeBuffer();
    }
  }

  @Override
  public int read2(final int pre, final int off) {
    TableCursor cursor = tableCursor.get();
    final int o = off + cursor(cursor, pre, false);
    try {
      final byte[] b = cursor.buffer.data;
      return ((b[o] & 0xFF) << 8) + (b[o + 1] & 0xFF);
    } finally {
      cursor.freeBuffer();
    }
  }

  @Override
  public int read4(final int pre, final int off) {
    TableCursor cursor = tableCursor.get();
    final int o = off + cursor(cursor, pre, false);
    try {
      final byte[] b = cursor.buffer.data;
      return ((b[o] & 0xFF) << 24) + ((b[o + 1] & 0xFF) << 16) +
          ((b[o + 2] & 0xFF) << 8) + (b[o + 3] & 0xFF);
    } finally {
      cursor.freeBuffer();
    }
  }

  @Override
  public long read5(final int pre, final int off) {
    TableCursor cursor = tableCursor.get();
    final int o = off + cursor(cursor, pre, false);
    try {
      final byte[] b = cursor.buffer.data;
      return ((long) (b[o] & 0xFF) << 32) + ((long) (b[o + 1] & 0xFF) << 24) +
          ((b[o + 2] & 0xFF) << 16) + ((b[o + 3] & 0xFF) << 8) + (b[o + 4] & 0xFF);
    } finally {
      cursor.freeBuffer();
    }
  }

  @Override
  public void write1(final int pre, final int off, final int v) {
    TableCursor cursor = tableCursor.get();
    final int o = off + cursor(cursor, pre, true);
    try {
      final Buffer bf = cursor.buffer;
      final byte[] b = bf.data;
      b[o] = (byte) v;
      bf.dirty = true;
    } finally {
      cursor.freeBuffer();
    }
  }

  @Override
  public void write2(final int pre, final int off, final int v) {
    TableCursor cursor = tableCursor.get();
    final int o = off + cursor(cursor, pre, true);
    try {
      final Buffer bf = cursor.buffer;
      final byte[] b = bf.data;
      b[o] = (byte) (v >>> 8);
      b[o + 1] = (byte) v;
      bf.dirty = true;
    } finally {
      cursor.freeBuffer();
    }
  }

  @Override
  public void write4(final int pre, final int off, final int v) {
    TableCursor cursor = tableCursor.get();
    final int o = off + cursor(cursor, pre, true);
    try {
      final Buffer bf = cursor.buffer;
      final byte[] b = bf.data;
      b[o]     = (byte) (v >>> 24);
      b[o + 1] = (byte) (v >>> 16);
      b[o + 2] = (byte) (v >>> 8);
      b[o + 3] = (byte) v;
      bf.dirty = true;
    } finally {
      cursor.freeBuffer();
    }
  }

  @Override
  public void write5(final int pre, final int off, final long v) {
    TableCursor cursor = tableCursor.get();
    final int o = off + cursor(cursor, pre, true);
    try {
      final Buffer bf = cursor.buffer;
      final byte[] b = bf.data;
      b[o]     = (byte) (v >>> 32);
      b[o + 1] = (byte) (v >>> 24);
      b[o + 2] = (byte) (v >>> 16);
      b[o + 3] = (byte) (v >>> 8);
      b[o + 4] = (byte) v;
      bf.dirty = true;
    } finally {
      cursor.freeBuffer();
    }
  }

  @Override
  protected void copy(final byte[] entries, final int pre, final int last) {
    if (pre < last) {  // only perform this if the loop will run at least once
      TableCursor cursor = tableCursor.get();
      try {
        for(int o = 0, i = pre; i < last; ++i, o += IO.NODESIZE) {
          final int off = cursor(cursor, i, true);
          final Buffer bf = cursor.buffer;
          System.arraycopy(entries, o, bf.data, off, IO.NODESIZE);
          bf.dirty = true;
        }
      } finally {
        cursor.freeBuffer();  // correct because loop has run at least once
      }
    }
  }

  @Override
  public void delete(final int pre, final int nr) {
    if(nr == 0) return;
    dirty = true;
    TableCursor cursor = tableCursor.get();

    // get first block
    cursor(cursor, pre, true);

    // some useful variables to make code more readable
    int from = pre - cursor.fpre;
    final int last = pre + nr;

    try {
      // check if all entries are in current block: handle and return
      if(last - 1 < cursor.npre) {
        copy(cursor, cursor.buffer.data, from + nr, cursor.buffer.data,
            from, cursor.npre - last);
        updatePre(cursor, nr);

        // if whole block was deleted, remove it from the index
        if(cursor.npre == cursor.fpre) {
          // mark the block as empty
          freePages.clear(pages[cursor.page]);

          Array.move(fpres, cursor.page + 1, -1, used - cursor.page - 1);
          Array.move(pages, cursor.page + 1, -1, used - cursor.page - 1);

          --used;
          readPage(cursor, cursor.page, false);
        }
        return;
      }

    } finally {
      cursor.freeBuffer();
    }

    // handle blocks whose entries are to be deleted entirely

    // first count them
    int unused = 0;
    while(cursor.npre < last) {
      if(from == 0) {
        ++unused;
        // mark the blocks as empty; range clear cannot be used because the
        // blocks may not be consecutive
        freePages.clear(pages[cursor.page]);
      }
      setPage(cursor, cursor.page + 1);
      from = 0;
    }

    // if the last block is empty, clear the corresponding bit
    readBlock(cursor, pages[cursor.page], true);
    try {
      final Buffer bf = cursor.buffer;
      if(cursor.npre == last) {
        freePages.clear((int) bf.pos);
        ++unused;
        if(cursor.page < used - 1) readPage(cursor, cursor.page + 1, true);
        else ++cursor.page;
      } else {
        // delete entries at beginning of current (last) block
        copy(cursor, bf.data, last - cursor.fpre, bf.data, 0, cursor.npre - last);
      }
    } finally {
      cursor.freeBuffer();
    }

    // now remove them from the index
    if(unused > 0) {
      Array.move(fpres, cursor.page, -unused, used - cursor.page);
      Array.move(pages, cursor.page, -unused, used - cursor.page);
      used -= unused;
      cursor.page -= unused;
    }

    // update index entry for this block
    fpres[cursor.page] = pre;
    cursor.fpre = pre;
    updatePre(cursor, nr);
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    final int nnew = entries.length;
    if(nnew == 0) return;
    dirty = true;

    // number of records to be inserted
    final int nr = nnew >>> IO.NODEPOWER;

    TableCursor cursor = tableCursor.get();
    int split = 0;
    if(used == 0) {
      // special case: insert new data into first block if database is empty
      readPage(cursor, 0, true);
      freePages.set(0);
      ++used;
    } else if(pre > 0) {
      // find the offset within the block where the new records will be inserted
      split = cursor(cursor, pre - 1, true) + IO.NODESIZE;
    } else {
      // all insert operations will add data after first node.
      // i.e., there is no "insert before first document" statement
      Util.notexpected("Insertion at beginning of populated table.");
    }

    // number of bytes occupied by old records in the current block
    final int nold = cursor.npre - cursor.fpre << IO.NODEPOWER;
    // number of bytes occupied by old records which will be moved at the end
    final int moved = nold - split;

    try {

      // special case: all entries fit in the current block
      Buffer bf = cursor.buffer;
      if(nold + nnew <= IO.BLOCKSIZE) {
        Array.move(bf.data, split, nnew, moved);
        System.arraycopy(entries, 0, bf.data, split, nnew);
        bf.dirty = true;

        // increment first pre-values of blocks after the last modified block
        for(int i = cursor.page + 1; i < used; ++i) fpres[i] += nr;
        // update cached variables (fpre is not changed)
        cursor.npre += nr;
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
        if(cursor.page + 1 < used) {
          final int o = occSpace(cursor.page + 1) << IO.NODEPOWER;
          if(remain <= IO.BLOCKSIZE - o) {
            // copy the last records
            readPage(cursor, cursor.page + 1, true);
            bf = cursor.buffer;
            System.arraycopy(bf.data, 0, bf.data, remain, o);
            System.arraycopy(all, all.length - remain, bf.data, 0, remain);
            bf.dirty = true;
            // reduce the pre value, since it will be later incremented with nr
            fpres[cursor.page] -= remain >>> IO.NODEPOWER;
            // go back to the previous block
            readPage(cursor, cursor.page - 1, true);
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
      final int exp;
      synchronized (blocksLock) {
        exp = blocks + needed - (blocks - used);
      }
      if(exp > fpres.length) {
        // resize directory arrays if existing ones are too small
        final int ns = Math.max(fpres.length << 1, exp);
        fpres = Arrays.copyOf(fpres, ns);
        pages = Arrays.copyOf(pages, ns);
      }

      // make place for the blocks where the new entries will be written
      Array.move(fpres, cursor.page + 1, needed, used - cursor.page - 1);
      Array.move(pages, cursor.page + 1, needed, used - cursor.page - 1);

      // write the all remaining entries
      while(needed-- > 0) {
        freeBlock(cursor);
        nrem += write(cursor, all, nrem);
        fpres[cursor.page] = fpres[cursor.page - 1] + IO.ENTRIES;
        pages[cursor.page] = (int) cursor.buffer.pos;
      }

    } finally {
      cursor.freeBuffer();
    }

    // increment all fpre values after the last modified block
    for(int i = cursor.page + 1; i < used; ++i) fpres[i] += nr;

    meta.size += nr;

    // update cached variables
    cursor.fpre = fpres[cursor.page];
    cursor.npre = cursor.page + 1 < used && fpres[cursor.page + 1] < meta.size ?
        fpres[cursor.page + 1] : meta.size;
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Searches for the block containing the entry for the specified pre value.
   * Reads the block and returns its offset inside the block.
   * @param cursor the cursor on which this operation shall operate
   * @param pre pre of the entry to search for
   * @param write must be set to true if we intend to change the buffer
   * @return offset of the entry in the block
   */
  private int cursor(final TableCursor cursor, final int pre, final boolean write) {
    setCurrentPage(cursor);
    int fp = cursor.fpre;
    int np = cursor.npre;
    if(pre < fp || pre >= np) {
      final int last = used - 1;
      int l = 0;
      int h = last;
      int m = cursor.page;
      while(l <= h) {
        if(pre < fp) h = m - 1;
        else if(pre >= np) l = m + 1;
        else break;
        m = h + l >>> 1;
        fp = fpres[m];
        np = m == last ? meta.size : fpres[m + 1];
      }
      if(l > h) {
        synchronized (blocksLock) {
        Util.notexpected(
          "Data Access out of bounds:" +
          "\n- pre value: " + pre +
          "\n- #used blocks: " + used +
          "\n- #total locks: " + blocks +
          "\n- access: " + m + " (" + l + " > " + h + ']');
        }
      }
      readPage(cursor, m, write);
    } else {
      readBlock(cursor, pages[cursor.page], write);
    }
    return pre - cursor.fpre << IO.NODEPOWER;
  }

  /**
   * Updates the page pointers.
   * @param cursor the cursor on which this operation shall operate
   * @param p page index
   */
  private void setPage(final TableCursor cursor, final int p) {
    cursor.page = p;
    setCurrentPage(cursor);
  }

  /**
   * Updates cursor.fpre and cursor.npre.
   * @param cursor the cursor on which this operation shall operate
   */
  private void setCurrentPage(final TableCursor cursor) {
    cursor.fpre = fpres[cursor.page];
    cursor.npre = cursor.page + 1 >= used ? meta.size : fpres[cursor.page + 1];
  }

  /**
   * Updates the index pointers and fetches the requested block.
   * @param cursor the cursor on which this operation shall operate
   * @param p index of the block to fetch
   * @param write must be set to true if we intend to change the buffer
   */
  private void readPage(final TableCursor cursor, final int p, final boolean write) {
    setPage(cursor, p);
    readBlock(cursor, pages[p], write);
  }

  /**
   * Reads a block from disk.
   * @param cursor the cursor on which this operation shall operate
   * @param b block to fetch
   * @param write must be set to true if we intend to change the buffer
   */
  private void readBlock(final TableCursor cursor, final int b, final boolean write) {
    synchronized (blocksLock) {
      if(b >= blocks) {
        blocks = b + 1;
      }
    }
    cursor.acquireBuffer(cursor, b, write);
  }

  /**
   * Moves the cursor to a free block (either new or existing empty one).
   * @param cursor the cursor on which this operation shall operate
   */
  private void freeBlock(final TableCursor cursor) {
    final int b = freePages.nextFree(0);
    freePages.set(b);
    readBlock(cursor, b, true);
    ++used;
    ++cursor.page;
  }

  /**
   * Updates the firstPre index entries.
   * @param cursor the cursor on which this operation shall operate
   * @param nr number of entries to move
   */
  private void updatePre(final TableCursor cursor, final int nr) {
    // update index entries for all following blocks and reduce counter
    for(int i = cursor.page + 1; i < used; ++i) fpres[i] -= nr;
    meta.size -= nr;
    cursor.npre = cursor.page + 1 < used && fpres[cursor.page + 1] < meta.size ?
      fpres[cursor.page + 1] : meta.size;
  }

  /**
   * Convenience method for copying blocks.
   * @param cursor the cursor on which this operation shall operate
   * @param s source array
   * @param sp source position
   * @param d destination array
   * @param dp destination position
   * @param l source length
   */
  private void copy(final TableCursor cursor, final byte[] s, final int sp,
      final byte[] d, final int dp, final int l) {
    System.arraycopy(s, sp << IO.NODEPOWER, d, dp << IO.NODEPOWER,
        l << IO.NODEPOWER);
    cursor.buffer.dirty = true;
  }

  /**
   * Fill the current buffer with bytes from the specified array from the
   * specified offset.
   * @param cursor the cursor on which this operation shall operate
   * @param s source array
   * @param o offset from the beginning of the array
   * @return number of written bytes
   */
  private int write(final TableCursor cursor, final byte[] s, final int o) {
    final Buffer bf = cursor.buffer;
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
