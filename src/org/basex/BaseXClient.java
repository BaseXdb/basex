package org.basex;

import org.basex.core.ALauncher;
import org.basex.core.ClientLauncher;
import org.basex.core.Process;

/**
 * This is the starter class for the client console mode.
 * It overwrites the {@link BaseX} standalone class and sends all
 * commands to the server instance.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXClient extends BaseX {
  /**
   * Main method of the database client, launching a local
   * client instance that sends all commands to a server instance.
   * Use <code>-h</code> to get a list of all available command-line
   * arguments.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new BaseXClient().run(args);
  }

  /**
   * Constructor.
   */
  public BaseXClient() {
    super(false);
  }

  @Override
  protected ALauncher launcher(final Process pr) {
    return new ClientLauncher(pr, context.prop);
  }
}
