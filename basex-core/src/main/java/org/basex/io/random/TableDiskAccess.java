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
 * This class stores the table on disk and reads it page-wise.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableDiskAccess extends TableAccess {
  /** Number of table readers (must be 1 << n). */
  private static final int READERS = 1 << 4;
  /** Maximum number of pages to read ahead (must be 1 << n, at most half of the buffers). */
  private static final int AHEAD = 1 << 3;

  /** Table readers, hashed by thread ID (entries can be {@code null}). */
  private final TableReader[] readers = new TableReader[READERS];
  /** Reader with write access; used for updates and if no other reader is available. */
  private final TableReader master;
  /** Indicates if the table is read-locked and additional readers can be assigned. */
  private volatile boolean pooled;
  /** Bitmap storing free (=0) and used (=1) pages (can be {@code null}). */
  private BitArray usedPages;
  /** File lock (can be {@code null}). */
  private FileLock lock;

  /** First PRE values, ascending (can be {@code null}; set with the first update). */
  private int[] fPreIndex;
  /** Page index (can be {@code null}; set with the first update). */
  private int[] pageIndex;
  /** Total number of pages. */
  private int pages;
  /** Number of used pages. */
  private int used;

  /**
   * Constructor.
   * @param meta meta data
   * @param nodes number of nodes
   * @param write write lock
   * @throws IOException I/O exception
   */
  public TableDiskAccess(final MetaData meta, final int nodes, final boolean write)
      throws IOException {
    super(meta, nodes);

    // read meta and index data
    try(DataInput in = new DataInput(meta.dbFile(DATATBL + 'i'))) {
      // total number of pages
      pages = in.readNum();
      // number of used pages (0: empty table; MAX: no mapping)
      used = in.readNum();
      if(used == Integer.MAX_VALUE) {
        // no mapping: total and used number of pages is identical
        used = pages;
      } else if(used != 0) {
        // read page index and first PRE values from disk
        fPreIndex = in.readNums();
        pageIndex = in.readNums();
        // read block bitmap
        final int s = in.readNum();
        usedPages = new BitArray(in.readLongs(s), used);
      }
    }

    // initialize data file
    master = new TableReader(true, null);
    if(!lock(write)) throw new BaseXException(Text.DB_PINNED_X, meta.name);
  }

  /**
   * Checks if the table of the specified database is locked.
   * @param db name of database
   * @param ctx database context
   * @return result of check
   */
  public static boolean locked(final String db, final Context ctx) {
    final IOFile table = MetaData.file(ctx.soptions.dbPath(db), DATATBL);
    if(!table.exists()) return false;

    try(FileChannel fc = new RandomAccessFile(table.file(), "rw").getChannel()) {
      return fc.tryLock() == null;
    } catch(final IOException ex) {
      Util.debug(ex);
      return true;
    }
  }

  @Override
  public void flush(final boolean all) throws IOException {
    synchronized(master) {
      for(final Buffer buffer : master.buffers.all()) {
        master.write(buffer);
      }
      if(!dirty || !all) return;

      try(DataOutput out = new DataOutput(meta.dbFile(DATATBL + 'i'))) {
        final int p = pages;
        boolean regular = true;

        // check if page mapping is regular (are all pages used and in ascending order?)
        if(fPreIndex != null) {
          regular = p == used;
          for(int i = 0; i < p && regular; i++) regular = fPreIndex[i] == i * IO.ENTRIES;
          for(int i = 0; i < p && regular; i++) regular = pageIndex[i] == i;
          if(regular) removeMapping();
        }

        if(regular) {
          // no mapping available or required
          // (0: empty table; MAX: no mapping, see TableOutput#close)
          out.writeNum(p);
          out.writeNum(used == 0 ? 0 : Integer.MAX_VALUE);
        } else {
          out.writeNum(p);
          out.writeNum(used);
          out.writeNum(p);
          for(int s = 0; s < p; s++) out.writeNum(fPreIndex[s]);
          out.writeNum(p);
          for(int s = 0; s < p; s++) out.writeNum(pageIndex[s]);
          out.writeLongs(usedPages.toArray());
        }
      }
      dirty = false;
    }
  }

  @Override
  public void close() throws IOException {
    flush(true);
    drain();
    synchronized(master) {
      master.close();
    }
  }

  @Override
  public boolean lock(final boolean write) {
    try {
      if(lock != null) {
        if(write != lock.isShared()) return true;
        lock.release();
      }
      // readers must be closed before the file is locked exclusively
      if(write) drain();
      lock = master.file.getChannel().tryLock(0, Long.MAX_VALUE, !write);
      pooled = lock != null && !write;
      return lock != null;
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
  }

  @Override
  public int read1(final int pre, final int offset) {
    final TableReader reader = reader();
    if(reader != master) return reader.read1(pre, offset);
    synchronized(master) {
      return master.read1(pre, offset);
    }
  }

  @Override
  public int read2(final int pre, final int offset) {
    final TableReader reader = reader();
    if(reader != master) return reader.read2(pre, offset);
    synchronized(master) {
      return master.read2(pre, offset);
    }
  }

  @Override
  public int read4(final int pre, final int offset) {
    final TableReader reader = reader();
    if(reader != master) return reader.read4(pre, offset);
    synchronized(master) {
      return master.read4(pre, offset);
    }
  }

  @Override
  public long read5(final int pre, final int offset) {
    final TableReader reader = reader();
    if(reader != master) return reader.read5(pre, offset);
    synchronized(master) {
      return master.read5(pre, offset);
    }
  }

  @Override
  public void write1(final int pre, final int offset, final int value) {
    final int o = offset + master.cursor(pre);
    final Buffer buffer = master.buffers.current();
    buffer.data[o] = (byte) value;
    buffer.dirty = true;
  }

  @Override
  public void write2(final int pre, final int offset, final int value) {
    final int o = offset + master.cursor(pre);
    final Buffer buffer = master.buffers.current();
    final byte[] data = buffer.data;
    data[o] = (byte) (value >>> 8);
    data[o + 1] = (byte) value;
    buffer.dirty = true;
  }

  @Override
  public void write4(final int pre, final int offset, final int value) {
    final int o = offset + master.cursor(pre);
    final Buffer buffer = master.buffers.current();
    final byte[] data = buffer.data;
    data[o]     = (byte) (value >>> 24);
    data[o + 1] = (byte) (value >>> 16);
    data[o + 2] = (byte) (value >>> 8);
    data[o + 3] = (byte) value;
    buffer.dirty = true;
  }

  @Override
  public void write5(final int pre, final int offset, final long value) {
    final int o = offset + master.cursor(pre);
    final Buffer buffer = master.buffers.current();
    final byte[] data = buffer.data;
    data[o]     = (byte) (value >>> 32);
    data[o + 1] = (byte) (value >>> 24);
    data[o + 2] = (byte) (value >>> 16);
    data[o + 3] = (byte) (value >>> 8);
    data[o + 4] = (byte) value;
    buffer.dirty = true;
  }

  @Override
  protected void copy(final byte[] entries, final int first, final int last) {
    dirty();
    for(int o = 0, i = first; i < last; ++i, o += IO.NODESIZE) {
      final int off = master.cursor(i);
      final Buffer buffer = master.buffers.current();
      Array.copy(entries, o, IO.NODESIZE, buffer.data, off);
      buffer.dirty = true;
    }
  }

  @Override
  public void delete(final int pre, final int count) {
    if(count == 0) return;

    // get first page
    dirty();
    master.cursor(pre);

    // some useful variables to make code more readable
    int from = pre - master.firstPre;
    final int last = pre + count;

    // check if all entries are in current page
    if(last <= master.nextPre) {
      // move entries in current page and decreases pointers to PRE values
      if(last < master.nextPre) {
        delete(master.buffers.current(), from, from + count, master.nextPre - last);
      }
      decreasePre(count);

      // if whole page was deleted, remove it from the index
      if(master.firstPre == master.nextPre) {
        // mark the page as empty
        usedPages.clear(pageIndex[master.page]);
        deletePages(1);
        master.readPage(master.page);
      }
    } else {
      // handle pages whose entries are to be deleted entirely

      // first count them
      int unused = 0;
      while(last > master.nextPre) {
        if(from == 0) {
          ++unused;
          // mark the pages as empty; range clear cannot be used because the
          // pages may not be consecutive
          usedPages.clear(pageIndex[master.page]);
        }
        master.setPage(master.page + 1);
        from = 0;
      }

      // if the last page is empty, clear the corresponding bit
      master.read(pageIndex[master.page]);
      final Buffer buffer = master.buffers.current();
      if(last == master.nextPre) {
        usedPages.clear((int) buffer.pos);
        ++unused;
        if(master.page + 1 < used) master.readPage(master.page + 1);
        else ++master.page;
      } else {
        // delete entries at beginning of current (last) page
        delete(buffer, 0, last - master.firstPre, master.nextPre - last);
      }

      // now remove them from the index
      if(unused > 0) {
        master.page -= unused;
        deletePages(unused);
      }

      // update index entry for this page
      fPreIndex[master.page] = pre;
      master.firstPre = pre;
      decreasePre(count);
    }
    if(used == 0) {
      master.buffers.init();
      removeMapping();
      pages = 1;
    }
  }

  @Override
  public void insert(final int pre, final byte[] entries) {
    final int nnew = entries.length;
    if(nnew == 0) return;
    dirty();

    // number of entries to be inserted
    final int nr = nnew >>> IO.NODEPOWER;

    int split = 0;
    if(used == 0) {
      // special case: insert new data into first page if database is empty
      master.readPage(0);
      usedPages.set(0);
      ++used;
    } else if(pre > 0) {
      // find the offset within the page where the new records will be inserted
      split = master.cursor(pre - 1) + IO.NODESIZE;
    }

    // number of bytes occupied by old records in the current page
    final int nold = master.nextPre - master.firstPre << IO.NODEPOWER;
    // number of bytes occupied by old records which will be moved at the end
    final int moved = nold - split;

    // special case: all entries fit in the current page
    Buffer buffer = master.buffers.current();
    if(nold + nnew <= IO.BLOCKSIZE) {
      Array.insert(buffer.data, split, nnew, nold, entries);
      buffer.dirty = true;

      // increment first pre-values of pages after the last modified page
      for(int i = master.page + 1; i < used; ++i) fPreIndex[i] += nr;
      // update cached variables (fpre is not changed)
      master.nextPre += nr;
      nodes += nr;
      return;
    }

    // append old entries at the end of the new entries
    final byte[] all = new byte[nnew + moved];
    Array.copy(entries, nnew, all);
    Array.copy(buffer.data, split, moved, all, nnew);

    // fill in the current page with new entries
    // number of bytes which fit in the first page
    int nrem = IO.BLOCKSIZE - split;
    if(nrem > 0) {
      Array.copyFromStart(all, nrem, buffer.data, split);
      buffer.dirty = true;
    }

    // number of new required pages and remaining bytes
    final int req = all.length - nrem;
    int needed = req >>> IO.BLOCKPOWER;
    final int remain = req & IO.BLOCKSIZE - 1;

    if(remain > 0) {
      // check if the last entries can fit in the page after the current one
      if(master.page + 1 < used) {
        final int o = occSpace(master.page + 1) << IO.NODEPOWER;
        if(remain <= IO.BLOCKSIZE - o) {
          // copy the last records
          master.readPage(master.page + 1);
          buffer = master.buffers.current();
          Array.copyFromStart(buffer.data, o, buffer.data, remain);
          Array.copyToStart(all, all.length - remain, remain, buffer.data);
          buffer.dirty = true;
          // reduce the PRE value, since it will be later incremented with nr
          fPreIndex[master.page] -= remain >>> IO.NODEPOWER;
          // go back to the previous page
          master.readPage(master.page - 1);
        } else {
          // there is not enough space in the page - allocate a new one
          ++needed;
        }
      } else {
        // this is the last page - allocate a new one
        ++needed;
      }
    }

    // number of expected pages: existing pages + needed page - empty pages
    final int exp = pages + needed - (pages - used);
    if(exp > fPreIndex.length) {
      // resize directory arrays if existing ones are too small
      final int ns = Math.max(fPreIndex.length << 1, exp);
      fPreIndex = Arrays.copyOf(fPreIndex, ns);
      pageIndex = Arrays.copyOf(pageIndex, ns);
    }

    // make place for the pages where the new entries will be written
    Array.insert(fPreIndex, master.page + 1, needed, used, null);
    Array.insert(pageIndex, master.page + 1, needed, used, null);

    // write the all remaining entries
    while(needed-- > 0) {
      final int p = usedPages.nextFree();
      usedPages.set(p);
      master.read(p);
      ++used;
      ++master.page;
      nrem += write(all, nrem);
      fPreIndex[master.page] = fPreIndex[master.page - 1] + IO.ENTRIES;
      pageIndex[master.page] = (int) master.buffers.current().pos;
    }

    // increment all fpre values after the last modified page
    for(int i = master.page + 1; i < used; ++i) fPreIndex[i] += nr;

    nodes += nr;

    // update cached variables
    master.firstPre = fPreIndex[master.page];
    master.nextPre = master.page + 1 < used && fPreIndex[master.page + 1] < nodes ?
      fPreIndex[master.page + 1] : nodes;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(Util.className(this)).append(" (").append("pages: ").append(pages);
    sb.append(", used: ").append(used).append(", page: ").append(master.page);
    sb.append(", firstPre: ").append(master.firstPre);
    sb.append(", nextPre: ").append(master.nextPre).append(")");
    if(fPreIndex != null) sb.append("\n- FPres: ").append(Arrays.toString(fPreIndex));
    if(pageIndex != null) sb.append("\n- Pages: ").append(Arrays.toString(pageIndex));
    if(usedPages != null) sb.append("\n- Used Pages: ").append(usedPages);
    return sb.toString();
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns a table reader for the current thread.
   * @return table reader
   */
  private TableReader reader() {
    if(!pooled) return master;
    // a reader is confined to its owner, so its cursor and buffers need no monitor
    final Thread thread = Thread.currentThread();
    final TableReader reader = readers[(int) (thread.threadId() & READERS - 1)];
    return reader != null && reader.owner == thread ? reader : newReader(thread);
  }

  /**
   * Assigns a table reader to the specified thread.
   * @param thread thread to assign the reader to
   * @return table reader
   */
  private TableReader newReader(final Thread thread) {
    synchronized(readers) {
      if(!pooled) return master;
      final int start = (int) (thread.threadId() & READERS - 1);
      for(int i = 0; i < READERS; i++) {
        final int r = start + i & READERS - 1;
        final TableReader reader = readers[r];
        if(reader == null) {
          try {
            final TableReader tr = new TableReader(false, thread);
            readers[r] = tr;
            return tr;
          } catch(final IOException ex) {
            // fall back to the master reader (e.g. if too many files are open)
            Util.debug(ex);
            return master;
          }
        }
        // adopt the reader of a thread that has terminated
        if(reader.owner == thread || !reader.owner.isAlive()) {
          reader.owner = thread;
          return reader;
        }
      }
      // all readers are in use: fall back to the shared master reader
      return master;
    }
  }

  /**
   * Closes all assigned table readers.
   * @throws IOException I/O exception
   */
  private void drain() throws IOException {
    pooled = false;
    synchronized(readers) {
      // no reader can be in flight: the caller holds, or is about to acquire, the write lock
      for(int r = 0; r < READERS; r++) {
        final TableReader reader = readers[r];
        if(reader != null) {
          readers[r] = null;
          reader.closed = true;
          reader.close();
        }
      }
    }
  }

  /**
   * Marks the data structures as dirty.
   */
  private void dirty() {
    // initialize data structures required for performing updates
    if(fPreIndex == null) {
      fPreIndex = new int[pages];
      for(int i = 0; i < pages; i++) fPreIndex[i] = i * IO.ENTRIES;
      pageIndex = new int[pages];
      for(int i = 0; i < pages; i++) pageIndex[i] = i;
      usedPages = new BitArray(used, true);
    }
    dirty = true;
  }

  /**
   * Return the specified PRE value.
   * @param pre index of the page to fetch
   * @return PRE value
   */
  private int fpre(final int pre) {
    return fPreIndex == null ? pre * IO.ENTRIES : fPreIndex[pre];
  }

  /**
   * Deletes pages in the page mapping.
   * @param count number of pages to delete
   */
  private void deletePages(final int count) {
    Array.remove(fPreIndex, master.page, count, used);
    Array.remove(pageIndex, master.page, count, used);
    used -= count;
  }

  /**
   * Decreases pointers to PRE value.
   * @param count number of entries to move
   */
  private void decreasePre(final int count) {
    final int nextPage = master.page + 1;
    for(int i = nextPage; i < used; ++i) fPreIndex[i] -= count;
    nodes -= count;
    master.nextPre = nextPage < used && fPreIndex[nextPage] < nodes ? fPreIndex[nextPage] : nodes;
  }

  /**
   * Convenience method for deleting buffer entries.
   * @param buffer buffer
   * @param from first entry to delete
   * @param to last entry to delete
   * @param length source length
   */
  private static void delete(final Buffer buffer, final int from, final int to, final int length) {
    final byte[] array = buffer.data;
    Array.copy(array, to << IO.NODEPOWER, length << IO.NODEPOWER, array, from << IO.NODEPOWER);
    buffer.dirty = true;
  }

  /**
   * Fills the current buffer with bytes from the specified array and offset.
   * @param array source array
   * @param offset array offset
   * @return number of written bytes
   */
  private int write(final byte[] array, final int offset) {
    final Buffer buffer = master.buffers.current();
    final int len = Math.min(IO.BLOCKSIZE, array.length - offset);
    Array.copyToStart(array, offset, len, buffer.data);
    buffer.dirty = true;
    return len;
  }

  /**
   * Calculates the occupied space in a page.
   * @param index page index
   * @return occupied space
   */
  private int occSpace(final int index) {
    return (index + 1 < used ? fPreIndex[index + 1] : nodes) - fPreIndex[index];
  }

  /**
   * Removes the page index.
   */
  private void removeMapping() {
    fPreIndex = null;
    pageIndex = null;
    usedPages = null;
  }

  /**
   * Page cursor with an exclusive file handle and buffer pool.
   */
  private final class TableReader {
    /** Buffer manager. */
    private final Buffers buffers = new Buffers();
    /** File storing all pages. */
    private final RandomAccessFile file;
    /** Thread this reader is confined to; only assigned while its previous owner is gone. */
    private Thread owner;
    /** Indicates that the reader was closed by a concurrent update. */
    private volatile boolean closed;

    /** Number of pages to read ahead. */
    private int ahead = 1;
    /** Page that continues the current sequential run. */
    private int nextRead = -1;
    /** Buffer for reading ahead (can be {@code null}). */
    private byte[] scratch;

    /** Pointer to current page. */
    private int page = -1;
    /** Pre value of the first entry in the current page. */
    private int firstPre = -1;
    /** First PRE value of the next page. */
    private int nextPre = -1;

    /**
     * Constructor.
     * @param write open file for writing
     * @param owner thread this reader is confined to (can be {@code null})
     * @throws IOException I/O exception
     */
    private TableReader(final boolean write, final Thread owner) throws IOException {
      file = new RandomAccessFile(meta.dbFile(DATATBL).file(), write ? "rw" : "r");
      this.owner = owner;
    }

    /**
     * Reads a byte value and returns it as an integer value.
     * @param pre PRE value
     * @param offset offset
     * @return integer value
     */
    private int read1(final int pre, final int offset) {
      final int o = offset + cursor(pre);
      final byte[] data = buffers.current().data;
      return data[o] & 0xFF;
    }

    /**
     * Reads a short value and returns it as an integer value.
     * @param pre PRE value
     * @param offset offset
     * @return integer value
     */
    private int read2(final int pre, final int offset) {
      final int o = offset + cursor(pre);
      final byte[] data = buffers.current().data;
      return ((data[o] & 0xFF) << 8) + (data[o + 1] & 0xFF);
    }

    /**
     * Reads an integer value.
     * @param pre PRE value
     * @param offset offset
     * @return integer value
     */
    private int read4(final int pre, final int offset) {
      final int o = offset + cursor(pre);
      final byte[] data = buffers.current().data;
      return ((data[o] & 0xFF) << 24) + ((data[o + 1] & 0xFF) << 16) +
        ((data[o + 2] & 0xFF) << 8) + (data[o + 3] & 0xFF);
    }

    /**
     * Reads a 5-byte value and returns it as a long value.
     * @param pre PRE value
     * @param offset offset
     * @return long value
     */
    private long read5(final int pre, final int offset) {
      final int o = offset + cursor(pre);
      final byte[] data = buffers.current().data;
      return ((long) (data[o] & 0xFF) << 32) + ((long) (data[o + 1] & 0xFF) << 24) +
        ((data[o + 2] & 0xFF) << 16) + ((data[o + 3] & 0xFF) << 8) + (data[o + 4] & 0xFF);
    }

    /**
     * Searches for the page containing the entry for the specified PRE value.
     * Reads the page and returns its offset inside the page.
     * @param pre PRE of the entry to search for
     * @return offset of the entry in the page
     */
    private int cursor(final int pre) {
      int fp = firstPre, np = nextPre;
      if(pre < fp || pre >= np) {
        final int last = used - 1;
        int l = 0, h = last, m = page;
        while(l <= h) {
          if(pre < fp) h = m - 1;
          else if(pre >= np) l = m + 1;
          else break;
          m = h + l >>> 1;
          fp = fpre(m);
          np = m == last ? nodes : fpre(m + 1);
        }
        if(l > h) throw Util.notExpected(
            "Data Access out of bounds:" +
            "\n- PRE value: " + pre +
            "\n- table size: " + nodes +
            "\n- first/next PRE value: " + fp + '/' + np +
            "\n- #total/used pages: " + pages + '/' + used +
            "\n- accessed page: " + m + " (" + l + " > " + h + ']');
        readPage(m);
      }
      return pre - firstPre << IO.NODEPOWER;
    }

    /**
     * Updates the page pointers.
     * @param pre page index
     */
    private void setPage(final int pre) {
      page = pre;
      firstPre = fpre(pre);
      nextPre = pre + 1 >= used ? nodes : fpre(pre + 1);
    }

    /**
     * Updates the index pointers and fetches the requested page.
     * @param pre page index
     */
    private void readPage(final int pre) {
      setPage(pre);
      read(pageIndex == null ? pre : pageIndex[pre]);
    }

    /**
     * Reads a page from disk.
     * @param pre page to fetch
     */
    private void read(final int pre) {
      if(!buffers.cursor(pre)) return;

      final Buffer buffer = buffers.current();
      try {
        write(buffer);
        buffer.pos = pre;
        if(pre >= pages) {
          // only reached by the master reader: the table grows during updates
          pages = pre + 1;
        } else {
          // pages that continue a sequential run are fetched in a single request; the master
          // reader is excluded, as the table can grow beyond the file size while it is updated
          ahead = owner != null && pre == nextRead ? Math.min(ahead << 1, AHEAD) : 1;
          final int count = Math.min(ahead, pages - pre);
          nextRead = pre + count;
          file.seek((long) pre << IO.BLOCKPOWER);
          if(count == 1) {
            file.readFully(buffer.data);
          } else if(readAhead(pre, count)) {
            // the requested page was evicted while reading ahead: fetch it again
            final Buffer current = buffers.current();
            write(current);
            current.pos = pre;
            file.seek((long) pre << IO.BLOCKPOWER);
            file.readFully(current.data);
          }
        }
      } catch(final IOException ex) {
        // queries are compiled before database locks are acquired: a concurrent update may
        // have closed this reader in the meantime
        if(!closed) throw new RuntimeException(Util.info(ex));
        buffers.cursor(pre);
        final Buffer current = buffers.current();
        current.pos = pre;
        readMaster(pre, current);
      }
    }

    /**
     * Reads a page through the master reader.
     * @param pre page to fetch
     * @param buffer target buffer
     */
    private void readMaster(final int pre, final Buffer buffer) {
      synchronized(master) {
        try {
          master.file.seek((long) pre << IO.BLOCKPOWER);
          master.file.readFully(buffer.data);
        } catch(final IOException ex) {
          throw new RuntimeException(Util.info(ex));
        }
      }
    }

    /**
     * Reads consecutive pages in a single request and caches them.
     * @param pre first page
     * @param count number of pages
     * @return flag indicating if the first page was evicted again
     * @throws IOException I/O exception
     */
    private boolean readAhead(final int pre, final int count) throws IOException {
      if(scratch == null) scratch = new byte[AHEAD << IO.BLOCKPOWER];
      file.readFully(scratch, 0, count << IO.BLOCKPOWER);
      Array.copyToStart(scratch, 0, IO.BLOCKSIZE, buffers.current().data);
      for(int c = 1; c < count; c++) {
        buffers.cursor(pre + c);
        final Buffer buffer = buffers.current();
        write(buffer);
        buffer.pos = pre + c;
        Array.copyToStart(scratch, c << IO.BLOCKPOWER, IO.BLOCKSIZE, buffer.data);
      }
      return buffers.cursor(pre);
    }

    /**
     * Writes the specified buffer to disk and resets the dirty flag.
     * @param buffer buffer to write
     * @throws IOException I/O exception
     */
    private void write(final Buffer buffer) throws IOException {
      if(!buffer.dirty) return;

      file.seek(buffer.pos << IO.BLOCKPOWER);
      file.write(buffer.data);
      buffer.dirty = false;
    }

    /**
     * Closes the file handle.
     * @throws IOException I/O exception
     */
    private void close() throws IOException {
      file.close();
    }
  }
}
