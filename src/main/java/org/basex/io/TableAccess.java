package org.basex.io;

import java.io.IOException;
import org.basex.data.MetaData;

/**
 * This abstract class defines the methods for accessing the
 * database table representation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class TableAccess {
  /** Meta data. */
  protected final MetaData meta;
  /** Filename prefix. */
  protected final String pref;
  /** Dirty index flag. */
  protected boolean dirty;

  /**
   * Constructor.
   * @param md meta data
   * @param pf file prefix
   */
  protected TableAccess(final MetaData md, final String pf) {
    meta = md;
    pref = pf;
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

  /**
   * Copies the specified entry into the database.
   * @param pre pre value
   * @param entries array of bytes containing the entries to insert
   */
  public abstract void set(int pre, byte[] entries);
}
