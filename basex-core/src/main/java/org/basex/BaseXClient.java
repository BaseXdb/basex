package org.basex;

import java.io.*;

import org.basex.util.*;

/**
 * This is the starter class for the client console mode.
 * All input is sent to the server instance.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXClient extends BaseX {
  /**
   * Main method of the database client, launching a client instance.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    try {
      new BaseXClient(args);
    } catch(final IOException ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public BaseXClient(final String... args) throws IOException {
    super(args);
  }

  @Override
  protected boolean local() {
    return false;
  }
}
