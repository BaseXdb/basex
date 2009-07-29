package org.basex.server;

import static org.basex.Text.*;
import java.io.IOException;
import java.net.Socket;
import org.basex.BaseX;
import org.basex.core.ALauncher;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Exit;

/**
 * This is the starter class for the client console mode.
 * It sends all commands to the server instance.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class BaseXClientNew extends BaseX {
  /** Socket reference. */
  final Socket socket;

  /**
   * Main method of the database client, launching a local
   * client instance that sends all commands to a server instance.
   * Use <code>-h</code> to get a list of all available command-line
   * arguments.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    try {
      new BaseXClientNew().run(args);
    } catch(final IOException ex) {
      BaseX.errln(SERVERERR);
    }
  }

  /**
   * Constructor.
   * @throws IOException I/O exception
   */
  public BaseXClientNew() throws IOException {
    super(false);
    socket = new Socket(context.prop.get(Prop.HOST),
        context.prop.num(Prop.PORT));
  }

  @Override
  protected void quit(final boolean force) {
    try {
      launcher(new Exit()).execute();
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
    super.quit(force);
  }

  @Override
  protected ALauncher launcher(final Process pr) {
    return new ClientLauncherNew(pr, socket);
  }
}
