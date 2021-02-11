package org.basex.io.random;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.io.*;

/**
 * This abstract class defines the methods for accessing the
 * database table representation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class TableAccess {
  /** Meta data. */
  final MetaData meta;
  /** Dirty index flag. */
  boolean dirty;

  /**
   * Constructor.
   * @param meta meta data
   */
  TableAccess(final MetaData meta) {
    this.meta = meta;
  }

  /**
   * Flushes the table contents.
   * @param all flush all contents or only buffers
   * @throws IOException I/O exception
   */
  public abstract void flush(boolean all) throws IOException;

  /**
   * Closes the table access.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;

  /**
   * Tries to acquires a lock on the table. If a lock exists, it is first released.
   * @param write write/read lock
   * @return success flag
   */
  public abstract boolean lock(boolean write);

  /**
   * Reads a byte value and returns it as an integer value.
   * @param pre pre value
   * @param offset offset
   * @return integer value
   */
  public abstract int read1(int pre, int offset);

  /**
   * Reads a short value and returns it as an integer value.
   * @param pre pre value
   * @param offset offset
   * @return integer value
   */
  public abstract int read2(int pre, int offset);

  /**
   * Reads an integer value.
   * @param pre pre value
   * @param offset offset
   * @return integer value
   */
  public abstract int read4(int pre, int offset);

  /**
   * Reads a 5-byte value and returns it as a long value.
   * @param pre pre value
   * @param offset offset
   * @return integer value
   */
  public abstract long read5(int pre, int offset);

  /**
   * Writes a byte value to the specified position.
   * @param pre pre value
   * @param offset offset
   * @param value value to be written
   */
  public abstract void write1(int pre, int offset, int value);

  /**
   * Writes a short value to the specified position.
   * @param pre pre value
   * @param offset offset
   * @param value value to be written
   */
  public abstract void write2(int pre, int offset, int value);

  /**
   * Writes an integer value to the specified position.
   * @param pre pre value
   * @param offset offset
   * @param value value to be written
   */
  public abstract void write4(int pre, int offset, int value);

  /**
   * Writes a 5-byte value to the specified position.
   * @param pre pre value
   * @param offset offset
   * @param value value to be written
   */
  public abstract void write5(int pre, int offset, long value);

  /**
   * Replaces entries in the database.
   * @param pre node to be replaced
   * @param entries new entries
   * @param count number of entries to be replaced
   */
  public final void replace(final int pre, final byte[] entries, final int count) {
    dirty();
    final int nsize = entries.length >>> IO.NODEPOWER;
    final int diff = count - nsize;
    final int last = diff <= 0 ? pre + nsize - Math.abs(diff) : pre + nsize;
    copy(entries, pre, last);
    final int off = last - pre << IO.NODEPOWER;

    // handle the remaining entries if the two subtrees are of different size
    if(diff < 0) {
      // case1: new subtree bigger than old one, insert remaining new nodes
      insert(last, Arrays.copyOfRange(entries, off, entries.length));
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
  final void set(final int pre, final byte[] entries) {
    dirty();
    copy(entries, pre, pre + (entries.length >>> IO.NODEPOWER));
  }

  /**
   * Marks the data structures as dirty.
   */
  protected abstract void dirty();

  /**
   * Copies the specified values into the database.
   * @param entries entries to copy
   * @param pre first target pre value
   * @param last last pre value
   */
  protected abstract void copy(byte[] entries, int pre, int last);

  /**
   * Deletes the specified number of entries from the database.
   * @param pre pre value of the first node to delete
   * @param count number of entries to be deleted
   */
  public abstract void delete(int pre, int count);

  /**
   * Inserts the specified entries into the database.
   * @param pre pre value
   * @param entries array of bytes containing the entries to insert
   */
  public abstract void insert(int pre, byte[] entries);
}
