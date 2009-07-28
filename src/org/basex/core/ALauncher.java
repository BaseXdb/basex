package org.basex.core;

import java.io.IOException;
import org.basex.io.PrintOutput;

/**
 * This class allows a generic execution of the specified process.
 * It is implemented both by the local as well as the client/server model.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class ALauncher {
  /** Process. */
  protected final Process proc;

  /**
   * Constructor.
   * @param pr process instance
   */
  protected ALauncher(final Process pr) {
    proc = pr;
  }

  /**
   * Executes a command.
   * @return success of operation
   * @throws IOException I/O exception
   */
  public abstract boolean execute() throws IOException;

  /**
   * Serializes the textual results of a command.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public abstract void out(final PrintOutput out) throws IOException;

  /**
   * Returns process info.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public abstract void info(final PrintOutput out) throws IOException;
}
