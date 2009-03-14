package org.basex.io;

import java.io.IOException;

/**
 * This abstract class defines the methods for accessing the
 * database table representation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class TableAccess {
  /**
   * Reads a byte from the specified position and returns it as integer.
   * @param p pre value
   * @param o offset
   * @return integer value
   */
  public abstract int read1(int p, int o);
  
  /**
   * Reads a short value from the specified position and returns it as integer.
   * @param p pre value
   * @param o offset
   * @return integer value
   */
  public abstract int read2(int p, int o);
  
  /**
   * Reads an integer value from the specified position.
   * @param p pre value
   * @param o offset
   * @return integer value
   */
  public abstract int read4(int p, int o);
  
  /**
   * Reads an integer value from the specified position.
   * @param p pre value
   * @param o offset
   * @return integer value
   */
  public abstract long read5(int p, int o);
  
  /**
   * Writes a byte to the specified position.
   * @param p pre value
   * @param o offset
   * @param v value to be written
   */
  public abstract void write1(int p, int o, int v);
  
  /**
   * Writes 2 bytes to the specified position.
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
   * Delete a range of entries.
   * @param pre pre value of the first node to delete
   * @param nr number of entries to be deleted
   */
  public abstract void delete(int pre, int nr);
  
  /**
   * Insert entries.
   * @param pre pre value of node to insert after
   * @param entries array of bytes containing the entries to insert
   */
  public abstract void insert(int pre, byte[] entries);
  
  /**
   * Flushes the table contents.
   * @throws IOException in case of write errors
   */
  public abstract void flush() throws IOException;
  
  /**
   * Closes the table access.
   * @throws IOException in case of write errors
   */
  public abstract void close() throws IOException;
}
