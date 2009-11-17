package org.basex.io;

import java.io.IOException;
import org.basex.core.Prop;

/**
 * This abstract class defines the methods for accessing the
 * database table representation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class TableAccess {
  /** Database properties. */
  protected final Prop prop;
  /** Name of the database. */
  protected final String db;
  /** Filename prefix. */
  protected final String pref;
  /** Dirty index flag. */
  protected boolean dirty;

  /**
   * Constructor.
   * @param nm name of the database
   * @param f prefix for all files (no ending)
   * @param pr database properties
   */
  public TableAccess(final String nm, final String f, final Prop pr) {
    db = nm;
    pref = f;
    prop = pr;
  }

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
   * @param pre pre value of node to insert after
   * @param entries array of bytes containing the entries to insert
   */
  public abstract void insert(int pre, byte[] entries);

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
}
