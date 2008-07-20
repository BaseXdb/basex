package org.basex.core;

import java.io.IOException;
import org.basex.io.PrintOutput;

/**
 * This class provides the architecture for local and client process
 * interpretation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Stefan Klinger
 * @author Christian Gruen
 */
public abstract class AbstractProcess extends Progress {
  /**
   * Executes a command.
   * @param ctx context reference
   * @return success of operation
   * @throws IOException I/O exception
   */
  public abstract boolean execute(final Context ctx) throws IOException;

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
