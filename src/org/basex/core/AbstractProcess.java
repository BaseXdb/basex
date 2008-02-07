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
  /** Command reference. */
  protected Command cmd;
  
  /**
   * Processes a command.
   * @return success of operation
   * @throws IOException I/O exception
   */
  public abstract boolean execute() throws IOException;

  /**
   * Processes a command.
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

  @Override
  public final String tit() {
    return "Processing...";
  }

  @Override
  public final String det() {
    return "Processing...";
  }

  @Override
  public final double prog() {
    return 0;
  }
}
