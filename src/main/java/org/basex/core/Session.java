package org.basex.core;

import java.io.IOException;
import java.io.OutputStream;
import org.basex.io.NullOutput;

/**
 * This class allows a generic process execution.
 * It is implemented both by the local as well as the client/server model.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Session {
  /**
   * Executes a command and prints the result to the specified stream.
   * @param cmd command to be executed
   * @param out output stream
   * @throws IOException I/O exception
   * @return success of operation
   */
  public abstract boolean execute(final String cmd, final OutputStream out)
    throws IOException;

  /**
   * Executes a process and prints the result to the specified stream.
   * @param pr process to be executed
   * @param out output stream
   * @throws IOException I/O exception
   * @return success of operation
   */
  public abstract boolean execute(final Proc pr, final OutputStream out)
    throws IOException;

  /**
   * Executes a process. {@link #execute(Proc, OutputStream)} should be
   * called if textual results are expected.
   * @param pr process to be executed
   * @throws IOException I/O exception
   * @return success of operation
   */
  public final boolean execute(final Proc pr) throws IOException {
    return execute(pr, new NullOutput());
  }

  /**
   * Executes a command. {@link #execute(String, OutputStream)} should be
   * called if textual results are expected.
   * @param pr process to be executed
   * @throws IOException I/O exception
   * @return success of operation
   */
  public final boolean execute(final String pr) throws IOException {
    return execute(pr, new NullOutput());
  }

  /**
   * Returns process info.
   * @return process info
   */
  public abstract String info();

  /**
   * Closes the session.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;
}
