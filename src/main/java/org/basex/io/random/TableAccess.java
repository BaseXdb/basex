package org.basex.io.random;

import java.io.IOException;
import org.basex.data.MetaData;
import org.basex.io.*;

/**
 * This abstract class defines the methods for accessing the
 * database table representation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class TableAccess {
  /** Meta data. */
  final MetaData meta;
  /** Dirty index flag. */
  boolean dirty;

  /**
   * Constructor.
   * @param md meta data
   */
  TableAccess(final MetaData md) {
    meta = md;
  }

  /**
   * Flushes the table contents.
   * @throws IOException I/O exception
   */
  public abstract void flush() throws IOException;

  /**
   * Closes the table access.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;

  /**
   * Locks or unlocks the table for write operations.
   * @param yes lock or unlock file
   * @return success flag
   */
  public abstract boolean writeLock(final boolean yes);

  /**
   * Reads a byte value and returns it as an integer value.
   * @param p pre value
   * @param o offset
   * @return integer value
   */
  public abstract int read1(int p, int o);

  /**
   * Reads a short value and returns it as an integer value.
   * @param p pre value
   * @param o offset
   * @return integer value
   */
  public abstract int read2(int p, int o);

  /**
   * Reads an integer value.
   * @param p pre value
   * @param o offset
   * @return integer value
   */
  public abstract int read4(int p, int o);

  /**
   * Reads a 5-byte value and returns it as a long value.
   * @param p pre value
   * @param o offset
   * @return integer value
   */
  public abstract long read5(int p, int o);

  /**
   * Writes a byte value to the specified position.
   * @param p pre value
   * @param o offset
   * @param v value to be written
   */
  public abstract void write1(int p, int o, int v);

  /**
   * Writes a short value to the specified position.
   * @param p pre value
   * @param o offset
   * @param v value to be written
   */
  public abstract void write2(int p, int o, int v);

  /**
   * Writes an integer value to the specified position.
   * @param p pre value
   * @param o offset
   * @param v value to be written
   */
  public abstract void write4(int p, int o, int v);

  /**
   * Writes a 5-byte value to the specified position.
   * @param p pre value
   * @param o offset
   * @param v value to be written
   */
  public abstract void write5(int p, int o, long v);

  /**
   * Replaces entries in the database.
   * @param pre node to be replaced
   * @param entries new entries
   * @param sub size of the subtree that is replaced
   */
  public final void replace(final int pre, final byte[] entries,
      final int sub) {

    dirty = true;
    final int nsize = entries.length >>> IO.NODEPOWER;
    final int diff = sub - nsize;
    final int last = diff <= 0 ? pre + nsize - Math.abs(diff) : pre + nsize;
    copy(entries, pre, last);
    final int off = last - pre << IO.NODEPOWER;

    // handle the remaining entries if the two subtrees are of different size
    if(diff < 0) {
      // case1: new subtree bigger than old one, insert remaining new nodes
      final byte[] tmp = new byte[entries.length - off];
      System.arraycopy(entries, off, tmp, 0, tmp.length);
      insert(last, tmp);
    } else if(diff > 0) {
      // case2: old subtree bigger than new one, delete remaining old nodes
      delete(last, diff);
    }
  }

  /**
   * Copies the specified entries into the database.
   * @param pre pre value
   * @param entries array of bytes containing the entries to insert
   */
  public final void set(final int pre, final byte[] entries) {
    dirty = true;
    copy(entries, pre, pre + (entries.length >>> IO.NODEPOWER));
  }

  /**
   * Copies the specified values into the database.
   * @param entries entries to copy
   * @param pre first target pre value
   * @param last last pre value
   */
  protected abstract void copy(final byte[] entries, final int pre,
      final int last);

  /**
   * Deletes the specified number of entries from the database.
   * @param pre pre value of the first node to delete
   * @param nr number of entries to be deleted
   */
  public abstract void delete(int pre, int nr);

  /**
   * Inserts the specified entries into the database.
   * @param pre pre value
   * @param entries array of bytes containing the entries to insert
   */
  public abstract void insert(int pre, byte[] entries);
}
