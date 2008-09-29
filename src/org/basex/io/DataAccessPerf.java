package org.basex.io;

import java.io.File;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.util.Performance;

/**
 * This class allows positional read access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DataAccessPerf extends DataAccess {
  /** Performance Container. */
  private Performance rp;
  /** Runtime. */
  public double t;
  /** Output-text. */
  public String otext;
  /**
   * Constructor, initializing the file reader.
   * @param db name of the database
   * @param fn the file to be read
   * @param text output text
   * @throws IOException IO Exception
   */
  public DataAccessPerf(final String db, final String fn, final String text) 
  throws IOException {
    super(db, fn);
    rp = new Performance();
    otext = text;
  }

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessPerf(final File f) throws IOException {
    super(f);
    rp = new Performance();
  }

  /**
   * Reads a number of bytes in range from -> to and returns them as array.
   * @param from starting position for reading
   * @param to ending position for reading
   * @return byte array
   */
  @Override
  public synchronized byte[] readBytes(final long from, final long to) {
    rp.initTimer();
    final byte[] array = super.readBytes(from, to);
    t += rp.getTime();
    return array;
  }
  
  /**
   * Reads an integer value from the specified position.
   * @param p position
   * @return integer value
   */
  @Override
  public synchronized int readInt(final long p) {
    rp.initTimer();
    final int i = super.readInt(p);
    t += rp.getTime();
    return i;
  }
  
  /**
   * Reads an 5-byte value from the specified file offset.
   * @param p position
   * @return long value
   */
  @Override
  public synchronized long read5(final long p) {
    rp.initTimer();
    final long l = super.read5(p);
    t += rp.getTime();
    return l;
  }

  /**
   * Closes the data access.
   * @throws IOException in case of write errors
   */
  @Override
  public synchronized void close() throws IOException {
    super.close();
    if (Prop.debug) {
      System.out.println("Time needed for " + otext + "-reading:" + t + " ns");
      System.out.println("Time needed for " + otext 
          + "-reading:" + (t / 1000000.00) + " ms");
    }
  }

}
