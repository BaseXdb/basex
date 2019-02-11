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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class TableDiskAccess extends TableAccess {
  /** Buffer manager. */
  private final Buffers bm = new Buffers();
  /** File storing all pages. */
  private final RandomAccessFile file;
  /** Bitmap storing free (=0) and used (=1) pages. */
  private BitArray usedPages;
  /** File lock. */
  private FileLock fl;

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
   * @param md meta data
   * @param write write lock
   * @throws IOException I/O exception
   */
  public TableDiskAccess(final MetaData md, final boolean write) throws IOException {
    super(md);

    // read meta and index data
    try(DataInput in = new DataInput(meta.dbfile(DATATBL + 'i'))) {
      // total number of pages
      pages = in.readNum();
      // number of used pages (max: no page mapping; 0: empty table)
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
    file = new RandomAccessFile(meta.dbfile(DATATBL).file(), "rw");
    if(!lock(write)) throw new BaseXException(Text.DB_PINNED_X, md.name);
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
    for(final Buffer b : bm.all()) write(b);
    if(!dirty || !all) return;

    try(DataOutput out = new DataOutput(meta.dbfile(DATATBL + 'i'))) {
      final int sz = pages;
      out.writeNum(sz);
      out.writeNum(used);
      if(fPreIndex != null) {
        out.writeNum(sz);
        for(int s = 0; s < sz; s++) out.writeNum(fPreIndex[s]);
        out.writeNum(sz);
        for(int s = 0; s < sz; s++) out.writeNum(pageIndex[s]);
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
      if(fl != null) {
        if(write != fl.isShared()) return true;
        fl.release();
      }
      fl = file.getChannel().tryLock(0, Long.MAX_VALUE, !write);
      return fl != null;
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
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
      Array.copy(entries, o, IO.NODESIZE, bf.data, off);
      bf.dirty = true;
    }
  }

  @Override
  public void delete(final int pre, final int nr) {
    if(nr == 0) return;

    // get first page
    dirty();
    cursor(pre);

    // some useful variables to make code more readable
    int from = pre - firstPre;
    final int last = pre + nr;

    // check if all entries are in current page
    if(last <= nextPre) {
      // move entries in current page and decreases pointers to pre values
      if(last < nextPre) delete(bm.current(), from, from + nr, nextPre - last);
      decreasePre(nr);

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
      final Buffer bf = bm.current();
      if(last == nextPre) {
        usedPages.clear((int) bf.pos);
        ++unused;
        if(page + 1 < used) readPage(page + 1);
        else ++page;
      } else {
        // delete entries at beginning of current (last) page
        delete(bf, 0, last - firstPre, nextPre - last);
      }

      // now remove them from the index
      if(unused > 0) {
        page -= unused;
        deletePages(unused);
      }

      // update index entry for this page
      fPreIndex[page] = pre;
      firstPre = pre;
      decreasePre(nr);
    }
    if(used == 0) {
      fPreIndex = null;
      pageIndex = null;
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
    Buffer bf = bm.current();
    if(nold + nnew <= IO.BLOCKSIZE) {
      Array.insert(bf.data, split, nnew, nold, entries);
      bf.dirty = true;

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
    Array.copy(bf.data, split, moved, all, nnew);

    // fill in the current page with new entries
    // number of bytes which fit in the first page
    int nrem = IO.BLOCKSIZE - split;
    if(nrem > 0) {
      Array.copyFromStart(all, nrem, bf.data, split);
      bf.dirty = true;
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
          bf = bm.current();
          Array.copyFromStart(bf.data, o, bf.data, remain);
          Array.copyToStart(all, all.length - remain, remain, bf.data);
          bf.dirty = true;
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
      pageIndex[page] = (int) bm.current().pos;
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
    final StringBuilder sb = new StringBuilder(Util.className(this) + " (");
    sb.append("size: " + pages + ", used: " + used + ")");
    if(fPreIndex != null) sb.append("\nFPres: " + Arrays.toString(fPreIndex));
    if(pageIndex != null) sb.append("\nPages: " + Arrays.toString(pageIndex));
    if(usedPages != null) sb.append("\nUsed Pages: " + usedPages);
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
   * @param p page index
   */
  private void setPage(final int p) {
    page = p;
    firstPre = fpre(p);
    nextPre = p + 1 >= used ? meta.size : fpre(p + 1);
  }

  /**
   * Updates the index pointers and fetches the requested page.
   * @param p page index
   */
  private void readPage(final int p) {
    setPage(p);
    read(pageIndex == null ? p : pageIndex[p]);
  }

  /**
   * Return the specified pre value.
   * @param p index of the page to fetch
   * @return pre value
   */
  private int fpre(final int p) {
    return fPreIndex == null ? p * IO.ENTRIES : fPreIndex[p];
  }

  /**
   * Reads a page from disk.
   * @param p page to fetch
   */
  private void read(final int p) {
    if(!bm.cursor(p)) return;

    final Buffer bf = bm.current();
    try {
      write(bf);
      bf.pos = p;
      if(p >= pages) {
        pages = p + 1;
      } else {
        file.seek(bf.pos * IO.BLOCKSIZE);
        file.readFully(bf.data);
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Writes the specified buffer to disk and resets the dirty flag.
   * @param bf buffer to write
   * @throws IOException I/O exception
   */
  private void write(final Buffer bf) throws IOException {
    if(!bf.dirty) return;

    file.seek(bf.pos * IO.BLOCKSIZE);
    file.write(bf.data);
    bf.dirty = false;
  }

  /**
   * Deletes pages in the page mapping.
   * @param nr number of pages to delete
   */
  private void deletePages(final int nr) {
    Array.remove(fPreIndex, page, nr, used);
    Array.remove(pageIndex, page, nr, used);
    used -= nr;
  }

  /**
   * Decreases pointers to pre value.
   * @param nr number of entries to move
   */
  private void decreasePre(final int nr) {
    final int nextPage = page + 1;
    for(int i = nextPage; i < used; ++i) fPreIndex[i] -= nr;
    meta.size -= nr;
    nextPre = nextPage < used && fPreIndex[nextPage] < meta.size ? fPreIndex[nextPage] : meta.size;
  }

  /**
   * Convenience method for delete buffer entries.
   * @param buffer buffer
   * @param from first entry to delete
   * @param to last entry to delete
   * @param l source length
   */
  private void delete(final Buffer buffer, final int from, final int to, final int l) {
    final byte[] array = buffer.data;
    Array.copy(array, to << IO.NODEPOWER, l << IO.NODEPOWER, array, from << IO.NODEPOWER);
    buffer.dirty = true;
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
    Array.copyToStart(s, o, len, bf.data);
    bf.dirty = true;
    return len;
  }

  /**
   * Calculate the occupied space in a page.
   * @param i page index
   * @return occupied space in number of records
   */
  private int occSpace(final int i) {
    return (i + 1 < used ? fPreIndex[i + 1] : meta.size) - fPreIndex[i];
  }
}
