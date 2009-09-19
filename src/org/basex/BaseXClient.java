package org.basex;

/**
 * This is the starter class for the client console mode.
 * It sends all commands to the server instance.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class BaseXClient extends BaseX {
  /**
   * Main method of the database client, launching a local
   * client instance that sends all commands to a server instance.
   * Use <code>-h</code> to get a list command-line arguments.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    new BaseXClient(args);
  }

  /**
   * Constructor.
   * @param args command line arguments
   */
  public BaseXClient(final String... args) {
    super(args);
  }
}
