package org.basex;

import java.io.IOException;

import org.basex.core.ALauncher;
import org.basex.core.ClientLauncher;

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
  public static void main(final String[] args) {
    new BaseXClient().run(args);
  }

  /**
   * Constructor.
   */
  public BaseXClient() {
    super(false);
    try {
      launcher = new ClientLauncher(context);
    } catch(final Exception ex) {
      BaseXServer.error(ex, true);
      standalone = true;
    }
  }

  @Override
  protected ALauncher launcher() throws IOException {
    if(launcher == null) launcher = new ClientLauncher(context);
    return launcher;
  }
}
