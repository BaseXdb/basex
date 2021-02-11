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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableDiskAccess extends TableAccess {
  /** Buffer manager. */
  private final Buffers buffers = new Buffers();
  /** File storing all pages. */
  private final RandomAccessFile file;
  /** Bitmap storing free (=0) and used (=1) pages. */
  private BitArray usedPages;
  /** File lock. */
  private FileLock lock;

  /** First pre values (ascending order); will be initialized with the first update. */
  private int[] fPreIndex;
  /** Page index; will be initialized with the first update. */
  private int[] pageIndex;
  /** Total number of pages. */
  private int pages;
  /** Number of used pages. */
  private int used;

  /** Pointer to current page. */
  private int page = -1;
  /** Pre value of the first entry in the current page. */
  private int firstPre = -1;
  /** First pre value of the next page. */
  private int nextPre = -1;

  /**
   * Constructor.
   * @param meta meta data
   * @param write write lock
   * @throws IOException I/O exception
   */
  public TableDiskAccess(final MetaData meta, final boolean write) throws IOException {
    super(meta);

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
        // read page index and first pre values from disk
        fPreIndex = in.readNums();
        pageIndex = in.readNums();
        // read block bitmap
        final int s = in.readNum();
        usedPages = new BitArray(in.readLongs(s), used);
      }
    }

    // initialize data file
    file = new RandomAccessFile(meta.dbFile(DATATBL).file(), "rw");
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
  public synchronized void flush(final boolean all) throws IOException {
    for(final Buffer buffer : buffers.all()) {
      write(buffer);
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
        // no mapping available or required (0: empty table; MAX: no mapping, see TableOutput#close)
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

  @Override
  public synchronized void close() throws IOException {
    flush(true);
    file.close();
  }

  @Override
  public boolean lock(final boolean write) {
    try {
      if(lock != null) {
        if(write != lock.isShared()) return true;
        lock.release();
      }
      lock = file.getChannel().tryLock(0, Long.MAX_VALUE, !write);
      return lock != null;
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
  }

  @Override
  public synchronized int read1(final int pre, final int offset) {
    final int o = offset + cursor(pre);
    final byte[] data = buffers.current().data;
    return data[o] & 0xFF;
  }

  @Override
  public synchronized int read2(final int pre, final int offset) {
    final int o = offset + cursor(pre);
    final byte[] data = buffers.current().data;
    return ((data[o] & 0xFF) << 8) + (data[o + 1] & 0xFF);
  }

  @Override
  public synchronized int read4(final int pre, final int offset) {
    final int o = offset + cursor(pre);
    final byte[] data = buffers.current().data;
    return ((data[o] & 0xFF) << 24) + ((data[o + 1] & 0xFF) << 16) +
      ((data[o + 2] & 0xFF) << 8) + (data[o + 3] & 0xFF);
  }

  @Override
  public synchronized long read5(final int pre, final int offset) {
    final int o = offset + cursor(pre);
    final byte[] data = buffers.current().data;
    return ((long) (data[o] & 0xFF) << 32) + ((long) (data[o + 1] & 0xFF) << 24) +
      ((data[o + 2] & 0xFF) << 16) + ((data[o + 3] & 0xFF) << 8) + (data[o + 4] & 0xFF);
  }

  @Override
  public void write1(final int pre, final int offset, final int value) {
    final int o = offset + cursor(pre);
    final Buffer buffer = buffers.current();
    buffer.data[o] = (byte) value;
    buffer.dirty = true;
  }

  @Override
  public void write2(final int pre, final int offset, final int value) {
    final int o = offset + cursor(pre);
    final Buffer buffer = buffers.current();
    final byte[] data = buffer.data;
    data[o] = (byte) (value >>> 8);
    data[o + 1] = (byte) value;
    buffer.dirty = true;
  }

  @Override
  public void write4(final int pre, final int offset, final int value) {
    final int o = offset + cursor(pre);
    final Buffer buffer = buffers.current();
    final byte[] data = buffer.data;
    data[o]     = (byte) (value >>> 24);
    data[o + 1] = (byte) (value >>> 16);
    data[o + 2] = (byte) (value >>> 8);
    data[o + 3] = (byte) value;
    buffer.dirty = true;
  }

  @Override
  public void write5(final int pre, final int offset, final long value) {
    final int o = offset + cursor(pre);
    final Buffer buffer = buffers.current();
    final byte[] data = buffer.data;
    data[o]     = (byte) (value >>> 32);
    data[o + 1] = (byte) (value >>> 24);
    data[o + 2] = (byte) (value >>> 16);
    data[o + 3] = (byte) (value >>> 8);
    data[o + 4] = (byte) value;
    buffer.dirty = true;
  }

  @Override
  protected void copy(final byte[] entries, final int pre, final int last) {
    for(int o = 0, i = pre; i < last; ++i, o += IO.NODESIZE) {
      final int off = cursor(i);
      final Buffer buffer = buffers.current();
      Array.copy(entries, o, IO.NODESIZE, buffer.data, off);
      buffer.dirty = true;
    }
  }

  @Override
  public void delete(final int pre, final int count) {
    if(count == 0) return;

    // get first page
    dirty();
    cursor(pre);

    // some useful variables to make code more readable
    int from = pre - firstPre;
    final int last = pre + count;

    // check if all entries are in current page
    if(last <= nextPre) {
      // move entries in current page and decreases pointers to pre values
      if(last < nextPre) delete(buffers.current(), from, from + count, nextPre - last);
      decreasePre(count);

      // if whole page was deleted, remove it from the index
      if(firstPre == nextPre) {
        // mark the page as empty
        usedPages.clear(pageIndex[page]);
        deletePages(1);
        readPage(page);
      }
    } else {
      // handle pages whose entries are to be deleted entirely

      // first count them
      int unused = 0;
      while(last > nextPre) {
        if(from == 0) {
          ++unused;
          // mark the pages as empty; range clear cannot be used because the
          // pages may not be consecutive
          usedPages.clear(pageIndex[page]);
        }
        setPage(page + 1);
        from = 0;
      }

      // if the last page is empty, clear the corresponding bit
      read(pageIndex[page]);
      final Buffer buffer = buffers.current();
      if(last == nextPre) {
        usedPages.clear((int) buffer.pos);
        ++unused;
        if(page + 1 < used) readPage(page + 1);
        else ++page;
      } else {
        // delete entries at beginning of current (last) page
        delete(buffer, 0, last - firstPre, nextPre - last);
      }

      // now remove them from the index
      if(unused > 0) {
        page -= unused;
        deletePages(unused);
      }

      // update index entry for this page
      fPreIndex[page] = pre;
      firstPre = pre;
      decreasePre(count);
    }
    if(used == 0) {
      buffers.init();
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
      readPage(0);
      usedPages.set(0);
      ++used;
    } else if(pre > 0) {
      // find the offset within the page where the new records will be inserted
      split = cursor(pre - 1) + IO.NODESIZE;
    }

    // number of bytes occupied by old records in the current page
    final int nold = nextPre - firstPre << IO.NODEPOWER;
    // number of bytes occupied by old records which will be moved at the end
    final int moved = nold - split;

    // special case: all entries fit in the current page
    Buffer buffer = buffers.current();
    if(nold + nnew <= IO.BLOCKSIZE) {
      Array.insert(buffer.data, split, nnew, nold, entries);
      buffer.dirty = true;

      // increment first pre-values of pages after the last modified page
      for(int i = page + 1; i < used; ++i) fPreIndex[i] += nr;
      // update cached variables (fpre is not changed)
      nextPre += nr;
      meta.size += nr;
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
    int needed = req / IO.BLOCKSIZE;
    final int remain = req % IO.BLOCKSIZE;

    if(remain > 0) {
      // check if the last entries can fit in the page after the current one
      if(page + 1 < used) {
        final int o = occSpace(page + 1) << IO.NODEPOWER;
        if(remain <= IO.BLOCKSIZE - o) {
          // copy the last records
          readPage(page + 1);
          buffer = buffers.current();
          Array.copyFromStart(buffer.data, o, buffer.data, remain);
          Array.copyToStart(all, all.length - remain, remain, buffer.data);
          buffer.dirty = true;
          // reduce the pre value, since it will be later incremented with nr
          fPreIndex[page] -= remain >>> IO.NODEPOWER;
          // go back to the previous page
          readPage(page - 1);
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
    Array.insert(fPreIndex, page + 1, needed, used, null);
    Array.insert(pageIndex, page + 1, needed, used, null);

    // write the all remaining entries
    while(needed-- > 0) {
      final int p = usedPages.nextFree();
      usedPages.set(p);
      read(p);
      ++used;
      ++page;
      nrem += write(all, nrem);
      fPreIndex[page] = fPreIndex[page - 1] + IO.ENTRIES;
      pageIndex[page] = (int) buffers.current().pos;
    }

    // increment all fpre values after the last modified page
    for(int i = page + 1; i < used; ++i) fPreIndex[i] += nr;

    meta.size += nr;

    // update cached variables
    firstPre = fPreIndex[page];
    nextPre = page + 1 < used && fPreIndex[page + 1] < meta.size ? fPreIndex[page + 1] : meta.size;
  }

  @Override
  protected void dirty() {
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(Util.className(this)).append(" (").append("pages: ").append(pages);
    sb.append(", used: ").append(used).append(", page: ").append(page);
    sb.append(", firstPre: ").append(firstPre).append(", nextPre: ").append(nextPre).append(")");
    if(fPreIndex != null) sb.append("\n- FPres: ").append(Arrays.toString(fPreIndex));
    if(pageIndex != null) sb.append("\n- Pages: ").append(Arrays.toString(pageIndex));
    if(usedPages != null) sb.append("\n- Used Pages: ").append(usedPages);
    return sb.toString();
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Searches for the page containing the entry for the specified pre value.
   * Reads the page and returns its offset inside the page.
   * @param pre pre of the entry to search for
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
        np = m == last ? meta.size : fpre(m + 1);
      }
      if(l > h) throw Util.notExpected(
          "Data Access out of bounds:" +
          "\n- pre value: " + pre +
          "\n- table size: " + meta.size +
          "\n- first/next pre value: " + fp + '/' + np +
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
    nextPre = pre + 1 >= used ? meta.size : fpre(pre + 1);
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
   * Return the specified pre value.
   * @param pre index of the page to fetch
   * @return pre value
   */
  private int fpre(final int pre) {
    return fPreIndex == null ? pre * IO.ENTRIES : fPreIndex[pre];
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
        pages = pre + 1;
      } else {
        file.seek(buffer.pos * IO.BLOCKSIZE);
        file.readFully(buffer.data);
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Writes the specified buffer to disk and resets the dirty flag.
   * @param buffer buffer to write
   * @throws IOException I/O exception
   */
  private void write(final Buffer buffer) throws IOException {
    if(!buffer.dirty) return;

    file.seek(buffer.pos * IO.BLOCKSIZE);
    file.write(buffer.data);
    buffer.dirty = false;
  }

  /**
   * Deletes pages in the page mapping.
   * @param count number of pages to delete
   */
  private void deletePages(final int count) {
    Array.remove(fPreIndex, page, count, used);
    Array.remove(pageIndex, page, count, used);
    used -= count;
  }

  /**
   * Decreases pointers to pre value.
   * @param count number of entries to move
   */
  private void decreasePre(final int count) {
    final int nextPage = page + 1;
    for(int i = nextPage; i < used; ++i) fPreIndex[i] -= count;
    meta.size -= count;
    nextPre = nextPage < used && fPreIndex[nextPage] < meta.size ? fPreIndex[nextPage] : meta.size;
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
    final Buffer buffer = buffers.current();
    final int len = Math.min(IO.BLOCKSIZE, array.length - offset);
    Array.copyToStart(array, offset, len, buffer.data);
    buffer.dirty = true;
    return len;
  }

  /**
   * Calculates the occupied space in a page.
   * @param index page index
   * @return occupied space in number of records
   */
  private int occSpace(final int index) {
    return (index + 1 < used ? fPreIndex[index + 1] : meta.size) - fPreIndex[index];
  }

  /**
   * Removes the page index.
   */
  private void removeMapping() {
    fPreIndex = null;
    pageIndex = null;
    usedPages = null;
  }
}
