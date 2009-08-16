package org.basex.server;

import java.io.IOException;
import org.basex.BaseX;

/**
 * This is the starter class for the client console mode.
 * It sends all commands to the server instance.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class BaseXClientNew extends BaseX {
  /** Launcher. */
  private ClientLauncherNew launcher;


  /**
   * Main method of the database client, launching a local
   * client instance that sends all commands to a server instance.
   * Use <code>-h</code> to get a list command-line arguments.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new BaseXClientNew().run(args);
  }

  /**
   * Constructor.
   */
  public BaseXClientNew() {
    super(false);
    try {
      launcher = new ClientLauncherNew(context);
    } catch(final Exception ex) {
      BaseXServerNew.error(ex, true);
      standalone = true;
    }
  }

  @Override
  protected ClientLauncherNew launcher() throws IOException {
    if(launcher == null) launcher = new ClientLauncherNew(context);
    return launcher;
  }
}
