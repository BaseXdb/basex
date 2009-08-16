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
public abstract class ALauncher {
  /**
   * Executes a process.
   * @param pr process to be executed
   * @throws IOException I/O exception
   * @return success of operation
   */
  public abstract boolean execute(final Process pr) throws IOException;

  /**
   * Serializes the textual results of a command.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public abstract void output(final PrintOutput out) throws IOException;

  /**
   * Returns process info.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public abstract void info(final PrintOutput out) throws IOException;
}
