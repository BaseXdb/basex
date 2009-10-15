package org.basex.core;

import java.io.IOException;
import org.basex.io.PrintOutput;

/**
 * This class allows a generic process execution.
 * It is implemented both by the local as well as the client/server model.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  public abstract boolean execute(final String cmd, final PrintOutput out)
    throws IOException;

  /**
   * Executes a process and prints the result to the specified stream.
   * @param pr process to be executed
   * @param out output stream
   * @throws IOException I/O exception
   * @return success of operation
   */
  public abstract boolean execute(final Process pr, final PrintOutput out)
    throws IOException;

  /**
   * Executes a process. This method should only be used if a command
   * does not return textual results.
   * @param pr process to be executed
   * @throws IOException I/O exception
   * @return success of operation
   */
  public final boolean execute(final Process pr) throws IOException {
    return execute(pr, null);
  }

  /**
   * Returns process info.
   * @return process info
   * @throws IOException I/O exception
   */
  public abstract String info() throws IOException;

  /**
   * Closes the session.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;
}
