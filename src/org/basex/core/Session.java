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
public interface Session {
  /**
   * Executes the specified command.
   * @param cmd command to be executed
   * @throws IOException I/O exception
   * @return success of operation
   */
  boolean execute(final String cmd) throws IOException;

  /**
   * Executes a process.
   * @param pr process to be executed
   * @throws IOException I/O exception
   * @return success of operation
   */
  boolean execute(final Process pr) throws IOException;

  /**
   * Serializes the result to the specified output stream.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void output(final PrintOutput out) throws IOException;

  /**
   * Returns process info.
   * @return process info
   * @throws IOException I/O exception
   */
  String info() throws IOException;

  /**
   * Closes the session.
   * @throws IOException I/O exception
   */
  void close() throws IOException;
}
